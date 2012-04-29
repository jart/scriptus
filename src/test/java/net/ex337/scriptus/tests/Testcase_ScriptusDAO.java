package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;

/**
 * 
 * Tests the basics of the {@link ScriptusDatastore} interface contract.
 * If this test passes for the implementation, it's probably OK 
 * - if it doesn't, it's definitely not.
 * 
 * @author ian
 *
 */
public class Testcase_ScriptusDAO extends BaseTestCase {
	
	private ScriptusDatastore datastore;
	
	@Override
	protected void setUp() throws Exception {
		
//		System.setProperty("scriptus.config", "test-scriptus.properties");
//		System.setProperty("scriptus.config", "filesystem-based-scriptus.properties");
		
		super.setUp();
		
		datastore = (ScriptusDatastore) appContext.getBean("datastore");
		
		datastore.createTestSources();
		
	}

	public void test_lifecycle() throws IOException {
		ScriptProcess newp = datastore.newProcess("test", "addTwoNumbers.js", "", "");
		newp.setArgs("foo bar");
		
		newp.save();
		
		ScriptProcess saved = datastore.getProcess(newp.getPid());
		
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
		
//		String postfix = Integer.toString(this.hashCode());
		
		UUID pid = UUID.randomUUID();
		
		String c = "tweet:1";
		
		datastore.registerTwitterCorrelation(new TwitterCorrelation(pid, "user", c));

		assertEquals("correct pid returned", pid, datastore.getTwitterCorrelationByID(c).getPid());
		
		datastore.unregisterTwitterCorrelation("tweet:1");
		
		assertEquals(null, datastore.getTwitterCorrelationByID(c));

	}

	public void test_scheduleTask() throws IOException {
		
		ScriptusDatastore datastore = (ScriptusDatastore) appContext.getBean("datastore");
		
		Calendar then = Calendar.getInstance();
		then.add(Calendar.HOUR, 3);
		
		Wake w = new Wake(UUID.randomUUID(), 1234, then.getTimeInMillis());
		
		datastore.saveScheduledTask(w);
		
		List<ScheduledScriptAction> actions = datastore.getScheduledTasks(Calendar.getInstance());
		
		assertFalse("doesnt contain task in future", actions.contains(w));
		
		actions = datastore.getScheduledTasks(then);
		
		assertTrue("contains task in future",  actions.contains(w));
		
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
		
		actions = datastore.getScheduledTasks(then);
		
		assertTrue("list not empty",  ! actions.isEmpty());
		
		found = false;
		
		for(ScheduledScriptAction t : actions){
			if(t.equals(w)) {
				found = true;
				break;
			}
		}
		
		datastore.deleteScheduledTask(neww.getPid(), neww.getNonce());

		actions = datastore.getScheduledTasks(then);
		
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
		
		datastore.updateTwitterLastMentions(ll);
		
		List<Long> l = datastore.getTwitterLastMentions();
		
		assertEquals("correct length", 1, l.size());
		assertEquals("contents", 12345L, ((Long)l.get(0)).longValue());

		ll = new ArrayList<Long>();
		ll.add(54321L);

		datastore.updateTwitterLastMentions(ll);

		l = datastore.getTwitterLastMentions();
		
		assertEquals("correct length", 1, l.size());
		assertEquals("contents", 54321L, ((Long)l.get(0)).longValue());

	}
	
	public void testListeners() throws InterruptedException {
		
		UUID r = UUID.randomUUID();
		
		datastore.registerTwitterListener(r, "foo");
		
		Thread.sleep(200);
		
		UUID s = UUID.randomUUID();
		
		datastore.registerTwitterListener(s, "foo");
		
		UUID g = datastore.getMostRecentTwitterListener("foo");
		
		assertEquals("correct uuid returned", g, s);
		
		datastore.unregisterTwitterListener(g, "foo");

		g = datastore.getMostRecentTwitterListener("foo");
		
		assertEquals("correct uuid returned", g, r);

	}
	
}
