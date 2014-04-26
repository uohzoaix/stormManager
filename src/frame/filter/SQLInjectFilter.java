package frame.filter;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

/**
 * 过滤SQL注入
 * 
 * @author Administrator
 * 
 */
public class SQLInjectFilter implements ActionFilter {
	private static final Log log = Logs.getLog(ActionFilter.class);

	@Override
	public View match(ActionContext actionContext) {
		HttpServletRequest request = actionContext.getRequest();
		// 防注入验证
		if (request.getQueryString() != null) {
			String queryString = request.getQueryString();
			if (queryString.contains("'") || queryString.contains("%27")) {
				if (log.isInfoEnabled()) {
					log.info(request.getRemoteAddr() + "尝试注入(get):" + request.getQueryString());
				}
				return new ServerRedirectView("/error.jsp?errorCode=4");
			}
		}

		Enumeration<?> e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement().toString();
			for (String value : request.getParameterValues(name)) {
				if (value.contains("'") || value.contains("%27")) {
					if (log.isInfoEnabled()) {
						log.info(request.getRemoteAddr() + "尝试注入(post):[" + name + "$$$" + value + "]");
					}
					return new ServerRedirectView("/error.jsp?errorCode=4");
				}
			}
		}

		return null;
	}

}
