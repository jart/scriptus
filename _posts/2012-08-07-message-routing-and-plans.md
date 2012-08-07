---
layout: post
title: New message routing and what&#8217;s next - heads up for a new datastore!
---

The latest series of commits changes nothing on the 'user' end of Scriptus but has affected quite a lot of the internals.

The biggest change is that the command line transport is now more of an interactive console. You now have far more flexibility in testing scripts locally, because the command-line tool can do anything that the Twitter transport can do.

Next, the internal interface between the transports in Scriptus and everything else is now a lot cleaner. This should make it easier for anyone wanting to implement a new transport.

And finally, the message 'routing' has changed. Previously, if multiple processes were listening to one user, or to any user, then the last process to call `listen()` would be the one to receive any messages. Now, all processes receive a copy of the message. This is quite a change from the previous situation, however it allows for a greater range of functionality in scripts - for example, scripts that 'listen' to a users tweet stream and collate information are now possible.

Given that the datastore has also been modified, this is now Scriptus 0.4.0, and will shortly be available to download at the normal location. 
	 
##Elsewhere

I'm happy to say that Andres from Politecnico di Milano has been working on [a Facebook transport for Scriptus - check it out on GitHub here](https://github.com/gantzer89/scriptus/).

##The future!

Having worked on Scriptus for a while now, and helped other people doing likewise, a couple of the more questionable design choices have become apparent. Chief among these is the decision to have two main datastore implementations - one local and an AWS S3/SDB hodge-podge.

This is bad thing because in exchange for very simple implementations we lose relations, proper indexing, easy querying, easy inspection, and a lot of other stuff. The AWS thing was nice to get my hands dirty with the tech, but has turned out to be pretty much useless. Contrariwise, the waaaaay-too-premature justification of 'scalability' is easily met since a Scriptus DB schema would be horizontally partitionable with no major problems.

Therefore I'm planning to scrap them both and replace them with a traditional RDBMS. I'll use an embedded database that needs no setup by default, and provide config options to use "real" DBs like MySQL or PostgreSQL.

If anyone reading this has a Scriptus install that's running long-term processes, and you would like to be able to upgrade seamlessly to new versions, [please let me know via email](mailto:ian.sollars@gmail.com), because otherwise I won't write a migration tool.

