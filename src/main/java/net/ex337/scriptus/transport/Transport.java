package net.ex337.scriptus.transport;

import java.util.List;
import java.util.UUID;

import net.ex337.scriptus.model.api.Message;
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
     */
    public long send(String to, String msg);
	
	/**
	 * Routes the next message from the given user to the process
	 * identified by the pid. The message if any will be handled
	 * via the registered {@link MessageReceiver}.
	 */
	void listen(UUID pid, String to);
	
	/**
	 * Registers the given receiver as being the endpoint
	 * for all incoming messages for this transport.
	 * 
	 * There should normally only be one registration per instance.
	 * 
	 * @param londonCalling
	 */
	void registerReceiver(MessageReceiver londonCalling);
	
	/**
	 * 
	 * The interface for code wishing to receive messages from
	 * this transport, see above.
	 * 
	 * @author ian
	 *
	 */
	public static interface MessageReceiver {
		/**
		 * The implementor should handle each message in a
		 * separate try-catch loop and not throw anything
		 * to the caller of this method.
		 * 
		 * @param incomings
		 */
		public void handleIncomings(List<Message> incomings);
	}

}
