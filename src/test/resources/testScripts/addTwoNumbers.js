
var pid = scriptus.fork();

if(pid == 0) {
	return scriptus.ask("foo", "give me your number");
}

var second = scriptus.ask("bar", "give me second number");

var first;

var waitedfor = scriptus.wait(function(result) {
	scriptus.say("bar", "in result fn, response from foo: "+result);
	first = result;
});
/*
var f = eval(first);

f();

pipe(function(in, out) {

	var pid = fork();
	
	if(pid == 0) {
		say(in, "sending in");
		return;
	}
	
	say(listen(out));

})
*/
scriptus.say("foo", "parent waited for pid "+waitedfor);

scriptus.say("foo", "response from bar="+second+", from="+second.from+", cid="+second.cid);

scriptus.say("foo", "foo and bar="+(new Number(first) + Number(second)));
