package net.ex337.scriptus.tests;

import java.io.IOException;

import net.ex337.scriptus.datastore.ScriptusDatastore;

public class Testcase_BootSpring extends BaseTestCase {

	
	public void test_doNothing() throws IOException {
		ScriptusDatastore datastore = (ScriptusDatastore) appContext.getBean("datastore");
	}

}
