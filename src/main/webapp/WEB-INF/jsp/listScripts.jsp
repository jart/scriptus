<!DOCTYPE html>
<html lang="en">
<head><title>Scriptus - user scripts</title>
<%@include file="head.jsp"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">
function run(id) {

	document.getElementById("runid").value = id;
	document.getElementById("owner").focus();

	return false;
}

function del(id) {

	if( ! confirm("sure?") ) {
		return false;
	}
	
	document.getElementById("deleteid").value = id;
	document.getElementById("deleteform").submit();

	return false;
}
</script>
</head>
<body>
<%@include file="header.jsp"%>

<%
java.util.Set<String> scripts = (java.util.Set<String>) request.getAttribute("scripts");

if(cfg.isCleanInstall() || scripts == null || scripts.isEmpty()){%>
	<div class="alert alert-success">
		<button type="button" class="close" data-dismiss="alert">x</button>
		<h4>Welcome to Scriptus!</h4>
		<p>You don't have any scripts saved on this installation yet.</p>
	
		<p>You can <a href="edit">create a new script here</a>, or you might like to try one of the <a href="https://github.com/ianso/scriptus/tree/master/scripts/">the examples</a>, which are <a href="https://github.com/ianso/scriptus/blob/master/docs/examples.md">explained and documented here.</a></p>
	
		<p>You can access <a href="http://127.0.0.1:<%=request.getLocalPort()%><%=request.getContextPath()%>/settings">the settings page here</a>, provided you are using the installation from localhost.</a>
    </div>


<%
}
%>


    <ul class="nav nav-pills">
	    <li>
		    <a href="#">Your scripts</a>
	    </li>
	    <li class="<%=(cfg.isCleanInstall() || scripts == null || scripts.isEmpty()) ? "active" : ""%>">
	    	<a href="#">Sample scripts</a>
	    </li>
	    <li><a href="edit">New script</a></li>
	</ul>

<div class="row">

	<div class="span9">

<%	if(scripts != null) for(String s : scripts) {
		%><li>
	
		<a href="edit?script=<%=s%>"><%=s%></a> 
		&nbsp; [<a href="#" onClick="run('<%=s%>')">run</a>] 
		&nbsp; [<a href="#" onClick="del('<%=s%>')">delete</a>]

		</li><%
	}
	%></ul>

	</div>

	<div class="span3" id="runScriptDiv" style="display:none">
	
	<form action="run" method="POST">
		<fieldset>
			<legend>Run script</legend>
			<p>
				<label for="runid">Script:</label>
				<input type="text" name="runid" id="runid"/>
			</p>
			<p>
				<label for="owner">Owner (twitter name):</label>
				<input type="text" name="owner" id="owner"/>
			</p>
			<p>
				<label for="args">Arguments:</label>
				<input type="text" name="args" id="args"/>
			</p>
			<p>
				<input type="submit" id="submit" value="Run"/>
			</p>
		</fieldset>
	</form>

	</div>
</div>

<form action="delete" method="POST" style="display:none" id="deleteform">
	<input type="hidden" name="deleteid" id="deleteid"/>
</form>


</body></html>
			
