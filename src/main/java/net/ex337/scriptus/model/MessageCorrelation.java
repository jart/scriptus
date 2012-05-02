package net.ex337.scriptus.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 * Used as a container object to keep track of the link between
 * messages and processes, by pid. Includes the original message
 * id, a timestap, and the user we expect to hear the response 
 * from (if any).
 * 
 * TODO move to a better location
 * 
 * @author ian
 *
 */
public class MessageCorrelation implements Serializable {
	
	private static final long serialVersionUID = -1766085884095311452L;
	
	private UUID pid;
	private String user;
	private String messageId;
	private long timestamp;
	
	public MessageCorrelation(UUID pid, String user, String sourceSnowflake, long timestamp) {
		super();
		this.pid = pid;
		this.user = user;
		this.messageId = sourceSnowflake;
		this.timestamp = timestamp;
	}
	
	public UUID getPid() {
		return pid;
	}
	public void setPid(UUID pid) {
		this.pid = pid;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String sourceSnowflake) {
		this.messageId = sourceSnowflake;
	}

    public long getTimestamp() {
        return timestamp;
    }
	
}