/*
I'm creating this script because I find myself relearning some of the 
same lessons in life over and over again. This script listens to the 
me and then repeats what is said at the period defined in the message,
until I tell it to stop. For example if I say:

"3 M: To live fully, you regularly need time to be still and listen. Find quiet spaces sometimes!"

This will repeat every three months. The duration can also be a number 
of hours (inadvisable!) or just a date if the message should repeat once
only, so this script can also act as a reminders service too.

The script has two extra commands, "stop" and "dump" that stops all reminders
and dumps all the messages to the log file, respectively.

*/

var newpost;

var children = [];
var msgs = [];

while(newpost = listen({timeout:"2d", to:owner})) {

	if(newpost == null) {
		continue;
	}
	
	if(newpost == "stop") {
		for(var i = 0; i != children.length; i++) {
			kill(children[i]);
		}
		exit("stopping main script");
	} else if( newpost == "dump") {
		log(msgs.join(", "));
		continue;
	}

	if(newpost.indexOf(":") == -1) {
		continue;
	}

	var msg = newpost.substring(newpost.indexOf(":")+1);
	var duration = newpost.substring(0, newpost.indexOf(":"));
	
	if( ! isValidTimeout(duration)) {
		say("sorry, didn't understand "+duration, {to:newpost.from});
		continue;
	}

	var pid = fork();
	
	if(pid != null) {
		children.push(pid);
		msgs.push(newpost);
		continue;
	}
	
	var response;
	
	if(isDate(duration)) {
		sleep(duration);
		say(msg);
		exit("leaving, message said on date");
	}
	
	var mId = say(msg);
	
	do {
		response = listen({inReplyTo:mId, timeout:duration});
	} while( ! isNegative(response));
	
	exit("stopping reminder due to stop order" +msg);

}

/*
 * utility functions, should move these into a dedicated file
 * and use eval(load(file)) or something
 */

function isValidTimeout(s) {

	if( ! s) return false;

	try{
		new Number(s);
		return true;
	} catch(e) {}

	if(s.match(/[0-9]*\s*[smhdwMqyDC]{1}/)) {
		return true;
	}
	
	return isDate(s);
}

var SCRIPTUS_DATE_FORMAT = /([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2})/;

function isDate(s) {

	if( ! s) return false;

	try{
		new Date(s);
		return true;
	} catch(e) {}

	if(s.match(SCRIPTUS_DATE_FORMAT)) {
		return true;
	}
	
	return false;

}

function getDate(s) {

	try{
		return new Date(s);
	} catch(e) {}

	if(s.match(SCRIPTUS_DATE_FORMAT)) {
		var bits = SCRIPTUS_DATE_FORMAT.exec(s);
		return new Date(bits[1], Number(bits[2])+1, Number(bits[3])+1, bits[4], bits[5], bits[6]);
	}
	
}

function isNegative(s) {
	if( ! s) return false;
	return new String(s).toLowerCase().match(/[nsx]{1}|stop|no/);
}
