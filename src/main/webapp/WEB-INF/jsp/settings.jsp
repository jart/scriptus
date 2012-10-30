<html>
<head><title>Scriptus - settings</title>
<%@include file="head.jsp"%>
<%@page import="net.ex337.scriptus.config.ScriptusConfig, net.ex337.scriptus.config.ScriptusConfig.DatastoreType, net.ex337.scriptus.config.ScriptusConfig.TransportType"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
</head><body>
<%@include file="header.jsp"%>

<%if(cfg.isCleanInstall()){%>
<div style="background-color:yellow">
	<h2>Welcome!</h2>
	
	<p>No configuration file has been defined or saved yet. Below the defaults are presented.</p>
</div>
<%}%>

<p>If you save the settings, they will be kept in a properties file here:</p>

<p><code><%=cfg.getConfigLocation()%></code></p>

<p>If you want to start scriptus with another config file, you can pass a URL (file:// or http://, file-name, etc.) via the <code>scriptus.config</code> system property when starting.</p>

<form action="settings" method="POST">

<fieldset><legend>Scriptus settings</legend>

<p>Enter or modify settings here. Secret tokens are not presented and must be re-filled each time.</p>

<p>
	<label for="datastore">Data storage:</label>
	<select id="datastore" name="datastore">
		<option <%=cfg.getDatastoreType() == DatastoreType.Embedded? "selected=\"selected\"" : "" %> value="Embedded">Embedded database</option>
		<option <%=cfg.getDatastoreType() == DatastoreType.Db? "selected=\"selected\"" : "" %> value="Db">Database (external)</option>
		<option <%=cfg.getDatastoreType() == DatastoreType.Memory? "selected=\"selected\"" : "" %> value="Memory">In memory (transient)</option>
	</select>
</p>

<p>
<label for="transport">Transport:</label>
	<select id="transport" name="transport">
		<option <%=cfg.getTransportType() == TransportType.Twitter? "selected=\"selected\"" : "" %> value="Twitter">Twitter</option>
		<option <%=cfg.getTransportType() == TransportType.CommandLine? "selected=\"selected\"" : "" %> value="CommandLine">Command prompt</option>
		<option <%=cfg.getTransportType() == TransportType.Dummy? "selected=\"selected\"" : "" %> value="Dummy">Dummy response</option>
	</select>
</p>


<p>Important note concerning Twitter: the permissions of the application 
must be read-write <em>before</em> you generate the access token, and the
access token must be given read-write permissions at the moment of creation. 
Changing the permissions after all the tokens have been generated doesn't 
work for some reason. If your say()s and ask()s are not showing up on Twitter,
check the log files for errors.</p>

<p>
	<label for="twitterConsumerKey">Twitter consumer key:</label>
	<input type="text" name="twitterConsumerKey" id="twitterConsumerKey" value="<%=cfg.getTwitterConsumerKey()%>" />
</p>

<p>
	<label for="twitterConsumerSecret">Twitter consumer secret:</label>
	<input type="text" name="twitterConsumerSecret" id="twitterConsumerSecret" />
</p>

<p>
	<label for="twitterAccessToken">Twitter access token:</label>
	<input type="text" name="twitterAccessToken" id="twitterAccessToken" value="<%=cfg.getTwitterAccessToken()%>" />
</p>

<p>
	<label for="twitterAccessTokenSecret">Twitter access token secret:</label>
	<input type="text" name="twitterAccessTokenSecret" id="twitterAccessTokenSecret" />
</p>

<input type="submit" value="Save"/>

</form>

</body></html>
