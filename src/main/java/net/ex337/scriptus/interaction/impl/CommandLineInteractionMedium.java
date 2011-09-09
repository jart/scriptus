
package net.ex337.scriptus.interaction.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

import javax.annotation.PostConstruct;

import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.api.Message;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * An interaction medium that takesresponses to ask()s
 * and listen()s from the command-line. Synchronised to 
 * allow multi-threading. If you want to not respond to
 * an ask or listen, just hit enter (reply "").
 * 
 * @author ian
 *
 */
public class CommandLineInteractionMedium implements InteractionMedium {

	private static final Log LOG = LogFactory.getLog(CommandLineInteractionMedium.class);

	private MessageReceiver receiver;

	private BufferedReader r;

	@PostConstruct
	public void init() {
		r = new BufferedReader(new InputStreamReader(System.in));
	}
	
	//@Override
	/*
	 * synch to make command-line interaction thread-safe
	 */
	private synchronized void send(final UUID pid, final String to, final String msg) {
		
		//just sending, no response expected
		if(pid == null) {
			LOG.debug("send  to:"+to+" msg:"+msg);
			System.out.println("send to:"+to+" msg:"+msg);
			return;
		}
		
		
		LOG.debug("send "+ pid.toString()+" to:"+to+" msg:"+msg);
		System.out.println("send "+ pid.toString()+" to:"+to+" msg:"+msg);
		
		try {
			final String in = r.readLine();
			
			if(StringUtils.isNotEmpty(in)) {
				//would normally be async?
				receiver.handleIncomings(new ArrayList<Message>() {{
					add(new Message(pid, to, in));
				}});
			}
			
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

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
	public void listen(final UUID pid, String to) {
		send(pid, to, null);
	}

}
