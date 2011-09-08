<html>
<%
String scriptId = (String)request.getAttribute("scriptId");
String scriptSource = (String)request.getAttribute("scriptSource");
%>
<head><title><%= (scriptId == null ? "New Script" : "Edit script "+scriptId) %><title>
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
    width: 100%;
    height: 50%;
}
</style>
</head>
<body>
<p>[<a href="<%=request.getContextPath()%>/">login</a> | <a href="<%=request.getContextPath()%>/scripts/list">list scripts</a> | <a href="http://127.0.0.1:<%=request.getLocalPort()%><%=request.getContextPath()%>/settings">settings (127.0.0.1 access only)</a> | <a href="<%=request.getContextPath()%>/?logout">logout</a>]</p>
	<h1><%= (scriptId == null ? "New Script" : "Edit script "+scriptId) %></h1>
<!--fixme escaping-->
	
<form method="POST" action="edit">
<p>
	<label for="scriptid">Script name:</label>
	<input type="text" id="scriptid" name="scriptid" value="<%=scriptId == null ? "" : scriptId %>"/>
</p>
<textarea id="source" name="source" style="display:none"></textarea> 
<input type="submit" value="Save" onClick="saveScript()"/>
</form>	
<div id="editor"><%=scriptSource == null ? "" : scriptSource%></div>

</body>
</html>
