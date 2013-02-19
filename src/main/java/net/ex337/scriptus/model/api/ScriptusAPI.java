package net.ex337.scriptus.model.api;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.functions.Ask;
import net.ex337.scriptus.model.api.functions.Exec;
import net.ex337.scriptus.model.api.functions.Fork;
import net.ex337.scriptus.model.api.functions.Get;
import net.ex337.scriptus.model.api.functions.Kill;
import net.ex337.scriptus.model.api.functions.Listen;
import net.ex337.scriptus.model.api.functions.Log;
import net.ex337.scriptus.model.api.functions.Say;
import net.ex337.scriptus.model.api.functions.Sleep;
import net.ex337.scriptus.model.api.functions.Wait;
import net.ex337.scriptus.model.api.output.NormalTermination;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

/**
 * 
 * Object that implements the Scriptus API in Java and forms the global
 * scope for running Scripts. Can be called from within the script via
 * either scriptus.fork() or just fork(), like window.alert() etc. in
 * a browser.
 * 
 * All member fields must be serialisable.
 * 
 * @author ian
 *
 */
public class ScriptusAPI extends ScriptableObject implements Serializable {

    private static final String MSG_ID_ARG = "messageId";
    private static final String TIMEOUT_ARG = "timeout";
	private static final String TO_ARG = "to";

	private static final long serialVersionUID = 4654244388518182133L;

	private static Random rnd = new SecureRandom();
	
	private TimeUnit waitSleepUnit;
	private int defaultTimeoutLength;
	
	private Pattern durationPattern;
	
	public ScriptusAPI(ScriptusConfig config) {
		waitSleepUnit = config.getWaitSleepUnit();
		defaultTimeoutLength = config.getDefaultTimeoutLength();
		durationPattern = Pattern.compile(ScriptusConfig.DURATION_FORMAT);
	}

	@Override
	public String getClassName() {
		return "scriptus";
	}

	//one day: i18n
	public static final List<ScriptusMethod> SCRIPTUS_API = Collections.unmodifiableList(new ArrayList<ScriptusMethod>() {{
        add(new ScriptusMethod("log", "void log(message)", 
                "Writes a message to the log file.", Object.class));

        add(new ScriptusMethod("fork", "pid fork()", 
                "Clones the current process and returns to the parent with the process id (pid), and to the child process with 0."));
        
        add(new ScriptusMethod("kill", "kill(pid)", 
                "Terminates the process specified with the proces ID. The process to kill must be a child of the current process.", 
                String.class));
        
        add(new ScriptusMethod("exec", "exec(script, args)", 
                "Executes the script with the name and arguments given. The new program will have the pid of the current process.", 
                String.class, String.class));
        
        add(new ScriptusMethod("listen", "listen({to:\"user\", messageId:\"id\", timeout:duration})", 
                "Listens to the next message, optionally from a specified user or in reply to a given message ID, waiting 24 hours by default if no duration is specified.",
                NativeObject.class));
        
        add(new ScriptusMethod("say", "messageId say(\"message\", {to:\"user\"})", 
                "Says a message, optionally to a specific user, and returns the ID of the sent message.", 
                String.class, NativeObject.class));
        
        add(new ScriptusMethod("ask", "response ask(\"question\", {to:\"user\", timeout:duration})", 
                "Says a message, optionally to a specific user, and returns the first response, or null if no response is received within the timeout period (24 hours by default)", 
                String.class, NativeObject.class));
        
        add(new ScriptusMethod("get", "webpage get(\"url\")", 
                "Returns the contents of an HTTP GET request to the specified http:// or https:// URL.", 
                String.class));
        
        add(new ScriptusMethod("sleep", "sleep(duration)", 
                "Pauses execution of the current process for the duration provided.", 
                Object.class));
        
        add(new ScriptusMethod("exit", "exit(result)", 
                "Terminates the current process, returning the optional result to the waiting parent process, if any.", 
                Object.class));
        add(new ScriptusMethod("wait", "wait(function, pid)", 
                "Waits for the process specified by pid, or the last child created if the pid is absent, executing the optional function provided with the result, if any.", 
                true));
        //FIXME wait
    }});
	
	public Scriptable createScope(Context cx) throws SecurityException, NoSuchMethodException {
		Scriptable globalScope = cx.initStandardObjects(this);

		for(ScriptusMethod m : SCRIPTUS_API){
		    
		    if(m.hasSeparateImpl()) {
		        continue;
		    }
		    
	        globalScope.put(m.getMethodName(), globalScope, 
	                new FunctionObject(
	                        m.getMethodName(), 
	                        this.getClass().getMethod(m.getMethodName(), m.getMethodArgs()), 
                        globalScope)
	        );
		}
		
//		globalScope.put("log", globalScope, new FunctionObject("log", this.getClass().getMethod("log", Object.class), globalScope));
//		globalScope.put("fork", globalScope, new FunctionObject("fork", this.getClass().getMethod("fork"), globalScope));
//		globalScope.put("kill", globalScope, new FunctionObject("kill", this.getClass().getMethod("kill", String.class), globalScope));
//		globalScope.put("exec", globalScope, new FunctionObject("exec", this.getClass().getMethod("exec", String.class, String.class), globalScope));
//		globalScope.put("listen", globalScope, new FunctionObject("listen", this.getClass().getMethod("listen", NativeObject.class), globalScope));
//		globalScope.put("say", globalScope, new FunctionObject("say", this.getClass().getMethod("say", String.class, NativeObject.class), globalScope));
//		globalScope.put("ask", globalScope, new FunctionObject("ask", this.getClass().getMethod("ask", String.class, NativeObject.class), globalScope));
//		globalScope.put("get", globalScope, new FunctionObject("get", this.getClass().getMethod("get", String.class), globalScope));
//		globalScope.put("sleep", globalScope, new FunctionObject("sleep", this.getClass().getMethod("sleep", Object.class), globalScope));
//		globalScope.put("exit", globalScope, new FunctionObject("exit", this.getClass().getMethod("exit", Object.class), globalScope));

		String source;
		try {
			source = IOUtils.toString(globalScope.getClass().getClassLoader().getResourceAsStream("wait.js"));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}

		Function getLastPid = new FunctionObject("_intern_getLastPid", this.getClass().getMethod("_intern_getLastPid"),
				globalScope);
		Function wait_intern = new FunctionObject("_intern_wait", this.getClass().getMethod("_intern_wait", String.class), globalScope);

		Function wait_ctor = cx.compileFunction(globalScope, source, "wait_internal.js", 0, null);

		Function wait = (Function) wait_ctor.call(cx, globalScope, null, new Object[] { wait_intern, getLastPid });
		
		
		globalScope.put("wait", this, wait);
		
		globalScope.put("scriptus", globalScope, this);
		
		return globalScope;
	}

	private Calendar getDuration(Object o) {
		
		Calendar until = Calendar.getInstance();
		
		boolean foundDate = false;
		
		if(o instanceof Double) {
			
			foundDate = true;
			
			int waitUnits = ((Double)o).intValue();

			long timeInSeconds = TimeUnit.SECONDS.convert(waitUnits, waitSleepUnit);

			until.add(Calendar.SECOND, (int) timeInSeconds);

		} else if(o instanceof ScriptableObject && "Date".equals(((ScriptableObject)o).getClassName())) {

			foundDate = true;
			
			Date d = (Date) Context.jsToJava(o, Date.class);
			
			until.setTime(d);
			
		} else if(o instanceof String) {
			
			DateFormat sdf = new SimpleDateFormat(ScriptusConfig.DATE_FORMAT);
			
			try {
				Date d = sdf.parse((String)o);
				until.setTime(d);
				foundDate = true;
			} catch(ParseException e) {
				//do nothing
			}
			
			if( ! foundDate) {
				Matcher m = durationPattern.matcher((String)o);
				
				while(m.find()) {
					foundDate = true;
					
					int dur = Integer.parseInt(m.group(1));
					
					char unit = m.group(2).charAt(0);
					
					int calUnit;
					
					switch(unit) {
						case 's':  calUnit = Calendar.SECOND;				break;
						case 'm':  calUnit = Calendar.MINUTE;				break;
						case 'h':  calUnit = Calendar.HOUR_OF_DAY;			break;
						case 'd':  calUnit = Calendar.DATE;					break;
						case 'w':  calUnit = Calendar.DATE; dur *= 7;		break;
						case 'M':  calUnit = Calendar.MONTH;				break;
						case 'q':  calUnit = Calendar.MONTH; dur *= 3;		break;
						case 'y':  calUnit = Calendar.YEAR;					break;
						case 'D':  calUnit = Calendar.YEAR; dur *= 10;		break; // decade
						case 'C':  calUnit = Calendar.YEAR;	dur *= 100;		break; // century :-D go long!
						default : throw new WrappedException(new ScriptusRuntimeException("unrecognised unit: "+unit));
					}
					
					until.add(calUnit, dur);
				}
			}
		}
		
		if( ! foundDate) {
			throw new WrappedException(new ScriptusRuntimeException("Could not recognise duration argument: "+o+" must either be integer >1, or match date format "+ScriptusConfig.DATE_FORMAT+" or duration format regexp "+ScriptusConfig.DURATION_FORMAT));
		}

		return until;
		
	}
	
	public void log(Object o) {
        ContinuationPending pending = Context.getCurrentContext().captureContinuation();
        pending.setApplicationState(new Log(o));
        throw pending;
	}

	public Object fork() {
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Fork());
		throw pending;
	}

	public Object kill(String pid) {
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Kill(UUID.fromString(pid)));
		throw pending;
	}

	public Object exec(String program, String args) {
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Exec(program, args));
		throw pending;
	}

	public Object exit(Object result) {
		
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new NormalTermination(result));
		throw pending;
	}

	public Object sleep(Object untilArg) {
		
		if(untilArg == null) {
			throw new ScriptusRuntimeException("no arg presented for sleep");
		}
		
		Calendar until = getDuration(untilArg);
		
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Sleep(until, rnd.nextLong()));
		throw pending;
	}


	public String get(String url) {
		
		if(url == null) {
			throw new ScriptusRuntimeException("no arg presented for sleep");
		}
		
		URL u;
		
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			throw new ScriptusRuntimeException(e);
		}
		
		if( ! "http".equals(u.getProtocol())  && ! "https".equals(u.getProtocol())) {
			throw new ScriptusRuntimeException("only http and https supported right now");
		}
		
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Get(u));
		throw pending;
	}

	public Object ask(String msg, NativeObject params) {

		String who = null;
		
		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.HOUR, defaultTimeoutLength);
		
		if(params != null) {
			
			Object owho = params.get(TO_ARG, params);
			
			if(owho != null && owho != NativeObject.NOT_FOUND) {
				who = owho.toString();
			}
			
			Object otimeout = params.get(TIMEOUT_ARG, params);
			
			if(otimeout != null && otimeout != NativeObject.NOT_FOUND) {
				timeout = getDuration(otimeout);
			}
			
		}
		
		if(msg == null){
			throw new WrappedException(new ScriptusRuntimeException("invalid args to ask function, format: ask(question, [{who:who, timeout:duration}]"));
		}

		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Ask(msg, who, timeout, rnd.nextLong()));
		throw pending;
	}

	public Object say(String msg, NativeObject params) {

		String who = null;

        if(params != null) {
			
			Object owho = params.get(TO_ARG, params);
			
			if(owho != null && owho != NativeObject.NOT_FOUND) {
				who = owho.toString();
			}
			
		}
		
		if(msg == null){
			throw new WrappedException(new ScriptusRuntimeException("no message argument to say function"));
		}
		
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Say(msg, who));
		throw pending;
	}

	public Object listen(NativeObject params) {

		String who = null;
		
		String messageId = null;

		long timeInSeconds = TimeUnit.SECONDS.convert(defaultTimeoutLength, waitSleepUnit);

		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.SECOND, (int) timeInSeconds);
		
		if(params != null) {
			
			Object owho = params.get(TO_ARG, params);
			
			if(owho != null  && owho != NativeObject.NOT_FOUND) {
				who = owho.toString();
			}
			
			Object otimeout = params.get(TIMEOUT_ARG, params);
			
			if(otimeout != null  && otimeout != NativeObject.NOT_FOUND) {
				timeout = getDuration(otimeout);
			}
			
			Object omessageId = params.get(MSG_ID_ARG, params);
            
            if(omessageId != null  && omessageId != NativeObject.NOT_FOUND) {
                messageId = omessageId.toString();
            }
			
		}
		
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Listen(who, timeout, rnd.nextLong(), messageId));
		throw pending;
	}

	public Object _intern_wait(String pid) {
		ContinuationPending pending = Context.getCurrentContext().captureContinuation();
		pending.setApplicationState(new Wait(UUID.fromString(pid)));
		throw pending;
	}

	public Object _intern_getLastPid() {
		ScriptProcess process = (ScriptProcess) Context.getCurrentContext().getThreadLocal("process");
		return process.getLastChild().toString();
	}

}