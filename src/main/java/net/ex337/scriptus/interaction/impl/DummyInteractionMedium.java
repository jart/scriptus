
package net.ex337.scriptus.interaction.impl;

import java.util.ArrayList;
import java.util.UUID;

import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.api.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dummy interaction mediul. Never responds to listen()s, and
 * always responds to ask()s with the supplied response.
 * 
 * The one response configured in the Spring configuration file.
 * TODO A regexp-based matching might be a good idea to make testing script branches easier.
 */
public class DummyInteractionMedium implements InteractionMedium {

	private static final Log LOG = LogFactory.getLog(DummyInteractionMedium.class);

	private MessageReceiver receiver;
	
	public String response;
	
	//@Override
	private void send(final UUID pid, final String to, final String msg) {

		LOG.debug("send "+ (pid == null ? "" : pid)+" to:"+to+" msg:"+msg);
		
		if(pid == null) {
			return;
		}
		
		receiver.handleIncomings(new ArrayList<Message>() {{
			add(new Message(pid, to, response));
		}});

	}

	@Override
	public void registerReceiver(MessageReceiver londonCalling) {

		LOG.debug("registerReceiver "+ londonCalling);
		
		this.receiver = londonCalling;
	}

	@Override
	public void say(String to, String msg) {
		send(null, to, msg);
	}

	@Override
	public void ask(UUID pid, String to, String msg) {
		send(pid, to, msg);
	}

	@Override
	public void listen(UUID pid, String to) {
		//do nothing
	}

	public void setResponse(String response) {
		this.response = response;
	}

}

