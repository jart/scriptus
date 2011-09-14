package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.interaction.impl.DummyInteractionMedium;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.functions.Ask;
import net.ex337.scriptus.model.api.functions.Fork;
import net.ex337.scriptus.model.api.functions.Listen;
import net.ex337.scriptus.model.api.functions.Say;
import net.ex337.scriptus.model.api.functions.Sleep;
import net.ex337.scriptus.model.api.output.ErrorTermination;
import net.ex337.scriptus.model.api.output.NormalTermination;

/**
 * Tests the Scriptus API calls.
 * 
 * @author ian
 *
 */
public class Testcase_ScriptusBasics extends BaseTestCase {

	private static final String TEST_USER = "test";
	private ProcessScheduler c;
	private ScriptusDAO dao;
	private InteractionMedium m;
	
	private static final Map<String,String> testSources = new HashMap<String,String>() {{
		put("return.js", "return \"result\";");
		put("syntaxError.js", "return nonexitent()");
		put("throw.js", "try {throw \"this is an error\"} catch(e) {throw (typeof e);}");
		put("fiddle.js", "scriptus.fork = function() {return \"not forking\";};return scriptus.fork()");
		put("fiddle2.js", "scriptus = {}; return scriptus.fork()");
		put("fork.js", "var pid = scriptus.fork(); return pid;");
		put("forkNoPrefix.js", "var pid = fork(); return pid;");
		put("exit.js", "function foo() {scriptus.exit(\"result\");} foo(); return \"bad result\"");
		put("sleepHour.js", "scriptus.sleep(3);");
		put("sleepDate.js", "scriptus.sleep(\"2012-9-11 10:00\");");
		put("sleepDuration.js", "sleep(\"1y 2M 3d 4h\");");
		put("sleepBadDuration.js", "sleep(\"1x\");");
		put("sleepBadDate.js", "scriptus.sleep(\"2012 10:00\");");
		put("wait.js", 
				"var pid = scriptus.fork(); " +
				"if(pid == 0) {" +
				"	return \"waited\"" +
				"} " +
				"return scriptus.wait(function(arg){return arg+\"foo\"});"
			);
		put("wait2.js", 
				"var pid = scriptus.fork(); " +
				"if(pid == 0) {" +
				"	return \"waited\"" +
				"} " +
				"return scriptus.wait(function() {});");

		put("ask.js", "var f = scriptus.ask(\"give me your number\", {to:\"foo\"}); if(f != \"response\") throw 1;");
		put("askTimeout.js", "var f = scriptus.ask(\"give me your number\", {to:\"foo\", timeout:3}); if(f != \"response\") throw 1;");
		put("defaultAsk.js", "var f = scriptus.ask(\"give me your number\"); if(f != \"response\") throw 1;");
		put("say.js", "var foo = scriptus.say(\"message\", {to:\"foo\"}); if(foo == null) throw 1;");
		put("defaultSay.js", "scriptus.say(\"message\"); if(foo == null) throw 1;");
		put("listen.js", "var foo = scriptus.listen({to:\"foo\"}); if(foo == null) throw 1;");
		put("defaultListen.js", "var foo = scriptus.listen(); if(foo == null) throw 1;");
		put("eval.js", "var foo = eval(\"function() {scriptus.listen({to:\\\"foo\\\"});}\")(); if(foo == null) throw 1;");
		put("breakSec.js", "java.lang.System.out.println(\"foo\");");
		put("breakSec2.js", "var s = \"foo\"; s.getClass().forName(\"java.lang.System\")");
		put("breakSec3.js", "java.lang.System.out.println(\"foo\");");
		put("breakSec4.js", "var s = new Date(); var e = typeof s; var t = new RegExp(\"\\\\w+\"); var e = typeof t");
		put("breakSec5.js", "try {throw \"exStr\";} catch(e) {var s = typeof e}");
	}};

	@Override
	protected void setUp() throws Exception {

		System.setProperty("scriptus.config", "test-scriptus.properties");
		
		super.setUp();
		
		m = (InteractionMedium) appContext.getBean("interaction");
		
		c = (ProcessScheduler) appContext.getBean("scheduler");
		
		dao = (ScriptusDAO) appContext.getBean("dao");
		
		for(Map.Entry<String,String> e : testSources.entrySet()) {
			dao.saveScriptSource(TEST_USER, e.getKey(), e.getValue());
		}
		
		//((DummyInteractionMedium)m).response = "response";
		
	}
	

	@Override
	protected void tearDown() throws Exception {
	}

	
	
	public void test_return() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "return.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Correct result", r instanceof NormalTermination);
		
		NormalTermination n = (NormalTermination) r;

		r.visit(c, m, dao, p); //sould say

		assertEquals("Correct result", "result", n.getResult());
		

	}

	public void test_syntaxError() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "syntaxError.js", "", "owner");
		
		ScriptAction r = p.call();

		assertTrue("Error termination", r instanceof ErrorTermination);
		
	}

	public void test_throwException() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "throw.js", "", "owner");
		
		ScriptAction r = p.call();

		assertTrue("Error termination", r instanceof ErrorTermination);
		
	}
	
	
	public void test_fiddleWithAPI() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "fiddle.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Didn't fork correctly", ! (r instanceof Fork));
	}

	public void test_fiddleWithAPI2() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "fiddle2.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("error condition", r instanceof ErrorTermination);
		
	}

	
	public void test_fork() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "fork.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Forked correctly", r instanceof Fork);
		
		r.visit(c, m, dao, p);

	}
	
	public void test_forkNoPrefix() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "forkNoPrefix.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Forked correctly", r instanceof Fork);
		
		r.visit(c, m, dao, p);

	}
	
	public void test_exit() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "exit.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Exited correctly", r instanceof NormalTermination);
		assertTrue("correct exit", ((NormalTermination)r).getResult().equals("result"));

	}

	public void test_sleepHour() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "sleepHour.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("slept correctly", r instanceof Sleep);

	}

	public void test_sleepDate() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "sleepDate.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("slept correctly", r instanceof Sleep);

	}

	public void test_sleepDuration() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "sleepDuration.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("slept correctly", r instanceof Sleep);

		Sleep s = (Sleep)r;
		
		Calendar target = Calendar.getInstance();
		target.add(Calendar.YEAR, 1);
		target.add(Calendar.MONTH, 2);
		target.add(Calendar.DATE, 3);
		target.add(Calendar.HOUR, 4);
		
		Calendar c = s.getUntil();
		
		assertEquals("good year", target.get(Calendar.YEAR), c.get(Calendar.YEAR));
		assertEquals("good month", target.get(Calendar.MONTH), c.get(Calendar.MONTH));
		assertEquals("good day", target.get(Calendar.DATE), c.get(Calendar.DATE));
		assertEquals("good hour", target.get(Calendar.HOUR), c.get(Calendar.HOUR));
		
	}

	public void test_sleepBadDuration() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "sleepBadDuration.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("error", r instanceof ErrorTermination);

		
	}

	public void test_sleepBadDate() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "sleepBadDate.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Forked correctly", r instanceof ErrorTermination);

	}

	public void test_wait() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "wait.js", "", "owner");
		
		ScriptAction r = p.call();

		assertTrue("Forked correctly", r instanceof Fork);

		r.visit(c, m, dao, p);
		
	}

	public void test_wait2() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "wait2.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Forked correctly", r instanceof Fork);
		
		r.visit(c, m, dao, p);
		//process and then
//				assertTrue("Waited correctly", r instanceof Wait);
	}

	public void test_ask() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "ask.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Asked correctly", r instanceof Ask);
		assertTrue("Asked correctly foo", ((Ask)r).getWho().equals("foo"));
		
		p.save();

		r.visit(c, m, dao, p);
		
	}

	public void test_defaultAsk() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "defaultAsk.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Asked correctly", r instanceof Ask);
		assertTrue("Asked correctly owner", ((Ask)r).getWho().equals("owner"));
		
		p.save();

		r.visit(c, m, dao, p);
		
	}


	public void test_askTimeout() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "askTimeout.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Asked correctly", r instanceof Ask);
		assertTrue("Asked correctly owner", ((Ask)r).getWho().equals("foo"));
		
		p.save();

		r.visit(c, m, dao, p);
		
	}

	public void test_say() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "say.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Said correctly", r instanceof Say);
		assertTrue("Said correctly to user", ((Say)r).getWho().equals("foo"));
		assertTrue("Said correctly message", ((Say)r).getMsg().equals("message"));
		
		p.save();
		
		r.visit(c, m, dao, p);
	}

	public void test_defaultSay() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "defaultSay.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Said correctly", r instanceof Say);
		assertTrue("Said to owner correctly", ((Say)r).getWho().equals("owner"));

	}

	public void test_listen() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "listen.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Listened correctly", r instanceof Listen);
		assertTrue("Listened correctly to", ((Listen)r).getWho().equals("foo"));
		
		p.save();
		
		r.visit(c, m, dao, p);
	}

	public void test_defaultListen() throws IOException {

		ScriptProcess p = dao.newProcess(TEST_USER, "defaultListen.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Listened correctly", r instanceof Listen);
		assertTrue("Listened correctly to", ((Listen)r).getWho().equals("owner"));
		
		p.save();
		
		r.visit(c, m, dao, p);
	}

	public void test_eval() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "eval.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Listened correctly", r instanceof Listen);

		p.save();

		r.visit(c, m, dao, p);
	}

	public void test_addTwoNumbers() throws IOException {

		((DummyInteractionMedium)m).response = "4";

		ScriptProcess p = dao.newProcess(TEST_USER, "addTwoNumbers.js", "", "owner");
		
		ScriptAction r = p.call();

		p.save();

		assertTrue("First correctly", r instanceof Fork);
		
		//everything else should happen immediately with mocks
		r.visit(c, m, dao, p);
		
		((DummyInteractionMedium)m).response = "response";
	}

	public void test_breakSecurity() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "breakSec.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Failed correctly", r instanceof ErrorTermination);
		
		System.out.println(((ErrorTermination)r).getError());
		
		r.visit(c, m, dao, p);
	}

	public void test_breakSecurity2() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "breakSec2.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Failed correctly", r instanceof ErrorTermination);
		
		System.out.println(((ErrorTermination)r).getError());
		
		r.visit(c, m, dao, p);
	}

	public void test_breakSecurity3() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "breakSec3.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Failed correctly", r instanceof ErrorTermination);
		
		System.out.println(((ErrorTermination)r).getError());
		
		r.visit(c, m, dao, p);
	}

	public void test_breakSecurity4() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "breakSec4.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Failed correctly", r instanceof ErrorTermination);
		
		System.out.println(((ErrorTermination)r).getError());
		
		r.visit(c, m, dao, p);
	}

	public void test_breakSecurity5() throws IOException {
		
		ScriptProcess p = dao.newProcess(TEST_USER, "breakSec5.js", "", "owner");
		
		ScriptAction r = p.call();
		
		assertTrue("Failed correctly", r instanceof ErrorTermination);
		
		System.out.println(((ErrorTermination)r).getError());
		
		r.visit(c, m, dao, p);
	}
	
	
}
