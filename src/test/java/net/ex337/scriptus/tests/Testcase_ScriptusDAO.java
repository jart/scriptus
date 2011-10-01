package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;

/**
 * 
 * Tests the basics of the {@link ScriptusDAO} interface contract.
 * If this test passes for the implementation, it's probably OK 
 * - if it doesn't, it's definitely not.
 * 
 * @author ian
 *
 */
public class Testcase_ScriptusDAO extends BaseTestCase {
	
	private ScriptusDAO dao;
	
	@Override
	protected void setUp() throws Exception {
		
		System.setProperty("scriptus.config", "test-scriptus.properties");
//		System.setProperty("scriptus.config", "filesystem-based-scriptus.properties");
		
		super.setUp();
		
		dao = (ScriptusDAO) appContext.getBean("dao");
		
		dao.createTestSources();
		
	}

	public void test_lifecycle() throws IOException {
		ScriptProcess newp = dao.newProcess("test", "addTwoNumbers.js", "", "");
		newp.setArgs("foo bar");
		
		newp.save();
		
		ScriptProcess saved = dao.getProcess(newp.getPid());
		
		assertEquals("pid same", newp.getPid(), saved.getPid());
		
		assertEquals("args same", newp.getArgs(), saved.getArgs());
		
	}
	
	public void test_uuid() throws IOException, ClassNotFoundException, InterruptedException {
		
		UUID r = UUID.randomUUID();
		
		byte[] out = SerializableUtils.serialiseObject(r);
		
		Thread.sleep(1000);
		
		UUID i = (UUID) SerializableUtils.deserialiseObject(out);
		
		assertEquals("UUID serialisation isn't playing silly buggers", r, i);
		
		
	}
	
	public void testCorrelationIDs() throws InterruptedException {
		
		String postfix = Integer.toString(this.hashCode());
		
		UUID pid = UUID.randomUUID();
		
		String cid = "cid"+postfix;
		
		dao.registerTwitterCorrelation(new TwitterCorrelation(pid, "user", cid, 123));

		assertEquals("correct pid returned", pid, dao.getTwitterCorrelationByID(cid).getPid());
		
		dao.unregisterTwitterCorrelation(cid);
		
		assertEquals(null, dao.getTwitterCorrelationByID(cid));

	}

	public void test_scheduleTask() throws IOException {
		
		ScriptusDAO dao = (ScriptusDAO) appContext.getBean("dao");
		
		Calendar then = Calendar.getInstance();
		then.add(Calendar.HOUR, 3);
		
		Wake w = new Wake(UUID.randomUUID(), 1234);
		
		dao.scheduleTask(then, w);
		
		List<ScheduledScriptAction> actions = dao.getScheduledTasks(Calendar.getInstance());
		
		assertTrue("no actions in list", actions.isEmpty());
		
		actions = dao.getScheduledTasks(then);
		
		assertTrue("list not empty",  ! actions.isEmpty());
		
		boolean found = false;
		
		Wake neww = null;
		
		for(ScheduledScriptAction t : actions){
			if(t.equals(w)) {
				neww = (Wake) t;
				found = true;
				break;
			}
		}
		
		assertTrue("retrieved task", found);
		
		then.add(Calendar.HOUR, 1);
		
		actions = dao.getScheduledTasks(then);
		
		assertTrue("list not empty",  ! actions.isEmpty());
		
		found = false;
		
		for(ScheduledScriptAction t : actions){
			if(t.equals(w)) {
				found = true;
				break;
			}
		}
		
		dao.deleteScheduledTask(neww);

		actions = dao.getScheduledTasks(then);
		
		found = false;
		
		for(ScheduledScriptAction t : actions){
			if(t.equals(w)) {
				found = true;
				break;
			}
		}
		
		assertFalse("task deleted", found);

	}

	public void testLastMentions() {
		
		List<Long> ll = new ArrayList<Long>();
		ll.add(12345L);
		
		dao.updateTwitterLastMentions(ll);
		
		List<Long> l = dao.getTwitterLastMentions();
		
		assertEquals("correct length", 1, l.size());
		assertEquals("contents", 12345L, ((Long)l.get(0)).longValue());

		ll = new ArrayList<Long>();
		ll.add(54321L);

		dao.updateTwitterLastMentions(ll);

		l = dao.getTwitterLastMentions();
		
		assertEquals("correct length", 1, l.size());
		assertEquals("contents", 54321L, ((Long)l.get(0)).longValue());

	}
	
	public void testListeners() throws InterruptedException {
		
		UUID r = UUID.randomUUID();
		
		dao.registerTwitterListener(r, "foo");
		
		Thread.sleep(200);
		
		UUID s = UUID.randomUUID();
		
		dao.registerTwitterListener(s, "foo");
		
		UUID g = dao.getMostRecentTwitterListener("foo");
		
		assertEquals("correct uuid returned", g, s);
		
		dao.unregisterTwitterListener(g, "foo");

		g = dao.getMostRecentTwitterListener("foo");
		
		assertEquals("correct uuid returned", g, r);

	}
	
}
