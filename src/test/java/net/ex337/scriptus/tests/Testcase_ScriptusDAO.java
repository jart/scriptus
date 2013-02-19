package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.datastore.impl.jpa.dao.LogMessageDAO;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ProcessListItem;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TransportAccessToken;
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
	    
//	    ScriptusConfig.FORCE_CLEAN_INSTALL = true;
	    
		System.setProperty("scriptus.config", "test-scriptus.properties");
//		System.setProperty("scriptus.config", "filesystem-based-scriptus.properties");
		
		super.setUp();
		
		datastore = (ScriptusDatastore) appContext.getBean("datastore");
		
		datastore.createSamples();
		
	}

	public void test_lifecycle() throws IOException {
		ScriptProcess newp = datastore.newProcess("test", "addTwoNumbers.js", true, "", "");
		newp.setArgs("foo bar");
		
		newp.save();
		
		ScriptProcess saved = datastore.getProcess(newp.getPid());
		
		assertEquals("pid same", newp.getPid(), saved.getPid());
		
		assertEquals("args same", newp.getArgs(), saved.getArgs());

        assertTrue("count processes", datastore.countRunningProcesses("test") >= 1);

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
        String f = "from:"+Math.abs(r.nextInt());
        String u = "user:"+Math.abs(r.nextInt());
		
		MessageCorrelation m = new MessageCorrelation(UUID.randomUUID(), f, c, System.currentTimeMillis(), TransportType.Dummy, u);
		
		datastore.registerMessageCorrelation(m);

		Set<MessageCorrelation> cc = datastore.getMessageCorrelations(c, f, u);
		
		assertTrue("correct pid returned", cc.contains(m));
		
		datastore.unregisterMessageCorrelation(m);
		
		assertTrue("nothing left", ! datastore.getMessageCorrelations(c, f, u).contains(m));

		//listen({to:"user", messageId:"foo"})
        MessageCorrelation both      = new MessageCorrelation(UUID.randomUUID(), f,    c,    System.currentTimeMillis(), TransportType.Dummy, u);
        //listen({to:"user"})
        MessageCorrelation byuser    = new MessageCorrelation(UUID.randomUUID(), f,    null, System.currentTimeMillis(), TransportType.Dummy, u);
        //listen({messageId:"foo"})
        MessageCorrelation messageId = new MessageCorrelation(UUID.randomUUID(), null, c,    System.currentTimeMillis(), TransportType.Dummy, u);
        //listen()
        MessageCorrelation byNull    = new MessageCorrelation(UUID.randomUUID(), null, null, System.currentTimeMillis(), TransportType.Dummy, u);

        MessageCorrelation byNullOtheruser    = new MessageCorrelation(UUID.randomUUID(), null, null, System.currentTimeMillis(), TransportType.Dummy, u+r.nextInt());

        datastore.registerMessageCorrelation(both);
        datastore.registerMessageCorrelation(byuser);
        datastore.registerMessageCorrelation(messageId);
        datastore.registerMessageCorrelation(byNull);
        datastore.registerMessageCorrelation(byNullOtheruser);
        
        Set<MessageCorrelation> cboth = datastore.getMessageCorrelations(c, f, u);
        Set<MessageCorrelation> cbyuser = datastore.getMessageCorrelations(null, f, u);

        assertTrue("user contains user", cbyuser.contains(byuser));
        assertTrue("userte contains null", cbyuser.contains(byNull));
        assertFalse("userte not contains null from other user", cbyuser.contains(byNullOtheruser));

        assertTrue("both contains both", cboth.contains(both));
        assertTrue("both contains msgid", cboth.contains(messageId));
        assertTrue("both contains null", cboth.contains(byNull));
        assertFalse("neither contains null other user", cboth.contains(byNullOtheruser));
        

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
	
	public void testChildren() {
	    
	    UUID parent = UUID.randomUUID();
	    UUID ch1 = UUID.randomUUID();
	    UUID ch2 = UUID.randomUUID();
	    UUID ch3 = UUID.randomUUID();
	    
        datastore.addChild(parent, ch1, 1);
        datastore.addChild(parent, ch2, 4);
        datastore.addChild(parent, ch3, 10);
	    
        List<UUID> ch = datastore.getChildren(parent);
        
        assertEquals("correct size", 3, ch.size());
        assertEquals("ch1 ok", ch1, ch.get(0));
        assertEquals("ch2 ok", ch2, ch.get(1));
        assertEquals("ch3 ok", ch3, ch.get(2));
        
        UUID l = datastore.getLastChild(parent);
        
        assertEquals("last pid ok", ch3, l);
        
        datastore.removeChild(parent, ch3);
        
        l = datastore.getLastChild(parent);
        
        assertEquals("last pid ok", ch2, l);
        
        ch = datastore.getChildren(parent);
        
        assertEquals("removed for good", 2, ch.size());
        
        assertTrue("see?", ! ch.contains(ch3));
	}
	
	public void testProcessListItem() {
	    
	    String uid = UUID.randomUUID().toString();
	    
	    ScriptProcess p = datastore.newProcess(uid, "addTwoNumbers.js", false, "aarfgs", uid);
	    p.setSource("");
	    datastore.writeProcess(p);
	    
	    List<ProcessListItem> i = datastore.getProcessesForUser(uid);
	    
	    assertEquals("good size", 1, i.size());
	    
	    ProcessListItem l = i.get(0);
	    
	    assertEquals("good pid", p.getPid(), l.getPid());
	    assertEquals("uid", uid, l.getUid());
	}
	
	public void testScriptEditing() {
	    
	    String uid = UUID.randomUUID().toString();
        String name = UUID.randomUUID().toString();
        String src = UUID.randomUUID().toString();
	    
	    datastore.saveScriptSource(uid, name, src);
	    
	    Set<String> s = datastore.listScripts(uid);
	    
	    assertTrue("script found", s.contains(name)); 
	    
	    String retrievedSrc = datastore.loadScriptSource(uid, name);
	    
	    assertEquals("source saved OK", src, retrievedSrc);
	    
	    datastore.saveScriptSource(uid, name+name, src+src);
	    
        retrievedSrc = datastore.loadScriptSource(uid, name+name);
        
        assertEquals("source saved OK again", src+src, retrievedSrc);
        
        datastore.deleteScript(uid, name+name);
        
        s = datastore.listScripts(uid);
        
        assertFalse("script deleted", s.contains(name+name));
        
        assertTrue("count scripts", datastore.countSavedScripts(uid) >= 1 );

	}
	
	public void testAccessTokens() {
	    
	    String uid = UUID.randomUUID().toString();

	    TransportAccessToken t = new TransportAccessToken(uid, TransportType.Dummy, "accessToken"+uid, "accessSecret"+uid);
	    
	    datastore.saveTransportAccessToken(t);
	    
	    assertTrue("found new token", datastore.getInstalledTransports(uid).contains(TransportType.Dummy));
	    
	    datastore.deleteTransportAccessToken(uid, TransportType.Dummy);

	    assertFalse("deleted new token", datastore.getInstalledTransports(uid).contains(TransportType.Dummy));

        datastore.saveTransportAccessToken(t);
        
        TransportAccessToken tt = datastore.getAccessToken(uid, TransportType.Dummy);
        
        assertEquals("access secret OK", t.getAccessSecret(), tt.getAccessSecret());
        assertEquals("access token OK", t.getAccessToken(), tt.getAccessToken());
        
	}
	
	public void testGetListeningCorrelations() {
	    
        String tt = UUID.randomUUID().toString();
        String u1 = UUID.randomUUID().toString();
        String u2 = UUID.randomUUID().toString();
	    
	    MessageCorrelation d1 = new MessageCorrelation(
	            UUID.randomUUID(),
	            "from1", "mid1", System.currentTimeMillis(), TransportType.Dummy, u1);

	    datastore.registerMessageCorrelation(d1);

        MessageCorrelation d2 = new MessageCorrelation(
                UUID.randomUUID(),
                "from2", "mid2", System.currentTimeMillis(), TransportType.Dummy, u2);

        datastore.registerMessageCorrelation(d2);
        
        MessageCorrelation d3 = new MessageCorrelation(
                UUID.randomUUID(),
                "from3", "mid3", System.currentTimeMillis(), TransportType.Dummy, u2);

        datastore.registerMessageCorrelation(d3);
        
	    List<String> uids = datastore.getListeningCorrelations(TransportType.Dummy);
	    
	    assertTrue("our mcs found", uids.size() > 2);
        assertTrue("found u1", uids.contains(u1));
        assertTrue("found u2", uids.contains(u2));
        
        assertTrue("u2 only once", uids.indexOf(u2) == uids.lastIndexOf(u2));
	    
        datastore.unregisterMessageCorrelation(d1);
        datastore.unregisterMessageCorrelation(d2);
	    
	}

    public void testLogMessages() {
        
        String uid = UUID.randomUUID().toString();
        String msg = UUID.randomUUID().toString();
        UUID pid = UUID.randomUUID();
        
        datastore.saveLogMessage(pid, uid, msg);
        
        List<LogMessageDAO> l = datastore.getLogMessages(uid);
        
        assertEquals("found msg", 1, l.size());
        assertEquals("msg OP", msg, l.get(0).message);
        
        datastore.deleteLogMessage(l.get(0).id.id, uid);
        
        l = datastore.getLogMessages(uid);
        
        assertTrue("empty list now", l.isEmpty());
        
    }
    
}
