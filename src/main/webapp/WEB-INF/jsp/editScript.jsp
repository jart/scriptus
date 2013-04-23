<html>
<%@page import="net.ex337.scriptus.model.api.*"%>
<%
String scriptId = (String)request.getAttribute("scriptId");
String scriptSource = (String)request.getAttribute("scriptSource");
%>
<head><title>Test script</title>
<%@include file="head.jsp"%>
<script src="../js/ace.js" type="text/javascript" charset="UTF-8"></script>
<script src="../js/mode-javascript.js" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">

var editor;

window.onload = function() {
    editor = ace.edit("editor");
	var JavaScriptMode = require("ace/mode/javascript").Mode;
	editor.getSession().setMode(new JavaScriptMode());
};

function saveScript() {

	document.getElementById("source").value = editor.getSession().getValue();

}

</script>
<style type="text/css">
#editor {
    position: relative;
/*    width: 100%;*/
    height: 50%;
    margin-bottom:1ex;
}
#quickref {
}
#quickref p {
    float: left;
    margin-right:2ex;
}

#quickref p a {
  color:inherit;
};

#quickref p a:hover {
  text-decoration:none;
}

</style>
</head>
<body>
<%@include file="header.jsp"%>

    <ul class="nav nav-pills">
	    <li>
		    <a href="<%=request.getContextPath()%>/scripts/list/yours">Your scripts</a>
	    </li>
	    <li>
	    	<a href="<%=request.getContextPath()%>/scripts/list/samples">Sample scripts</a>
	    </li>
	    <li class="active"><a href="#"><%=scriptId == null ? "New " : "Edit" %> script</a></li>
	</ul>
<%
boolean samples = (request.getAttribute("sample") != null);

if(samples){%>
	<div class="alert alert-info">
		<button type="button" class="close" data-dismiss="alert">x</button>
		<p>When you save this script, it will be saved under 'Your scripts' above.</p>
		
		<p>The original sample script will remain unchanged for other users.</p>
    </div>
<%
}
%>

<div class="row">
	<div class="span10">
		<form method="POST" action="edit" class="form-inline">
			<p>
				<label for="scriptid">Script name:</label>
				<input type="text" id="scriptid" name="scriptid" value="<%=scriptId == null ? "" : scriptId %>"/>
				<input type="submit" value="Save" onClick="saveScript()" class="btn btn-primary"/>	
			</p>
			<textarea id="source" name="source" style="display:none"></textarea> 
			<div id="editor"><%=scriptSource == null ? "" : scriptSource%></div>
		</form>
			
	</div>
	<div class="span4" id="quickref">
		<h5>Quick reference</h5>
	
		<%
			for(ScriptusMethod m : ScriptusAPI.SCRIPTUS_API) {
		%>
			<p><code><a href="#" title="<%=m.getQuickDesc()%>" class="methodName"><%=m.getQuickSyntax()%></a></code></p>
		<%}%>
	</div>
		
</div>



</body>
</html>
