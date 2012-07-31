scriptus.say("before: "+new Date());
var s = scriptus.listen({timeout:"1m"});
scriptus.say("after: "+new Date()+", s="+s);

