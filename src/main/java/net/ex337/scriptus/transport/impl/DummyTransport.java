
package net.ex337.scriptus.transport.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dummy transport. Never responds to listen()s, and
 * always responds to ask()s with the supplied defaultResponse.
 * 
 * The one defaultResponse configured in the Spring configuration file.
 * TODO A regexp-based matching might be a good idea to make testing script branches easier.
 */
public class DummyTransport implements Transport {

	private static final Log LOG = LogFactory.getLog(DummyTransport.class);

	private MessageReceiver receiver;
	
    private Map<String,String> regexpResponseMatchers = new HashMap<String, String>();
    private Map<Pattern,String> cachedPatterns = new HashMap<Pattern, String>();
	
	public String defaultResponse;
	
	public void init() {
        cachedPatterns = new HashMap<Pattern, String>();
	    
	    for(Map.Entry<String, String> e : regexpResponseMatchers.entrySet()) {
	        Pattern p = Pattern.compile(e.getKey());
	        cachedPatterns.put(p, e.getValue());
	        
	    }
	    
	}
	
	//@Override
	private void send(final UUID pid, final String to, final String msg) {

		LOG.debug("send "+ (pid == null ? "" : pid)+" to:"+to+" msg:"+msg);
		
		if(pid == null) {
			return;
		}
		
		List<Message> responseList = new ArrayList<Message>();
		responseList.add(new Message(pid, to, getResponse(msg)));

		receiver.handleIncomings(responseList);

	}

	/**
	 * public for testing
	 * @param msg
	 * @return
	 */
    public String getResponse(final String msg) {
       
        String result = null;
        
        for(Map.Entry<Pattern,String> e : cachedPatterns.entrySet()) {
		    Matcher m;
		    if((m = e.getKey().matcher(msg)).matches()) {
	            result = m.replaceAll(e.getValue());
		        break;
		    }
		    
		}
		
		if(result == null) result = defaultResponse;
        return result;
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

	public void setDefaultResponse(String response) {
		this.defaultResponse = response;
	}

    public void setRegexpResponseMatchers(Map<String, String> regexpResponseMatchers) {
        this.regexpResponseMatchers = regexpResponseMatchers;
    }

}

