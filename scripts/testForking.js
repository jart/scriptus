var pid = scriptus.fork();

var test1result =  "the child finished first";

if(pid == 0) {

	scriptus.say("test1: in child");

	return test1result;
	
} else {

	scriptus.say("test1: in parent, sleeping");

	scriptus.sleep(20);

	scriptus.say("test1: in parent, slept");

}

var result;

var waitf = function(r) {result = r;};

scriptus.say("test1, in parent, waiting");

scriptus.wait(waitf);

scriptus.say("test1 completed: "+result);

if(test1result != result) {
  throw result;
}

var test2result =  "the parent waited first";

pid = scriptus.fork();

if(pid == 0) {

	scriptus.say("test2, in child, sleeping");

	scriptus.sleep(20);

	scriptus.say("test2: in child, slept");
	
	return test2result;
} else {

	scriptus.say("test2, in parent, waiting");

	scriptus.wait(waitf);
}

scriptus.say("test2, in parent, result="+result);

if(test2result != result) {
  throw result;
}

scriptus.say("test2: "+result);

return "all OK";

