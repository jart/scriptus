package net.ex337.scriptus.transport.twitter;

import java.util.List;

/**
 * The interface I use to isolate the Twitter integration
 * from Twitter-related logic.
 * 
 * @author ian
 *
 */
public interface TwitterClient {
	
	public List<Tweet> getMentions();
	
	public long tweet(String txt);

}
