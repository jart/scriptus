//import date library, see http://www.datejs.com/
eval(get("https://raw.github.com/ianso/scriptus/master/scripts/lib/date-en-US.js"));

/*
 * Set up initial variables: who to remind, what to remind about, and what date it is.
 * 
 * These could equally be provided as "args" parameters. Either way they should be 
 * validated & checked for null, which I'm not going to do here in the interests of
 * brevity.
 */
var who = ask("Who should I send reminders to?"); 

if(who.toLowerCase() == "me") who = owner;

var event = ask("What should I remind "+who+" about?");

var evtDate = new Date(ask("When is "+event+"? e.g. '5 Dec 2015'"));

/*
 * Reminders are said:
 * 1 day before, 1 week before, 1 month before, 6 months before, and each year before
 */

var now = new Date();

var remDates = [
	{msg:"1 day",    time: (1).days().before(evtDate)},
	{msg:"1 week",   time: (7).days().before(evtDate)},
	{msg:"1 month",  time: (1).months().before(evtDate)},
	{msg:"6 months", time: (6).months().before(evtDate)}
];

var yearsBeforeEvent = (12).months().before(evtDate);

var numYearsBefore = 1;

//we do an arbitrary number of years
while(yearsBeforeEvent.isAfter(now)) { 

	var message = (numYearsBefore+" year"+(numYearsBefore == 1 ? "" : "s"));
	
	remDates.push({msg:message, time:yearsBeforeEvent});
	
	numYearsBefore++;
	
	yearsBeforeEvent = (numYearsBefore * 12).months().before(evtDate);
	
	log("numYearsBefore="+numYearsBefore+", added message for "+yearsBeforeEvent);
	
}

/*
 * We fork() once for each reminder and then sleep each thread for the given amount of time.
 * There are definitely more efficient ways of doing this in terms of process size etc.
 * 
 * But this way we can illustrate kill().
 */
var children = [];

for(var i = 0; i != remDates.length; i++) {

	var reminder = remDates[i];

	if(reminder.time.isBefore(now)) {
		break;
	}

	var p = fork();
	
	if(p == 0) {
		log("sleeping until "+reminder.time);
		sleep(reminder.time);
		say(reminder.msg+" before "+event, {to:who});
		return;
	}
	
	children.push(p);

}

/*
 * Other commands could theoretically be added
 */
say("Say 'cancel' to stop reminders for "+event, {to:who});

while(Date.now().isBefore(evtDate)) {
	var ctrlMsg = listen({to:who, timeout:evtDate});
	
	if(ctrlMsg == null) {
		//probably after the event
		continue;
	}

	if(ctrlMsg.toUpperCase() == "CANCEL") {
		for(var pid in children) {
			kill(pid);
		}
	}
}

