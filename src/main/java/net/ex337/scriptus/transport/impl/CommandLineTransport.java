
package net.ex337.scriptus.transport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * 
 * An transport that takes responses to ask()s
 * and listen()s from the command-line. Synchronised to 
 * allow multi-threading. If you want to not respond to
 * an ask or listen, just hit enter (reply "").
 * 
 * @author ian
 *
 */
public class CommandLineTransport implements Transport {

    
    @Resource
    private MessageRouting routing;
    
    @Resource
    private ScriptusDatastore datastore;
    
    @Resource
    private ScriptusConfig config;
    
	private static final Log LOG = LogFactory.getLog(CommandLineTransport.class);

    private boolean halted;
    private boolean composing;
    private boolean newFlag;
    private long composingTo;
    
    private AtomicLong ctr = new AtomicLong();
    
    private Map<Long,String> sentMessages;
    
	private BufferedReader r;

	@PostConstruct
	public void init() {
	    
	    if(config.getTransportType() != TransportType.CommandLine) {
	        return;
	    }
	    
		r = new BufferedReader(new InputStreamReader(System.in));
		
		String lastCursor = datastore.getTransportCursor(TransportType.CommandLine);
		if(lastCursor != null) try {
		    ctr.set(Long.parseLong(lastCursor)+1);
		} catch(NumberFormatException nfe) {
		    //then it's not a number and we'll replace it.
		}
		
		sentMessages = new MapMaker().makeMap();
		
		new Thread(){
		    {
		        setDaemon(true);
		    }
		    public void run() {
		        CommandLineTransport.this.replLoop();
		    }
		}.start();
	}
	
	@PreDestroy
	public void destroy() {
	    halted = true;
	}
	
	//@Override
	/*
	 * synch to make command-line interaction thread-safe
	 */
	@Override
	public String send(final String to, final String msg) {
	    
	    long c = ctr.getAndIncrement();
	    
	    datastore.updateTransportCursor(TransportType.CommandLine, Long.toString(ctr.get()));
	    
	    final String id = "cmd:"+c;
		
	    sentMessages.put(c, msg);
	    
	    newFlag = true;
	    
	    return id;
				
	}
	
	private void replLoop() {
        prompt();
	    while( ! halted) try {
	        if( ! r.ready()) {
	            Thread.sleep(100);
	            continue;
	        } 
            String s = r.readLine();
	        if(composing) {
	            if( ! s.contains(":")){
	                System.out.println("Message format: \"user:this is a message to user\"");
	                composing = false;
	            } else {
	                int i = s.indexOf(":");
                    String to = s.substring(0, i);
                    String msg = s.substring(i+1);
	                Message m = new Message(to, msg);
	                if(composingTo != -1) {
	                    m.setInReplyToMessageId("cmd:"+composingTo);
	                }
	                List<Message> mm = new ArrayList<Message>();
	                mm.add(m);
	                routing.handleIncomings(mm);
	                System.out.println("OK");
	                composing = false;
	            }
            } else if("new".equalsIgnoreCase(s)) {
                composing = true;
                    composingTo = -1;
            } else if("ls".equalsIgnoreCase(s)) {
	            List<Long> l = new ArrayList<Long>(this.sentMessages.keySet());
	            Collections.sort(l);
	            for(Long ll : l) {
	                System.out.println(ll+": "+sentMessages.get(ll));
	            }
	        } else {
	            try{
	                composingTo = Integer.parseInt(s);
	                System.out.println("type message in format 'user:message'");
	                composing = true;
	            } catch(NumberFormatException nfe) {
	                System.out.println("Usage:\nls  - list messages,\nnew - write a new message.\nEnter a message # to compose a reply.");
	            }
	        }
            prompt();
        } catch(IOException e) {
            LOG.error("IOException in comand line transport", e);
            break;
        } catch(InterruptedException e) {
            LOG.error("InerruptedException in comand line transport", e);
            break;
	    }
	}

    private void prompt() {
        if(newFlag) {
            System.out.print("!");
            newFlag = false;
        }
        if(composing) {
            System.out.print("@");
        } else {
            System.out.print(">");
        }
    }

}
