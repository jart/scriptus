package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ScriptIdDAO {

    @Column(name="script_name")
    public String name;
    @Column(name="user_id")
    public String userId;
    @Override
    public int hashCode() {
        return name.hashCode() % userId.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == null || ! (obj instanceof ScriptIdDAO)) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }
    
    

}
