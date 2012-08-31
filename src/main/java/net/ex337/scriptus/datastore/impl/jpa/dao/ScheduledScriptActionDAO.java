package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name="tbl_scheduled_actions")
public class ScheduledScriptActionDAO {

    @Version
    @Column(name="version")
    public int version;

    @Column(name="pid")
    public String pid;
    
    @Column(name="nonce")
    public long nonce;

}
