This document explains in detail how to start, set up and run the Scriptus application server, along with a suggested development lifecycle for developing scripts.

#Running Scriptus

Scriptus requires JDK6 to run. You can download & setup the OpenJDK, or obtain the latest proprietary version either from Oracle or IBM. (The IBM JDK hasn't been tested).

Next, download and install Maven 2 from the Apache website. Follow the instructions here to configure Maven.

Once Maven is configured,  either download Scriptus as a zip or tar file, or make a checkout using Git.

You can use Maven to run Scriptus, or compile it into a WAR file that should be deployable in any standard Servlet container such as Tomcat.

To run Scriptus, use the following command in the Scriptus directory:

`mvn jetty:run`

To compile Scriptus, use the following command:

`mvn clean install`

You will then find a WAR file under the "target" directory.

By default Scriptus writes log files to the "logs" directory under the Scriptus directory. This location is initially defined using a relative path. The location can be configured in `src/main/resources/log4j.properties`.

#Configuring Scriptus

When you are running Scriptus, the first thing you will probably want to do is go to the settings page and configure Scriptus. If you've run Scriptus using maven, this will be at http://127.0.0.1:8080/settings. This page provides a very simple page for editing the Scriptus configuration file.

This file is by default stored in `~/.scriptus/config.properties`. If you want to keep it somewhere else, then you can provide a `scriptus.config` system property to specify the location:

`mvn jetty:run -Dscriptus.config=/etc/scriptus.conf`

#Programming with Scriptus

When a Scriptus program is run, it can be supplied with two parameters:

 - owner The screen name of the script owner. 
 
This is the person with whom ask, say & listen will interact with by default, and the person to whom the result of the root process will be said.

 - args The program arguments.
 
 This is a variable length string that can be anything you want. For example, in a script that runs a game tournament, the arguments could be "-game chess -players 2 -competitors 20".
 
Both variables are usable inside the Scriptus program as "owner" and "args" respectively.

##A note on JavaScript

Scriptus programs are written in JavaScript. If you're already familiar with programming, but haven't written much JavaScript, [Mozilla's "a re-introduction to JavaScript"](https://developer.mozilla.org/en/A_re-introduction_to_JavaScript) is a good place to start.

If you're completely new to programming then [Eloquent JavaScript](http://eloquentJavaScript.net/) is a highly recommended book that is also available online for free. Alternately you can [learn JavaScript very easily using interactive lessons at Codecademy.com](http://www.codecademy.com/). Finally, [this Stack Overflow question on learning JavaScript](http://stackoverflow.com/questions/11246/best-resources-to-learn-JavaScript) lists many other resources.

The way to return a result from a function in JavaScript is to use the `return` keyword, however in one of the odd quirks that JavaScript is famous for, `return` may not be used at the top-level of a JavaScript program, and instead the last variable to be evaluated is taken as the return value of the program. As a convenience to the programmer, who can always use `return` where expected, Scriptus programs are wrapped in a function before being executed as follows:

```JavaScript
function ___scriptus___ () {
	//your code goes here
}
```



