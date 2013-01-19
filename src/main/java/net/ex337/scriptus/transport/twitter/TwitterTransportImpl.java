package net.ex337.scriptus.transport.twitter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.TransportAccessToken;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * The Twitter transport. Periodically polls Twitter for mentions of the
 * configured account's screen name and sends any tweets found to the process
 * scheduler and Scriptus processes.
 * 
 * Delegates the actual work of interfacing with Twitter to the wired-in
 * {@link TwitterClient}, so we can test Twitter-related logic in this class
 * offline.
 * 
 * @author ian
 * 
 */
public abstract class TwitterTransportImpl implements Transport {

    private static final Log LOG = LogFactory.getLog(TwitterTransportImpl.class);

    @Resource
    private MessageRouting routing;

    @Resource
    private ScriptusDatastore datastore;

    @Resource
    private ScriptusConfig config;

//    @Resource(name = "twitterClient")
//    private TwitterClient twitter;

    private ScheduledExecutorService scheduledTwitterChecker;

    private Map<String,String> screenNameCache;

    @PostConstruct
    public void init() {
        
        screenNameCache = new MapMaker().expireAfterAccess(config.getSchedulerPollInterval()*2, config.getSchedulerTimeUnit()).makeMap();

        if (config.getTransportType() != TransportType.Twitter) {
            return;
        }

        scheduledTwitterChecker = new ScheduledThreadPoolExecutor(2);

        /*
         * everything is converted into seconds so that we can avoid Calendar
         * and use TimeUnit for everything.
         */
        long pollIntervalSeconds = TimeUnit.SECONDS.convert(config.getSchedulerPollInterval(),
                config.getSchedulerTimeUnit());

        long delay = pollIntervalSeconds - (System.currentTimeMillis() / 1000 % (pollIntervalSeconds / 2));

        scheduledTwitterChecker.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    TwitterTransportImpl.this.checkMessages();
                } catch (Exception e) {
                    LOG.error("exception checking for messaged on Twitter", e);
                }
            }

        }, delay, pollIntervalSeconds, TimeUnit.SECONDS);


    }

    @PreDestroy
    public void destroy() {
        scheduledTwitterChecker.shutdown();
    }

    /**
     * Retrieves all mentions from Twitter for the configured account's screen
     * name. Then checks all the tweets up to the last-checked tweet to see if
     * (a) they have a correlation ID hashtag, or (b) if the originating user is
     * being listened to by a process.
     * 
     * public for testcases
     * 
     */
    public void checkMessages() {

        /*
         * We store the last processed mentions as well as the
         * "transport cursor" because the twitter ID (which contains a
         * timestamp) is k-sorted, i.e. each ID can differ within 1 second + or
         * - so we need to retrieve not just the tweets after the last one but
         * also potentially tweets before the last one- so we need to keep track
         * of this.
         */
        List<String> userIds = datastore.getListeningCorrelations(TransportType.Twitter);
        
        for(String userId : userIds) {
            TransportAccessToken accessToken = datastore.getAccessToken(userId, TransportType.Twitter);

            String screenName = screenNameCache.get(userId);
            
            TwitterClient twitter = getTwitterClient(accessToken);
            
            if(screenName == null) {
                screenNameCache.put(userId, screenName = twitter.getScreenName());
            }

            @SuppressWarnings("unchecked")
            List<String> lastMentions = new ArrayList<String>();
            try {
                String cursor = datastore.getTransportCursor(TransportType.Twitter);
                if(cursor != null) {
                    lastMentions = (List<String>)SerializableUtils.deserialiseObject(cursor.getBytes(ScriptusConfig.CHARSET));
                }
            } catch (IOException e) {
                LOG.warn("couldn't get twitter cursor", e);
            } catch (ClassNotFoundException e) {
                LOG.warn("couldn't get twitter cursor", e);
            }
            
            Long lastMention = Long.MIN_VALUE;
            
            if( ! lastMentions.isEmpty()) {
                String lastMentionStr = lastMentions.get(lastMentions.size()-1);
                lastMention = Long.parseLong(lastMentionStr.substring(lastMentionStr.indexOf(":")+1));
            }
            

            LOG.debug("lastm:" + (lastMention == null ? "null" : snowflakeDate(getSecond(lastMention))));

            List<Tweet> mentions = twitter.getMentions();

            long ageThreshold = getAgeThreshold(lastMention);

            LOG.debug("ageThreshold:" + snowflakeDate(ageThreshold));

            if (mentions.isEmpty()) {
                return;
            }

            List<Message> incomings = new ArrayList<Message>();

            Collections.sort(mentions);

            List<String> processedIncomings = new ArrayList<String>();

            for (final Tweet s : mentions) {

                String messageId = "tweet:" + s.getSnowflake();
                /*
                 * dealt with this one
                 */
                if (lastMentions.contains(messageId)) {
                    continue;
                }
                /*
                 * i.e. we've gone beyond the last mention and a bit beyond
                 */
                long snAgeSecs = getSecond(s.getSnowflake());

                LOG.debug("snAgeSecs:" + snowflakeDate(snAgeSecs));

                /*
                 * we've passed last processed tweet
                 */
                if (snAgeSecs <= ageThreshold) {

                    break;
                }

                Message m = new Message(s.getScreenName(), cleanTweet(s, screenName), s.getCreation(), userId);
                if (s.getInReplyToId() != -1) {
                    m.setInReplyToMessageId("tweet:" + s.getInReplyToId());
                }

                incomings.add(m);

                processedIncomings.add(messageId);

            }

            routing.handleIncomings(incomings);

            if (!processedIncomings.isEmpty()) {
                try {
                    datastore.updateTransportCursor(TransportType.Twitter, new String(SerializableUtils.serialiseObject(processedIncomings), ScriptusConfig.CHARSET));
                } catch (IOException e) {
                    throw new ScriptusRuntimeException(e);
                }
            }
            
        }



    }

    private String cleanTweet(Tweet s, String screenName) {
        /*
         * remove trailing comments
         */

        String text = s.getText();
        if (text.contains("//")) {
            text = text.substring(0, text.indexOf("//"));
        }

        /*
         * strip mention
         */
        text = StringUtils.replaceOnce(text, "@" + screenName, "");

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
        if (seconds > Integer.MAX_VALUE) {
            return "too big";
        }
        c.add(Calendar.SECOND, (int) seconds);
        return twitterDF.format(c.getTime());
    }

    private long getAgeThreshold(Long snowflake) {
        long current = Long.MIN_VALUE;

        if (snowflake == null) {
            return current;
        }

        // round down
        long second = getSecond(snowflake);

        // 10-second window - one order of magnitude
        // outside of the 1-second window for twitters
        // k-sorted tweets

        if (second > current) {
            current = second;
        }

        /*
         * Twitter says they k-sort things to within 1 second, so 10 seconds is
         * a lot of leeway.
         */
        if (current != Long.MIN_VALUE) {
            return current - 60;
        } else {
            return current;
        }
    }

    private long getSecond(long snowflake) {
        // zero-out the last 22 bits (technically not necessary)
        snowflake &= ~(0x3FFFFF);

        // shift right
        snowflake = snowflake >> 22;

        // round down
        return (snowflake - (snowflake % 1000)) / 1000L;

        // FIXME is all this equivalent to (snowflake - (snowflake % 0x3FFFFFF)
        // / 0x3FFFFF?
    }

    @Override
    public String send(String userId, String to, String msg) {

        TransportAccessToken accessToken = datastore.getAccessToken(userId, TransportType.Twitter);

        TwitterClient twitter = getTwitterClient(accessToken);
        
        long id = twitter.tweet((to == null ? "" : "@" + to + " ") + msg);

        LOG.debug(id + " : " + "@" + to + " " + msg);

        return "tweet:" + id;

    }
    
    private TwitterClient getTwitterClient(TransportAccessToken token) {
        TwitterClient c = createTwitterClient();
        c.setCredentials(token);
        return c;
    }
    
    protected abstract TwitterClient createTwitterClient();
}
