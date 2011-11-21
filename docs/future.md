#Future directions for Scriptus

In the short-term I'm intending to continue adding examples and minor functionality (pipe, read, write etc.). However there are several major functional areas, with each of which there are major concerns or possibilities. I will enumerate these below.

##Transports

Right now the only serious transport is Twitter, but there are many obvious issues, such as the 140 character limit. I intend to resist adding a "web transport" for as long as possible, because the web lacks a 'push anywhere' component, and that would essentially convert Scriptus into a CMS, which it is not. However, other transports such as email could be a good idea. The API method would then have an additional option e.g.

```javascript
var resp = ask("Sit right down and write me a letter...", {to:"fred@example.com", transport:"email"});
```

This would have lots of follow-on implications, for example how do you manage a users identity when a user may have multiple transports.

Another potential "transport" would be to have a native clients for iOS and Android which would use their respective "push" implementations. There is a big attraction for making an Android client: once done, the Android client could effectively proxy Scriptus messages to and from other non-smart phones by SMS, provided the Android owner consents.

Because any social network with a good API can act as a transport, it might be interesting to add a stub application for Facebook too.

##Internals

A major feature of Scriptus is that Scriptus programs are meant to last for weeks, months, years or more. If this is so, we must think ahead to the upgrade & maintenance cycles of future releases. Scriptus must be backwards compatible with all its previous versions, otherwise the whole idea of having an environment for running long-term processes is void. Right now this is definitely not the case.

The chief impact of this is that the datastore should most definitely have a versioning system and that we foresee the capacity to migrate from old versions of stored data to new versions. Running long-term processes cannot be done if the program cannot be upgraded or have bugfixes applied to it while they run.

In addition to that, since users may well want to change datastores for their install of Scriptus, e.g. from filesystem to S3 and back again, an import/export functionality is also vitally important.

These two goals are complementary in many ways, and they may not be sexy but they are definitely important.

##Developing Scriptus programs

I've now written two or three short program using Scriptus and there are couple of things that are immediately clear. The first is that a program that is meant to run for years is difficult to test properly, because normally one would have to wait for years to see if the program terminates properly. How's that for a development cycle!

The solution to this is a time-machine. Specifically, to modify Rhino so that a delta can be applied to the apparent system time inside of running scripts, and to centralise all time-related functions in Scriptus into a single Clock object/service which we can wind back and forth as necessary to achieve the desired code paths in our tests.

An example of how this might work can be found in [this example test for `reminder.js`](https://raw.github.com/ianso/scriptus/master/scripts/test.reminder.js).

##The View Source Principle (VSP)

Scriptus may not be a "web framework" but it is intrinsically *of* the web. I consider VSP to be vital to the health of any system, and at present I find it annoying that users of Scriptus cannot examine the source of a program as it runs, either when debugging it or when interacting with it via Twitter.

I haven't investigated this fully, but adding VSP to Scriptus would probably involve attaching a custom debugger to the Rhino process when a continuation is thrown, and making some kind of presentable snapshot of the process that could be presented in an online 'debugging' UI.

##Quick starting

Secondly, an online *interactive* debugger would be not only a better way of developing scripts, but would allow potential users to immediately see how Scriptus works, e.g. by letting them run through small example scripts in their browsers. However, an in-browser implementation of the Scriptus API is hobbled chiefly by the lack of `fork()`, which can't be imitated by web workers since `fork` directly rests upon the ability to copy the working memory of a process. [The Narcissus project](https://github.com/mozilla/narcissus) might help here.

##API

The API is about interaction with users, networking, process control & IPC. For interaction, it would be quite  easy to add methods such as `choose("option 1", "option 2")`, but more interestingly, location-based calls such as `where()` or `at()` could enable a new class of applications. So too could multimedia calls such as `look()` or `listen()` for using pictures, audio or videos as input.

For networking, `get` should be complemented with `post` and maybe a method for PUT, DELETE etc., and these should allow access to headers etc. for them to become completely capable of using all of HTTPs functionality.

In terms of process control & IPC, `pipe` and the other ideas were nicked from UNIX and the C API. If we continue to look to the same sources for inspiration, some interesting directions suggest themselves.

Scriptus today has two major failings. Firstly there is no good way of persisting data other than as variables in the program - i.e. there is no filesystem or equivalent. Secondly, there is no integration possible with external programs, and no place to store libraries of code except online for inclusion via `eval(get())`.

Adding a filesystem-type set of APIs to Scriptus could solve these problems, especially if one could mount external resources, or bind external programs to file descriptors, following in some style the "everything is a file" approach of UNIX. In addition, a procfs would allow for process administration. 

The analogy goes further though - for example, one of the problems with long-running processes is in-place upgrading of software. This could be addressed by using a "small pieces loosely joined" approach that could also allow the implementation of a 'command prompt' for running social programs, for example:

```bash
$ poll "Is the private space industry sustainable long-term?" yes no
$ elect 2 "rocketry experts" | debate "Have we reached the limits of rocket design?"
```

At this point Scriptus essentially becomes a 'social operating system'. I find the potential intriguing...

