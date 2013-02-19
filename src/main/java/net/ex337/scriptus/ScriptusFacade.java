package net.ex337.scriptus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.scheduler.ProcessScheduler;
import net.ex337.scriptus.transport.Transport;

/**
 * A facadfe object giving access to almost all the functionality
 * of Scriptus. This is the object used by API calls to do their
 * stuff.
 * 
 * 
 * @author ian
 *
 */
public class ScriptusFacade {

    @Resource
    private ScriptusDatastore datastore;
    @Resource
    private ProcessScheduler scheduler;
    @Resource
    private Transport transport;
    @Resource
    private ScriptusConfig config;

    /*
     * zero-arg constructor for prototype bean
     */
    public ScriptusFacade() {
        
    }

    public ScriptusFacade(ScriptusDatastore datastore, ProcessScheduler scheduler, Transport transport, ScriptusConfig config) {
        this.datastore = datastore;
        this.scheduler = scheduler;
        this.transport = transport;
        this.config = config;
    }
    public ScriptProcess newProcess(String userId, String source, String args, String owner) {
        return datastore.newProcess(userId, source, false, args, owner);
    }
    public ScriptProcess getProcess(UUID uuid) {
        return datastore.getProcess(uuid);
    }
//    public void writeProcess(UUID pid, byte[] serialisedProcess) {
//        datastore.writeProcess(pid, serialisedProcess);
//    }
//    public byte[] loadProcess(UUID pid) {
//        return datastore.loadProcess(pid);
//    }
    public void deleteProcess(UUID pid) {
        datastore.deleteProcess(pid);
    }
//    public Set<String> listScripts(String userId) {
//        return datastore.listScripts(userId);
//    }
//    public String loadScriptSource(String userId, String name) {
//        return datastore.loadScriptSource(userId, name);
//    }
//    public void saveScriptSource(String userId, String name, String source) {
//        datastore.saveScriptSource(userId, name, source);
//    }
//    public void deleteScript(String userId, String name) {
//        datastore.deleteScript(userId, name);
//    }
//    public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
//        return datastore.getScheduledTasks(dueDate);
//    }
    public void deleteScheduledTask(UUID pid, long nonce) {
        datastore.deleteScheduledTask(pid, nonce);
    }
//    public void saveScheduledTask(ScheduledScriptAction task) {
//        datastore.saveScheduledTask(task);
//    }
    public void registerMessageCorrelation(MessageCorrelation cid) {
        datastore.registerMessageCorrelation(cid);
    }
    public Set<MessageCorrelation> getMessageCorrelationByID(String inReplyTo, String from, String userId) {
        return datastore.getMessageCorrelations(inReplyTo, from, userId);
    }
    public void unregisterMessageCorrelation(MessageCorrelation correlation) {
        datastore.unregisterMessageCorrelation(correlation);
    }
//    public void createTestSources() {
//        datastore.createTestSources();
//    }
    public void runWithLock(UUID pid, Runnable r) {
        scheduler.runWithLock(pid, r);
    }
//    public void executeNewProcess(String userId, String sourceName, String args, String owner) {
//        scheduler.executeNewProcess(userId, sourceName, args, owner);
//    }
    public void execute(UUID pid) {
        scheduler.execute(pid);
    }
    public void updateProcessState(UUID pid, Object o) {
        datastore.updateProcessState(pid, o);
    }
    public void markAsKilledIfRunning(UUID pid) {
        scheduler.markAsKilledIfRunning(pid);
    }
    public String send(String userId, String to, String msg) {
        return transport.send(userId, to, msg);
    }

    public void scheduleTask(ScheduledScriptAction action) {
        scheduler.scheduleTask(action);
    }

    public List<UUID> getChildren(UUID parent) {
        return datastore.getChildren(parent);
    }

    public void removeChild(UUID parent, UUID child) {
        datastore.removeChild(parent, child);
    }

    public void addChild(int seq, UUID parent, UUID newChild) {
        datastore.addChild(parent, newChild, seq);
    }

    public void markProcessFinished(UUID pid) {
        datastore.markProcessFinished(pid);
    }

    public TransportType getTransportType() {
        return config.getTransportType();
    }

    public void saveLogMessage(UUID pid, String userId, String message) {
        datastore.saveLogMessage(pid, userId, message);
        
    }

}
