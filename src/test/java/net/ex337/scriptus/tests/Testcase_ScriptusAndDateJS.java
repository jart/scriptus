package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.functions.Get;
import net.ex337.scriptus.model.api.functions.Say;
import net.ex337.scriptus.scheduler.ProcessScheduler;
import net.ex337.scriptus.transport.Transport;

/**
 * Tests the Scriptus API calls.
 * 
 * @author ian
 *
 */
public class Testcase_ScriptusAndDateJS extends BaseTestCase {

	private static final String TEST_USER = "test";
	private ProcessScheduler c;
	private ScriptusDatastore datastore;
	private Transport m;
	
	private static final Map<String,String> testSources = new HashMap<String,String>() {{
		put("evalget.js", 
				"var ss = get(\"https://raw.github.com/ianso/scriptus/master/scripts/lib/date-en-US.js\");" +
				"eval(ss);" +
//              "say((12).months().ago());"+
//		        "say((12).months()._dateElement);"+
//				"var s = (12).months();" +
//				"s._dateElement='months';" +
//				"say(s + ' ' + s._dateElement + ' ' + s.before(new Date()));"+
				"say((12).month().before(new Date()));"+
				"");
	}};

	@Override
	protected void setUp() throws Exception {

		System.setProperty("scriptus.config", "test-scriptus.properties");
		
		super.setUp();
		
		m = (Transport) appContext.getBean("transport");
		
		c = (ProcessScheduler) appContext.getBean("scheduler");
		
		datastore = (ScriptusDatastore) appContext.getBean("datastore");
		
		for(Map.Entry<String,String> e : testSources.entrySet()) {
			datastore.saveScriptSource(TEST_USER, e.getKey(), e.getValue());
		}
		
		//((DummyTransport)m).response = "response";
		
	}
	

	@Override
	protected void tearDown() throws Exception {
	}

	
	public void test_evalGet() throws IOException {
		
		ScriptProcess p = datastore.newProcess(TEST_USER, "evalget.js", false, "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("slept correctly", r instanceof Get);

		p.save();

		Get g = (Get) r;
		
		g.visit(new ScriptusFacade(datastore, c, m), p);
		
		p = datastore.getProcess(p.getPid());
		
		assertTrue("got content", p.getState() instanceof String);
		
		r = p.call();
		
		assertTrue("said correctly", r instanceof Say);
		
		System.out.println(((Say)r).getMsg());
		
//		assertTrue("contains time", ((Say)r).getMsg().contains("return new Date().clearTime();"));
	}



//	/**
//	 * tests for when child process finished before wait() is called
//	 */
//	public void test_wait() throws IOException {
//
//		final ScriptProcess p = datastore.newProcess(TEST_USER, "wait.js", "", "owner");
//		
//		p.save();
//		
//		ScriptAction r = p.call();
//
//		assertTrue("Forked correctly", r instanceof Fork);
//		
//		final ThreadLocal<Boolean> executedParentPostFork = new ThreadLocal<Boolean>();
//		final ThreadLocal<Boolean> executedParentPostWait = new ThreadLocal<Boolean>();
//		final ThreadLocal<Boolean> executedChild = new ThreadLocal<Boolean>();
//		
//		ProcessScheduler testScheduler = new ProcessSchedulerDelegate(c) {
//			
//			private UUID childPid;
//
//			@Override
//			public void execute(UUID pid) {
//				
//				if( ! pid.equals(p.getPid())) {
//					
//					executedChild.set(Boolean.TRUE);
//					
//					childPid = pid;
//					
//					super.execute(pid);
//
//					return;
//				}
//				
//				if(pid.equals(p.getPid())) {
//
//					if(Boolean.TRUE.equals(executedParentPostFork.get())) {
//						
//						executedParentPostWait.set(Boolean.TRUE);
//						
//						ScriptAction enfin = datastore.getProcess(pid).call();
//						
//						assertTrue("script finished", enfin instanceof Termination);
//						assertEquals("script result OK", "waitedfoo"+childPid, ((Termination)enfin).getResult());
//						
//					} else {
//
//						executedParentPostFork.set(Boolean.TRUE);
//						
//						ScriptProcess p2 = datastore.getProcess(pid);
//						
//						ScriptAction r2 = p2.call();
//						
//						p2.save();
//
//						assertTrue("Waited correctly", r2 instanceof Wait);
//
//						//pause thread until child has termination
//						
//						r2.visit(this, m, datastore, p2);
//
//					}
//
//				}
//				
//			}
//			
//		};
//		
//		r.visit(testScheduler, m, datastore, p);
//		
//		assertEquals("Executed child", Boolean.TRUE, executedChild.get());
//		assertEquals("Executed parent (post-fork)", Boolean.TRUE, executedParentPostFork.get());
//		assertEquals("Executed parent (post-wait)", Boolean.TRUE, executedParentPostWait.get());
//		
//	}

	
}
