package net.ex337.scriptus.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

public class Testcase_ContinuationWierdness extends TestCase {

	public static class MyClass implements Serializable {

		private static final long serialVersionUID = 4189002778806232070L;

		public int f(int a) {
			Context cx = Context.enter();
			try {
				ContinuationPending pending = cx.captureContinuation();
				pending.setApplicationState(a);
				throw pending;
			} finally {
				Context.exit();
			}
		}

		public int g(int a) {
			Context cx = Context.enter();
			try {
				ContinuationPending pending = cx.captureContinuation();
				pending.setApplicationState(2 * a);
				throw pending;
			} finally {
				Context.exit();
			}
		}

		public String expr() {
			Context cx = Context.enter();
			try {
				ContinuationPending pending = cx.captureContinuation();
				pending.setApplicationState("2*3");
				throw pending;
			} finally {
				Context.exit();
			}
		}
	}

	public void test_1_PrototypesSerializationContinuationsOhMy() throws IOException, ClassNotFoundException {

		byte[] serializedData = null;

		{
			Scriptable globalScope;
			Context cx = Context.enter();
			try {
				globalScope = cx.initStandardObjects();
				cx.setOptimizationLevel(-1); // must use interpreter mode
				globalScope.put("myObject", globalScope, Context.javaToJS(new MyClass(), globalScope));
			} finally {
				Context.exit();
			}

			
			cx = Context.enter();
			try {
				cx.setOptimizationLevel(-1); // must use interpreter mode
				cx.evaluateString(
						globalScope,
						"function f(a) { Number.prototype.blargh = function() {return 'foo';}; var k = myObject.f(a); var t = []; return new Number(8).blargh(); }",
						"function test source", 1, null);
				Function f = (Function) globalScope.get("f", globalScope);
				Object[] args = { 7 };
				cx.callFunctionWithContinuations(f, globalScope, args);
				fail("Should throw ContinuationPending");
			} catch (ContinuationPending pending) {
				// serialize
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ScriptableOutputStream sos = new ScriptableOutputStream(baos, globalScope);
				sos.writeObject(globalScope);
				sos.writeObject(pending.getContinuation());
				sos.close();
				baos.close();
				serializedData = baos.toByteArray();
			} finally {
				Context.exit();
			}
		}

		{
			try {
				Context cx = Context.enter();
				
				Scriptable globalScope = cx.initStandardObjects();
				globalScope.put("myObject", globalScope, Context.javaToJS(new MyClass(), globalScope));

				// deserialize
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
				ScriptableInputStream sis = new ScriptableInputStream(bais, globalScope);
				globalScope = (Scriptable) sis.readObject();
				Object continuation = sis.readObject();
				sis.close();
				bais.close();

				Object result = cx.resumeContinuation(continuation, globalScope, 8);
				assertEquals("foo", result);
			} finally {
				Context.exit();
			}
		}
		
	}

	public void test_2_PrototypesSerializationContinuationsOhMy() throws IOException, ClassNotFoundException {

		byte[] serializedData = null;

		{
			Scriptable globalScope;
			Context cx = Context.enter();
			try {
				globalScope = cx.initStandardObjects();
				cx.setOptimizationLevel(-1); // must use interpreter mode
				globalScope.put("myObject", globalScope, Context.javaToJS(new MyClass(), globalScope));
			} finally {
				Context.exit();
			}

			
			cx = Context.enter();
			try {
				cx.setOptimizationLevel(-1); // must use interpreter mode
				cx.evaluateString(
						globalScope,
						"function f(a) { Number.prototype.blargh = function() {return 'foo';}; var k = myObject.f(a); var t = []; return new Number(8).blargh(); }",
						"function test source", 1, null);
				Function f = (Function) globalScope.get("f", globalScope);
				Object[] args = { 7 };
				cx.callFunctionWithContinuations(f, globalScope, args);
				fail("Should throw ContinuationPending");
			} catch (ContinuationPending pending) {
				// serialize
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream sos = new ObjectOutputStream(baos);
				sos.writeObject(globalScope);
				sos.writeObject(pending.getContinuation());
				sos.close();
				baos.close();
				serializedData = baos.toByteArray();
			} finally {
				Context.exit();
			}
		}

		{
			try {
				Context cx = Context.enter();
				
				Scriptable globalScope;
				
//				Scriptable globalScope = cx.initStandardObjects();
//				globalScope.put("myObject", globalScope, Context.javaToJS(new MyClass(), globalScope));

				// deserialize
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
				ObjectInputStream sis = new ObjectInputStream(bais);
				globalScope = (Scriptable) sis.readObject();
				Object continuation = sis.readObject();
				sis.close();
				bais.close();

				Object result = cx.resumeContinuation(continuation, globalScope, 8);
				assertEquals("foo", result);
			} finally {
				Context.exit();
			}
		}
		
	}

	//	public void testInlineFunctionsSerializationContinuationsOhMy() throws IOException, ClassNotFoundException {
//
//		Scriptable globalScope;
//		Context cx = Context.enter();
//		try {
//			globalScope = cx.initStandardObjects();
//			cx.setOptimizationLevel(-1); // must use interpreter mode
//			globalScope.put("myObject", globalScope, Context.javaToJS(new MyClass(), globalScope));
//		} finally {
//			Context.exit();
//		}
//
//		cx = Context.enter();
//		try {
//			cx.setOptimizationLevel(-1); // must use interpreter mode
//			cx.evaluateString(globalScope, "function f(a) { var k = eval(myObject.expr()); var t = []; return k; }",
//					"function test source", 1, null);
//			Function f = (Function) globalScope.get("f", globalScope);
//			Object[] args = { 7 };
//			cx.callFunctionWithContinuations(f, globalScope, args);
//			fail("Should throw ContinuationPending");
//		} catch (ContinuationPending pending) {
//			// serialize
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ScriptableOutputStream sos = new ScriptableOutputStream(baos, globalScope);
//			sos.writeObject(globalScope);
//			sos.writeObject(pending.getContinuation());
//			sos.close();
//			baos.close();
//			byte[] serializedData = baos.toByteArray();
//
//			// deserialize
//			ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
//			ScriptableInputStream sis = new ScriptableInputStream(bais, globalScope);
//			globalScope = (Scriptable) sis.readObject();
//			Object continuation = sis.readObject();
//			sis.close();
//			bais.close();
//
//			Object result = cx.resumeContinuation(continuation, globalScope, "2+3");
//			assertEquals(5, ((Number) result).intValue());
//		} finally {
//			Context.exit();
//		}
//	}

}
