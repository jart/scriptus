<html>
<head><title>Scriptus - active processes</title>
<%@include file="head.jsp"%>
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
	
	document.getElementById("pid").value = pid;
	document.getElementById("killform").submit();

	return false;
}
</script>
</head>
<body>
<%@include file="header.jsp"%>

<ul class="nav nav-pills">
    <li class="active">
	    <a href="<%=request.getContextPath()%>/processes/list">List processes</a>
    </li>
    <li>
    	<a href="<%=request.getContextPath()%>/processes/logs">Process logs</a>
    </li>
</ul>

<%
List<ProcessListItem> processes = (List<ProcessListItem>)request.getAttribute("processes");
%>

<table class="table table-striped"><caption></caption>
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
		<th>Program</th>
		<th>Version</th>
		<th>State</th>
		<th>Alive?</th>
		<th>Size (kb)</th>
		<th>Created</th>
		<th>Modified</th>
		<th>&nbsp;</th>
	</tr>
</thead>
<tbody>
<%
	for(ProcessListItem p : processes) {
%>
	<tr>
		<td title="<%=p.getPid().toString()%>"><%=p.getPid().toString().substring(0,10)%>...</td>
		<td><a href="edit?script=<%=p.getSourceName()%>"><%=p.getSourceName()%></a></td>
		<td><%=p.getVersion()%></td>
		<td><%=p.getStateLabel()%></td>
		<td><%=p.isAlive() ? "Yes" : "No"%></td>
		<td><%=p.getSize()/1024%></td>
		<td><%=p.getCreated()%></td>
		<td><%=p.getLastmod()%></td>
		<td><a class="btn btn-danger" onclick="kill('<%=p.getPid().toString()%>')">Delete</a></td>
	</tr>
<%}%>
</tbody>
</table>



<form action="<%=request.getContextPath()%>/processes/list" method="POST" id="killform">
	<input type="op" name="op" id="op" value="kill"/>
	<input type="hidden" name="pid" id="pid"/>
</form>


</body></html>

