package net.ex337.scriptus.model.api.functions;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
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
	public void visit(final ScriptusFacade scriptus, final ScriptProcess process) {

		/*
		 * TODO long-term, this should send a request to a "get processor" to isolate the app IO in one node/module
		 * 
		 * Also this is probably a horrifically naive way of doing it.
		 */
		try {
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setConnectTimeout(60000);
			c.connect();
			String content = IOUtils.toString(c.getInputStream(), c.getContentEncoding());
			scriptus.updateProcessState(process.getPid(), content);
			scriptus.execute(process.getPid());
			//TODO make a ConvertsToScriptable object that will add headers property etc.
		} catch(IOException e) {
			throw new ScriptusRuntimeException(e);
		}

		
	}

}

