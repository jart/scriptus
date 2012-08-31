package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Version;

@Table(name="tbl_cursors")
public class TransportCursorDAO {
  
    @Column(name="transport")
    public String transport;
    @Column(name="cursor_data")
    public String cursor;
    @Version
    @Column(name="version")
    public int version;

}
