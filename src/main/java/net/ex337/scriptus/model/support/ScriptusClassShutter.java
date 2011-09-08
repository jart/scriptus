package net.ex337.scriptus.model.support;

import java.io.Serializable;

import org.mozilla.javascript.ClassShutter;

public final class ScriptusClassShutter implements ClassShutter, Serializable {

		private static final long serialVersionUID = 5797434393197067157L;

		@Override
		public boolean visibleToScripts(String fullClassName) {
			//maybe exceptions?
			return false;
		}
	}