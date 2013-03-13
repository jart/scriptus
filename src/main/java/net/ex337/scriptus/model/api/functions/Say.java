package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;

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
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {

		String messageId = scriptus.send(process.getUserId(), process.getTransport(), who, msg);
		
		scriptus.updateProcessState(process.getPid(), messageId);

		scriptus.execute(process.getPid());

	}
}
