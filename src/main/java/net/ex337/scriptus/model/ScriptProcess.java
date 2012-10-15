package net.ex337.scriptus.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Resource;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.api.ScriptusAPI;
import net.ex337.scriptus.model.api.Termination;
import net.ex337.scriptus.model.api.output.ErrorTermination;
import net.ex337.scriptus.model.api.output.NormalTermination;
import net.ex337.scriptus.model.support.ScriptusClassShutter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

/**
 * Represents one script process. The source of the process
 * is loaded at initialisation time and is kept with the 
 * process, i.e. changing a script will have no effect on
 * currently executing scripts.
 * 
 * @author ian
 *
 */
public class ScriptProcess implements Callable<ScriptAction>, Runnable, Serializable, Cloneable {

	private static final Log LOG = LogFactory.getLog(ScriptProcess.class);

	/**
	 * The length of the ID in bytes.
	 */
	public static final int ID_SIZE_BYTES = 16;

	private static final long serialVersionUID = -7512596370437192858L;

	private UUID pid;
	private UUID waiterPid;
	private String userId;
	private String source;
	private String sourceName;
	private String args;
	private String owner;
	private Object state;
	private int version;
	private Function compiled;
	private boolean isRoot;
	
	private transient Object continuation;
	private transient Scriptable globalScope;
	
	private boolean isKilled;
	
	@Resource(name="datastore")
	private transient ScriptusDatastore datastore;

	@Resource
	private transient ScriptusConfig config;
	
	@Resource
	private transient ScriptusFacade facade;
	
	public ScriptProcess() {
	}

	/**
	 * 
	 * Initialises a process using the supplied source from the given user ID.
	 * 
	 * @param userId
	 * @param sourceName
	 * @param args
	 * @param owner
	 */
	public void init(String userId, final String sourceName, String args, String owner) {

		LOG.debug("ctor, source=" + sourceName);

//		this.datastore = datastore;
		this.userId = userId;
		this.sourceName = sourceName;
		this.args = args;
		this.owner = owner;
		this.source = datastore.loadScriptSource(userId, sourceName);
		this.isRoot = true;
		this.version = 0;

		Context cx = Context.enter();
		cx.setClassShutter(new ScriptusClassShutter());
		cx.setOptimizationLevel(-1); // must use interpreter mode
		
		try {

			ScriptusAPI scriptusApi = new ScriptusAPI(config);

			Scriptable globalScope = scriptusApi.createScope(cx);
			
			setGlobalScope(globalScope);
			
			compiled = cx.compileFunction(globalScope, "function ___scriptus___ () {"+source+"}", sourceName, 0, null);

		} catch (ScriptusRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ScriptusRuntimeException(e);
		} finally {
			Context.exit();
		}

	}

	/**
	 * Writes the script state to DAO. If the pid is null we assign a new one.
	 */
	public void save() {
	    
	    datastore.writeProcess(this);
	    version++;
	}

	/**
	 * 
	 * Executes the script and returns the ScriptAction of the next API call, or a {@link Termination}
	 * if the script finished.
	 * 
	 */
	public ScriptAction call() {

		Object result;

		try {

			if (continuation == null) {

				LOG.debug("starting new script");

				Context cx = Context.enter();
				cx.putThreadLocal("process", this);
				cx.setClassShutter(new ScriptusClassShutter());

				globalScope.put("args", globalScope, Context.javaToJS(args, globalScope));
				globalScope.put("owner", globalScope, Context.javaToJS(owner, globalScope));

				try {
					// running for first time
					result = cx.callFunctionWithContinuations(compiled, globalScope, new Object[0]);
				} finally {
					Context.exit();

				}

			} else {

				LOG.debug("continuing existing script " + getPid().toString().substring(30));

				if (state instanceof ConvertsToScriptable) {
					state = ((ConvertsToScriptable) state).toScriptable();
				}

				Context cx = Context.enter();
				cx.setClassShutter(new ScriptusClassShutter());
				cx.putThreadLocal("process", this);
				try {
					result = cx.resumeContinuation(continuation, globalScope, state);
				} finally {
					Context.exit();
				}

			}

		} catch (RhinoException e) {

			LOG.error("script error", e);

			return new ErrorTermination(e);

		} catch (ContinuationPending cp) {

			continuation = cp.getContinuation();

			state = cp.getApplicationState();

			LOG.error("script continuation, state obj=" + state.getClass());

			if (state instanceof ScriptAction) {
				return (ScriptAction) state;
			} else {
				throw new ScriptusRuntimeException("Continuation state not ScriptAction:" + state);
			}

		}

		state = result;

		return new NormalTermination(result);

	}


	/**
	 * Does the same as call() above, but also saves the process and calls visit()
	 * on the resulting {@link ScriptAction}.
	 */
	@Override
	public void run() {
		
		ScriptAction result = this.call();
		
		if(isKilled) {
			return;
			
		}

		this.save();
		
		result.visit(facade, this);
		
		
	}
	
	/**
	 * If the process is running when kill() is called,
	 * this method stops the next continuation from
	 * executing.
	 */
	public void kill() {
		isKilled = true;
	}

	/**
	 * Copies the process, used in fork()ing.
	 * Not a complete clone, the differences are:
	 *  - isRoot is false
	 *  - version is 0
	 *  - pid & waiterPid is null
	 *  - children is empty
	 * 
	 */
	public ScriptProcess clone() {
		ScriptProcess r = new ScriptProcess();
		r.args = this.args;
		r.compiled = this.compiled;
		r.continuation = this.continuation;
		r.globalScope = this.globalScope;
		r.source = this.source;
		r.sourceName = this.sourceName;
		r.state = this.state;
		r.userId = this.userId;
		r.datastore = this.datastore;
		r.config = this.config;
		r.owner = this.owner;
		r.facade = this.facade;
		// ?
		r.isRoot = false;
		r.version = 0;
		r.pid = null;
		r.waiterPid = null;

		return r;

	}

	/**
	 * deletes the script from DAO.
	 */
	public void delete() {
		
		/*
		 * TODO should this recursively delete child processes?
		 * 
		 */
		
		datastore.deleteProcess(getPid());
	}

	
	public String getSource() {
		return source;
	}

	public Object getContinuation() {
		return continuation;
	}

	public Object getState() {
		return state;
	}

	public void setState(Object state) {
		this.state = state;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public UUID getWaiterPid() {
		return waiterPid;
	}

	public void setWaiterPid(UUID pid) {
		this.waiterPid = pid;
	}

	public String getArgs() {
		return args;
	}

	public Scriptable getGlobalScope() {
		return globalScope;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setContinuation(Object continuation) {
		this.continuation = continuation;
	}

	public void setGlobalScope(Scriptable globalScope) {
		this.globalScope = globalScope;
	}

	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	public UUID getPid() {
		return pid;
	}

	public void setPid(UUID pid) {
		this.pid = pid;
	}


	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}


	


	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public String getUserId() {
		return userId;
	}

	public String getSourceName() {
		return sourceName;
	}

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Function getCompiled() {
        return compiled;
    }

    public void setCompiled(Function compiled) {
        this.compiled = compiled;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public UUID getLastChild() {
        return datastore.getLastChild(this.pid);
    }

    public void addChild(UUID childPid) {
        /*
         * since the version is incremented when we save, this means it's
         * OK to use as a child sequence - they don't have to be contiguous
         */
        datastore.addChild(this.pid, childPid, version);
    }

}
