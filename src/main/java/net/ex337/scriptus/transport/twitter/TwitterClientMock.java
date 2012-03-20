package net.ex337.scriptus.transport.twitter;

import java.util.ArrayList;
import java.util.List;

import net.ex337.scriptus.exceptions.ScriptusRuntimeException;

public class TwitterClientMock implements TwitterClient {
    
    public List<Tweet> mockTweets = new ArrayList<Tweet>();
    
    public List<String> statusUpdates = new ArrayList<String>();

	public TwitterClientMock() {
		//TODO load mocks
	}
	
	@Override
	public List<Tweet> getMentions() {

	    return mockTweets;
	    
	}
	
	private long ctr = 1;

	@Override
	public long tweet(String txt) {
	    
	    statusUpdates.add(txt);
		
		if(txt.length() > 140) {
			throw new ScriptusRuntimeException("tweet > 140 characters: "+txt);
		}
		
		return ctr++;
	}

	
	
}
