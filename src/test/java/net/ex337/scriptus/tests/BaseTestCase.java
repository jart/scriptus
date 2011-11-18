
package net.ex337.scriptus.tests;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

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

		appContext = new ClassPathXmlApplicationContext(getConfigFile());
		
	}

	@Override
	protected void tearDown() throws Exception {
		appContext.close();
	}

	public ApplicationContext getAppContext() {
		return appContext;
	}
	
	
	
	

}
