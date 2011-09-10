package net.ex337.scriptus.model.api.functions;

import java.io.Serializable;
import java.util.UUID;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.ScriptAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Fork extends ScriptAction implements Serializable {

	private static final long serialVersionUID = -4710405332189677156L;

	private static final Log LOG = LogFactory.getLog(Fork.class);

	public void visit(ProcessScheduler controller, InteractionMedium medium, ScriptusDAO dao, final ScriptProcess parent) {
		
		final UUID childPid = UUID.randomUUID();
		
		dao.runWithLock(childPid, new Runnable() {

			@Override
			public void run() {
				parent.setState(childPid.toString());
				parent.getChildren().add(childPid);
				parent.save();
			}
		});

		
		
		LOG.debug("Forking "+parent.getPid().toString().substring(30)+", child="+childPid.toString().substring(30));
		
		ScriptProcess child = parent.clone();
		//fork() response
		child.setState(0);
		child.setRoot(false);
		
		//important :-/
		child.setPid(childPid);
		
		child.save();
		
		controller.execute(parent.getPid());
		controller.execute(child.getPid());
		
	}

}

