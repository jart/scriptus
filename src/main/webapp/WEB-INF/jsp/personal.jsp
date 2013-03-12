<html>
<head><title>Scriptus - personal transport</title>
<%@include file="head.jsp"%>
<%@page import="java.util.List"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig"%>
<%@page import="net.ex337.scriptus.model.ProcessListItem"%>
<%@page import="net.ex337.scriptus.datastore.impl.jpa.dao.PersonalTransportMessageDAO"%>

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

function reply(parent) {

	document.getElementById("replyDiv").style.display="";
	if(parent) {
		document.getElementById("parent").value = parent;
	} else {
		document.getElementById("parent").value = "";
	}
	if(parent) {
		$("#messageLegend").html("Reply");
		document.getElementById("submit").setAttribute("value", "Reply");
	} else {
		$("#messageLegend").html("Write message");
		document.getElementById("submit").setAttribute("value", "Send");
	}
	
	document.getElementById("from").focus();

	return false;
}

</script>
</head>
<body>
<%@include file="header.jsp"%>

<ul class="nav nav-pills">
    <li>
	    <a href="<%=request.getContextPath()%>/transports">Transorts</a>
    </li>
    <li class="active">
    	<a href="<%=request.getContextPath()%>/transports/personal">Personal</a>
    </li>
</ul>

<%
List<PersonalTransportMessageDAO> m = (List<PersonalTransportMessageDAO>)request.getAttribute("messages");
%>

<div class="row">

	<div class="span9">

		<table class="table table-striped"><caption></caption>
			<thead>
			<!--    public String id;
			    public String parent;
			    public String message;
			    public String from;
			-->
				<tr>
					<th>Message ID</th>
					<th>'From'</th>
					<th>Message</th>
					<th>In reply to</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody>
			<%
				for(PersonalTransportMessageDAO p : m) {
			%>
				<tr>
					<td title="<%=p.id%>"><a name="<%=p.id%>"><%=p.id.substring(0,10)%>...</a></td>
					<td><%=p.from%></td>
					<td><%=p.message%></td>
					<td><% if(p.parent == null && ! "".equals(p.parent)) {%>-<%} else {%>
						 <a href="#<%=p.parent%>" title="<%=p.parent%>"><%=p.parent.substring(0,10)%>...</a>
					<%} %></td>
					<td>
						<a class="btn btn-primary" onclick="reply('<%=p.id%>')">Reply</a>
						<a class="btn btn-danger" onclick="del('<%=p.id%>')">Delete</a>
					</td>
				</tr>
			<%}%>
			</tbody>
		</table>
		
		<p><a href="#" onClick="reply();return false;" class="btn btn-primary">New message</a></p>

	</div>

	<div class="span3" id="replyDiv" style="display:none">
	
	<form action="<%=request.getContextPath()%>/transports/personal" method="POST">
		<fieldset>
			<legend id="messageLegend">Reply</legend>
			<p>
				<label for="from">'From':</label>
				<input type="text" name="from" id="from"/>
			</p>
			<p>
				<label for="msg">Message:</label>
				<input type="text" name="msg" id="msg"/>
			</p>
			<p>
				<input type="submit" id="submit" value="Reply" class="btn btn-primary"/>
			</p>
		</fieldset>
		<input type="hidden" name="op" id="op" value="msg"/>
		<input type="hidden" name="parent" id="parent" value=""/>
	</form>

	</div>


</div>


<form action="<%=request.getContextPath()%>/transports/personal" method="POST" id="delform" style="display:none">
	<input type="hidden" name="op" id="op" value="del"/>
	<input type="hidden" name="id" id="id"/>
</form>


</body></html>

