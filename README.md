Scriptus is a way of programming interactions between people. What's new is that these interactions can be complicated and span days, weeks, months or years: elections, chess tournaments and games are all easy to create.

It's easy to get started. Scriptus programs interact with people via Twitter. (If this idea takes off, then other potential clients could include iPhone, Android, and SMS.)

Scriptus creates a JavaScript environment for programming interactions between people and programs on Twitter. Scripts are run inside a simple UNIX-like process model.

On Twitter, people are asked questions using @mentions and questions are tracked using #hashtags. Responses are sent back to Scriptus in the same way. Anything after "//" will be ignored. For example:

	@ianso #ef3hED electing HOW many presidents-for-life?!
	@robotoscriptu #ef3hED 4 //but I can depose them whenever I want!

A Scriptus server can be stopped at any time. Programs will continue where they left off when it is started again. Scriptus programs are designed to run for days, months or years, waiting for user input or waking as necessary. 

(Scriptus uses Rhino and serialisable continuations to make long-term persistence of program state possible, and can uses either an in-memory, filesystem or AWS-backed datastore.)

For more, see [the API documentation](https://github.com/ianso/scriptus/tree/master/docs).

You need JDK 6 or more recent, and [Maven 2.2+](http://maven.apache.org/) to build Scriptus. Once you've checked out the project, you need one command:

 * `mvn jetty:run` will immediately run Scriptus on port 8080, at [http//127.0.0.1:8080/scriptus/](http//127.0.0.1:8080/scriptus/).
 
Alternately, `mvn clean install` will give you a WAR file that can be deployed in any standard servlet container.

The license is GPL v2 or above.

