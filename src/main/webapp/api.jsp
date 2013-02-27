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

<h1>A quick reference to the Scriptus API</h1>

<p>This page lists all the Scriptus API calls with their arguments and a short description. For fuller explanations including examples, see <a href="https://github.com/ianso/scriptus/blob/master/docs/api.md">the full documentation on GitHub</a>.</p>

<p>All extra parameters passed as an 'options' object using <code>{option:value,...}</code> are optional. When you see <code>duration</code> below, you can use a number of hours, a date as a string, which must be in the future, format <tt>yyyy-MM-dd HH:mm</tt>, a JavaScript Date object, which must be in the future, or a duration in the format <tt>0-9* [smhdwMqyDC]</tt> where the number indicates the quantity and the letter indicates the unit: seconds, minutes, hours, days, weeks, Months, quarters, years, Decades or Centuries respectively.</p>

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
			
