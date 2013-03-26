package net.ex337.scriptus.tests;

import java.util.UUID;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;

public class Testcase_EmbeddedSchemaCreation extends BaseTestCase {

    private ScriptusDatastore datastore;

    @Override
    public void setUp() throws Exception {
        
        ScriptusConfig.FORCE_CLEAN_INSTALL = true;
        
        System.setProperty("scriptus.config", "test-scriptus.properties");
        
        super.setUp();

        datastore = (ScriptusDatastore) appContext.getBean("datastore");

    }
    
    @Override
    public void tearDown() {

    }
    
    public void testCursors() {
        
        UUID s = UUID.randomUUID();
        
        datastore.updateTransportCursor(TransportType.Dummy, s.toString());
        
        UUID t  = UUID.fromString(datastore.getTransportCursor(TransportType.Dummy));
        
        assertEquals("cursor updated", s, t);
        
    }


}
