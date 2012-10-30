<%
String pageLabel = (String)request.getAttribute("pageLabel");
if(pageLabel == null) pageLabel = "";
%><div class="navbar">
	<div class="navbar-inner">
		<a class="brand" href="<%=request.getContextPath()%>/">Scriptus</a>
		<ul class="nav">
			<li class="<%=pageLabel.equals("scripts") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/scripts/list">Scripts</a></li>
			<li class="<%=pageLabel.equals("processes") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/processes/list">Processes</a></li>
			<li class="<%=pageLabel.equals("settings") ? "active" : "" %>"><a href="http://127.0.0.1:<%=request.getLocalPort()%><%=request.getContextPath()%>/settings">Settings (local access only)</a></li>
		    <li class="dropdown">
			    <a class="dropdown-toggle" data-toggle="dropdown" role="button" href="#">Documentation
			    <b class="caret"></b></a>
	
			    <ul class="dropdown-menu">
					<li><a href="https://github.com/ianso/scriptus/blob/master/docs/userguide.md">User guide</a>
					<li><a href="https://github.com/ianso/scriptus/blob/master/docs/api.md">API docs</a><li>
					<li><a href="https://github.com/ianso/scriptus/blob/master/docs/knownproblems.md">Known problems</a><li>
			    </ul>
			</li>
		</ul>
		<ul class="nav pull-right">
			<li><a href="<%=request.getContextPath()%>/?logout">Logout</a></li>
		</ul>
	</div>
</div>