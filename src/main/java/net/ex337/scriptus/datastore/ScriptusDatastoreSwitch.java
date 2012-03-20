package net.ex337.scriptus.datastore;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
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

	@Resource(name = "fileDatastore")
	private ScriptusDatastore file;

	@Resource(name = "awsDatastore")
	private ScriptusDatastore aws;

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
		} else if (datastore == DatastoreType.File) {
			activeImpl = file;
		} else if (datastore == DatastoreType.Aws) {
			activeImpl = aws;
		}
	}

	public void writeProcess(UUID pid, byte[] script) {
		activeImpl.writeProcess(pid, script);
	}

	public byte[] loadProcess(UUID pid) {
		return activeImpl.loadProcess(pid);
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

	public ScriptProcess newProcess(String userId, String source, String args, String owner) {
		return activeImpl.newProcess(userId, source, args, owner);
	}

	public ScriptProcess getProcess(UUID uuid) {
		return activeImpl.getProcess(uuid);
	}

	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
		return activeImpl.getScheduledTasks(dueDate);
	}

	public void deleteScheduledTask(ScheduledScriptAction t) {
		activeImpl.deleteScheduledTask(t);
	}

	public void saveScheduledTask(Calendar when, ScheduledScriptAction task) {
		activeImpl.saveScheduledTask(when, task);
	}

	@Override
	public void createTestSources() {
		activeImpl.createTestSources();
	}

	public void registerTwitterCorrelation(TwitterCorrelation cid) {
		activeImpl.registerTwitterCorrelation(cid);
	}

	public TwitterCorrelation getTwitterCorrelationByID(long snowflake) {
		return activeImpl.getTwitterCorrelationByID(snowflake);
	}

	public void unregisterTwitterCorrelation(long snowflake) {
		activeImpl.unregisterTwitterCorrelation(snowflake);
	}

	public List<Long> getTwitterLastMentions() {
		return activeImpl.getTwitterLastMentions();
	}

	public void updateTwitterLastMentions(List<Long> processedIncomings) {
		activeImpl.updateTwitterLastMentions(processedIncomings);
	}

	public UUID getMostRecentTwitterListener(String screenName) {
		return activeImpl.getMostRecentTwitterListener(screenName);
	}

	public void unregisterTwitterListener(UUID uuid, String string) {
		activeImpl.unregisterTwitterListener(uuid, string);
	}

	public void registerTwitterListener(UUID pid, String to) {
		activeImpl.registerTwitterListener(pid, to);
	}

}
