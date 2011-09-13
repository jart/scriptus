package net.ex337.scriptus.interaction;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.Medium;

/**
 * An interaction medium implementation that does 
 * nothing  but proxy to whatever implementation has been 
 * configured in {@link ScriptusConfig}.
 * 
 * In order for a configuration change to take effect,
 * the container must be restarted (calling init() again).
 * This is done automatically if you change the config
 * via the web interface.
 * 
 */
public class InteractionMediumSwitch implements InteractionMedium {
	
	@Resource(name="twitterInteraction")
	private InteractionMedium twitter;
	
	@Resource(name="cmdLineInteraction")
	private InteractionMedium cmdLine;

	@Resource(name="dummyInteraction")
	private InteractionMedium dummy;

	private InteractionMedium activeImpl;
	
	@Resource
	private ScriptusConfig config;
	
	@PostConstruct
	public void init() {
		switchMedium(config.getMedium());
	}

	public void switchMedium(Medium medium) {
		if(medium == Medium.Twitter) {
			activeImpl = twitter;
		} else if(medium == Medium.CommandLine) {
			activeImpl = cmdLine;
		} else if(medium == Medium.Dummy) {
			activeImpl = dummy;
		}			
	}

	public void say(String to, String msg) {
		activeImpl.say(to, msg);
	}

	public void ask(UUID pid, String to, String msg) {
		activeImpl.ask(pid, to, msg);
	}

	public void listen(UUID pid, String to) {
		activeImpl.listen(pid, to);
	}

	public void registerReceiver(MessageReceiver londonCalling) {
		activeImpl.registerReceiver(londonCalling);
	}

	
}