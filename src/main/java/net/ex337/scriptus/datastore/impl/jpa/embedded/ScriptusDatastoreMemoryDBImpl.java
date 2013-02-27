package net.ex337.scriptus.datastore.impl.jpa.embedded;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

public abstract class ScriptusDatastoreMemoryDBImpl extends ScriptusDatastoreEmbeddedDBImpl {
    
    @PostConstruct
    public void init() throws SQLException, IOException {
        
        /*
         * does everything like embedded, but creates schema evey time.
         */
        super.createDBSchema();

        
    }
}
