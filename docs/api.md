
Scriptus is a way of programming interactions between people. What's new is that these interactions can span days, weeks, months or years, and can be complex: elections, chess tournaments and even ARGs are all easy to create.

Scriptus interacts with people via Twitter, because it's the easiest way in which people can have messages pushed to them. If this idea takes off, then other potential clients could include iPhone, Android, and SMS.

On Twitter, people are asked questions using @mentions and questions are tracked using #hashtags. Responses are sent back to Scriptus in the same way. Anything after "//" will be ignored. For example:

```
@ianso #ef3hED electing HOW many presidents-for-life?!
@robotoscriptu #ef3hED 4 //but I can depose them whenever I want!
```

Scripts are written in JavaScript and are wrapped in a function declaration before being executed - this is why /return/ acts as expected. All the usual JavaScript objects such as Date and String are present and correct, as are top-level functions such as eval(). However, none of the methods associated with in-browser JavaScript such as alert() are present. 

SIDEBAR
Although eval() works, scriptus API methods as listed below cannot be executed within them. For example, this code wouldn't work:
eval("scriptus.say('Where ARE my socks?')")
Whereas this code would:
eval("function(){scriptus.say('Where are MY socks?';}")();

The scriptus API is very simple, and can be divided into two main sections, that of interaction and program control. The interaction functions are as follows:

say(message, [{who:person}])

This method sends the message provided to the person specified. If no person is present, the message will be "said" to the script owner.

/message/ listen([{who:who, timeout:1}])

This method listens for any messages sent from the person specified, or the owner if none is specified, and returns the first one it finds as a string.

The string has an extra properties: 
 * /cid/, the communication ID (message ID)
 * /from/, the user who sent the tweet
 
The options object is optional and is a Javascript object in which extra parameters can be added. At present there is only one supported parameter, which is "timeout", whose value should be a number. If present, this number specifies how long to listen for before returning, in *** An example follows:

var message = scriptus.listen({who:"ianso", timeout:1}); //listen for 1 hour

/message/ ask(message, {who:"ianso", timeout:1})

This method sends a message to a person in the style of /say/, and then awaits responses in the style of /listen/. The result, as with /listen/, has extra properties for correlation and identification. The options are as for the listen() method

The program control methods are basically taken from UNIX and are as follows:

/pid/ fork()

The fork() method is used to split the current process into two separate processes. Each process then continues exectution.

The child process will have /0/ returned as the pid, and the parent process will have returned the pid of the child.

The pid is not a number like in UNIX, but a UUID, represented as a string.

/pid/ wait(fn), /pid/ wait(fn, pid)

This method blocks on the process indicated by the provided pid, or the last child process created if none is provided.

As in UNIX, the method returns the pid of the process that was waited for. If no process was waited for, either because a bad pid was provided or no pid was provided and the process has not yet forked, -1 is returned.

If the argument 'fn' is provided, it must be a function. Upon waiting sucessfully, this function is called with one argument, which is the result returned by the child process.

A simple example of using fork() and wait() is as follows:

var pid = scriptus.fork();

if(pid == 0) {
	//I'm in the child process
	return "WAAAAAAH!";
}

//I'm in the parent process
var result;

wait(function(val){result = val;});

//result == "WAAAAAH!";

exit([val])

Quits the current process with the val as the return value, or null if not specified. The value can be any primitive or object.

This basically does the same as "return" in the top-level function but it can be called from anywhere.

sleep(int/string)

The process will lie dormant for the specified amount of time (in hours) or until the date-time provided (format yyyy-MM-dd HH:mm).

It's better to use (listen) so that a script can be woken up by prodding it if necessary.

TODO pipe(arr);

When multiple processes have been created via fork() you may want them to communicate with each other without terminating. This is what pipe() is for. As with UNIX, the method returns two "ids" at index 0 and 1 of the provided object 'arr', respresenting the start and end of the pipe respectively. The ids can then be used in the methods say() and listen() above in the 'person' parameter.

TODO get(url);

This command gets the result of an HTTP GET. In conjunction with eval() it can be used to import other scripts into your script. This can be used to setup libraries, such as parts of dojo for example.

When choosing URLs to import, I recommend that you use URLs to sites that you trust and are highly unlikely to change - for example, a link to a specific revision in a source code repo such as GitHub, Google Code or SourceForge.

At present, only HTTP URLs are supported. If this project takes off, SVN and Git URLs would be good to have too.

TODO post(url)


