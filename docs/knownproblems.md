
#Known problems

Scriptus does generally work as expected, however there are some quirks, bugs and niggles that are good to know about. Below are listed all such problems of which I'm currently aware. This page will be updated to reflect the status of the latest build.

##Scheduler timing

The scheduler currently polls the storage for tasks to execute every minute, on the minute. This means that timeouts and durations of a minute or less will take up to a minute plus the time to next poll. e.g. a `sleep()` of 1 minute at 04:34:40 will wake the process at roughly 04:36:00.

##CID namespace size

The correlation ID used in ask() is a number between 0 and 0xFFFFFF, rendered base 62 using 0-9, A-Z and a-z as the alphabet.

This CID should be stored on a per-user basis, as the namespace is too small to guarantee uniqueness over a big deployment.

As there are no big deployments right now this is very theoretical.

##Rhino continuations & nested function calls

Because of the way Rhino captures continuations, nested function calls around continuations can fail unexpectedly. The following example is very unsafe but illustrates the problem:

```javascript
//this works no problem:
var code = ask("What should I execute?");
eval(code); 

//this doesn't work:
eval(ask("What should I try and execute?");
```

##Listen timeout

The `listen()` call works by registering a process as a listener inside the Twitter interaction medium. We then poll Twitter every minute for mentions by the user being listened to.

However, if the user 'speaks' after the last poll but before the timeout, then this tweet is not picked up before the process is woken and null is returned to the program.

In addition, the 'listen' is not unregistered on waking, causing a needless process load & nonce check.

##Process re-parenting & storage economy

In UNIX, when a root process terminates its children if any are 're-parented' to a caretaker (or something) and continue execution. In Scriptus, child processes continue execution, but when they terminate, it is assumed that a root process will wait on them at some point and so their state & result is persisted.

This could balloon storage over time, which would cost money if AWS is being used as the datastore. Good hygiene and tidiness is not yet completely enforced.

