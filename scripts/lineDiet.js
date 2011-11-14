exit("not tested yet, so it's definitely not working");


//import date library, see http://www.datejs.com/
//forked and bugfixed, see https://github.com/ianso/datejs
eval(get("https://raw.github.com/ianso/scriptus/master/scripts/lib/date-en-US.js"));

function getMinSafeWeight(height) {
	//curves reverse-calculated from http://www.nhs.uk/Livewell/healthy-living/Pages/height-weight-chart.aspx
}

var currentWeight = Number(ask("How much do you weigh now? (in Kg., e.g. '70')"));
var desiredWeight = Number(ask("How much would you like to weigh?"));
var dietEnd = Date.parse(ask("By when would you like to achieve this? (e.g. 5 Dec 2015)"));

/*
sanity checks: 
  current is more than desired,
  dietEnd is after now, 
  desired weight is healthy,
  desired rate of weight loss is medically safe (ask for age maybe?)
*/

/*

work out how much to lose per week

*/
while(new Date().isBefore(dietEnd)) {

	var currentWeight = ask("How much do you weigh now (kg)?");
	var now = new Date();//to millis
	
	/*the goal of the program is a linear incremental weight loss.*/
	var targetWeight = desiredWeight + ((currentWeight - desiredWeight) / (dietEnd.getTime() - now.getTime()));
	
	//since we deal in doubles, we need to remove the "roughly equals" possiblity...
	
	if(targetWeight > currentWeight) {
		say("Reduce your caloric intake to remain on track");
	} else if(targetWeight < currentWeight) {
		say("You're doing fine, so maintain your current diet");
	}
	
	sleep("1w");

}

//then, go get into the habit of the new diet, a flat-line tracking for a bit?



