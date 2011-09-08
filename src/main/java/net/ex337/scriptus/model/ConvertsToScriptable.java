package net.ex337.scriptus.model;

import org.mozilla.javascript.Scriptable;

public interface ConvertsToScriptable {
	public Scriptable toScriptable();
}