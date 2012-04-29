package net.ex337.scriptus.model.scheduler;

import java.util.UUID;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;

import org.apache.commons.lang.StringUtils;

public class Wake extends ScheduledScriptAction {
	
	private UUID pid;
	
	private long nonce;

	public Wake() {
		
	}

	public Wake(UUID pid, long nonce, long when) {
		this.pid = pid;
		this.nonce = nonce;
		setWhen(when);
	}

	@Override
	public UUID getPid() {
		return pid;
	}

	@Override
	public void visit(ScriptusFacade scriptus, ScriptProcess process) {
	    
	    /*
	     * FIXME if this is used from a listen() or ask(),
	     * do one final check of the transports before
	     * we set the state to null
	     */
		
		if(process.getState() instanceof HasTimeout){
			//to confirm that it's the same wait
			//and that nothing's changed in the mean time.
		    
			if(((HasTimeout)process.getState()).getNonce() == nonce) {
				//return null
				scriptus.updateProcessState(process.getPid(), null);
				scriptus.execute(pid);
			}
		}

	}

	@Override
	public String toString() {
		return this.getClass().getName()+"/"+pid+"/"+nonce+"/"+getWhen();
	}

	@Override
	public void fromString(String s) {
		String[] bits = StringUtils.split(s, "/");
		this.pid = UUID.fromString(bits[1]);
        this.nonce = Long.parseLong(bits[2]);
        setWhen(Long.parseLong(bits[3]));
	}
	
	public boolean equals(Object that){
		if(that == null) {
			return false;
		}
		return this.toString().equals(that.toString());
	}
	
	//since toString returns string representation of object, it follows....
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public long getNonce() {
		return nonce;
	}

}
