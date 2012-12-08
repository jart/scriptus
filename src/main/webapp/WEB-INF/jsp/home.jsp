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

<% if(request.getAttribute("lastNewsItemLink") != null) {%>
	<p>Latest dev news: <a href="<%=request.getAttribute("lastNewsItemLink")%>"><%=request.getAttribute("lastNewsItemHeadline")%></a></p>
<%}%>

<div class="row">
	<div class="span8">
		<br />
	
		<p>Scriptus is <strong>programming on a human timescale.</strong></p>
		
		<p>Scriptus programs can run for days, months or years, so you can think long-term.</p>
		<br />
		<p>Scriptus is <strong>freedom of assembly.</strong></p>
		
		<p>Scriptus communicates via social networks like you do, and helps groups organise better.</p>

	</div>

	<div class="span4 lightborder" id="runScriptDiv">
<p><tt>var <span class="codevar">goal</span> = <span class="codeverb">ask</span>(<br />
  <span class="codestring">"where do you want to be,"</span><br />
  +<span class="codestring">" a year from now?"</span>);<br />
  <br />
<span class="codeverb">sleep</span>(<span class="codestring">"1 y"</span>); //"1y" is 1 year<br />
<br />
<span class="codeverb">say</span>(<span class="codestring">"Remember this? "</span>+<span class="codevar">goal</span>+<br />
    <span class="codestring">" How's it going?"</span>);<tt></p>
	</div>
</div>

</body></html>
			
