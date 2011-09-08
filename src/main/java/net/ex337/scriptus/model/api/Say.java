package net.ex337.scriptus.model.api;

import java.io.Serializable;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.ScriptAction;

public class Say extends ScriptAction implements Serializable {

	private static final long serialVersionUID = 418900227788062200L;

	private String msg;
	private String who;

	public Say(String msg, String who) {
		this.msg = msg;
		this.who = who;
	}

	public String getMsg() {
		return msg;
	}

	public String getWho() {
		return who;
	}

	public String toString() {
		return msg;
	}

	@Override
	public void visit(ProcessScheduler scheduler, InteractionMedium medium, ScriptusDAO dao, ScriptProcess process) {

		medium.say(who, msg);
		
		scheduler.updateProcessState(process.getPid(), null);

		scheduler.execute(process.getPid());

	}
}
