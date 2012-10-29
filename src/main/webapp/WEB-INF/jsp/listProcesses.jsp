<html>
<head><title>Scriptus - active processes</title>
<%@page import="java.util.List"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%@page import="net.ex337.scriptus.model.ProcessListItem"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">

function kill(pid) {

	if( ! confirm("sure?") ) {
		return false;
	}
	
	document.getElementById("killpid").value = pid;
	document.getElementById("killform").submit();

	return false;
}
</script>
</head>
<body>
<%@include file="header.jsp"%>

<h1>Scriptus - active processes</h1>

<%
List<ProcessListItem> processes = (List<ProcessListItem>)request.getAttribute("processes");
%>

<table><caption></caption>
<thead>
<!--    private UUID pid;
    private String uid;
    private String stateLabel;
    private int version;
    private int size;
    private Date created;
    private Date lastmod;-->
	<tr>
		<th>PID</th>
		<th>program</th>
		<th>version</th>
		<th>state</th>
		<th>Approx size on disk (kb)</th>
		<th>Created</th>
		<th>Modified</th>
		<th>Delete?</th>
	</tr>
</thead>
<tbody>
<%
	for(ProcessListItem p : processes) {
%>
	<tr>
		<td title="<%=p.getPid().toString()%>"><%=p.getPid().toString().substring(0,10)%>...</td>
		<td><%=p.getSourceName()%></td>
		<td><%=p.getVersion()%></td>
		<td><%=p.getStateLabel()%></td>
		<td><%=p.getSize()/1024%></td>
		<td><%=p.getCreated()%></td>
		<td><%=p.getLastmod()%></td>
		<td><a onclick="kill('<%=p.getPid().toString()%>')">X</a></td>
	</tr>
<%}%>
</tbody>
</table>



<form action="kill" method="POST" style="display:none" id="killform">
	<input type="hidden" name="killpid" id="killpid"/>
</form>

<p>Logged in as <%= session.getAttribute("openid") %>. <a href="<%=request.getContextPath()%>?logout"></a></p>


</body></html>

