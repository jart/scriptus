package net.ex337.scriptus.transport;

import net.ex337.scriptus.model.api.ScriptusAPI;

/**
 * The interface defining how scripts can interact with users.
 * This interface is used by the API objects created in
 * {@link ScriptusAPI}.
 * 
 * @author ian
 *
 */
public interface Transport {
    
    /**
     * Sends the message to the user specified,
     * and returns the ID of the message sent.
     * 
     * The ID is an opaque string.
     * 
     */
    public String send(String to, String msg);
	
	

}
