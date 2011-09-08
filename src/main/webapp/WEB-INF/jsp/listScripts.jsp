<html>
<head><title>Scriptus - user scripts</title>
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
<p>[<a href="<%=request.getContextPath()%>/">login</a> | <a href="<%=request.getContextPath()%>/scripts/list">list scripts</a> | <a href="http://127.0.0.1:<%=request.getLocalPort()%><%=request.getContextPath()%>/settings">settings (127.0.0.1 access only)</a> | <a href="<%=request.getContextPath()%>/?logout">logout</a>]</p>

<h1>Scriptus - user scripts</h1><ul>

<p><a href="edit">new</a></p>

<%			
			java.util.Set<String> scripts = (java.util.Set<String>) request.getAttribute("scripts");
			
			for(String s : scripts) {
				%><li>
				
				<a href="edit?script=<%=s%>"><%=s%></a> 
				&nbsp; [<a href="#" onClick="run('<%=s%>')">run</a>] 
				&nbsp; [<a href="#" onClick="del('<%=s%>')">delete</a>]
		
				</li><%
			}
%>
</ul>

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

<form action="delete" method="POST" style="display:none" id="deleteform">
	<input type="hidden" name="deleteid" id="deleteid"/>
</form>

<p>Logged in as <%= session.getAttribute("openid") %>. <a href="<%=request.getContextPath()%>?logout"></a></p>


</body></html>
			
