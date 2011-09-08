package net.ex337.scriptus.interaction;

import java.util.List;
import java.util.UUID;

import net.ex337.scriptus.model.api.Message;



public interface InteractionMedium {
	
	void say(String to, String msg);
	void ask(UUID pid, String to, String msg);
	void listen(UUID pid, String to);
	
	void registerReceiver(MessageReceiver londonCalling);
	
	public static interface MessageReceiver {
		public void handleIncomings(List<Message> incomings);
	}

}
