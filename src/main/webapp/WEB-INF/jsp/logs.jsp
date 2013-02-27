<html>
<head><title>Scriptus - active processes</title>
<%@include file="head.jsp"%>
<%@page import="java.util.List"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%@page import="net.ex337.scriptus.datastore.impl.jpa.dao.LogMessageDAO"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
<script type="text/javascript">

function del(id) {

	if( ! confirm("sure?") ) {
		return false;
	}
	
	document.getElementById("id").value = id;
	document.getElementById("delform").submit();

	return false;
}
</script>
</head>
<body>
<%@include file="header.jsp"%>

<ul class="nav nav-pills">
    <li>
	    <a href="<%=request.getContextPath()%>/processes/list">List processes</a>
    </li>
    <li class="active">
    	<a href="<%=request.getContextPath()%>/processes/logs">Process logs</a>
    </li>
</ul>

<%
List<LogMessageDAO> logs = (List<LogMessageDAO>)request.getAttribute("logs");
%>

<table class="table table-striped"><caption></caption>
<thead>


<!--

    public String message;
    public long created;
    public String pid;
    public LogMessageDAOId id;
	    public String id;
	    public String userId;
		  -->
	<tr>
		<th>Timestamp</th>
		<th>PID</th>
		<th>Message</th>
		<th>&nbsp;</th>
	</tr>
</thead>
<tbody>
<%
	for(LogMessageDAO l : logs) {
%>
	<tr>
		<td><%=l.created%></td>
		<td title="<%=l.pid%>"><%=l.pid.substring(0,10)%>...</td>
		<td><%=l.message%></td>
		<td><a class="btn btn-danger" onclick="del('<%=l.id.id%>')">Delete</a></td>
	</tr>
<%}%>
</tbody>
</table>



<form action="<%=request.getContextPath()%>/processes/logs" method="POST" style="display:none" id="delform">
	<input type="hidden" name="op" id="op" value="delete"/>
	<input type="hidden" name="id" id="id"/>
</form>


</body></html>

