
/*
 * Thanks to Sander De Dycker for this one
 *  - please use it responsibly :-)
 */

var resp = null;

do {
	resp = ask("Knock knock", {
		timeout : "1m"
	});
} while (resp == null || resp != "Who's there?");

do {
	resp1 = ask("Madame", {
		timeout : "1m"
	});
} while (resp1 == null || resp1 != "Madame who?");

say("Madame foot is stuck in the doorway!");

