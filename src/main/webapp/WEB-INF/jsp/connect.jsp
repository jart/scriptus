<!DOCTYPE html>
<html lang="en">
<head><title>Scriptus - connect to Twitter</title>
<%@page import="java.util.List"%>
<%@include file="head.jsp"%>
<script type="text/javascript">

function del(transport) {
	
	document.getElementById("transport").value = transport;
	document.getElementById("op").value = "del";
	document.getElementById("transportform").submit();

	return false;
}


function connect(transport) {
	
	document.getElementById("transport").value = transport;
	document.getElementById("op").value = "connect";
	document.getElementById("transportform").submit();

	return false;
}

</script>
</head>
<body>
<%@include file="header.jsp"%>

<%

List installed = (List)request.getAttribute("installedTransports");
List free = (List)request.getAttribute("freeTransports");

%><p>Installed transports:</p><ul><%

for(Object o : installed) {
	%><li><%=o.toString()%> <a href="" onclick="del('<%=o.toString()%>')" class="btn btn-danger">Delete</a></li><%
}

%></ul><p>Free transports:</p><ul><%

for(Object o : free) {
	%><li><%=o.toString()%> <a href="#" onclick="connect('<%=o.toString()%>')"  class="btn btn-primary">Connect</a></li><%
}

%></ul>



<form action="<%=request.getContextPath()%>/scripts/delete" method="POST" style="display:none" id="transportform">
	<input type="hidden" name="transport" id="transport"/>
	<input type="hidden" name="op" id="op" value="fals"/>
</form>


</body></html>
			
