package net.ex337.scriptus.tests;

import java.io.IOException;

import net.ex337.scriptus.dao.ScriptusDAO;

public class Testcase_BootSpring extends BaseTestCase {

	
	public void test_doNothing() throws IOException {
		ScriptusDAO dao = (ScriptusDAO) appContext.getBean("dao");
	}

}
