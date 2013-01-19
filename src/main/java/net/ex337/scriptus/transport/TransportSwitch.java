package net.ex337.scriptus.transport;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;

/**
 * A transport implementation that does 
 * nothing  but proxy to whatever implementation has been 
 * configured in {@link ScriptusConfig}.
 * 
 * In order for a configuration change to take effect,
 * the container must be restarted (calling init() again).
 * This is done automatically if you change the config
 * via the web interface.
 * 
 */
public class TransportSwitch implements Transport {
	
	@Resource(name="twitterTransport")
	private Transport twitter;
	
	@Resource(name="cmdLineTransport")
	private Transport cmdLine;

	@Resource(name="dummyTransport")
	private Transport dummy;

	private Transport activeImpl;
	
	@Resource
	private ScriptusConfig config;
	
	@PostConstruct
	public void init() {
		switchMedium(config.getTransportType());
	}

	public void switchMedium(TransportType transportType) {
		if(transportType == TransportType.Twitter) {
			activeImpl = twitter;
		} else if(transportType == TransportType.CommandLine) {
			activeImpl = cmdLine;
		} else if(transportType == TransportType.Dummy) {
			activeImpl = dummy;
		}			
	}

    public String send(String userId, String to, String msg) {
        return activeImpl.send(userId, to, msg);
    }

	
}
