package net.ex337.scriptus.model;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.transport.Transport;

/**
 * 
 * All API calls result in a continuation with the application state being
 * an implementation of this method. The implementations MUST be serializable.
 * 
 * @author ian
 *
 */
public abstract class ScriptAction {

	public abstract void visit(ScriptusFacade scriptus, ScriptProcess process);

}
