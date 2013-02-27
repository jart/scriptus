package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="scriptus.tbl_log")
public class LogMessageDAO {

    @EmbeddedId
    public LogMessageDAOId id;

    @Column(name="message")
    public String message;
    
    @Column(name="created")
    public long created;

    @Column(name="pid")
    public String pid;

}
