package net.ex337.scriptus.model.support;

import java.io.Serializable;

import org.mozilla.javascript.ClassShutter;

/**
 * Used to deny access to any and all Java classes
 * from the Scriptus Rhino environment.
 * 
 * @author ian
 *
 */
public final class ScriptusClassShutter implements ClassShutter, Serializable {

		private static final long serialVersionUID = 5797434393197067157L;

		@Override
		public boolean visibleToScripts(String fullClassName) {
			//maybe exceptions?
			return false;
		}
	}