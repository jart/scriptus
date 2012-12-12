<!DOCTYPE html>
<html lang="en">
<head><title>Welcome to Scriptus</title>
<%@include file="head.jsp"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%
//ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">
</script>
</head>
<body>
<%@include file="header.jsp"%>

<%

//boolean clean = cfg.isCleanInstall();
%>

<div class="row">
	<div class="span12">
		<h1>Welcome to Scriptus!</h1>		
	</div>
</div>
<ul>
	<li>New to Scriptus? <a href="about.jsp">Learn about Scriptus here.</a></li>
	<li>Writing programs? <a href="<%=request.getContextPath()%>/scripts/list">These sample programs</a> and the API overview can help you get started.</li>
	<%
	Integer countProcesses = (Integer)request.getAttribute("countProcesses");
	Integer countScripts = (Integer)request.getAttribute("countScripts");
	
	if(countScripts != 0 || countProcesses != 0) {
	%>	<li>You have
	 <a href="<%=request.getContextPath()%>/scripts/list"><%=countScripts%> 
	 	saved script<%=(countScripts == 1 ? "" : "")%></a>
	 	 and <%
	 	 if(countProcesses == 0) {
	 	 	%>no processes running.<%
	 	 } else {
	 	 %><a href="<%=request.getContextPath()%>/processes/list"><%=countProcesses%> process<%=(countProcesses == 1 ? "" : "es")%> running</a>.<%
	 	 } 
	 	 %></li><%
	} else {
		//new user ? 
	}
	
	%>
	<li>Have feedback? <a href="feedback.jsp">Don't hesitate to send it.</a></li>
<% if(request.getAttribute("lastNewsItemLink") != null) {%>
	<li>Latest news from the <a href="http://ianso.github.com/scriptus/">dev blog</a>: <a href="<%=request.getAttribute("lastNewsItemLink")%>"><%=request.getAttribute("lastNewsItemHeadline")%></a></li>
<%}%>
	<li><a href="">Follow us on Twitter</a> or <a href="https://github.com/ianso/scriptus">check out the project on GitHub</a>.</li>
</ul>

<div class="row">
	<div class="span8">
		<br />

	</div>

</body></html>
			
