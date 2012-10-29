package net.ex337.scriptus.model;

import java.util.Date;
import java.util.UUID;

public class ProcessListItem {

    private UUID pid;
    private String uid;
    private String stateLabel;
    private String sourceName;
    private int version;
    private int size;
    private Date created;
    private Date lastmod;
    
    public ProcessListItem(String pid, String uid, String stateLabel, String sourceName, int version, int size, long created, long lastmod) {
        this.pid = UUID.fromString(pid);
        this.uid = uid;
        this.stateLabel = stateLabel;
        this.sourceName = sourceName;
        this.version = version;
        this.size = size;
        this.created = new Date(created);
        this.lastmod = new Date(lastmod);
    }

    public UUID getPid() {
        return pid;
    }

    public void setPid(UUID pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStateLabel() {
        return stateLabel;
    }

    public void setStateLabel(String stateLabel) {
        this.stateLabel = stateLabel;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastmod() {
        return lastmod;
    }

    public void setLastmod(Date lastmod) {
        this.lastmod = lastmod;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

}
