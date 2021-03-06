This document explains in nitty-gritty detail how to start, set up and run the Scriptus application server, along with a suggested development lifecycle for developing Scriptus programs.

#Running Scriptus

Scriptus requires JDK 6 to run. You can download & setup the OpenJDK, or obtain the latest proprietary version either from Oracle or IBM. The IBM JDK should work but hasn't been tested.

Next, [download Scriptus here](http://iansopublic.s3.amazonaws.com/scriptus.war). Scriptus can be executed directly or deployed in any standard Servlet container such as Tomcat.

To run Scriptus, use the following command:

`java -jar scriptus.war`

By default Scriptus writes log files to "logs" under the Scriptus directory. This location is initially defined using a relative path. The location can be configured in `src/main/resources/log4j.properties`. There are two options at the command-line:

 *   `-p` or `--port` allows you to specify which port Scriptus should serve on. 
 *   `-c` or `--config` allows you to specify a config file, either as a URL, classpath or relative path.
 
#Scriptus administration

Scriptus has web interface that users can use to play with programs. By default this tool is available at [http://127.0.0.1:8080/](http://127.0.0.1:8080/) if you run Scriptus with no command-line arguments. The interface asks users to identify themselves using OpenID, as a simple way of allowing multiple users to store and run their programs on the same server, and connect using their own Twitter accounts.

The OpenID identification can be disabled by putting `disableOpenID=true` in the config file.

#Configuring Scriptus

When you are running Scriptus, the first thing you will probably want to do is run some sample programs. Once you've connected to Twitter, you can find these under the 'scripts' menu. I'm working on improving this section.

The main Scriptus configuration file is by default stored in `~/.scriptus/config.properties`. If you want to keep it somewhere else, then you can provide a `-c` option to specify the location:

`java -jar scriptus.war -c /etc/scriptus.conf`

The format is a standard key-value pair separated by an equals sign.

The two major configurations for Scriptus are choosing where to store your data (´datastore´), and how to interact with people (`transport`).

##Choosing a datastore

The datastore is responsible for persisting saved programs and running program state. There are three implementations and they all have their uses.

 *   `Memory`: **In-memory** storage keeps everything in an in-memory H2 database. When Scriptus is shut down or restarted everything vanishes.
This storage method is used for running test cases in the Scriptus source code, but can also be useful if you're debugging scripts offline that you've written & saved elsewhere. To

 *   `Embedded`: **Embedded DB** storage saves everything in an H2 database, stored under the folder "scriptusdb" under the starting location. It's not currently possible to specify another location.

 *   `PostgreSQL`: **PostgreSQL** storage saves everything in an PostgreSQL database. To create the database you will need to run the SQL [scriptus.postgresql.sql](https://github.com/ianso/scriptus/blob/master/src/main/resources/sql/scriptus.postgresql.sql), and then add the parameters `dbHost`, `dbPort`, `dbName`, `dbUsername`, `dbPassword`, and optionally `dbParameters` to the config file containing the correct information.
 
##Choosing a transport

The transport is the means via which Scriptus programs interact with their users. There are three transports and they all have their uses.

 *   `Dummy` just responds with a predetermined response to every `ask()` and not at all to a `listen()`.
It can be useful for testing simple scripts. To help with this goal, it checks the messages against a series of regular expressions and can respond with a replacement expression if one matches, otherwise a default response is returned. The regular expressions are in the file `src/main/resources/spring/scriptus.xml`.

 *   `CommandLine` sends all `ask()`s and `listen()`s to a simple command-line interface, where the local user can respond. This will shortly be replaced with an in-browser, per-user equivalent.
 
 *   `Twitter` is used to interact with users on Twitter. For this to work you need to link Scriptus to Twitter on the 'connect' page.

##A note on Twitter

On Twitter, people are asked questions using @mentions. Responses are sent back to Scriptus by replying to the tweets sent by Scriptus. Anything after "//" will be ignored. For example:

1. @robotoscriptu tweets `@ianso electing HOW many presidents-for-life?!`
2. @ianso replied with `@robotoscriptu 4 //but I can depose them whenever I want!`

In the above exchange, Twitter marks the second tweet as being a reply to the first if the "reply" function is used, and this is how Scriptus matches the answer from `ianso` to the question asked by `robotoscriptu`. `ianso` has replied "4".

At present, Twitter prevents users from sending identical status updates within a certain time period. This can sometimes present a problem for Scriptus if a running program needs the same input twice. In this case, adding something random after a "//" will make the status update unique enough for Twitter.

#Programming with Scriptus

Scriptus programs are stored on a per-user basis and saved using the admin tool. Scriptus programs interact with the outside world via [the Scriptus API](https://github.com/ianso/scriptus/tree/master/docs/api.md).

When a Scriptus program is run, it can be supplied with two parameters:

`owner` - The screen name of the script owner. 
 
This parameter will be removed soon.

`args` - The program arguments.
 
This is a variable length string that can be anything you want. For example, in a script that runs a game tournament, the arguments could be `-game chess -players 2 -competitors 20`.
 
Both variables are usable inside the Scriptus program as `owner` and `args` respectively.

#Developing and testing your program

In writing the example programs for Scriptus there are one or two tips and tricks that I've found handy. Debugging is generally not fun, but it would be kind of a drag to have a program run for a month and then crap out with an error, when it should be running for another year. So without further ado, some suggestions:

 *  **Write your programs in a text editor and use SCM.** Scriptus doesn't do version control and only offers primitive syntax highlighting in its built-in editor, ACE. So treat yourself to a decent text editor when writing Scriptus programs, and upload the finished version when you're ready to test.

 *  **Run programs with the command-line transport and in-memory storage first.** That way you can test out your program in private, without making a mess in your twitter feed or on your hard-drive.
 
 *  **Use `listen()` instead of `sleep()`.** That way you can tell the running program "OK stop waiting and carry on".

 * **For durations, use constants. For dates, use arithmetic and isolate it in one place.** You can easily change these constants to smaller durations during testing and longer ones when running the program for real.
 
 * **Processes are (relatively) cheap, so use them liberally if it simplifies your code.** In a normal program, too many processes leads to CPU contention and resource exhaustion. In Scriptus, a sleeping or waiting process uses no memory or CPU, only storage space, generally kilobytes per process. With this in mind, I think it's fine if you want to launch 100 processes to run a questionnaire for 100 different users, for example.
 
 * **Use `log()` statements.** Without them, since there is not currently a debugger, it's very difficult to tell what your program is doing. See [this Stack Overflow question on logging best practices](http://stackoverflow.com/questions/270651/what-guidelines-do-you-adhere-to-for-writing-good-logging-statements).

That's all for now. If you have more, please send them in, so we can all benefit from the wisdom of collective experience :-)

##A note on JavaScript

Scriptus programs are written in JavaScript. If you're already familiar with programming, but haven't written much JavaScript, [Mozilla's "a re-introduction to JavaScript"](https://developer.mozilla.org/en/A_re-introduction_to_JavaScript) is a good place to start.

If you're completely new to programming then [Eloquent JavaScript](http://eloquentJavaScript.net/) is a highly recommended book that is also available online for free. Alternately you can [learn JavaScript very easily using interactive lessons at Codecademy.com](http://www.codecademy.com/). Finally, [this Stack Overflow question on learning JavaScript](http://stackoverflow.com/questions/11246/best-resources-to-learn-JavaScript) lists many other resources.

The way to return a result from a function in JavaScript is to use the `return` keyword, however in one of the odd quirks that JavaScript is famous for, `return` may not be used at the top-level of a JavaScript program, and instead the last variable to be evaluated is taken as the return value of the program. As a convenience to the programmer, who can always use `return` where expected, Scriptus programs are wrapped in a function before being executed as follows:

```JavaScript
function ___scriptus___ () {
	//your code will go here
}
```



