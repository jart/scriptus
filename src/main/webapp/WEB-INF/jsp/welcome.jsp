<!DOCTYPE html>
<html lang="en">
<head><title>Welcome to Scriptus</title>
<%@include file="head.jsp"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">
</script>
</head>
<body>
<%@include file="header.jsp"%>

<%

boolean clean = cfg.isCleanInstall();


<div class="row">

	<div class="span9">
	
		<h1>Welcome to Scriptus!</h1>
		
		<h2>Scriptus is <strong>Programming on a human timescale.</strong></h2>
		
		<p>Scriptus programs can run for days, months or years, so you can think long-term.</p>
		
		<h2>Scriptus is <strong>freedom of assembly.</strong></h2>
		
		<p>Scriptus communicates via social networks like you do.</p>

	</div>

	<div class="span3" id="runScriptDiv">
		<p><code>var goal = ask("where do you want to be a year from now?);
		<br />sleep("1y"); //"1y" is 1 year
		<br />say("Remember this? "+goal+" How's it going?");</code>
	</div>
</div>

</body></html>
			
