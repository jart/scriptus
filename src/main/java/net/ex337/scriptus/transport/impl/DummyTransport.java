
package net.ex337.scriptus.transport.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
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

    @Resource
    private MessageRouting routing;
	
    private Map<String,String> regexpResponseMatchers = new HashMap<String, String>();
    private Map<Pattern,String> cachedPatterns = new HashMap<Pattern, String>();
	
	public String defaultResponse;


   private AtomicLong ctr = new AtomicLong();

	public void init() {
        cachedPatterns = new HashMap<Pattern, String>();
	    
	    for(Map.Entry<String, String> e : regexpResponseMatchers.entrySet()) {
	        Pattern p = Pattern.compile(e.getKey());
	        cachedPatterns.put(p, e.getValue());
	        
	    }
	    
	}
	
	@Override
	public String send(String userId, final String to, final String msg) {

        final String id = "dummy:"+ctr.getAndIncrement();
        
        new Thread() {
            public void run() {
                try {
                  //so that the wrapper method returns first.
                    sleep(200);
                } catch (InterruptedException e) {
                    //TNSH;
                    throw new RuntimeException(e);
                }
                Message m = new Message(to, getResponse(msg), System.currentTimeMillis(), "dummy_user");
                m.setInReplyToMessageId(id);
                
                List<Message> responseList = new ArrayList<Message>();
                responseList.add(m);

                //this should be done after the function returns!
                routing.handleIncomings(responseList);
                
            }
        }.start();
		
        return id;

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

	public void setDefaultResponse(String response) {
		this.defaultResponse = response;
	}

    public void setRegexpResponseMatchers(Map<String, String> regexpResponseMatchers) {
        this.regexpResponseMatchers = regexpResponseMatchers;
    }

}

