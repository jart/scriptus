Scriptus programs are written in JavaScript and are wrapped in a function declaration before being executed - this is why `return` acts as expected. All the usual JavaScript objects such as Date and String are present and correct, as are top-level functions such as eval(). However, none of the methods associated with in-browser JavaScript such as alert() are present. 

The scriptus API is very simple, and can be divided into three main sections, that of interaction, program control, and networking.

#Interaction

##say()

```javascript
say("this is a message");

say("this is a message to Tim", {to:"tim"});

```

This method sends the message provided to the person specified.

The second argument, providing additional parameters, is optional. "tim" above is an example Twitter screen name.

If no person is present, the message will be "said" to the script owner.

##listen()

```javascript
var heard = listen();

var heardFromHarper = listen({to:"harper"});
```

This method listens for messages. It returns the first message it receives as a string. If the "to" option is missing, it listens to the owner.

##Using durations in Scriptus

By default listen() waits for *24 hours* before returning to the program with `null` if no message has been received in that time.

This can be changed using the "timeout" option, which can be a number, a date, or a duration. These options can be used wherever a time or duration is possible in Scriptus:

```javascript
//For Leo, a number of hours (4):

var heardFromLeo = listen({to:"leo", timeout:4}); 

//For Charles, a date: May day 2020!
//format: yyyy-MM-dd HH:mm

var heardFromCharles = listen({to:"charles", timeout:"2020-05-01 07:00"}); 

//For Kahlil, a duration - 4 years and a day:

var heardFromKahlil = listen({to:"kahlil", timeout:"4y, 1d"});
```

In the above snippet, 'y' stands for years and 'd' stands for days. Any number of durations can be combined. The comma is optional, so "4y, 1d" is the same as "4y 1d". Other letters represent other things:

<table>
    <tr>
        <th>Letter</th>
        <th>Time period</th>
    </tr>
    <tr><td>s</td><td>Second</td></tr>
    <tr><td>m</td><td>Minute (lowercase)</td></tr>
    <tr><td>h</td><td>Hour</td></tr>
    <tr><td>d</td><td>Day (lowercase)</td></tr>
    <tr><td>w</td><td>Week</td></tr>
    <tr><td>M</td><td>Month (uppercase)</td></tr>
    <tr><td>q</td><td>Quarter (3 months)</td></tr>
    <tr><td>y</td><td>Year</td></tr>
    <tr><td>D</td><td>Decade (uppercase)</td></tr>
    <tr><td>C</td><td>Century</td></tr>
</table>

##ask()

```javascript
var deepThoughts = ask("Wherefore?");

var probablyNotDeep = ask("Dude! What does mine say?", {to:"chester"});

var unlikely = ask("A short novel please", {to:"victor", timeout:"17y"});
```

This method sends a message to a person in the style of `say`, and then awaits responses in the style of `listen`. The result, as with `listen`, is returned as a string, or null if the method times out.

#Program control

The program control methods are basically taken from UNIX and are as follows:

##fork()

```javascript
var pid = fork();

if(pid == 0) {
	//this is the child;
	return "WAAAAAAA";
}
```

The fork() method is used to split the current process into two separate processes. Each process then continues execution. One of the processes is the 'child' and one of the processes is the 'parent'.

The child process will have `0` returned as the process identifier (or 'pid'), and the parent process will have returned the pid of the child.

The pid is not a number like in UNIX, but a UUID, represented as a string.

Both processes will continue to execute as normal. The parent process, knowing the identity of the child, can use the following function:

##wait()

```javascript
var pid = fork();

if(pid == 0) {
	//this is the child;
	return "WAAAAAAA";
}

var fromChild;

var receiver = function(result) {fromChild = result;}

var waitedFor = wait(receiver);

//fromChild == "WAAAAAA";
//waitedFor == pid;
```

What's happened here is a bit more tricky. The fork() call was used to split the process in two as above. Then an empty variable was declared for the results of the child process. Then another variable, "receiver" was declared as a *function* that takes the result of the child process.

The wait function itself returns the PID of the process it waited for. If you have forked() multiple processes and want to wait for one specific process, then you can select the process using it's pid:

```javascript
var waitedFor = wait(receiver, pid);
```

If no pid is given, then the last process forked is waited for.

If no process was waited for, either because a bad pid was provided or no pid was provided and the process has not yet forked, -1 is returned.

##exit()
```javascript
exit("persued by a bear");
```

Quits the current process with the supplied argument as the return value, or null if not specified. The value can be any primitive or object.

This basically does the same as "return" in the top-level function but it can be called from anywhere in the process.

##sleep()
```javascript
sleep("8h");
```

The process will lie dormant for the specified amount of time, which as elsewhere may be a number of hours, a date, or a duration.

In many places it may be better to use (listen) so that a script can be woken up by prodding it if necessary.

##TODO pipe(obj);

When multiple processes have been created via fork() you may want them to communicate with each other without terminating. This is what pipe() is for. In UNIX, the method returns two "ids" at index 0 and 1 of the provided array 'arr', respresenting the start and end of the pipe respectively. In Scriptus, the supplied object will have the properties "in" and "out" set an ID. The IDs can then be used in the methods say(out) and listen(in) above in the 'person' parameter.

#Networking

The idea behind these methods is to allow the programming environment to interact with other services on the Internet. Right now the methods are very simple but adding more stuff in future is on the cards.

##get()
```javascript
var response = get("http://www.google.com/robots.txt");
var sslResponse = get("https://encrypted.google.com/robots.txt");
```

This command gets the result of an HTTP GET. In conjunction with eval() it can be used to import other scripts into your script. This can be used to setup libraries, such as parts of dojo for example.

When choosing URLs to import, I recommend that you use URLs to sites that you trust and are highly unlikely to change - for example, a link to a specific revision in a source code repo such as GitHub, Google Code or SourceForge.

At present, only HTTP and HTTPS URLs are supported. If this project takes off, SVN and Git URLs would be good to have too.

The HTTP GET is executed with a timeout of 60 seconds.

##TODO post(url)

##Note about eval()

Although eval() works, the scriptus API methods as listed above cannot be executed within them. For example, this code wouldn't work:

```javascript
eval("scriptus.say('Where ARE my socks?')")
```

Whereas this code should:

```javascript
eval("function(){scriptus.say('Where are MY socks?');}")();
```

The reason for this is [documented here](http://mxr.mozilla.org/mozilla/source/js/rhino/testsrc/org/mozilla/javascript/tests/ContinuationsApiTest.java#203).
