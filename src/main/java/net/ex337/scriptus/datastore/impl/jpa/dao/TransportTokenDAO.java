package net.ex337.scriptus.datastore.impl.jpa.dao;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="scriptus.tbl_transport_access_tokens")
public class TransportTokenDAO {
    
    @EmbeddedId
    public TransportTokenIdDAO id;
    @Version
    @Column(name="version")
    public int version;
    
    @Column(name="version")
    public String keyId;

    @Column(name="access_token")
    public byte[] accessToken;

    @Column(name="access_secret")
    public byte[] accessSecret;
   
}
