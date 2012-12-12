package net.ex337.scriptus.model;

import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.impl.jpa.dao.TransportTokenDAO;

public class TransportAccessToken {

    @Resource
    private ScriptusConfig config;
    
    private String userId;
    private String keyId;
    private TransportType transport;
    private transient String accessToken;
    private transient String accessSecret;
    
    public TransportAccessToken(TransportTokenDAO dao) {
        
        this.userId = dao.id.userId;
        this.transport = TransportType.valueOf(dao.id.transport);
        if(dao.accessToken != null){
            this.accessToken = config.decrypt(dao.accessToken, keyId);
        }
        if(dao.accessSecret != null) {
            this.accessSecret = config.decrypt(dao.accessSecret, keyId);
        }
    }

}
