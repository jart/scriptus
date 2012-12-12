package net.ex337.scriptus.datastore.impl.jpa.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TransportTokenIdDAO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4080478199373970674L;

    @Column(name="transport")
    public String transport;
    @Column(name="user_id")
    public String userId;
    @Override
    public int hashCode() {
        return transport.hashCode() % userId.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == null || ! (obj instanceof TransportTokenIdDAO)) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }
    
    

}
