package net.ex337.scriptus.tests;

import java.io.IOException;
import java.util.UUID;

import net.ex337.scriptus.config.ScriptusConfig;

public class Testcase_ScriptusConfig extends BaseTestCase {

    @Override
    public void setUp() throws Exception {
        
        System.setProperty("scriptus.config", "test-scriptus.properties");
//        ScriptusConfig.FORCE_CLEAN_INSTALL = true;

        super.setUp();
        
    }
	
	public void test_encryptDecrypt() throws IOException {
		
	    ScriptusConfig c = (ScriptusConfig) appContext.getBean("config");
		
	    String s = UUID.randomUUID().toString();
	    
	    String k = c.getLatestKeyId();
	    
	    assertEquals("latest key chosen", "999999999999", k);
	    
	    byte[] ct = c.encrypt(s, k);
	    
	    String pt = c.decrypt(ct, k);
	    
	    assertEquals("encrypt-decrypt OK", s, pt);
		
	}

}
