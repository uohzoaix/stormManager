package frame.module;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import org.nutz.mvc.NutFilter;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.ioc.provider.ComboIocProvider;
import org.nutz.mvc.upload.UploadAdaptor;

import frame.filter.IPCheck;
import frame.filter.SQLInjectFilter;
import frame.system.SystemSetup;

/**
 * @描述 本类为整个应用的默认模块类。在这个类上，你可以：
 *     <ul>
 *     <li>通过 '@Modules' 注解声明整个应用有哪些模块
 *     <li>通过 '@IocBy' 注解声明整个应用，应采用何种方式进行反转注入。如果没有声明，整个应用将不支持 Ioc
 *     <li>通过 '@Localization' 注解声明整个应用的本地地化字符串的目录
 *     <li>通过 '@SetupBy' 注解声明应用启动的关闭时，应该进行的处理。（通过 Setup 接口）
 *     <li>通过 '@Ok' 注解声明整个应用默认的成功视图
 *     <li>通过 '@Fail' 注解声明整个应用默认的失败视图
 *     </ul>
 * @Modules 配置所有的功能模块，配置的模块可以使用ioc、mvc
 * @IocBy 配置所有ioc注入文件，JsonIocProvider的构造参数为保存配置文件的目录路径<br>
 *        1.a.26之前，JsonIocProvider的args只能接收具体的js配置文件<br>
 *        1.a.26及以后可以配置目录，框架会读取内部所有.json和.js文件，单个文件仍然可以配置
 * @Filters 整个应用的过滤器，用于权限验证<br>
 *          这里的过滤器优先级最低，可以被具体模块入口处的过滤器覆盖
 */
@WebFilter(urlPatterns = { "*.do" }, initParams = { @WebInitParam(name = "modules", value = "frame.module.MainModule") })
@IocBy(type = ComboIocProvider.class, args = { "*json", "frame/ioc", "*annotation", "frame.module" })
@SetupBy(SystemSetup.class)
@Modules(scanPackage = true)
@Ok("json")
@Fail("json")
@Filters({ @By(type = IPCheck.class), @By(type = SQLInjectFilter.class) })
@AdaptBy(type = JsonAdaptor.class)
public final class MainModule extends NutFilter {
}