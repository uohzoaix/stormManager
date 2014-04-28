package frame.module;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.thrift7.TException;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.KillOptions;
import backtype.storm.generated.NotAliveException;
import backtype.storm.generated.RebalanceOptions;
import backtype.storm.utils.BufferFileInputStream;

import com.esotericsoftware.minlog.Log;

import frame.exception.ServiceException;
import frame.manager.StormClientManager;
import frame.manager.StormClientManager.Status;
import frame.pojo.Topology;
import frame.util.FileUtil;

@At("")
@IocBean
public class Console {

	Logger LOG = LoggerFactory.getLogger(Console.class);

	@Inject
	private StormClientManager stormClientManager;

	@Inject
	private FileUtil fileUtil;

	private List<String> configurations;

	@At()
	@Ok("jsp")
	public void index(HttpServletRequest request) throws TException, IllegalArgumentException, IllegalAccessException {
		Map<Status, List<Topology>> stormStatus = stormClientManager.getTopologyStatus();
		List<String> topologies = new ArrayList<String>();
		if (stormStatus.size() > 0) {
			for (Map.Entry<Status, List<Topology>> entry : stormStatus.entrySet()) {
				topologies.add(entry.getKey().getName());
				for (Topology topology : entry.getValue()) {
					topologies.add(StringUtils.replace(Json.toJson(topology, JsonFormat.compact()), "\"", "'"));
				}
			}
			request.setAttribute("status", topologies);
		} else {
			request.setAttribute("status", "暂无作业信息");
		}
		getConf(request);
	}

	@At
	public void inactive(@Param("name") String name) {
		try {
			stormClientManager.getClient().getClient().deactivate(name);
		} catch (NotAliveException e) {
			Log.error("topology with name " + name.toUpperCase() + " is NotAlive!");
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	@At
	public void reblance(@Param("name") String name, @Param("wait") Integer waitSecs) {
		try {
			Topology topology = stormClientManager.topos.get(name);
			RebalanceOptions options = new RebalanceOptions();
			options.set_wait_secs(waitSecs);
			options.set_num_workers(topology.getWorkerNum());
			// options.set_num_executors(topology.getExcutorNum());
			stormClientManager.getClient().getClient().rebalance(name, options);
		} catch (NotAliveException e) {
			Log.error("topology with name " + name.toUpperCase() + " is NotAlive!");
		} catch (InvalidTopologyException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	@At
	public void active(@Param("name") String name) {
		try {
			stormClientManager.getClient().getClient().activate(name);
		} catch (NotAliveException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	@At
	public void kill(@Param("name") String name, @Param("wait") Integer waitSecs) {
		KillOptions options = new KillOptions();
		options.set_wait_secs(waitSecs);
		try {
			stormClientManager.getClient().getClient().killTopologyWithOpts(name, options);
		} catch (NotAliveException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	@At
	@AdaptBy(type = UploadAdaptor.class, args = { "${app.root}/WEB-INF/tmp" })
	public void uploadJar(@Param("jar") TempFile file, @Param("mainMethod") String mainMethod, @Param("jobName") String jobName,
			@Param("config") String[] configs, @Param("configVal") Object[] configVals) {
		if (null == file) {
			throw new ServiceException("请选择上传文件!");
		}
		if ((Lang.isEmptyArray(configs) && !Lang.isEmptyArray(configVals)) || (!Lang.isEmptyArray(configs) && Lang.isEmptyArray(configVals))) {
			throw new ServiceException("所提交的参数有误!");
		}
		StringBuffer configKV = null;
		if (!Lang.isEmptyArray(configs) && !Lang.isEmptyArray(configVals)) {
			if (configs.length != configVals.length) {
				throw new ServiceException("参数名值对不对应!");
			}
			configKV = new StringBuffer("");
			for (int i = 0; i < configs.length; i++) {
				configKV.append(configs[i] + ":" + configVals[i]);
			}
		}
		submit(fileUtil.upload(file), mainMethod, jobName, configKV.toString());
	}

	/**
	 * 获取storm的所有可配置参数
	 */
	@At
	public void getConf(HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException {
		if (configurations == null || configurations.size() == 0) {
			configurations = new ArrayList<String>();
			Class<Config> clz = Config.class;
			Field[] fields = clz.getDeclaredFields();
			for (Field field : fields) {
				if (StringUtils.isAllUpperCase(field.getName().replaceAll("_", "")) && field.getType().getName().equals(String.class.getName())
						&& field.getModifiers() == 25 && field.getName().startsWith("TOPOLOGY_")) {
					if (field.get(clz) != null)
						configurations.add(field.get(clz).toString());
				}
			}
		}
		request.setAttribute("configs", configurations);
	}

	/**
	 * 
	 * @param jarPath
	 * @param jobName
	 * @param stormConf
	 *            可以自定义一些参数
	 */
	@At
	public String submit(String jarPath, String mainMethod, String jobName, String stormConf) {
		boolean emptyMain = StringUtils.isEmpty(mainMethod);
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(new File(jarPath));
			Enumeration<JarEntry> es = jarFile.entries();
			while (es.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) es.nextElement();
				String name = jarEntry.getName();
				if (name != null && name.endsWith(".class")) {
					Class<?> c = Lang.loadClass(name.replace("/", ".").substring(0, name.length() - 6));
					Method[] classMethods = c.getDeclaredMethods();
					for (Method method : classMethods) {
						if (!emptyMain) {
							if (mainMethod.equals(c.getName())) {
								System.setProperty("storm.jar", jarPath);
								method.invoke(c, (Object) new String[] { jobName, stormConf });
								return "提交成功";
							}
						} else {
							if (method.getName().equals("main")) {
								method.invoke(c, (Object) new String[] { jobName, stormConf });
								return "提交成功";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				jarFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "提交失败";
	}

	public String uploadFile(String jarPath) {
		try {
			String uploadLocation = stormClientManager.getClient().getClient().beginFileUpload();
			LOG.info("上传" + jarPath + "到: " + uploadLocation);
			BufferFileInputStream is = new BufferFileInputStream(jarPath);
			while (true) {
				byte[] toSubmit = is.read();
				if (toSubmit.length == 0)
					break;
				stormClientManager.getClient().getClient().uploadChunk(uploadLocation, ByteBuffer.wrap(toSubmit));
			}
			stormClientManager.getClient().getClient().finishFileUpload(uploadLocation);
			LOG.info("成功上传到: " + uploadLocation);
			return uploadLocation;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		// 获取路径：klass.getProtectionDomain().getCodeSource().getLocation()
		// List<NutResource> nss =
		// Scans.me().scan("/Users/uohzoaix/Desktop/StormStarter.jar");
		// findMain("/Users/uohzoaix/Desktop/StormStarter.jar");
	}

}
