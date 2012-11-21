package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="scriptus.tbl_script")
public class ScriptDAO {

    @Column(name="script_src")
    public byte[] source;
    @EmbeddedId
    public ScriptIdDAO id;
    @Version
    @Column(name="version")
    public int version;

}
