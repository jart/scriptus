
package net.ex337.scriptus.tests;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.Dao;
import net.ex337.scriptus.config.ScriptusConfig.Medium;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class BaseTestCase extends TestCase {

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
