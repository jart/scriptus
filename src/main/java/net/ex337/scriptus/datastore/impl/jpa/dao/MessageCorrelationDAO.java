package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="scriptus.tbl_message_correlation")
public class MessageCorrelationDAO {

    @Id
    @Column(name="pid")
    public String pid;
    @Column(name="user_id")
    public String userId;
    @Column(name="from_id")
    public String from;
    @Column(name="transport")
    public String transport;
    @Column(name="message_id")
    public String messageId;
    @Column(name="timestamp")
    public long timestamp;
    @Version
    @Column(name="version")
    public int version;

}
