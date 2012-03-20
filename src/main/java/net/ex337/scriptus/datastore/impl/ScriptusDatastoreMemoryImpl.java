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
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.TwitterCorrelation;
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

	private final Map<Long,TwitterCorrelation> correlationMap = new HashMap<Long,TwitterCorrelation>();
	
	private final Map<ScheduledScriptAction, Long> scheduledActions = new HashMap<ScheduledScriptAction,Long>();

	private final Map<String,Map<UUID, Long>> listeners = new HashMap<String,Map<UUID, Long>>();

	private List<Long> lastMentions = new ArrayList<Long>();
	
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
		
		for(Map.Entry<ScheduledScriptAction, Long> e : scheduledActions.entrySet()){
			if(e.getValue() <= dueDateMs) {
				result.add(e.getKey());
			}
		}
		
		return result;
	}

	@Override
	public void deleteScheduledTask(ScheduledScriptAction t) {
		
		scheduledActions.remove(t);
		
	}

	@Override
	public void saveScheduledTask(Calendar until, ScheduledScriptAction task) {
		
		scheduledActions.put(task, until.getTimeInMillis());
		
	}

	@Override
	public void registerTwitterCorrelation(TwitterCorrelation cid) {
		correlationMap.put(cid.getSourceSnowflake(), cid);
	}

	@Override
	public TwitterCorrelation getTwitterCorrelationByID(long cid) {
		return correlationMap.get(cid);
	}

	@Override
	public void unregisterTwitterCorrelation(long cid) {
		correlationMap.remove(cid);
	}


	@Override
	public List<Long> getTwitterLastMentions() {
		return lastMentions;
	}

	@Override
	public void updateTwitterLastMentions(List<Long> processedIncomings) {
		this.lastMentions = processedIncomings;
	}

	@Override
	public void registerTwitterListener(UUID pid, String to) {
		
		Map<UUID,Long> l = listeners.get(to);
		
		if(l == null) {
			listeners.put(to, l = new HashMap<UUID, Long>());
		}
		
		l.put(pid, System.currentTimeMillis());
		
	}
	
	@Override
	public UUID getMostRecentTwitterListener(String screenName) {
		
		Map<UUID,Long> tos = listeners.get(screenName);
		
		if(tos == null) {
			return null;
		}
		
		UUID mostRecentPid = null;
		Long mostRecentTime = null;
		
		for(Map.Entry<UUID,Long> e : tos.entrySet()) {
			if(mostRecentTime == null || mostRecentTime <= e.getValue()) {
				mostRecentPid = e.getKey();
				mostRecentTime = e.getValue();
			}
		}
 		
		return mostRecentPid;
	}

	@Override
	public void unregisterTwitterListener(UUID uuid, String to) {
		Map<UUID, Long> tos = listeners.get(to);
		if(tos == null) {
			return;
		}
		tos.remove(uuid);
	}


}
