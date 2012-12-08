<%@ page session="true" %>
<html>
<head>
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
	<div class="span12">
		<h1>What is Scriptus?</h1>

<p>Scriptus lets you weave code into your life.</p>

<p>Scriptus is code that puts people first. It combines JavaScript, Twitter and a simple UNIX-like process model.</p>

<p>Listening to people is this easy: <code>listen();</code></p>

<p>Talking to people is this easy: <code>say("hello!");</code></p>

<p>Scriptus is about the long term. What question would you ask yourself a year from now?</p>

<div class="lightborder"><p><tt>var question = ask("what would you ask yourself a year from now?");<br />
sleep("1 y");<br />
say(question);</tt></p></div>

<p>Scriptus goes beyond life-hacking or the quantified self, to allow you to instrument your
 life as you see fit. Just as computer-assisted chess players are stronger than either
  humans or computers, so can we be better when computers help us. Scriptus puts you directly
   in charge of the code, and puts the code as close to you as possible, by using the same
    social networks you use yourself.</p>

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
