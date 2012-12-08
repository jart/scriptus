<%
String pageLabel = (String)request.getAttribute("pageLabel");
if(pageLabel == null) pageLabel = "";
%><div class="navbar">
	<div class="navbar-inner">
		<a class="brand" href="<%=request.getContextPath() + ((session.getAttribute("openid") != null) ? "/home" : "/") %>">Scriptus</a>
		<ul class="nav">
			<li class="<%=pageLabel.equals("about") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/about.jsp">About</a></li>
			<%if(session.getAttribute("openid") != null) {%>
				<li class="<%=pageLabel.equals("scripts") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/scripts/list">Scripts</a></li>
				<li class="<%=pageLabel.equals("processes") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/processes/list">Processes</a></li>
				<li class="<%=pageLabel.equals("settings") ? "active" : "" %>"><a href="http://127.0.0.1:<%=request.getLocalPort()%><%=request.getContextPath()%>/settings">Settings (local access)</a></li>
			<%}%>
			
			
			<li class="<%=pageLabel.equals("feedback") ? "active" : "" %>"><a href="<%=request.getContextPath()%>/feedback.jsp">Feedback</a></li>
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
		<%if(session.getAttribute("openid") != null) {%>
			<ul class="nav pull-right">
			
				<li class="<%=pageLabel.equals("settings") ? "you" : "" %>"><a href="<%=request.getContextPath()%>/you"><%= session.getAttribute("name") %></a></li>
				<li><a href="<%=request.getContextPath()%>/?logout">Logout</a></li>
			</ul>
		<%}%>
	</div>
</div>