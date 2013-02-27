<%@ page session="true" %>
<html>
<head>
	<%@include file="/WEB-INF/jsp/head.jsp"%>
	<link type="text/css" rel="stylesheet" href="css/openid.css" />
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
            session.setAttribute("name", "no OpenID");
            session.setAttribute("email", "nope@example.com");
}

    if (request.getParameter("logout")!=null)
    {
        session.removeAttribute("openid");
        session.removeAttribute("openid-claimed");
%><!--
    <p>Logged out!<p>
--><%
    }
	if (session.getAttribute("openid") != null) {
		response.sendRedirect(request.getContextPath()+"/home");
		return;
	 }
%>

<div class="row">
	<div class="span12">
		<div class="hero-unit">
		    <h1>Welcome to Scriptus!</h1>
			<p>Scriptus is <strong>code that puts people first.</strong></p>
			<p>Scriptus frees you to think and act directly with yourself and other people in code.<p> <a href="about.jsp">Learn more about Scriptus here.</a></p>
		</div>
	</div>
</div>	
<div class="row">

	<div class="span4">
		<h4>Know JavaScript? It's this easy:</h4>
		<div class="lightborder">
			<p><tt>var <span class="codevar">goal</span> = <span class="codeverb">ask</span>(<br />
			  <span class="codestring">"where do you want to be,"</span><br />
			  +<span class="codestring">" a year from now?"</span>);<br />
			  <br />
			<span class="codeverb">sleep</span>(<span class="codestring">"1 y"</span>); //"1y" is 1 year<br />
			<br />
			<span class="codeverb">say</span>(<span class="codestring">"Remember this? "</span>+<span class="codevar">goal</span>+<br />
			    <span class="codestring">" How's it going?"</span>);</tt></p>
		</div>
	</div>
	<div class="span8">
		<h4>Get started by signing in with any OpenID:</h4>
		<form method="POST" action="consumer_redirect.jsp" id="openid_form">
			<fieldset>
				<div id="openid_choice">
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
	</div>
</div>


</body>
</html>
