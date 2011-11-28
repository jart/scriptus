package net.ex337.scriptus.tests;

import java.io.IOException;

import net.ex337.scriptus.transport.impl.DummyTransport;

public class Testcase_DummyTransport extends BaseTestCase {

    @Override
    protected void setUp() throws Exception {
        System.setProperty("scriptus.config", "test-scriptus.properties");
        
        super.setUp();
    }
	
    
	public void test_dummyTransport() throws IOException {
	    
	    DummyTransport dummy = (DummyTransport) appContext.getBean("dummyTransport");
	    
        assertEquals("straight text replacement", "ACK", dummy.getResponse("SYN"));
        assertEquals("regexp replacement", "CAT", dummy.getResponse("DIG"));
        assertEquals("variable replacement", "I have 1024 dogs", dummy.getResponse("I have 1024 cats"));
	    
	}


}
