package net.ex337.scriptus.datastore.impl.jpa.dao;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="tbl_process")
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
    @Column(name="source")
    public String source;
    @Column(name="sourceId")
    public String sourceId;
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

    @OneToMany(mappedBy="parent")
    public List<ChildProcessPIDDAO> children;
}
