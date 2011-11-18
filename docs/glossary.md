#Glossary

Every project inevitably creates its own terminology, and this page is an attempt to collate it in one location and explain it.

##transport, previously "interaction medium"

A means through which Scriptus communicates with its users.

##program / script

A stored program, written in JavaScript, against the Scriptus API.

##running program / process

A program running in Scriptus

##pid

Each process is identified by a Process IDentifier, which right now is a v4 UUID, although it may become a URN (by prefixing "pid:") in future.

##script owner

When running a script, you can can have its owner. By default, ask(), say() and listen() revert to this user.

##datastore, previously "DAO"

This is where Scriptus stores all data. There are several different types of datastore, as discussed in the internal architecture page. 

##Continuation, serializing

See [the Wikipedia](http://en.wikipedia.org/wiki/Continuation) [definitions](http://en.wikipedia.org/wiki/Serialization) for good explanations.
