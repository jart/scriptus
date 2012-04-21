---
layout: post
title: Multiple replies possible with new say()
---

The `say()` function now returns a "message ID" that can be passed to `listen()`. This allows program writers to collect multiple responses to the same tweet, for example.

To make this functionality more useful, and to ensure the API remains harmonious, additional breaking changes have been made. Previously, if no `to` option was specified, then the script owner was used as a default parameter. Now, in the absence of `to`, no-one is addressed specifically.

This means that `ask()` and `listen()` without a `to` may return a response from anybody; and `say()` without `to` addresses nobody in particular. In Twitter for example, there would be no `@user` before the tweet.

Because a messages author can now be unknown at the point in the program where it is received, the message has a new attribute, `from`, which is the name of the user who wrote the message. For Twitter this will be the screen name of the user.

As usual, this is all reflected in the updated documentation.

This version is also incompatible with previous versions at the storage level, so I guess that makes it Scriptus 0.3.0. Please let me know if breaking backwards compatibility is annoying you if you use Scriptus.

##In other news

The methods by which tweets are passed to scripts as a result of various API calls is not entirely consistent. One tweet can only be processed by one script, to start with, and a listen `to` a user without a `messageId` may return a tweet made before the call to `listen()`. I think. So, this logic needs fixing.

I've been greatly encouraged by hearing from some people at Politecnico di Milano who've done several interesting things with Scriptus, and as a result have inspired the direction of my recent work. So thanks to Nela, Emil & Andres for their kind words and feedback.

