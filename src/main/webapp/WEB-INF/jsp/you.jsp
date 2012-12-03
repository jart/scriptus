<html>
<head><title>Scriptus - You</title>
<%@include file="head.jsp"%>
<%@page import="java.util.List"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%@page import="net.ex337.scriptus.model.ProcessListItem"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">
</script>
</head>
<body>
<%@include file="header.jsp"%>

<p>You are: <%= session.getAttribute("name") %></p>
<p>Email: <%= session.getAttribute("email") %></p>
<p>OpenID: <%= session.getAttribute("openid") %></p>
<p>Claimed OpenID: <%= session.getAttribute("openid-claimed") %></p>

</body></html>

