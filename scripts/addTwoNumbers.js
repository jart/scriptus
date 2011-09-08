
var pid = scriptus.fork();

if(pid == 0) {
	return scriptus.ask("foo", "give me your number");
}

var second = scriptus.ask("bar", "give me second number");

var first = scriptus.wait();

scriptus.say("foo", "foo and bar="+(first + second));

