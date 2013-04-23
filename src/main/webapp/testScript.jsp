<!DOCTYPE html>
<html lang="en">
<head><title>Welcome to Scriptus</title>
<%@include file="/WEB-INF/jsp/head.jsp"%>
<%@page import="net.ex337.scriptus.model.api.*"%>
<script type="text/javascript">


var ctr = 0;

function log(m) {
	alert("log "+m);
}

function kill(pid) {
	alert("kill "+pid);
}

function fork() {
	if( ! confirm("return to parent (cancel) or child (ok)?")) {
		return "pid:"+(ctr++);
	}
	return null;
}

function exec(file, args) {
	alert("exec "+file+", args="+args);
	throw file;
}

function listen(opts) {
	var s = prompt("listen "+dumpOpts(opts));
	if(s && s != "") {
		s.messageId = (ctr++);
		return s;
	}
	return null;
}
        
function say(msg, opts) {
	alert("say "+msg+" "+dumpOpts(opts));
	return (ctr++);
}
        
function ask(msg, opts) {
	var s = prompt("ask "+msg+" "+dumpOpts(opts));
	if(s && s != "") {
		s.messageId = (ctr++);
		return s;
	}
	return null;
}
        
function get(url) {
	var s = prompt("get "+url);
	if(s && s != "") {
		return s;
	}
	return null;
}
        
function sleep(duration) {
	alert("sleep "+duration);
}
        
function exit(result) {
	alert("exit "+result);
	throw "exit "+result;
}

function wait(fn, pid) {
	var s = prompt("wait pid="+pid);
	if(s && s != "") {
		if(fn) fn(s);
	}
}        

function dumpOpts(opts) {
	var r = "";
	if( !opts) return r;
	if(opts.to) {
		r+="to = "+opts.to+" ";
	}
	if(opts.timeout) {
		r+="timeout = "+opts.timeout+" ";
	}
	if(opts.messageId) {
		r+="messageId = "+opts.messageId+" ";
	}
	return r;
		
}

	function test() {
		var script = document.getElementById("test").value;
		var cmpls = "var f = function(owner, args) {"+
			script+
		"};try{"+
			"f(prompt(\"owner\"), prompt(\"args\"));"+
		"} catch(e){"+
			"alert(\"error:\"+e);if(console && console.trace) {console.trace();}"+
		"}";

	 
	 	var testScript = document.getElementById("testScript");
	 	if(testScript) {
	 		testScript.parentNode.removeChild(testScript);
	 	}
	 
    	var el = document.createElement("script");
    	el.setAttribute("type", "text/javascript");
    	el.setAttribute("id", "testScript");
    	el.innerHTML = cmpls;
		 var head = document.getElementsByTagName('head')[0];
    	head.appendChild(el);
   		
		f();
		
	}

</script>
</head>
<body>
<%@include file="/WEB-INF/jsp/header.jsp"%>

<%

%>

<h1>Test scripts</h1>

<textarea style="width:100%; height:15ex" id="test"></textarea>

<a href="#" onclick="test();return false" class="btn btn-primary">Test</a>


</body></html>
			
