package net.ex337.scriptus.datastore;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.model.ProcessListItem;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

/**
 * An implementation of the Scriptus datastore that does 
 * nothing  but proxy to whatever implementation has been 
 * configured in {@link ScriptusConfig}.
 * 
 * In order for a configuration change to take effect,
 * the container must be restarted (calling init() again).
 * This is done automatically if you change the config
 * via the web interface.
 * 
 */
public class ScriptusDatastoreSwitch implements ScriptusDatastore {

	@Resource(name = "memoryDatastore")
	private ScriptusDatastore memory;

//    @Resource(name = "dbDatastore")
//    private ScriptusDatastore db;

    @Resource(name = "embeddedDatastore")
    private ScriptusDatastore embedded;

	private ScriptusDatastore activeImpl;

	@Resource
	private ScriptusConfig config;

	@PostConstruct
	public void init() {
		switchDAO(config.getDatastoreType());
	}

	private void switchDAO(DatastoreType datastore) {
		if (datastore == DatastoreType.Memory) {
			activeImpl = memory;
//        } else if (datastore == DatastoreType.Db) {
//            activeImpl = db;
        } else if (datastore == DatastoreType.Embedded) {
            activeImpl = embedded;
		}
	}

	public Set<String> listScripts(String userId) {
		return activeImpl.listScripts(userId);
	}

	public String loadScriptSource(String userId, String name) {
		return activeImpl.loadScriptSource(userId, name);
	}

	public void saveScriptSource(String userId, String name, String source) {
		activeImpl.saveScriptSource(userId, name, source);
	}

	public void deleteScript(String userId, String parameter) {
		activeImpl.deleteScript(userId, parameter);
	}

	@Override
	public void deleteProcess(UUID pid) {
		activeImpl.deleteProcess(pid);
	}

	public ScriptProcess newProcess(String userId, String sourceId, boolean sample, String args, String owner) {
		return activeImpl.newProcess(userId, sourceId, sample, args, owner);
	}

	public ScriptProcess getProcess(UUID uuid) {
		return activeImpl.getProcess(uuid);
	}

	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
		return activeImpl.getScheduledTasks(dueDate);
	}

	public void deleteScheduledTask(UUID pid, long nonce) {
		activeImpl.deleteScheduledTask(pid, nonce);
	}

	public void saveScheduledTask(ScheduledScriptAction task) {
		activeImpl.saveScheduledTask(task);
	}

	@Override
	public void createSamples() {
		activeImpl.createSamples();
	}

	public void registerMessageCorrelation(MessageCorrelation cid) {
		activeImpl.registerMessageCorrelation(cid);
	}

	public Set<MessageCorrelation> getMessageCorrelations(String inReplyToMessageId, String fromUser) {
		return activeImpl.getMessageCorrelations(inReplyToMessageId, fromUser);
	}

	public void unregisterMessageCorrelation(MessageCorrelation c) {
		activeImpl.unregisterMessageCorrelation(c);
	}

    public void updateProcessState(UUID pid, Object o) {
        activeImpl.updateProcessState(pid, o);
    }

    public String getTransportCursor(TransportType transport) {
        return activeImpl.getTransportCursor(transport);
    }

    public void updateTransportCursor(TransportType transport, String cursor) {
        activeImpl.updateTransportCursor(transport, cursor);
    }

    public void writeProcess(ScriptProcess p) {
        activeImpl.writeProcess(p);
    }

    public List<UUID> getChildren(UUID parent) {
        return activeImpl.getChildren(parent);
    }

    public void removeChild(UUID parent, UUID child) {
        activeImpl.removeChild(parent, child);
    }

    public void addChild(UUID parent, UUID newChild, int seq) {
        activeImpl.addChild(parent, newChild, seq);
    }

    public UUID getLastChild(UUID pid) {
        return activeImpl.getLastChild(pid);
    }

    public List<ProcessListItem> getProcessesForUser(String uid) {
        return activeImpl.getProcessesForUser(uid);
    }

    public void markProcessFinished(UUID pid) {
        activeImpl.markProcessFinished(pid);
    }

    public int countSavedScripts(String user) {
        return activeImpl.countSavedScripts(user);
    }

    public int countRunningProcesses(String user) {
        return activeImpl.countRunningProcesses(user);
    }
    
    

}
