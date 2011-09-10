package net.ex337.scriptus.model;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;

/**
 * 
 * All API calls result in a continuation with the application state being
 * an implementation of this method. The implementations MUST be serializable.
 * 
 * @author ian
 *
 */
public abstract class ScriptAction {

	public abstract void visit(ProcessScheduler scheduler, InteractionMedium medium, ScriptusDAO dao, ScriptProcess process);

}
