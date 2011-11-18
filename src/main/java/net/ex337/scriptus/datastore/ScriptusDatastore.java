package net.ex337.scriptus.datastore;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TwitterCorrelation;
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

	/**
	 * Low-level CRUD methods to for serialised processes 
	 * in the datastore.
	 */
	public void writeProcess(UUID pid, byte[] serialisedProcess);
	
	/**
	 * @see #writeProcess(UUID, byte[])
	 */
	public byte[] loadProcess(UUID pid);

	
	/**
	 * @see #writeProcess(UUID, byte[])
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
	public void deleteScheduledTask(ScheduledScriptAction t);

	/**
	 * @see #getScheduledTasks(Calendar)
	 */
	public void scheduleTask(Calendar when, ScheduledScriptAction task);

	/*
	 * this stuff should be in a TwitterInteractionMediumDAO.
	 */
	
	/**
	 * Used to correlate tweets sent to ask() twitter users things,
	 * which have a short, high-entropy #tag associated with them
	 * to keep track of them.
	 */
	public void registerTwitterCorrelation(TwitterCorrelation cid);
	
	/**
	 * @see #registerTwitterCorrelation(TwitterCorrelation)
	 */
	public TwitterCorrelation getTwitterCorrelationByID(String cid);
	
	/**
	 * @see #registerTwitterCorrelation(TwitterCorrelation)
	 */
	public void unregisterTwitterCorrelation(String cid);

	/**
	 * Used as a cursor to keep track of the tweets we've already
	 * processed.
	 * 
	 * @return a list of tweet IDs for tweets processed at the last poll of the Twitter API.
	 */
	public List<Long> getTwitterLastMentions();
	/**
	 * @see #getTwitterLastMentions()
	 */
	public void updateTwitterLastMentions(List<Long> processedIncomings);

	/**
	 * Used for tracking processes listening to twitter users.
	 * Because no correlation ID is sent to the user, if multiple
	 * processes listen to the same user, they receive that users
	 * mentions in the reverse order in which the processes listened.
	 * 
	 * Once a process has received a mention from a user, or if the
	 * listen() times out, the listener is unregistered.
	 */
	public UUID getMostRecentTwitterListener(String screenName);
	
	/**
	 * see {@link #getMostRecentTwitterListener(String)}
	 */
	public void unregisterTwitterListener(UUID pid, String to);
	/**
	 * see {@link #getMostRecentTwitterListener(String)}
	 */
	public void registerTwitterListener(UUID pid, String to);

	/**
	 * Loads under a user "test" all scripts found under the
	 * directory "testScripts", if it exists.
	 */
	void createTestSources();
	
}
