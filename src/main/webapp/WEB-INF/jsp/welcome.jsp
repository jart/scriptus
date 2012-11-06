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

<div class="row">
	<div class="span8">
		<br />
	
		<p>Scriptus is <strong>programming on a human timescale.</strong></p>
		
		<p>Scriptus programs can run for days, months or years, so you can think long-term.</p>
		<br />
		<p>Scriptus is <strong>freedom of assembly.</strong></p>
		
		<p>Scriptus communicates via social networks like you do, and helps groups organise better.</p>

	</div>

	<div class="span4" id="runScriptDiv">
<pre class="code">var goal = ask(
  "where do you want to be,"
  +" a year from now?);
  
sleep("1y"); //"1y" is 1 year

say("Remember this? "+goal+
    " How's it going?");</pre>
	</div>
</div>

</body></html>
			
