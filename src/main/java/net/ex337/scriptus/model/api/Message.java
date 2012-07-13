package net.ex337.scriptus.model.api;

import java.io.Serializable;

import net.ex337.scriptus.model.ConvertsToScriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class Message implements Serializable, ConvertsToScriptable {

	private static final long serialVersionUID = -1501095557162637957L;
	
	private String from;
	private String msg;
	private String inReplyToMessageId;
	
	public Message(String from, String msg) {
		super();
		this.from = from;
		this.msg = msg;
	}
	
	public String getFrom() {
		return from;
	}
	public String getMsg() {
		return msg;
	}

	@Override
	public Scriptable toScriptable() {
		
		Context cx = Context.enter();
		
		Scriptable result;
		
		try {
			Scriptable globalScope = cx.initStandardObjects();

			result = (Scriptable) cx.newObject(globalScope, "String", new Object[] {msg});
			
			result.put("from", result, getFrom());

		} finally {
			Context.exit();
		}
		
		return result;
	}

    public String getInReplyToMessageId() {
        return inReplyToMessageId;
    }

    public void setInReplyToMessageId(String inReplyToMessageId) {
        this.inReplyToMessageId = inReplyToMessageId;
    }
	
}