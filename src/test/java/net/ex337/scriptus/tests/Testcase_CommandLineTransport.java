package net.ex337.scriptus.tests;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.transport.impl.CommandLineTransport;
import twitter4j.TwitterException;

public class Testcase_CommandLineTransport extends BaseTestCase {
	
	CommandLineTransport i;
	
	ScriptusConfig conf;

	protected void setUp() throws Exception {

       System.setProperty("scriptus.config", "test-scriptus.properties");

	    super.setUp();
		
		conf = (ScriptusConfig) getAppContext().getBean("config");
		conf.setTransportType(TransportType.Twitter);
		
		i = (CommandLineTransport) getAppContext().getBean("cmdLineTransport");
		i.init();
		
		
	}
	
	public void testSay() throws TwitterException, InterruptedException {
		
		i.send("user", "robotoscriptu", "098765231");

      Thread.sleep(5000);

      i.send("user", "robotoscriptu", "FOOBAR");

		Thread.sleep(100000);
		
		assertTrue(true);
		
	}
	
	
}
