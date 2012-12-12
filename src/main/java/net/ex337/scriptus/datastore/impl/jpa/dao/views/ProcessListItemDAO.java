package net.ex337.scriptus.datastore.impl.jpa.dao.views;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="scriptus.v_proclist")
public class ProcessListItemDAO {

    @Id
    @Column(name="pid")
    public String pid;
    
    @Column(name="user_id")
    public String uid;
    
    @Column(name="version")
    public int version;
    
    @Column(name="state_label")
    public String stateLabel;
    
    @Column(name="id_source")
    public String sourceName;
    
    @Column(name="size")
    public int sizeOnDisk;
    
    @Column(name="created")
    public long created;

    @Column(name="lastmod")
    public long lastmod;

    @Column(name="alive")
    public boolean alive;

}
