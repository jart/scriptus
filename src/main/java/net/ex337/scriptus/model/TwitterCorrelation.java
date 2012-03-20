package net.ex337.scriptus.model;

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
 * TODO move to a better location
 * 
 * @author ian
 *
 */
public class TwitterCorrelation implements Serializable {
	
	private static final long serialVersionUID = -1766085884095311452L;
	
	private UUID pid;
	private String user;
	private long sourceSnowflake;
	
	public TwitterCorrelation(UUID pid, String user, long sourceSnowflake) {
		super();
		this.pid = pid;
		this.user = user;
		this.sourceSnowflake = sourceSnowflake;
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
	public long getSourceSnowflake() {
		return sourceSnowflake;
	}
	public void setSourceSnowflake(long sourceSnowflake) {
		this.sourceSnowflake = sourceSnowflake;
	}
	
}