package net.ex337.scriptus.model.support;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.ScriptusAPI;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

public abstract class ContextCall implements Callable<Object>, Serializable {
	
	private static final long serialVersionUID = 2954636270973183753L;
	
	private boolean called = false;
	
	private boolean createScope;
	
	private ScriptProcess process;
	private ScriptusConfig config;
	
	public ContextCall(ScriptusConfig config, ScriptProcess process, boolean createScope) throws ScriptusRuntimeException {
		this.config = config;
		this.createScope = createScope;
		this.process = process;
		call();
	}
			
	public final Object call() throws ScriptusRuntimeException {
		
		if(called) {
			throw new ScriptusRuntimeException("already called");
		}
		
		Context cx = Context.enter();
		cx.setClassShutter(new ScriptusClassShutter());
		
		try {

			cx.setOptimizationLevel(-1); // must use interpreter mode

			if(createScope) {
				
				ScriptusAPI scriptusApi = new ScriptusAPI(config);

				Scriptable globalScope = scriptusApi.createScope(cx);
				
				process.setGlobalScope(globalScope);
			}
			
			call(cx);

		} catch (Exception e) {
			throw new ScriptusRuntimeException(e);
		} finally {
			Context.exit();
			called = true;
		}
		return null;
		
	}

	public abstract void call(Context cx) throws Exception;
}