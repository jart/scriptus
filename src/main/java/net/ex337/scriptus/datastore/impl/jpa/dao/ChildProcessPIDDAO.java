package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name="tbl_process_child")
public class ChildProcessPIDDAO {

    public ChildProcessPIDDAO() {
        
    }

    public ChildProcessPIDDAO(ProcessDAO parent, String child) {
        super();
        this.child = child;
        this.parent = parent;
    }

    @Column(name="child")
    public String child;
    
    @ManyToOne()
    @Column(name="parent")
    public ProcessDAO parent;

    
}
