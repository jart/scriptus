<!DOCTYPE html>
<html lang="en">
<head><title>Welcome to Scriptus</title>
<%@include file="/WEB-INF/jsp/head.jsp"%>
<%@page import="net.ex337.scriptus.model.api.*"%>
</head>
<body>
<%@include file="/WEB-INF/jsp/header.jsp"%>

<%

%>

<h1>Scriptus API</h1>

<p>This page lists all the Scriptus API calls with their arguments and a short description. For a fuller explanation including examples, see XXX.</p>

<p>Duration XXX</p>

<table class="table table-striped"><caption></caption>
<thead>
	<tr>
		<th>Method name</th>
		<th>Syntax</th>
		<th>Description</th>
	</tr>
</thead>
<tbody>
<%
	for(ScriptusMethod m : ScriptusAPI.SCRIPTUS_API) {
%>
	<tr>
		<td><%=m.getMethodName()%></td>
		<td><code><%=m.getQuickSyntax()%></code></td>
		<td><%=m.getQuickDesc()%></td>
	</tr>
<%}%>
</tbody>
</table>


</body></html>
			
