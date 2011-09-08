
var minutes;

var disbanded = false;

while( ! disbanded) {

	//1. blank list if necessary
	if(minutes == null) {
		minutes = {actions[]};
	}

	var accepted = false;

	do {
		
		//1. followup old actions
		forkAndWait(minutes.actions, function(action){
			var status = prompt(24h, act.assignedTo, "Pls report status of "+a.action);
			if(status == null) status = "No response";
			return status;
		}, function(action, result) {
			action.status = result;
		});

		//2. process actions
		for(var m in minutes.actions) {
			if(m.status == "DELETE") {
		
			} else if(m.status == "DONE") {
		
			} else if(m.status.startsWith("@")) {//delegation

			}
		}
	

		//3. get new actions
		var newActions = poll(24h, participants, "New actions to add? (use @person message to assign)");

		for(var i in newActions) {
			minutes.actions.push({action:i,assignedTo:null/*@foo*/});
		}

		//4.review actions
		var objections = review();

		if( objections.length != 0) {
			say("objections: "+(objections.length-1));
		}

	} while( objections.length != 0 );

	//CHECK actions assigned to me (script) incld. eval 

	sleepUntil(newtM);
}

function reportActionStatus(action) {
	var status = prompt(24h, act.assignedTo, "Pls report status of "+a.action);
	if(status == null) status = "No response";
	return status;
}

function forEach(list, perFnFork, waitFn) {
	for(var m in list) {
		var pid = fork();
		if(pid == 0) {
			return perFnFork(m);
		}
		m.pid = pid;
	}
	for(var m in list) {
		waitpid(m.pid, curry(m, waitFn));
	}
}

function curry(m, fn) {
	return function(arg) {
		return fn(m, arg);
	}
}

function review() {

	var objections = [];

	var responses = poll(24h, participants, "Minutes updated! Please give OK/NOK");

	for( r : responses ) {
		if(r.indexOf("NOK") != -1) {
			objections.push(r);
		}
	}
	
	return objections;

}


