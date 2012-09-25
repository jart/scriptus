package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name="tbl_message_correlation")
public class MessageCorrelationDAO {

    @Id
    @Column(name="pid")
    public String pid;
    @Column(name="user_id")
    public String user;
    @Column(name="message_id")
    public String messageId;
    @Column(name="timestamp")
    public long timestamp;
    @Version
    @Column(name="version")
    public int version;

}
