
var pid = scriptus.fork();

if(pid == 0) {
	return scriptus.ask("give me a number", {to:"adam"});
}

var second = scriptus.ask("give me a number", {to:"bart"});

var first;

scriptus.wait(function(result) {first=result;});

//this is string concatenation!

scriptus.say("both numbers="+(first + second), {to:"carole"});

