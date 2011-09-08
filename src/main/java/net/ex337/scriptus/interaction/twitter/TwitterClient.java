package net.ex337.scriptus.interaction.twitter;

import java.util.List;

public interface TwitterClient {
	
	public List<Tweet> getMentions();
	
	public long tweet(String txt);

}
