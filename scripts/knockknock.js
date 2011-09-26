
/*
 * Thanks to Sander De Dycker for this one
 *  - please use it responsibly :-)
 */

var resp = null;

do {
	do {
		resp = ask("Knock knock", {timeout:"1m"});
	} while (resp == null || resp != "Who's there?");

	resp = ask("Madame", {timeout:"1m"});
	
} while (resp == null || resp != "Madame who?");

say("Madame foot is stuck in the doorway!");

