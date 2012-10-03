package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="tbl_scheduled_actions")
public class ScheduledScriptActionDAO {

    @Version
    @Column(name="version")
    public int version;

    @Id
    @Column(name="pid")
    public String pid;
    
    @Column(name="action")
    public String action;
    
    @Column(name="nonce")
    public long nonce;

    @Column(name="when")
    public long when;

}
