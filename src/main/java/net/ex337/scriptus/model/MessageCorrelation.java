package net.ex337.scriptus.model;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.config.ScriptusConfig.TransportType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Used as a container object to keep track of the link between
 * messages and processes, by pid. Includes the original message
 * id, a timestap, and the user we expect to hear the response 
 * from (if any), and the process owner & transport.
 * 
 * TODO move to a better location
 * 
 * @author ian
 *
 */
public class MessageCorrelation implements Serializable {
	
	private static final long serialVersionUID = -1766085884095311452L;
	
	private UUID pid;
	private String from;
	private String messageId;
	private long timestamp;
	private TransportType transport;
    private String userId;
	
	public MessageCorrelation(UUID pid, String from, String sourceSnowflake, long timestamp, TransportType transport, String userId) {
		super();
		this.pid = pid;
		this.transport = transport;
		this.from = from;
		this.messageId = sourceSnowflake;
		this.timestamp = timestamp;
		this.userId = userId;
	}
	
	public MessageCorrelation() {
    }

    public UUID getPid() {
		return pid;
	}
	public void setPid(UUID pid) {
		this.pid = pid;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String user) {
		this.from = user;
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
	
    public int hashCode(){
        HashCodeBuilder h = new HashCodeBuilder();
        h.append(pid);
        h.append(messageId);
        h.append(timestamp);
        h.append(from);
        h.append(userId);
        h.append(transport);
        return h.toHashCode();
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public TransportType getTransport() {
        return transport;
    }

    public String getUserId() {
        return userId;
    }

    public void setTransport(TransportType transport) {
        this.transport = transport;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    
}