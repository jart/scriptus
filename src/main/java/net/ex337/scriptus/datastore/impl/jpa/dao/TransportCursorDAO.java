package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="tbl_cursors")
public class TransportCursorDAO {

    @Id
    @Column(name="transport")
    public String transport;
    @Column(name="cursor_data")
    public String cursor;
    @Version
    @Column(name="version")
    public int version;

}
