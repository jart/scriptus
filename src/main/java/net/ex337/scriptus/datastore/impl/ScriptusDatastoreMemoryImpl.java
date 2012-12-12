package net.ex337.scriptus.datastore.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import net.ex337.scriptus.exceptions.ProcessNotFoundException;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ProcessListItem;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.support.ScriptusClassShutter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

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

	private final Map<UUID,List<UUID>> children = new HashMap<UUID,List<UUID>>();

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

    @Override
    public void writeProcess(ScriptProcess p) {

        if (p.getPid() == null) {
            p.setPid(UUID.randomUUID());
        } else {
            p.setVersion(p.getVersion()+1);
        }

        LOG.debug("saving " + p.getPid().toString().substring(30));

        Context cx = Context.enter();
        cx.setClassShutter(new ScriptusClassShutter());
        cx.setOptimizationLevel(-1); // must use interpreter mode
        
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(p);
            out.writeObject(p.getGlobalScope());
            out.writeObject(p.getContinuation());
            out.close();

            processes.put(p.getPid(), bout.toByteArray());
                
        } catch (ScriptusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptusRuntimeException(e);
        } finally {
            Context.exit();
        }

        
    }
    
    @Override
    public ScriptProcess getProcess(UUID pid) {

        if (pid == null) {
            throw new ScriptusRuntimeException("Cannot load null pid");
        }

        LOG.debug("loading " + pid.toString().substring(30));

        Context cx = Context.enter();
        cx.setClassShutter(new ScriptusClassShutter());
        cx.setOptimizationLevel(-1); // must use interpreter mode
        
        try {

            byte[] process = processes.get(pid);
            
            if(process == null) {
                throw new ProcessNotFoundException(pid.toString());
            }
            
            InputStream bais = new ByteArrayInputStream(process);

//          ScriptusAPI tmpScriptusApi = new ScriptusAPI(config);
//          Scriptable tmpGlobalScope = tmpScriptusApi.createScope(cx);

            ObjectInputStream in = new ObjectInputStream(bais);

            ScriptProcess p = (ScriptProcess) in.readObject();
            
            ScriptProcess result = createScriptProcess();
            
            result.setPid(p.getPid());
            result.setWaiterPid(p.getWaiterPid());
            result.setSource(p.getSource());
            result.setSourceName(p.getSourceName());
            result.setUserId(p.getUserId());
            result.setArgs(p.getArgs());
            result.setState(p.getState());
            result.setCompiled(p.getCompiled());
            result.setOwner(p.getOwner());
            result.setRoot(p.isRoot());
            result.setVersion(p.getVersion());
            
            p = null;

            result.setGlobalScope((ScriptableObject) in.readObject());
            result.setContinuation(in.readObject());

            in.close();
            
            return result;
            
        } catch (ScriptusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptusRuntimeException(e);
        } finally {
            Context.exit();
        }

    }

    @Override
    public List<UUID> getChildren(UUID parent) {
        if(children.get(parent) == null) {
            children.put(parent, new ArrayList<UUID>());
        }
        return children.get(parent);
    }

    @Override
    public void removeChild(UUID parent, UUID child) {
        if(children.get(parent) != null){
            children.get(parent).remove(child);
        }
        
    }

    @Override
    public void addChild(UUID parent, UUID newChild, int seq) {
        if(children.get(parent) == null) {
            children.put(parent, new ArrayList<UUID>());
        }
        children.get(parent).add(newChild);
    }

    @Override
    public UUID getLastChild(UUID pid) {
        List<UUID> l;
        if((l = children.get(pid)) == null) {
            return null;
        }
        return l.get(l.size()-1);
    }

    @Override
    public List<ProcessListItem> getProcessesForUser(String uid) {
        
        List<ProcessListItem> result = new ArrayList<ProcessListItem>();
        
        for(byte[] b : this.processes.values()) {
            ObjectInputStream i;
            try {
                i = new ObjectInputStream(new ByteArrayInputStream(b));
                ScriptProcess p = (ScriptProcess) i.readObject();
                if(p.getUserId().equals(uid)){
                    result.add(new ProcessListItem(p.getPid().toString(), uid, "state", p.getSourceName(), p.getVersion(), 111, System.currentTimeMillis(), System.currentTimeMillis(), p.isAlive()));
                }
                i.close();
            } catch (IOException e) {
                throw new ScriptusRuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new ScriptusRuntimeException(e);
            }
        }
        
        return result;
    }
    

    public void markProcessFinished(UUID pid) {
        ScriptProcess p = getProcess(pid);
        p.setAlive(false);
        writeProcess(p);
    }

    @Override
    public int countSavedScripts(String user) {
        return 1;
    }

    @Override
    public int countRunningProcesses(String user) {
        return 1;
    }
    
    
    

}
