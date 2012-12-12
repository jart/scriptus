
var pid = scriptus.fork();

if(pid == 0) {
	return ask("foo", "give me your number");
}

sleep("1Y");

var second = ask("bar", "give me second number");

var first;

var waitedfor = wait(function(result) {
	say("bar", "in result fn, response from foo: "+result);
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
say("foo", "parent waited for pid "+waitedfor);

say("foo", "response from bar="+second+", from="+second.from+", cid="+second.cid);

say("foo", "foo and bar="+(new Number(first) + Number(second)));
