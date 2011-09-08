package net.ex337.scriptus.model.scheduler;

import java.util.UUID;

import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.ScriptAction;

public abstract class ScheduledScriptAction extends ScriptAction {

	public abstract UUID getPid();
	
	public abstract String toString();
	public abstract void fromString(String s);
	
	public abstract long getNonce();

	public static ScheduledScriptAction readFromString(String s) {
		
		String klazz = s.substring(0, s.indexOf("/"));
		
		ScheduledScriptAction a;
		try {
			a = (ScheduledScriptAction) Class.forName(klazz).newInstance();
		} catch (InstantiationException e) {
			throw new ScriptusRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptusRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new ScriptusRuntimeException(e);
		}
		a.fromString(s);
		
		return a;
		
	}
	
}
