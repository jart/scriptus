package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name="tbl_message_correlation")
public class MessageCorrelationDAO {

    @Column(name="pid")
    private String pid;
    @Column(name="user")
    private String user;
    @Column(name="message_id")
    private String messageId;
    @Column(name="timestamp")
    private long timestamp;
    @Version
    @Column(name="version")
    private int version;

}
