package net.ex337.scriptus.model.api;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.model.ConvertsToScriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class Message implements Serializable, ConvertsToScriptable {

	private static final long serialVersionUID = -1501095557162637957L;
	
	private UUID pid;
	private String from;
	private String msg;
	public Message(UUID pid, String from, String msg) {
		super();
		this.pid = pid;
		this.from = from;
		this.msg = msg;
	}
	
	public String getFrom() {
		return from;
	}
	public String getMsg() {
		return msg;
	}

	public UUID getPid() {
		return pid;
	}

	@Override
	public Scriptable toScriptable() {
		
		Context cx = Context.enter();
		
		Scriptable result;
		
		try {
			Scriptable globalScope = cx.initStandardObjects();

			result = (Scriptable) cx.newObject(globalScope, "String", new Object[] {msg});

		} finally {
			Context.exit();
		}
		
		return result;
	}
	
	
}