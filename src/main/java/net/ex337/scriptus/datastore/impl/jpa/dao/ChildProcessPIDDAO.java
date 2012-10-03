package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="tbl_process_child")
public class ChildProcessPIDDAO {

    public ChildProcessPIDDAO() {
        
    }

    public ChildProcessPIDDAO(ProcessDAO parent, String child) {
        super();
        this.child = child;
        this.parent = parent;
    }

    @Id
    @Column(name="child")
    public String child;
    
    @ManyToOne()
    public ProcessDAO parent;

    
}
