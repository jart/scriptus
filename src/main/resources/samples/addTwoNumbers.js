
var pid = scriptus.fork();

if(pid == 0) {
	return ask("give me your number", {to:"foo"});
}

var second = ask("give me a second number", {to:"bar"});

var first;

var waitedfor = wait(function(result) {
	say("in result fn, response from foo: "+result, {to:"bar"});
	first = result;
});

say("parent waited for pid "+waitedfor, {to:"foo"});

say("response from bar="+second+", from="+second.from+", cid="+second.cid, {to:"foo"});

say("foo and bar="+(new Number(first) + Number(second)), {to:"foo"});
