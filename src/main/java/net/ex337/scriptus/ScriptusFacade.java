package net.ex337.scriptus;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.transport.Transport;

public class ScriptusFacade implements ScriptusDatastore, ProcessScheduler, Transport {
    
    private ScriptusDatastore datastore;
    private ProcessScheduler scheduler;
    private Transport transport;
    
    public ScriptusFacade(ScriptusDatastore datastore, ProcessScheduler scheduler, Transport transport) {
        this.datastore = datastore;
        this.scheduler = scheduler;
        this.transport = transport;
    }
    public ScriptProcess newProcess(String userId, String source, String args, String owner) {
        return datastore.newProcess(userId, source, args, owner);
    }
    public ScriptProcess getProcess(UUID uuid) {
        return datastore.getProcess(uuid);
    }
    public void writeProcess(UUID pid, byte[] serialisedProcess) {
        datastore.writeProcess(pid, serialisedProcess);
    }
    public byte[] loadProcess(UUID pid) {
        return datastore.loadProcess(pid);
    }
    public void deleteProcess(UUID pid) {
        datastore.deleteProcess(pid);
    }
    public Set<String> listScripts(String userId) {
        return datastore.listScripts(userId);
    }
    public String loadScriptSource(String userId, String name) {
        return datastore.loadScriptSource(userId, name);
    }
    public void saveScriptSource(String userId, String name, String source) {
        datastore.saveScriptSource(userId, name, source);
    }
    public void deleteScript(String userId, String name) {
        datastore.deleteScript(userId, name);
    }
    public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
        return datastore.getScheduledTasks(dueDate);
    }
    public void deleteScheduledTask(ScheduledScriptAction t) {
        datastore.deleteScheduledTask(t);
    }
    public void saveScheduledTask(Calendar when, ScheduledScriptAction task) {
        datastore.saveScheduledTask(when, task);
    }
    public void registerTwitterCorrelation(TwitterCorrelation cid) {
        datastore.registerTwitterCorrelation(cid);
    }
    public TwitterCorrelation getTwitterCorrelationByID(long snowflake) {
        return datastore.getTwitterCorrelationByID(snowflake);
    }
    public void unregisterTwitterCorrelation(long snowflake) {
        datastore.unregisterTwitterCorrelation(snowflake);
    }
    public List<Long> getTwitterLastMentions() {
        return datastore.getTwitterLastMentions();
    }
    public void updateTwitterLastMentions(List<Long> processedIncomings) {
        datastore.updateTwitterLastMentions(processedIncomings);
    }
    public UUID getMostRecentTwitterListener(String screenName) {
        return datastore.getMostRecentTwitterListener(screenName);
    }
    public void unregisterTwitterListener(UUID pid, String to) {
        datastore.unregisterTwitterListener(pid, to);
    }
    public void registerTwitterListener(UUID pid, String to) {
        datastore.registerTwitterListener(pid, to);
    }
    public void createTestSources() {
        datastore.createTestSources();
    }
    public void runWithLock(UUID pid, Runnable r) {
        scheduler.runWithLock(pid, r);
    }
    public void executeNewProcess(String userId, String sourceName, String args, String owner) {
        scheduler.executeNewProcess(userId, sourceName, args, owner);
    }
    public void execute(UUID pid) {
        scheduler.execute(pid);
    }
    public void updateProcessState(UUID pid, Object o) {
        scheduler.updateProcessState(pid, o);
    }
    public void markAsKilledIfRunning(UUID pid) {
        scheduler.markAsKilledIfRunning(pid);
    }
//    public void say(String to, String msg) {
//        transport.say(to, msg);
//    }
//    public void ask(UUID pid, String to, String msg) {
//        transport.ask(pid, to, msg);
//    }
    public long send(String to, String msg) {
        return transport.send(to, msg);
    }
    public void listen(UUID pid, String to) {
        transport.listen(pid, to);
    }
    public void registerReceiver(MessageReceiver londonCalling) {
        transport.registerReceiver(londonCalling);
    }
    
    
    

}
