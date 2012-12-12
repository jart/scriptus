package net.ex337.scriptus.tests;


import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

public class JsTest {

	public static void main(String[] args) {
		
		Context cx = Context.enter();
		
		try {

			JsClass s = new JsClass("bar");
			
			Scriptable globalScope = cx.initStandardObjects(s);

			cx.setOptimizationLevel(-1); // must use interpreter mode
			
			FunctionObject f = new FunctionObject("foo", JsClass.class.getMethod("foo", String.class, Object.class), globalScope);
			
			cx.putThreadLocal("tl", "bla");
			
			globalScope.put("foo", globalScope, f);//necessary or scriptus.foo won't work either...
			globalScope.put("scriptus", globalScope, s);
			
//			globalScope.setPrototype(s);
			
			Script ss = cx.compileString("scriptus.foo('dfd');var f = foo('baz', scriptus); foo(f);scriptus.foo('dfd');", "<test>", 1, null);

			try {
				cx.executeScriptWithContinuations(ss, globalScope);
			} catch(ContinuationPending p) {
				System.out.println("pending:"+p.getApplicationState());
		
				p.setApplicationState("foo");
				
				SerializableUtils.serialiseObject(p);

				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				ScriptableOutputStream out = new ScriptableOutputStream(bout, globalScope);
//				out.addExcludedName("scriptus");
				out.writeObject(globalScope);
				out.writeObject(p);
				out.close();

			}
			
//			System.out.println(ss.exec(cx, globalScope));
			
//			System.out.println(cx.evaluateString(globalScope, "scriptus.foo();", "<test>", 1, null));
			
		} catch (Exception e) {
			throw new ScriptusRuntimeException(e);
		} finally {
			Context.exit();
		}
		
	}
	
	public static class JsClass extends ScriptableObject {
		
		private String pref;
		
		public JsClass(String pref){this.pref = pref;};
		
//		public void jsConstructor() {
//			
//		}
		
		public void foo(String p, Object o) {
			
			System.out.println(pref+"foo"+p+", o="+o);
			
			Context cx = Context.getCurrentContext();
			
			System.out.println("tl="+cx.getThreadLocal("tl"));
			
//			ContinuationPending c = cx.captureContinuation();
//			c.setApplicationState(new Object());
//			
//			throw c;
			
		}

		@Override
		public String getClassName() {
			return "scriptus";
		}
		
	}
}

