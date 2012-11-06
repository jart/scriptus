<%@ page session="true" %>
<html>
<head>
	<link type="text/css" rel="stylesheet" href="css/openid.css" />
	<script type="text/javascript" src="js/jquery-1.2.6.min.js"></script>
	<script type="text/javascript" src="js/openid-jquery.js"></script>
	<script type="text/javascript" src="js/openid-en.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			openid.init('openid_identifier');
		});
	</script>
</head>
<body>
<%

net.ex337.scriptus.config.ScriptusConfig f = (net.ex337.scriptus.config.ScriptusConfig)

org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean("config");

if(f.getDisableOpenID()) {

        session.setAttribute("openid", "no-openid");
        session.setAttribute("openid-claimed", "no-openid-claimed");
}

    if (request.getParameter("logout")!=null)
    {
        session.removeAttribute("openid");
        session.removeAttribute("openid-claimed");
%>
    Logged out!<p>
<%
    }
	if (session.getAttribute("openid") != null) {
		response.sendRedirect(request.getContextPath()+"/home");
		return;
	 }
%>
<h1>Scriptus - openID login</h1>
<form method="POST" action="consumer_redirect.jsp" id="openid_form">
		<fieldset>
			<legend>Sign-in or Create New Account</legend>
			<div id="openid_choice">
				<p>Please click your account provider:</p>
				<div id="openid_btns"></div>
			</div>
			<div id="openid_input_area">
				<input id="openid_identifier" name="openid_identifier" type="text" value="http://" />
				<input id="openid_submit" type="submit" value="Sign-In"/>
			</div>
			<noscript>
				<p>OpenID is service that allows you to log-on to many different websites using a single identity.
				Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
			</noscript>
		</fieldset>
</form>
</body>
</html>
