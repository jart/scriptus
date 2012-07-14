package net.ex337.scriptus.transport.twitter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Twitter transport. Periodically polls Twitter for mentions
 * of the configured account's screen name and sends any tweets found to
 * the process scheduler and Scriptus processes.
 * 
 * Delegates the actual work of interfacing with Twitter to the wired-in
 * {@link TwitterClient}, so we can test Twitter-related logic in this
 * class offline.
 * 
 * @author ian
 *
 */
public class TwitterTransportImpl implements Transport {

	private static final Log LOG = LogFactory.getLog(TwitterTransportImpl.class);

	@Resource
	private MessageRouting routing;
	
	@Resource
	private ScriptusDatastore datastore;

	@Resource
	private ScriptusConfig config;
	
	@Resource(name="twitterClient")
	private TwitterClient twitter;

    private ScheduledExecutorService scheduledTwitterChecker;
    
    private String screenName;

    @PostConstruct
	public void init() {
		
		if(config.getTransportType() != TransportType.Twitter) {
			return;
		}

		scheduledTwitterChecker = new ScheduledThreadPoolExecutor(2);

		/*
		 * everything is converted into seconds
		 * so that we can avoid Calendar and use TimeUnit for everything.
		 */
		long pollIntervalSeconds = TimeUnit.SECONDS.convert(config.getSchedulerPollInterval(), config.getSchedulerTimeUnit());
		
		long delay = pollIntervalSeconds - (System.currentTimeMillis() /1000 % (pollIntervalSeconds/2));
		
		scheduledTwitterChecker.scheduleAtFixedRate(new Runnable(){

			@Override
			public void run() {
				try {
					TwitterTransportImpl.this.checkMessages();
				} catch(Exception e) {
					LOG.error("exception checking for messaged on Twitter", e);
				}
			}
			
		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);
		
		screenName = twitter.getScreenName();

	}

    @PreDestroy
	public void destroy() {
    	scheduledTwitterChecker.shutdown();
	}

	/**
	 * Retrieves all mentions from Twitter for the configured account's 
	 * screen name. Then checks all the tweets up to the last-checked
	 * tweet to see if (a) they have a correlation ID hashtag, or 
	 * (b) if the originating user is being listened to by a process.
	 * 
	 * public for testcases
	 * 
	 */
	public void checkMessages() {
		
		String lastMention = datastore.getTransportCursor(TransportType.Twitter);
		
        LOG.debug("lastm:"+snowflakeDate(getSecond(Long.parseLong(lastMention))));
		
		List<Tweet> mentions = twitter.getMentions();
	
		long ageThreshold = getAgeThreshold(lastMention);

		LOG.debug("ageThreshold:"+snowflakeDate(ageThreshold));

		if(mentions.isEmpty()) {
			return;
		}
		
		List<Message> incomings =  new ArrayList<Message>(); 
		Long lastProcessedIncoming = null;
		
		Collections.sort(mentions);
		
		for(final Tweet s : mentions) {
			/*
			 * dealt with this one
			 */
//			if(lastMentions.contains(s.getSnowflake())) {
//				continue;
//			}
			/*
			 * i.e. we've gone beyond the last mention and a bit beyond
			 */
			long snAgeSecs = getSecond(s.getSnowflake());
			
			LOG.debug("snAgeSecs:"+snowflakeDate(snAgeSecs));
	
			/*
			 * we've passed last processed tweet
			 */
			if(snAgeSecs < ageThreshold) {
				
				break;
			}

			Message m = new Message(s.getScreenName(), cleanTweet(s, screenName));
			if(s.getInReplyToId() != -1) {
			    m.setInReplyToMessageId("tweet:"+s.getInReplyToId());
			}
			
            incomings.add(m);
			
			
            lastProcessedIncoming = s.getSnowflake();
		}
		
		
		routing.handleIncomings(incomings);
		
		if( lastProcessedIncoming != null) {
			datastore.updateTransportCursor(TransportType.Twitter, Long.toString(lastProcessedIncoming));
		}
		
		
	}

	private String cleanTweet(Tweet s, String screenName) {
		/*
		 * remove trailing comments
		 */
		
		String text = s.getText();
		if(text.contains("//")) {
			text = text.substring(0, text.indexOf("//"));
		}
		
		
		/*
		 * strip mention
		 */
		text = StringUtils.replaceOnce(text, "@"+screenName, "");
		
		/*
		 * trim
		 */
		text = text.trim();
		return text;
	}
	
	private final DateFormat twitterDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/*
	 * for visual comparison purposes only
	 */
	private String snowflakeDate(long seconds) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(twitterDF.parse("01/01/2000 00:00:00"));
		} catch (ParseException e) {
			return e.getMessage();
		}
		if(seconds > Integer.MAX_VALUE) {
			return "too big";
		}
		c.add(Calendar.SECOND, (int) seconds);
		return twitterDF.format(c.getTime());
	}
	
	private long getAgeThreshold(String snowflake) {
		long current = Long.MIN_VALUE;
		
		//round down
		long second = getSecond(Long.parseLong(snowflake));
		
		//10-second window - one order of magnitude
		//outside of the 1-second window for twitters
		//k-sorted tweets
		
		if(second > current) {
			current = second;
		}

		/*
		 * Twitter says they k-sort things to within
		 * 1 second, so 10 seconds is a lot of leeway.
		 */
		if( current != Long.MIN_VALUE) {
			return current - 60;
		} else {
			return current;
		}
	}
	
	private long getSecond(long snowflake) {
		//zero-out the last 22 bits (technically not necessary)
		snowflake &= ~(0x3FFFFF);
		
		//shift right
		snowflake = snowflake >> 22;

		//round down
		return (snowflake - (snowflake % 1000)) / 1000L;
		
		//FIXME is all this equivalent to (snowflake - (snowflake % 0x3FFFFFF) / 0x3FFFFF?
	}


    @Override
    public String send(String to, String msg) {
        
        long id = twitter.tweet((to == null ? "" : "@"+to+" ")+msg);
        
        LOG.debug(id+" : "+"@"+to+" "+msg);
        
        return "tweet:"+id;
            
    }
}
