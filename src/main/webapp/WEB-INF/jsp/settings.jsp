<html>
<head><title>Scriptus - settings</title>
<%@page import="net.ex337.scriptus.config.ScriptusConfig, net.ex337.scriptus.config.ScriptusConfig.DatastoreType, net.ex337.scriptus.config.ScriptusConfig.TransportType"%>
<%
ScriptusConfig cfg = (ScriptusConfig)request.getAttribute("config");
%>
</head><body>
<%@include file="header.jsp"%>

<h1>Scriptus - settings</h1>
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
		<option <%=cfg.getDatastoreType() == DatastoreType.Aws? "selected=\"selected\"" : "" %> value="Aws">Amazon AWS (S3+SDB)</option>
		<option <%=cfg.getDatastoreType() == DatastoreType.File? "selected=\"selected\"" : "" %> value="File">Local file-system (under home directory)</option>
		<option <%=cfg.getDatastoreType() == DatastoreType.Memory? "selected=\"selected\"" : "" %> value="Memory">In memory (transient)</option>
	</select>
</p>

<p>
<label for="transport">Transport:</label>
	<select id="transport" name="transport">
		<option <%=cfg.getTransportType() == TransportType.Twitter? "selected=\"selected\"" : "" %> value="Twitter">Twitter</option>
		<option <%=cfg.getTransportType() == TransportType.Facebook? "selected=\"selected\"" : "" %> value="Facebook">Facebook</option>
		<option <%=cfg.getTransportType() == TransportType.CommandLine? "selected=\"selected\"" : "" %> value="CommandLine">Command prompt</option>
		<option <%=cfg.getTransportType() == TransportType.Dummy? "selected=\"selected\"" : "" %> value="Dummy">Dummy response</option>
	</select>
</p>

<p>
	<label for="awsAccessKeyId">AWS Access key:</label>
	<input type="text" name="awsAccessKeyId" id="awsAccessKeyId" value="<%=cfg.getAWSAccessKeyId()%>"/>
</p>

<p>
	<label for="awsSecretKey">AWS Secret key:</label>
	<input type="text" name="awsSecretKey" id="awsSecretKey" />
</p>

<p>
	<label for="s3Bucket">S3 bucket (must be globally unique):</label>
	<input type="text" name="s3Bucket" id="s3Bucket" value="<%=cfg.getS3Bucket() == null ? "scriptus"+java.util.UUID.randomUUID() : cfg.getS3Bucket() %>" />
	<%= (cfg.getS3Bucket() == null ? "suggested" : "")%>
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

<p>
	<label for="facebookAccessToken">Facebook access token:</label>
	<input type="text" name="facebookAccessToken" id="facebookAccessToken" <%=cfg.getFacebookAppKey()==null || cfg.getFacebookAppSecret()==null?"disabled='disabled'":""%> readonly="readonly" value="<%=cfg.getFacebookAccessToken()!=null?cfg.getFacebookAccessToken():""%>" />
	<button type="button" <%=cfg.getFacebookAppKey()==null || cfg.getFacebookAppSecret()==null?"disabled='disabled'":""%> onclick="<%=cfg.getFacebookAppKey()!=null && cfg.getFacebookAppSecret()!=null?"window.location = 'https://www.facebook.com/dialog/oauth?client_id="+cfg.getFacebookAppKey()+"&redirect_uri="+"http://localhost:8080"+request.getContextPath()+"/settings?&scope=read_stream,publish_stream,manage_notifications&state=666":""%>'">Get access token</button>
</p>

<p>
	<label for="facebookAppKey">Facebook application key:</label>
	<input type="text" name="facebookAppKey" id="facebookAppKey" value="<%=cfg.getFacebookAppKey()!=null?cfg.getFacebookAppKey():""%>" />
</p>

<p>
	<label for="facebookAppSecret">Facebook application secret:</label>
	<input type="text" name="facebookAppSecret" id="facebookAppSecret" value="<%=cfg.getFacebookAppSecret()!=null?cfg.getFacebookAppSecret():""%>" />
</p>
<input type="submit" value="Save"/>
</fieldset>

</form>

</body></html>
