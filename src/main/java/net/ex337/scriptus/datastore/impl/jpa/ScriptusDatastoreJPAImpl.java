package net.ex337.scriptus.datastore.impl.jpa;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

public class ScriptusDatastoreJPAImpl implements ScriptusDatastore {

    @Override
    public ScriptProcess newProcess(String userId, String source, String args, String owner) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScriptProcess getProcess(UUID uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeProcess(UUID pid, byte[] serialisedProcess) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] loadProcess(UUID pid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteProcess(UUID pid) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> listScripts(String userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String loadScriptSource(String userId, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveScriptSource(String userId, String name, String source) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteScript(String userId, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteScheduledTask(UUID pid, long nonce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveScheduledTask(ScheduledScriptAction task) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerMessageCorrelation(MessageCorrelation cid) {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<MessageCorrelation> getMessageCorrelations(String inReplyToMessageId, String fromUser) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unregisterMessageCorrelation(MessageCorrelation correlation) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getTransportCursor(TransportType transport) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateTransportCursor(TransportType transport, String cursor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createTestSources() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateProcessState(UUID pid, Object o) {
        // TODO Auto-generated method stub

    }

}
