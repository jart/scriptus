package net.ex337.scriptus.model.api.functions;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import net.ex337.scriptus.ProcessScheduler;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.ScriptAction;
import net.ex337.scriptus.model.ScriptProcess;

import org.apache.commons.io.IOUtils;

public class Get extends ScriptAction implements Serializable {

	private static final long serialVersionUID = -1642528520387888606L;

	private URL url;

	public Get(URL url){
		this.url = url;
	}
	
	
	@Override
	public void visit(ProcessScheduler scheduler, InteractionMedium medium, ScriptusDAO dao, ScriptProcess process) {

		/*
		 * TODO long-term, this should send a request to a "get processor" to isolate the app IO in one node/module
		 * 
		 * Also this is probably a horrifically naive way of doing it.
		 */
		try {
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setConnectTimeout(50000);
//			c.setDoInput(true);
			c.connect();
			String content = IOUtils.toString(c.getInputStream(), c.getContentEncoding());
			scheduler.updateProcessState(process.getPid(), content);
			scheduler.execute(process.getPid());
			//TODO make a ConvertsToScriptable object that will add headers property etc.
		} catch(IOException e) {
			throw new ScriptusRuntimeException(e);
		}

		
	}

}

