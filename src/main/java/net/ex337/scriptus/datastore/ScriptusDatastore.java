package net.ex337.scriptus.datastore;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

/**
 * 
 * The interface used anywhere in Scriptus where stuff needs storing
 * somewhere. The contract workout can be found in {@link Testcase_ScriptusDAO}.
 * 
 * @author ian
 *
 */
public interface ScriptusDatastore {

	/**
	 * Create a new {@link ScriptProcess} using the supplied parameters
	 * 
	 * @param userId the openID user that owns the source
	 * @param source the source name
	 * @param args a string bound to the script environment as "args"
	 * @param owner a string bound to the script environment as "owner" and the twitter screen name to which the final result of the script will be said.
	 * @return a new {@link ScriptProcess}
	 */
	public ScriptProcess newProcess(String userId, String source, String args, String owner);

	/**
	 * Retrieve an existing process from the datastore.
	 * 
	 * @param uuid the PID of the process to retrieve.
	 */
	public ScriptProcess getProcess(UUID uuid);
	
    public List<UUID> getChildren(UUID parent);
    public void removeChild(UUID parent, UUID child);
    public void addChild(UUID parent, UUID newChild, int seq);

	
	/**
	 * deletes a process
	 */
	public void deleteProcess(UUID pid);


	//datastore methods for admin
	/**
	 * CRUDL operations owned by supplied openID user.
	 */
	public Set<String> listScripts(String userId);
	
	/**
	 * @see #listScripts(String)
	 */
	public String loadScriptSource(String userId, String name);
	
	/**
	 * @see #listScripts(String)
	 */
	public void saveScriptSource(String userId, String name, String source);
	
	/**
	 * @see #listScripts(String)
	 */
	public void deleteScript(String userId, String name);

	/**
	 * Scheduler methods. Mostly used to wake processes after
	 * timeout, backed by SDB.
	 * 
	 * @param dueDate retrieve all actions scheduled before this date.
	 * @return
	 */
	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate);

	/**
	 * @see #getScheduledTasks(Calendar)
	 */
	public void deleteScheduledTask(UUID pid, long nonce);

	/**
	 * @see #getScheduledTasks(Calendar)
	 */
	public void saveScheduledTask(ScheduledScriptAction task);

	/**
	 * Used to correlate messages sent to ask() users things.
	 * The message ID is used to retrieve the PID of the process
	 * that will treat the message.
	 */
	public void registerMessageCorrelation(MessageCorrelation cid);
	
	/**
	 * @param fromUser TODO
	 * @see #registerMessageCorrelation(MessageCorrelation)
	 */
	public Set<MessageCorrelation> getMessageCorrelations(String inReplyToMessageId, String fromUser);
	
	/**
	 * @see #registerMessageCorrelation(MessageCorrelation)
	 */
	public void unregisterMessageCorrelation(MessageCorrelation correlation);

	/**
	 * Used as a cursor to keep track of the tweets we've already
	 * processed.
	 * 
	 * @return a list of tweet IDs for tweets processed at the last poll of the Twitter API.
	 */
	public String getTransportCursor(TransportType transport);
	/**
	 * @see #getTransportCursor()
	 */
	public void updateTransportCursor(TransportType transport, String cursor);


	/**
	 * Loads under a user "test" all scripts found under the
	 * directory "testScripts", if it exists.
	 */
	public void createTestSources();

	/**
	 * Updates the process state, under lock, to the supplied object.
	 * 
	 */
    public void updateProcessState(UUID pid, Object o);

    public void writeProcess(ScriptProcess p);

    public UUID getLastChild(UUID pid);
	
}
