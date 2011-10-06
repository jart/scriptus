#Scriptus example programs

There are a variety of example programs for Scriptus in `src/scripts`, and some of them are even useful. Below, some of them are listed along with a brief discussion in each case of how they use Scriptus.

##`addTwoNumbers.js`

This example is a simple illustration of `fork()` and asks two people, `adam` and `bart` for a number each and relays the addition to a third person, `carole`.

Worth pointing out is two things: firstly that the final result is both numbers concatenated (20+10=2010), since the results of each ask() is a JavaScript `String`, and secondly that the full version of each function call is used. This is like using `window.alert()` and `window.prompt()` in client-side JavaScript on a web browser.

##`knockknock.js`

I like this one not just because it's the first program for Scriptus that I didn't write, but also because it has a sense of humour.

At the risk of ruining the joke, I think it works on multiple levels: first of all it's actually telling a joke to somebody in a traditional call-and-response format. But funnier than that, I think is that the "caller" in this case is a soulless automaton who will bug the "responder" indefinitely until the *properly spelt and grammatically correct* response is received *promptly* for each of the two calls.

Of course, running this program on someone who's not in on the joke would probably not be funny to them.

