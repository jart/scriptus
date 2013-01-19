<%@ page session="true" %>
<html>
<head>
	<title>What is Scriptus?</title>
	<%@include file="/WEB-INF/jsp/head.jsp"%>
	<link type="text/css" rel="stylesheet" href="css/openid.css" />
	<script type="text/javascript" src="js/openid-jquery.js"></script>
	<script type="text/javascript" src="js/openid-en.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			openid.init('openid_identifier');
		});
	</script>
</head>
<body>
<%request.setAttribute("pageLabel", "about");%>
<%@include file="/WEB-INF/jsp/header.jsp"%>

<div class="row">
	<div class="offset1 span10">
		<h1>What is Scriptus?</h1>
		
		<p>Scriptus lets you <strong>weave code into your life</strong>.</p>
		
		<p>Scriptus is <strong>code that puts people first</strong>.</p>
		
		<p><strong>Listening</strong> to people is this easy: <tt><span class="codeverb">listen</span>();</tt></p>
		
		<p><strong>Talking</strong> to people is this easy: <tt><span class="codeverb">say</span>(<span class="codestring">"Hello!"</span>);</tt></p>
		
	</div>

</div>

<div class="row">

	<div class="offset1 span10">
	
		<p>Scriptus is <strong>programming on a human timescale</strong>. Time is no object:</p>

	</div>

</div>

<div class="row">
	
	<div class="offset1 span10">
		<div class="lightborder"><p><tt>var <span class="codevar">question</span> = <span class="codeverb">ask</span>(<span class="codestring">"what would you ask yourself a year from now?"</span>);<br />
		<span class="codeverb">sleep</span>(<span class="codestring">"1y"</span>); //1y is 1 year<br />
		<span class="codeverb">say</span>(<span class="codestring">question</span>);</tt></p></div>
	</div>

</div>

<div class="row">
	<div class="offset1 span10">

<p>Scriptus allows you to <strong>instrument your
 life</strong> as you see fit.</p>
 

	</div>
</div>	


<div class="row">
	<div class="offset1 span10">
		<h1>Why use Scriptus?</h1>
	
    	<p>People and computers are good at different things.
    	 Scriptus is about people using computers to augment themselves.</p>
    	
    	<p>People have been doing this with technology since the axe was invented,
    	but programming is different, 
    	and Scriptus is a new way of thinking about programming.</p>
    	
    	<p>Many simple ideas such as personal motivation/self-improvement tools or bike maintenence schedules are simpler in Scriptus than on the web.
    	
    	<p>Scriptus makes it easy to organise or schedule anything you want in precisely the way you want.</p>
		
	</div>
</div>	


<div class="row">
	<div class="offset1 span10">
		
		<h1>How does Scriptus work?</h1>
		
		<p>Scriptus combines JavaScript, Twitter and a simple UNIX-like process model.</p>
		
		<p>Scriptus runs on the same social networks that you use yourself, on Twitter or Facebook.</p>

		<p>Scriptus processes are persisted on disk, so they survive restarts, can be moved anywhere, and can wait for input practically forever.</p>
		
	</div>
</div>

<%if(session.getAttribute("openid") == null) {%>
	<div class="row">
	
		<div class="span4">
			<h4>Know JavaScript? It's this easy:</h4>
			<div class="lightborder">
				<p><tt>var <span class="codevar">goal</span> = <span class="codeverb">ask</span>(<br />
				  <span class="codestring">"where do you want to be,"</span><br />
				  +<span class="codestring">" a year from now?"</span>);<br />
				  <br />
				<span class="codeverb">sleep</span>(<span class="codestring">"1 y"</span>); //"1y" is 1 year<br />
				<br />
				<span class="codeverb">say</span>(<span class="codestring">"Remember this? "</span>+<span class="codevar">goal</span>+<br />
				    <span class="codestring">" How's it going?"</span>);</tt></p>
			</div>
		</div>
		<div class="span8">
			<h4>Get started by signing in with any OpenID:</h4>
			<form method="POST" action="consumer_redirect.jsp" id="openid_form">
				<fieldset>
					<div id="openid_choice">
						<div id="openid_btns"></div>
					</div>
					<div id="openid_input_area">
						<input id="openid_identifier" name="openid_identifier" type="text" value="http://" />
						<input id="openid_submit" type="submit" value="Sign-In"/>
					</div>
					<noscript>
						<p>OpenID is service that allows you to log-on to many different websites using a single identity.
						Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
					</noscript>
				</fieldset>
			</form>	
		</div>
	</div>
<%} else {%>

	<p>You can get started now by running sample scripts, or writing your own...</p> 

<%}%>


</body>
</html>
