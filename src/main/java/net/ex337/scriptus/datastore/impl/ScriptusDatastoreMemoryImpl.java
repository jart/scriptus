package net.ex337.scriptus.datastore.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * In-memory, transient implementation of the Scriptus datastore.
 * 
 * Used for test-cases. Performance of some methods increases 
 * linearly with the amount of data stored. Limited by memory 
 * settings of the JVM you're running in.
 * 
 * @author ian
 *
 */
public abstract class ScriptusDatastoreMemoryImpl extends BaseScriptusDatastore implements ScriptusDatastore {

	private static final Log LOG = LogFactory.getLog(ScriptusDatastoreMemoryImpl.class);
	
	private final Map<UUID,byte[]> processes = new HashMap<UUID,byte[]>();

	private final Map<String,String> sources = new HashMap<String,String>();
	
    private Set<MessageCorrelation> correlations = new HashSet<MessageCorrelation>();
	
	private final Map<String, ScheduledScriptAction> scheduledActions = new HashMap<String, ScheduledScriptAction>();

	private Map<TransportType, String> cursors = new HashMap<TransportType, String>();
	
	@Resource
	private ScriptusConfig config;
	
	public void init() throws IOException {
		
		if(config.getDatastoreType() != DatastoreType.Memory) {
			return;
		}
		
	}
	
	@Override
	public void writeProcess(UUID pid, byte[] script) {
		processes.put(pid, script);
	}

	@Override
	public byte[] loadProcess(UUID pid) {
		return processes.get(pid);
	}

	@Override
	public void saveScriptSource(String userId, String name, String source) {
		sources.put(userId+"/"+name, source);
	}

	@Override
	public Set<String> listScripts(String userId) {
		Set<String> result = new HashSet<String>();
		String prefix = userId+"/";
		for(String s : this.sources.keySet()){
			if(s.startsWith(prefix)){
				result.add(s.substring(prefix.length()));
			}
		}
		return result;
	}

	@Override
	public void deleteScript(String openid, String scriptName) {
		sources.remove(openid+"/"+scriptName);
	}


	@Override
	public String loadScriptSource(String userId, String name) {
		return sources.get(userId+"/"+name);
	}

	@Override
	public void deleteProcess(UUID pid) {
		processes.remove(pid);
	}

	@Override
	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
		
		List<ScheduledScriptAction> result = new ArrayList<ScheduledScriptAction>();
		
		long dueDateMs = dueDate.getTimeInMillis();
		
		for(Map.Entry<String, ScheduledScriptAction> e : scheduledActions.entrySet()){
			if(e.getValue().getWhen() <= dueDateMs) {
				result.add(e.getValue());
			}
		}
		
		return result;
	}

	@Override
    public void deleteScheduledTask(UUID pid, long nonce) {
		
		scheduledActions.remove(pid+"/"+nonce);
		
	}

	@Override
	public void saveScheduledTask(ScheduledScriptAction task) {
		
		scheduledActions.put(task.getPid()+"/"+task.getNonce(), task);
		
	}

	@Override
	public void registerMessageCorrelation(MessageCorrelation correlation) {
	    correlations.add(correlation);
	}

	@Override
	public Set<MessageCorrelation> getMessageCorrelations(String cid, String fromUser) {
        
        Set<MessageCorrelation> result = new HashSet<MessageCorrelation>();

        for(MessageCorrelation c : correlations) {
            if(c.getMessageId() == null && c.getUser() == null){
                result.add(c);
            } else if(cid != null) {
                if(cid.equals(c.getMessageId()) && c.getUser() == null){
                    result.add(c);
                } else if(cid.equals(c.getMessageId()) && fromUser.equals(c.getUser())){
                    result.add(c);
	            }
	        } else {
               if((fromUser.equals(c.getUser()) && c.getMessageId() == null)) {
                    result.add(c);
               } else if(c.getMessageId() == null && (c.getUser() == null) || fromUser.equals(c.getUser())) {
	                result.add(c);
	           } 
	        }
	    }

		return result;
	}

	@Override
	public void unregisterMessageCorrelation(MessageCorrelation correlation) {
        correlations.remove(correlation);
	}

	
    @Override
    public String getTransportCursor(TransportType transport) {
        return cursors.get(transport);
    }

    @Override
    public void updateTransportCursor(TransportType transport, String cursor) {
        cursors.put(transport, cursor);
    }

}
