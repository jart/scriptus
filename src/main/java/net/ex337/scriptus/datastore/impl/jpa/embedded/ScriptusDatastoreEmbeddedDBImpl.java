package net.ex337.scriptus.datastore.impl.jpa.embedded;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.impl.jpa.ScriptusDatastoreJPAImpl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class ScriptusDatastoreEmbeddedDBImpl extends ScriptusDatastoreJPAImpl {
    
    @Resource
    private DataSource embeddedDB;
    
    @Resource
    private TransactionTemplate txTemplate;

    private static final Log LOG = LogFactory.getLog(ScriptusDatastoreEmbeddedDBImpl.class);

    @Resource
    private ScriptusConfig config;

    @PostConstruct
    public void init() throws SQLException, IOException {

        File configFile = new File(config.getConfigLocation()).getAbsoluteFile();
        
        //fFIXME configlocation is thee  config file path
//        System.setProperty("derby.system.home", configFile.getParent());
        
//        new jdbcte

        if(config.isCleanInstall()) {

            
            
            Connection conn = null;
            Statement s = null;
            
            
            try{
                conn = embeddedDB.getConnection();
                
//                conn.setAutoCommit(false);
                
                String schema = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/scriptus.derbydb.sql"), ScriptusConfig.CHARSET_STR);
                
                s = conn.createStatement();
                
                for(String st : StringUtils.split(schema, ";")) {
                    
                    if(StringUtils.isEmpty(st.trim())) {
                        continue;
                    }
                    LOG.debug(st);
                    s.execute(st.trim());
                }
                
            } catch(SQLException e) {
                throw e;
            } finally {
                
                if(s != null){
                    s.close();
                }
                if(conn != null) {
                    conn.close();
                }
            }
            
            txTemplate.execute(new TransactionCallback<Void>() {

                @Override
                public Void doInTransaction(TransactionStatus status) {
                    ScriptusDatastoreEmbeddedDBImpl.this.createSamples();
                    return null;
                }
                
            });

        } else {
            
            //setup normal connection
            
        }


        
    }

    @PreDestroy
    public void destroy() {
        try {
            // the shutdown=true attribute shuts down Derby
            DriverManager.getConnection("jdbc:derby:;shutdown=true");

            // To shut down a specific database only, but keep the
            // engine running (for example for connecting to other
            // databases), specify a database in the connection URL:
            // DriverManager.getConnection("jdbc:derby:" + dbName +
            // ";shutdown=true");
        } catch (SQLException se) {
            if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {
                // OK
            } else {
                LOG.error("did not shut down normally", se);
            }
        }
    }

    public ScriptusConfig getConfig() {
        return config;
    }

    public void setConfig(ScriptusConfig config) {
        this.config = config;
    }

}
