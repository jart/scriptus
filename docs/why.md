
#Motivation

My reasons for writing Scriptus are many and varied. At a pragmatic and selfish level, I have never found an organisational tool that 'fits' me and does exactly what I want, and Scriptus will make it very easy to write the various 'helpers' that I think I need in order to live a better life.

This might seem extremely geeky, and it is, but it's also no different than using a Filofax or other diary to keep oneself oriented correctly. Computers and the Internet have done away with these tools for the most part, but they haven't, in my opinion, yet offered a reasonable replacement.
<!--
In a more social context, I think that although IT is transforming our social substrates, it hasn't yet built many social structures. For me the difference is between a well-maintained park, and a football club that plays in that park. The first is the 'infrastructure' that allows many different social interactions, and the second is people meeting, acting together and forming a community of sorts.

"Facebook and twitter are good at the former but not the latter"
 - why this isn't true - communities form on Facebook and Twitter all the time, and they're ad-hoc, spontaneous and they form without the need for having their conventions programmed in.
-->

In secular terms, I believe we have a responsibility to help improve the world we live in. I also believe that all technology is ultimately value-neutral, and that morality only comes into play when people interact with it.

Technology does have affordances, however. For example, a gun is easy to fire, but the right thing to do with a gun in many circumstances (if not all) is to leave it alone. Another example: a Facebook app that lets you rank your friends from best to worst has social affordances that make it easy to create jealousy, pettiness, or status-seeking behaviour.

Can technology have 'positive' affordances? At one level, yes - for example, software that helps coordinate aid relief, or a payments processor that makes charitable donations easier to bundle with purchases.

At another level, it's better to allow people space to express their free will. Then people will do more, and show more imagination in both good and bad ways than you ever expected.

With this in mind, how can technology make the world a better place? Not through coercion, but maybe through affordances that make it easier for people to interact with each other however they choose.

##Language: JavaScript

With this in mind, my choice of language is one that is readily available & teachable to anyone with a web browser - JavaScript. The type of program is one that interacts directly with people, like an IRC bot. And the time-frame is not the typical immediacy of a computer interaction that is measured in terms of 'length of session in front of a screen', but over the more human durations days, weeks, or months.

This means that program state must be captured and stored, since such long interactions cannot persist in memory. The best way to do this in modern languages is by serialising continuations. JavaScript has just one implementation, Rhino, that permits serialisable continuations.

##Interaction medium: Twitter

Conversations must involve both talking and listening, or sending and receiving, whereas the web is typically request-response and requires the users presence in front of a device.

I considered four major, easily available, commonly used "push" technologies that enable two-way conversations and notifications: SMS, iPhone push, Android C2DM, and Twitter. Receiving SMS in online application requires paying for custom telephone numbers or integrating with proprietary services. iPhone push and Android C2DM are only available on one platform each. Twitter on the other hand, already has clients across multiple platforms, takes care of the push problem, and is relatively open, at the moment.


