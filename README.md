Scriptus is a way of programming interactions between people. What's new is that these interactions can be complicated and span days, weeks, months or years: elections, chess tournaments and games are all easy to create.

A simple example:

```javascript
var task = ask("What to do?");

sleep("1d");//1 day

var response;

do {

  response = ask("did you do "+task+" yet?", {timeout:"1d"});

} while( response != "Stop it!" | "yes");
```

It's easy to get started. Scriptus programs interact with people via Twitter and has a simple UNIX-like process model.

On Twitter, people are asked questions using @mentions and questions are tracked using #hashtags. Responses are sent back to Scriptus in the same way. Anything after "//" will be ignored. For example:

	@ianso #ef3hED electing HOW many presidents-for-life?!
	@robotoscriptu #ef3hED 4 //but I can depose them whenever I want!

A Scriptus server can be stopped at any time. Programs will continue where they left off when it is started again.

You will need JDK 6 or more recent. [Download Scriptus here](http://downloads.github.com/ianso/scriptus/scriptus.war) and then launch it using this command

```
java -jar scriptus.war
```

Scriptus will immediately start at [http://127.0.0.1:8080/](http://127.0.0.1:8080/). You can change the port using the `-p` option.

[The Scriptus minisite](http://ianso.github.com/scriptus/) includes a blog + RSS feed for notable updates. [The user guide documents how to run Scriptus](https://github.com/ianso/scriptus/blob/master/docs/userguide.md), and [the API documentation shows how to use it](https://github.com/ianso/scriptus/blob/master/docs/api.md). More is available at [the documentation index](https://github.com/ianso/scriptus/blob/master/docs/index.md).

The license is GPL v2 or above.

