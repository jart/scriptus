package net.ex337.scriptus.dao;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 * Used as a container object to keep track of the link between
 * hashtags and processes, by pid. Includes the original tweet
 * id so as to avoid taking the original for the response if
 * the user was asking the 'scriptus' user, and the user we
 * expect to hear the response from, to avoid impersonation
 * (and make the CID namespace per-user, meaning it can be shorter).
 * 
 * @author ian
 *
 */
public class TwitterCorrelation implements Serializable {
	
	private static final long serialVersionUID = -1766085884095311452L;
	
	private String id;
	private UUID pid;
	private String user;
	private long sourceSnowflake;
	
	public TwitterCorrelation(UUID pid, String user, String cid, long sourceSnowflake) {
		super();
		this.pid = pid;
		this.user = user;
		this.id = cid;
		this.sourceSnowflake = sourceSnowflake;
	}

	public TwitterCorrelation(UUID pid, String user, String cid) {
		this(pid, user, cid, -1);
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
	public String getId() {
		return id;
	}
	public void setId(String cid) {
		this.id = cid;
	}
	public long getSourceSnowflake() {
		return sourceSnowflake;
	}
	public void setSourceSnowflake(long sourceSnowflake) {
		this.sourceSnowflake = sourceSnowflake;
	}
	
	
	
}