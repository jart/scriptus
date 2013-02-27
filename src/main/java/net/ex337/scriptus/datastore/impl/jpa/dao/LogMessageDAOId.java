package net.ex337.scriptus.datastore.impl.jpa.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LogMessageDAOId implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -230461017194734319L;
    
    public LogMessageDAOId(){
        
    }

    public LogMessageDAOId(String logId, String openid) {
        this.id = logId;
        this.userId = openid;
    }
    @Column(name="id")
    public String id;
    @Column(name="user_id")
    public String userId;

}
