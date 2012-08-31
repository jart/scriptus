package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name="tbl_process")
public class ProcessDAO {
    
    @Column(name="pid")
    public String pid;
    @Version
    @Column(name="version")
    public int version;
    @Column(name="waiting_pid")
    public String waitingPid;
    
    @Column(name="user_id")
    public String userId;
    @Column(name="source")
    public String source;
    @Column(name="id_source")
    public int id_source;
    @Column(name="args")
    public String args;
    @Column(name="owner")
    public String owner;
    @Column(name="state")
    public byte[] state;
    @Column(name="compiled")
    public byte[] compiled;
    @Column(name="root")
    public boolean isRoot;
    
    @Column(name="continuation")
    public byte[] continuation;
    @Column(name="global_scope")
    public byte[] globalScope;

    
}
