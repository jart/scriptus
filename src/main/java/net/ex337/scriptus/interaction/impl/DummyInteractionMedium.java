
package net.ex337.scriptus.interaction.impl;

import java.util.ArrayList;
import java.util.UUID;

import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.api.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * dummy. never responds to listen()s
 */
public class DummyInteractionMedium implements InteractionMedium {

	private static final Log LOG = LogFactory.getLog(DummyInteractionMedium.class);

	private MessageReceiver receiver;
	
	public String response;
	
	//@Override
	private void send(final UUID pid, final String to, final String msg) {

		LOG.debug("send "+ (pid == null ? "" : pid)+" to:"+to+" msg:"+msg);
		System.out.println("send "+ (pid == null ? "" : pid)+" to:"+to+" msg:"+msg);
		
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
		String cid = UUID.randomUUID().toString().substring(30);
		LOG.info("registering cid "+cid+" for pid "+pid.toString().substring(30));
		send(pid, to, msg);
	}

	@Override
	public void listen(UUID pid, String to) {

	}

	public void setResponse(String response) {
		this.response = response;
	}

}

