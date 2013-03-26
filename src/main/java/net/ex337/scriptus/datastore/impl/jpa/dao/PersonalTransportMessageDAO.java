package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="scriptus.tbl_personal_msg")
public class PersonalTransportMessageDAO {
    
    @Id
    @Column(name="id")
    public String id;
    @Column(name="parent")
    public String parent;
    @Column(name="message")
    public String message;
    @Column(name="msg_from")
    public String from;
    @Column(name="userId")
    public String userId;
    @Column(name="created")
    public long created;

}
