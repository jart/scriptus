package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="scriptus.tbl_process_child")
public class ChildProcessPIDDAO {

    @Id
    @Column(name="child")
    public String child;
    
    @Column(name="parent")
    public String parent;

    @Column(name="seq")
    public int seq;

    
}
