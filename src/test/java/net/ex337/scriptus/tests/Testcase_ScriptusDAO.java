package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
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
		
		System.setProperty("scriptus.config", "test-scriptus.properties");
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
		
		Random r = new Random();
		
		String c = "tweet:"+Math.abs(r.nextInt());
		String u = "user:"+Math.abs(r.nextInt());
		
		MessageCorrelation m = new MessageCorrelation(UUID.randomUUID(), u, c, System.currentTimeMillis());
		
		datastore.registerMessageCorrelation(m);

		Set<MessageCorrelation> cc = datastore.getMessageCorrelations(c, u);
		
		assertTrue("correct pid returned", cc.contains(m));
		
		datastore.unregisterMessageCorrelation(m);
		
		assertTrue("nothing left", ! datastore.getMessageCorrelations(c, u).contains(m));

		//listen({to:"user", messageId:"foo"})
        MessageCorrelation both      = new MessageCorrelation(UUID.randomUUID(), u,    c,    System.currentTimeMillis());
        //listen({to:"user"})
        MessageCorrelation byuser    = new MessageCorrelation(UUID.randomUUID(), u,    null, System.currentTimeMillis());
        //listen({messageId:"foo"})
        MessageCorrelation messageId = new MessageCorrelation(UUID.randomUUID(), null, c,    System.currentTimeMillis());
        //listen()
        MessageCorrelation byNull    = new MessageCorrelation(UUID.randomUUID(), null, null, System.currentTimeMillis());
        
        datastore.registerMessageCorrelation(both);
        datastore.registerMessageCorrelation(byuser);
        datastore.registerMessageCorrelation(messageId);
        datastore.registerMessageCorrelation(byNull);
        
        Set<MessageCorrelation> cboth = datastore.getMessageCorrelations(c, u);
        Set<MessageCorrelation> cbyuser = datastore.getMessageCorrelations(null, u);

        assertTrue("user contains user", cbyuser.contains(byuser));
        assertTrue("user contains null", cbyuser.contains(byNull));

        assertTrue("both contains both", cboth.contains(both));
        assertTrue("both contains msgid", cboth.contains(messageId));
        assertTrue("both contains null", cboth.contains(byNull));
        

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
	
	
	public void testCursors() {
	    
	    UUID s = UUID.randomUUID();
	    
	    datastore.updateTransportCursor(TransportType.CommandLine, s.toString());
	    
	    UUID t  = UUID.fromString(datastore.getTransportCursor(TransportType.CommandLine));
	    
	    assertEquals("cursor updated", s, t);
	    
	}
	
}
