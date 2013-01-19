package net.ex337.scriptus.transport.twitter;

import java.util.ArrayList;
import java.util.List;

import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.TransportAccessToken;

public class TwitterClientMock implements TwitterClient {
    
    /*
     * these are static because the client is now a per-user bean...
     */
    
    public static List<Tweet> mockTweets = new ArrayList<Tweet>();
    
    public static List<String> statusUpdates = new ArrayList<String>();

    public String screenName = "mock";
    
	public TwitterClientMock() {
		//TODO load mocks
	}
	
	@Override
	public List<Tweet> getMentions() {

	    return mockTweets;
	    
	}
	
	private long ctr = System.currentTimeMillis();

	@Override
	public long tweet(String txt) {
	    
	    statusUpdates.add(txt);
		
		if(txt.length() > 140) {
			throw new ScriptusRuntimeException("tweet > 140 characters: "+txt);
		}
		
		return ctr++;
	}

    @Override
    public String getScreenName() {
        return screenName;
    }

    @Override
    public void setCredentials(TransportAccessToken token) {
        //don't need this
    }

	
	
}
