package frame.system;

import java.util.Timer;
import java.util.TimerTask;

import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

public class SystemSetup implements Setup {

	private static final Log log = Logs.get();

	private final static int INTERVAL = 30 * 1000; // 在线用户的未读消息刷新间隔时间 30秒

	private static Timer timer = new Timer("schedule Runner"); // 建立调度任务线程

	@Override
	public void init(NutConfig config) {
		Ioc ioc = config.getIoc();

		final Dao dao = ioc.get(null, "dao");

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
			}
		}, 1000, INTERVAL);
		if (log.isInfoEnabled()) {
			log.info("系统初始化完毕");
		}
	}

	@Override
	public void destroy(NutConfig config) {
		if (timer != null) {
			timer.cancel();
			timer = null;
			if (log.isInfoEnabled()) {
				log.info("schedule Runner 已关闭");
			}
		}
	}

}
