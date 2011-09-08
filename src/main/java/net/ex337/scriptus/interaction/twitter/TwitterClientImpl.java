package net.ex337.scriptus.interaction.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.exceptions.InteractionException;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClientImpl implements TwitterClient {

    private Twitter twitter;
    
    private String screenName;

    public TwitterClientImpl(ScriptusConfig config) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(config.getTwitterConsumerKey())
		.setOAuthConsumerSecret(config.getTwitterConsumerSecret())
		.setOAuthAccessToken(config.getTwitterAccessToken())
		.setOAuthAccessTokenSecret(config.getTwitterAccessTokenSecret())
		.setIncludeEntitiesEnabled(true);

		twitter = new TwitterFactory(cb.build()).getInstance();
		
		try {
			screenName = twitter.getScreenName();
		} catch (IllegalStateException e) {
			throw new ScriptusRuntimeException(e);
		} catch (TwitterException e) {
			throw new ScriptusRuntimeException(e);
		}
    }
    
	@Override
	public List<Tweet> getMentions() {
		
		/*
		 * to avoid duplicates (we have to search and get mentions,
		 * because mentions by themselves are broken  / sometimes
		 * don't update on a timely basis.
		 */
		
		Set<Tweet> result = new TreeSet<Tweet>();
		//FIXME or 200 mentions = DOS
		try {
			List<Status> mentions = twitter.getMentions();
			
			Query q = new Query("@"+screenName);
			
			QueryResult r = twitter.search(q);
			
			for(Status s : mentions) {
				result.add(new Tweet(s.getId(), s.getText(), s.getUser().getScreenName()));
			}
			
			for(twitter4j.Tweet t : r.getTweets()) {
				//compute hashcodes like in mockimpl
				result.add(new Tweet(t.getId(), t.getText(), t.getFromUser()));
			}
			
		} catch (TwitterException e) {
			throw new InteractionException(e);
		}
		
		return new ArrayList<Tweet>(result);
	}

	@Override
	public long tweet(String txt) {
		
		if(txt.length() > 140) {
			throw new ScriptusRuntimeException("tweet > 140 characters: "+txt);
		}
		
		Status s;
		try {
			s = twitter.updateStatus(new StatusUpdate(txt));
		} catch (TwitterException e) {
			throw new ScriptusRuntimeException(e);
		}
		return s.getId();
	}

}
