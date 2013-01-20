package net.ex337.scriptus.model;

import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.impl.jpa.dao.TransportTokenDAO;

public class TransportAccessToken {

    @Resource
    private ScriptusConfig config;
    
    private String userId;
    private TransportType transport;
    private transient String accessToken;
    private transient String accessSecret;
    
    public TransportAccessToken(String userId, TransportType transport, String accessToken, String accessSecret) {
        super();
        this.userId = userId;
        this.transport = transport;
        this.accessToken = accessToken;
        this.accessSecret = accessSecret;
    }

    public TransportAccessToken() {
        
    }
    
    public void load(TransportTokenDAO dao) {
        this.userId = dao.id.userId;
        this.transport = TransportType.valueOf(dao.id.transport);
        if(dao.accessToken != null){
            this.accessToken = config.decrypt(dao.accessToken, dao.keyId);
        }
        if(dao.accessSecret != null) {
            this.accessSecret = config.decrypt(dao.accessSecret, dao.keyId);
        }
    }

    public String getUserId() {
        return userId;
    }

    public TransportType getTransport() {
        return transport;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessSecret() {
        return accessSecret;
    }
    
    

}
