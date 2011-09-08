
function looseWeight(days) {

	var t = interaction.prompt("Enter target weight:");««

	var md = 0;

	while(md <= days) {

		var w = interaction.prompt("Target weight: "+t+", current weight:");

		if(w > t) {
			md = 0;
			interaction.alert("Eat less!");
		} else {
			md++;
			interaction.alert("Maintain diet");
		}
	}

	interaction.alert("Congrats, target weight "+t+" maintained for "+days+" days, keep it up!");
}

looseWeight(3);
