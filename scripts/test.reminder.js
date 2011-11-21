/*
This script doesn't work, but would execute a call-by-call test on reminder.js against an API including the calls expect(call), respond([result]), result = continue([substArgs]), setClock(time).

Of course a proper test-case should just observe inputs and outputs, whereas this test is the mirror-image of the script itself.

It hasn't been tested obviously, because the below API doesn't exist yet.

TODOS
- Context.setSystemTimeDelta(long millis) {
  - add from NativeDate, boolean flag for optimizing if necessary
}
 - centralise clock in system + scheduler

*/
expect(get); continue();
expect(ask); respond("FRED");
expect(ask); respond("THE EVENT");
expect(ask); respond("3 Dec 2015");

for(1...5) {
	expect(fork);
	var result = continue();
	if(result == 0) {//child
		expect(sleep);
		setClock("5 Dec 2015");
		expect(say);
		return;
	}
}
expect(say);
expect(ask);
respond("cancel");
for(1..5) {
	expect(kill); continue();
}

