
package net.ex337.scriptus.tests;

import junit.framework.TestCase;

import net.ex337.scriptus.config.ScriptusConfig;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base test case for running Scriptus tests that 
 * boots Spring. To specify a different configuration
 * file, override {@link #setUp()}, set the 
 * "scriptus.config" system property and then call
 * super.setUp.
 * 
 * @author ian
 *
 */
public abstract class BaseTestCase extends TestCase {

	protected AbstractApplicationContext appContext;

	public String getConfigFile() {
        return "spring/scriptus.xml";
	}

	@Override
	protected void setUp() throws Exception {
	    
	    ScriptusConfig c = new ScriptusConfig();
	    c.init();
	    
//	    System.setProperty("DatastoreType", c.getDatastoreType().toString());

	    appContext = new ClassPathXmlApplicationContext(new String[]{getConfigFile()}, false);
	    appContext.getEnvironment().getPropertySources().addFirst(c.new ScriptusConfigPropertySource("ScriptusConfig", c));
	    appContext.refresh();
	    
		
	}

	@Override
	protected void tearDown() throws Exception {
		appContext.close();
	}

	public ApplicationContext getAppContext() {
		return appContext;
	}
	
	
	
	

}
