package net.ex337.scriptus.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.ProcessSchedulerImpl;
import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.model.api.functions.Ask;
import net.ex337.scriptus.transport.Transport;
import net.ex337.scriptus.transport.twitter.Tweet;
import net.ex337.scriptus.transport.twitter.TwitterClientMock;
import net.ex337.scriptus.transport.twitter.TwitterTransportImpl;

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

    private TwitterTransportImpl twitter;

    private TwitterClientMock clientMock;

    private static final Map<String, String> testSources = new HashMap<String, String>() {
        {
            put("ask.js",
                    "var f = scriptus.ask(\"give me your number please\", {to:\"ianso\"}); return f;");
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

        for (Map.Entry<String, String> e : testSources.entrySet()) {
            datastore.saveScriptSource(TEST_USER, e.getKey(), e.getValue());
        }

        clientMock = (TwitterClientMock) appContext.getBean("twitterClientMock");
        // ((DummyTransport)m).response = "response";
        
        twitter = (TwitterTransportImpl) appContext.getBean("twitterTransport");

    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void test_ask() throws IOException {

        ScriptProcess p = datastore.newProcess(TEST_USER, "ask.js", "", "owner");

        ScriptAction r = p.call();

        assertTrue("Asked correctly", r instanceof Ask);
        assertTrue("Asked correctly foo", ((Ask) r).getWho().equals("ianso"));

        p.save();

        long nonce = ((Ask) r).getNonce();

        final ThreadLocal<Long> tweetId = new ThreadLocal<Long>();

        ScriptusFacade f = new ScriptusFacade(datastore, c, m) {

            @Override
            public void registerTwitterCorrelation(TwitterCorrelation cid) {
                tweetId.set(cid.getSourceSnowflake());
                super.registerTwitterCorrelation(cid);
            }

        };

        r.visit(f, p); // sould say

        List<Message> incomings = new ArrayList<Message>();

        TwitterCorrelation cc = datastore.getTwitterCorrelationByID(tweetId.get());

        assertEquals("correct pid registered", p.getPid(), cc.getPid());
        assertEquals("correct user registered", "ianso", cc.getUser());

        Tweet t = new Tweet(123, "reply", "ianso", tweetId.get());

        clientMock.getMentions().add(t);
        
        //should find & process the reply
        twitter.checkMessages();
       
//        boolean found = false;
//        
//        for(String s : clientMock.statusUpdates) {
//            if(s.equals("@owner reply")) {
//                found = true; break;
//            }
//        }
//       
//        assertTrue("found reply sent to owner", found);
        
        //check that the next execution leads to normal termination with value "reply"
        

    }

}
