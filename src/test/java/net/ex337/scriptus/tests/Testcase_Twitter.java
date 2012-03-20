package net.ex337.scriptus.tests;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.transport.twitter.TwitterTransportImpl;
import twitter4j.TwitterException;

public class Testcase_Twitter extends BaseTestCase {
	
	TwitterTransportImpl i;
	
	ScriptusConfig conf;

	protected void setUp() throws Exception {
		super.setUp();
		
		conf = (ScriptusConfig) getAppContext().getBean("config");
		conf.setTransportType(TransportType.Twitter);
		
		i = (TwitterTransportImpl) getAppContext().getBean("twitterTransport");
		i.init();
		
		
	}
	
	public void testSay() throws TwitterException {
		
		i.send("robotoscriptu", "098765231");
		
		assertTrue(true);
		
	}
	
	
	public void testBitFiddling() {
		
		long snowflakeid = 105727536104865792L;
	
		System.out.println(Long.toBinaryString(snowflakeid));
		
		//9223372036850581504
		//=111111111111111111111111111111111111111110000000000000000000000
		//(i.e. max time for 41-bit timestamp in snowflake
		//0x3FFFFF == non-timestamp stuff		
		snowflakeid &= ~(0x3FFFFF);
		
		snowflakeid = snowflakeid >> 22;

		System.out.println(Long.toBinaryString(snowflakeid));

		//round down
		long second = (snowflakeid - (snowflakeid % 1000)) / 1000L;
		
		//10-second window - one order of magnitude
		//outside of the 1-second window for twitters
		//k-sorted tweets
		second -= 10;
		
		System.out.println("snowflakeid="+snowflakeid);
		System.out.println("second="+second);

		System.out.println(Long.parseLong("111111111111111111111111111111111111111110000000000000000000000", 2));
	}
	
}
