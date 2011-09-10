package net.ex337.scriptus.model;

import org.mozilla.javascript.Scriptable;

/**
 * All non-primitive objects resulting from API calls that will 
 * be passed back into the scripting environment must implement 
 * this interface. The object will be converted to it's JavaScript
 * representation via the {@link #toScriptable()} method.
 * 
 * @author ian
 *
 */
public interface ConvertsToScriptable {
	public Scriptable toScriptable();
}