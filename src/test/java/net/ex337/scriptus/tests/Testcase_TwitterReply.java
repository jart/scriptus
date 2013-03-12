package net.ex337.scriptus.tests;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TransportAccessToken;
import net.ex337.scriptus.model.api.functions.Ask;
import net.ex337.scriptus.model.api.functions.Listen;
import net.ex337.scriptus.scheduler.ProcessScheduler;
import net.ex337.scriptus.scheduler.ProcessSchedulerImpl;
import net.ex337.scriptus.transport.Transport;
import net.ex337.scriptus.transport.twitter.Tweet;
import net.ex337.scriptus.transport.twitter.TwitterClientMock;
import net.ex337.scriptus.transport.twitter.TwitterTransportImpl;

import org.apache.commons.lang.StringUtils;

/**
 * Tests the Scriptus API calls.
 * 
 * @author ian
 * 
 */
public class Testcase_TwitterReply extends BaseTestCase {

    private static final String TEST_USER = "test";
    private ProcessScheduler c;
    private ScriptusDatastore datastore;
    private Transport m;
    private ScriptusConfig config;

    private TwitterTransportImpl twitter;

    private TwitterClientMock clientMock;

    private static final Map<String, String> testSources = new HashMap<String, String>() {
        {
            put("ask.js",
                "var f = scriptus.ask(\"give me your number please\", {to:\"ianso\"}); " +
                "return f;");
            put("listen.js",
                "var s = scriptus.listen({timeout:\"1m\"});" +
                "return \"s=\"+s;");
        }
    };

    @Override
    protected void setUp() throws Exception {
        
        ProcessSchedulerImpl.EXECUTE_INLINE = true;

        System.setProperty("scriptus.config", "test-scriptus-twitter.properties");

        super.setUp();

        m = (Transport) appContext.getBean("transport");

        c = (ProcessScheduler) appContext.getBean("scheduler");

        datastore = (ScriptusDatastore) appContext.getBean("datastore");
        
        config = (ScriptusConfig) appContext.getBean("config");

        for (Map.Entry<String, String> e : testSources.entrySet()) {
            datastore.saveScriptSource(TEST_USER, e.getKey(), e.getValue());
        }

        clientMock = (TwitterClientMock) appContext.getBean("twitterClientMock");
        // ((DummyTransport)m).response = "response";
        
        twitter = (TwitterTransportImpl) appContext.getBean("twitterTransport");
        
        try{
            TransportAccessToken c = datastore.getAccessToken(TEST_USER, TransportType.Twitter);
        } catch(Exception e) {
            datastore.saveTransportAccessToken(new TransportAccessToken(TEST_USER, TransportType.Twitter, "token", "secret"));
        }
        

    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void test_ask() throws IOException {

        ScriptProcess p = datastore.newProcess(TEST_USER, "ask.js", false, "", "owner", TransportType.Twitter);

        ScriptAction r = p.call();

        assertTrue("Asked correctly", r instanceof Ask);
        assertTrue("Asked correctly foo", ((Ask) r).getWho().equals("ianso"));

        p.save();

        final ThreadLocal<String> tweetId = new ThreadLocal<String>();

        ScriptusFacade f = new ScriptusFacade(datastore, c, m, config) {

            @Override
            public void registerMessageCorrelation(MessageCorrelation cid) {
                tweetId.set(cid.getMessageId());
                super.registerMessageCorrelation(cid);
            }

        };

        r.visit(f, p); // sould say

        Set<MessageCorrelation> ccc = datastore.getMessageCorrelations(tweetId.get(), "ianso", TEST_USER, TransportType.Twitter);

        assertEquals("1 correlation", 1, ccc.size());

        assertEquals("correct pid registered", ccc.iterator().next().getPid(), ccc.iterator().next().getPid());
        assertEquals("correct user registered", "ianso", ccc.iterator().next().getFrom());

        Tweet t = new Tweet(123, "reply", "ianso", Long.parseLong(StringUtils.remove(tweetId.get(), "tweet:")));

        clientMock.getMentions().add(t);
        
        //should find & process the reply
        twitter.checkMessages();
       
        boolean found = false;
        
        for(String s : clientMock.statusUpdates) {
            if(s.equals("@owner reply")) {
                found = true; break;
            }
        }
       
        assertTrue("found reply sent to owner", found);
        
        //check that the next execution leads to normal termination with value "reply"
        

    }

    public void test_listen() throws IOException {

        ScriptProcess p = datastore.newProcess(TEST_USER, "listen.js", false, "", "owner", TransportType.Twitter);

        ScriptAction r = p.call();

        assertTrue("listened correctly", r instanceof Listen);
        assertTrue("listened correctly to no-one", ((Listen) r).getWho() == null);

        p.save();

//        final ThreadLocal<String> tweetId = new ThreadLocal<String>();

        ScriptusFacade f = new ScriptusFacade(datastore, c, m, config) {

            @Override
            public void registerMessageCorrelation(MessageCorrelation cid) {
//                tweetId.set(cid.getMessageId());
                super.registerMessageCorrelation(cid);
            }

        };

        r.visit(f, p); // sould say

        Set<MessageCorrelation> ccc = datastore.getMessageCorrelations(null, "ianso", TEST_USER, TransportType.Twitter);

        assertEquals("1 correlation", 1, ccc.size());

        assertEquals("correct pid registered", ccc.iterator().next().getPid(), ccc.iterator().next().getPid());
        assertEquals("correct user registered", null, ccc.iterator().next().getFrom());

        Tweet t = new Tweet(123, "reply", "ianso");

        clientMock.getMentions().add(t);
        
        //should find & process the reply
        twitter.checkMessages();
       
        boolean found = false;
        
        for(String s : clientMock.statusUpdates) {
            if(s.contains("s=reply")) {
                found = true; break;
            }
        }
       
        assertTrue("found reply sent to owner", found);
        
        //check that the next execution leads to normal termination with value "reply"
        

    }

}
