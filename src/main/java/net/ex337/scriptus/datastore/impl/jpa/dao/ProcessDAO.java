package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="scriptus.tbl_process")
public class ProcessDAO {

    @Id
    @Column(name="pid")
    public String pid;
    @Version
    @Column(name="version")
    public int version;
    @Column(name="waiting_pid")
    public String waitingPid;
    
    @Column(name="user_id")
    public String userId;

    @Lob
    @Column(name="source")
    public byte[] source;
    
    @Column(name="id_source")
    public String sourceId;
    
    @Column(name="args")
    public String args;
    
    @Column(name="owner")
    public String owner;
    
    @Lob
    @Column(name="state")
    public byte[] state;
    
    @Column(name="root")
    public boolean isRoot;
    
    @Column(name="alive")
    public boolean isAlive;
    
    @Lob
    @Column(name="script_state")
    public byte[] script_state;
    
    @Column(name="state_label")
    public String state_label;
    
    @Column(name="created")
    public long created;

    @Column(name="lastmod")
    public long lastmod;

}
