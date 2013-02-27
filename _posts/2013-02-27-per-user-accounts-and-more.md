---
layout: post
title: Per-user Twitter accounts!
---

The trunk version of Scriptus now allows each user to link their own Twitter account to their login. Scriptus is no longer a 'single-user' install. This will make it easier for me to throw up a demo server for people to try out. 

In addition to this, there's a new quick-reference  page in the app itself, and the docs are now all up-to-date. The 'in-memory' DB uses H2, like the embedded DB, and there's the option of using a PostgreSQL instance as well if you want.

Next, I'm going to add a web-based transport to replace the command-line transport, so that users can test their scripts without filling up their Twitter feeds, or run private scripts that don't concern everyone else. After that I'm going to throw up a demo server.

The new version of Scriptus is also available to download as an executable war file at a new location: [Scriptus 0.7.0](http://iansopublic.s3.amazonaws.com/scriptus.war).
