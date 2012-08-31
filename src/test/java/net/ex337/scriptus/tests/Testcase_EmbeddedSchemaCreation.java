package net.ex337.scriptus.tests;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.impl.jpa.embedded.ScriptusDatastoreEmbeddedDBImpl;
import net.ex337.scriptus.transport.impl.DummyTransport;

public class Testcase_EmbeddedSchemaCreation extends TestCase {

    ScriptusConfig c;
    
    @Override
    public void setUp() throws Exception {
        System.setProperty("scriptus.config", "test-scriptus-clean.properties");
        
        c = new ScriptusConfig();
        c.init();

    }
    
    @Override
    public void tearDown() {
        new File(c.getConfigLocation()).delete();
    }
    
	public void test_createSchema() throws IOException, SQLException {
	    
	    ScriptusDatastoreEmbeddedDBImpl d = new ScriptusDatastoreEmbeddedDBImpl();
	    
	    d.setConfig(c);
	    d.init();
	    
	}


}
