/*

fixme point thresholds
date randomisation


*/

/*
 http://www.habitjudo.com/
"1. Pick 3 new simple daily habits, one of which must be 'Record your habits'
*/
var habits = [];
//var habitCount = [];
var numStartingHabits = 3;

for(var i = 0; i != numStartingHabits) {
	habits.push(ask("habit #"+(i+1)+"?"));
//	habitCount.push(0);
}

/*
2. Get a random number of points between 1 & 10 each day for each successful habit
*/
var maxNumPoints = 10;
/*
3. Add each day's points to a running point total
*/
var score = 0;
/*
4. At point totals that occur about every 3 days, get a small reward
*/
var avgDaysBetweenRewards = 3;
/*
5. At point totals that occur about every 10-12 days, you level up
*/
var avgLevelUpDays = 11;
/*
6. When you level up, you get to add a new habit

Every time you level up, you get to add a new habit. Habit Judo uses the judo belt system to represent levels. You start as a white belt, and move through the colors yellow, orange, green, blue, and brown on your way up to black belt. To keep you habits present-in-mind, you should symbolize your belt level by something on your person.

7. GO TO #2"

*/
var belts = ["white", "yellow", "orange", "green", "blue", "brown", "black"];
var currentLevel = 0;

say("record habits by saying 'did #1'");

var lastReward;
var lastLevelup;

while(recorded != "STOP") {

	//main event loop

	recorded=listen({timeout:"1d"});
	
	if(recorded = "did #") {
		if(x > habits.length) continue;
//		habitCount[x-1]++;
		var count = rnd(maxNumPoints);
		say(count+" points!");
	}
	
	var now = new Date();
	
	if( ! lastReward) {
		lastReward = now;
		continue;
	}
	
	if(now - lastReward > rnd(avgDaysBetweenRewards)) {
		say("reward yourself!");
		lastReward = now;
	}
	
	if( ! lastLevelup) {
		lastLevelup = now;
		continue;
	}
	
	if(now - lastLevelup > rnd(avgLevelUpDays)) {
		currentLevel++;
		say("you leveled up! Now at level "+(currentLevel+1)+", "+belts[currentLevel-1]+" belt!");
		lastLevelup = now;
		
		habits.push(ask("habit #"+habits.length+"?"));
//		habitCount.push(0);
		
	}
	
}

/*

Habit Judo begins with 3 easy daily habits. They should be little things that you should be doing but aren't, e.g. cleaning kitty litter, flossing, drinking 8 glasses of water, etc. Start easy, so that you build positive momentum for later. You start by keeping track of whether you complete each habit each day. This daily tracking is really important, it's essential to triggering the positive feedback loop that makes the system so powerful. In fact, it's so important that "Record your habits" should always be one of your first three habits (the other two you can choose yourself).

Each day, for each successful habit, you earn a RANDOM number of points between 1 and 10. You can roll at 10-sided die, or use the random number generator built into the sample spreadsheet linked below. This randomness is what produces the variable in the variable ratio. Your points accumulate each day. At certain point thresholds that occur about every 3 days, you get a small self-determined reward (something less than $10). At point thresholds that occur about every 10-12 days, you level up. Every time you level up, you get to add a new habit. Habit Judo uses the judo belt system to represent levels. You start as a white belt, and move through the colors yellow, orange, green, blue, and brown on your way up to black belt. To keep you habits present-in-mind, you should symbolize your belt level by something on your person. One easy method is wearing a silicone wristband (think LifeStrong bracelet) of your current belt color. You can get them from Amazon.com here. To make getting started even easier for you, there's an Excel spreadsheet pre-formatted for Habit Judo at the bottom of this page.
*/

/*
http://projects.metafilter.com/3055/Habit-Judo

Here's how it works:

You get RANDBETWEEN(1,10) for each habit you complete for each day. You log these on a spreadsheet that you update daily. Every at certain point levels that occur every ~3 days you get a self-determined reward. At point levels that occur every ~10 days you level up. I use a judo belt system. I'm now orange/green. I symbolize these by wearing a silicone wristband (e.g. the livestong bracelet type) of appropriate color for the relevant belt I'm at.

I began with 3 easy habits

1. Keep a ubiquitous capture device with you
2. Keep a to do list
3. Check in with the spreadsheet every day.

For every belt you level up, you get to add another habit.

Here's where I got the bracelets. (just an amazon link, I get no referral and I'm not affiliated with the bracelet maker) 

*/
