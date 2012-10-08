package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="tbl_script")
public class ScriptDAO {

    @Id
    @Column(name="script_src")
    public byte[] source;
    @Column(name="script_name")
    public String name;
    @Column(name="user_id")
    public String userId;
    @Version
    @Column(name="version")
    public int version;

}
