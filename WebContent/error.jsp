<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>${msg.SystemName }出错了</title>
</head>
<body>
<%
	Object obj = request.getParameter("errorCode");
	Map<Integer,String> map = new HashMap<Integer,String>();
	map.put(1,"login=请先登陆平台再操作");
	map.put(2,"login=您的帐号在其他位置登陆,您已被迫下线");
	map.put(3,"none=请从正确的路径访问!");
	map.put(4,"back=您提交的内容有非法字符");
	map.put(5,"none=您没有访问该模块的权限");

	if (obj != null) {
		String errMsg = map.get(Integer.valueOf(obj.toString()));
		if (errMsg == null) {
			errMsg = "none=未知错误,错误码:"+obj;
		}
		String[] msg = errMsg.split("=");
		out.write("<script>");
		out.write("alert('" + msg[1] + "');");
		if (msg[0].equals("login")) {
			out.write("window.location.href = '"+request.getContextPath()+"/login.jsp';");
		} else if (msg[0].equals("back")) {
			out.write("history.back();");
		}
		out.write("</script>");
	} else {
%>
错误信息:<br/>
<p>
	<c:catch var ="catchException">
		${obj.ok}&nbsp;&nbsp;&nbsp;
		${obj.detailMessage}
	</c:catch>
	<c:if test = "${catchException!=null}">
		${obj }<br><br>
		<c:catch var ="exception">
			${obj.cause }
		</c:catch>
	</c:if>
</p>
<%
	}
%>
<a href="javascript:void(0)" onclick="history.back()">返回</a>
<a href="/fbsms/login.jsp">返回登陆页面</a>
</body>
</html>