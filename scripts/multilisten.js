var msgId = say("say WHY?!");

var heard = listen({messageId:msgId});

say("I heard "+heard+" the first time from "+heard.from);

var heard = listen({messageId:msgId});

say("I heard "+heard+" the second time from "+heard.from);
