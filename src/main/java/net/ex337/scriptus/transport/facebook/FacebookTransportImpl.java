package net.ex337.scriptus.transport.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import net.ex337.scriptus.model.TransportAccessToken;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;
import net.ex337.scriptus.transport.twitter.TwitterClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;

import com.restfb.exception.FacebookOAuthException;

public abstract class FacebookTransportImpl implements Transport {

	private static final Log LOG = LogFactory
			.getLog(FacebookTransportImpl.class);

	@Resource
	private ScriptusDatastore datastore;

	@Resource
	private ScriptusConfig config;

	@Resource
	private MessageRouting routing;

	private ScheduledExecutorService scheduler;

	@PostConstruct
	public void init() {

		scheduler = new ScheduledThreadPoolExecutor(2);

		long pollIntervalSeconds = TimeUnit.SECONDS.convert(
				config.getSchedulerPollInterval(),
				config.getSchedulerTimeUnit());

		long delay = pollIntervalSeconds
				- (System.currentTimeMillis() / 1000 % (pollIntervalSeconds / 2));

		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					FacebookTransportImpl.this.checkMessages();
				} catch (FacebookOAuthException e1) {
                    //FIXME!
					LOG.error("Facebook access token became invalid, proceeding to delete it from my records in order to stop errors on the requests and prevent strange behaviors.", e1);
					//config.setFacebookAccessToken("");
					// Actually i don't have any way to communicate the user
					// that its token is now invalid :S
					// I can only delete it from my records and expect the user
					// to renew it
					try {
						config.save();
					} catch (IOException e2) {
						LOG.error("There was an error while saving the scriptus configuration after deleting saved facebook access token.", e2);
					}
				} catch (Exception e) {
					LOG.error("exception checking for messages on Facebook" + this.getClass().getSimpleName(), e);
				}
			}

		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);

	}

	@PreDestroy
	public void destroy() {
		scheduler.shutdown();
	}

	@Override
	public String send(String userId, String to, String msg) {
	    
	    
	    FacebookTransportClient facebook = null;/*FIXME*/
	    
		String id = facebook.publish(to, msg);
		LOG.debug(id + " : " + "@" + to + " " + msg);
		return "facebook:" + id;
	}

	@SuppressWarnings("unchecked")
	public void checkMessages() {
	    
        List<String> userIds = datastore.getListeningCorrelations(TransportType.Facebook);
        
        for(String userId : userIds) {
            TransportAccessToken accessToken = datastore.getAccessToken(userId, TransportType.Facebook);

            FacebookTransportClient facebook = getFacebookClient(accessToken);

            
            LOG.info("Start checkMessages");
            // Get most recently processed posts and most recently processed mention
            // (post/comment)
            String lastMention = null;
            List<String> processedPosts = new ArrayList<String>();
            try {
                String cursor = datastore
                        .getTransportCursor(TransportType.Facebook);
                if (cursor != null) {
                    processedPosts = (List<String>) SerializableUtils
                            .deserialiseObject(Base64.decode(cursor));
                    lastMention = processedPosts.get(0);
                    processedPosts.remove(0);
                }
            } catch (IOException e) {
                LOG.error("Error while decoding/deserializing processed post", e);
            } catch (ClassNotFoundException e) {
                LOG.error("Error while decoding/deserializing processed post", e);
            }

            // Get the time of the most recently processed mention (post/comment)
            Long lastMentionTime = facebook.getTime(lastMention);

            List<FacebookMention> mentions = new ArrayList<FacebookMention>();
            // Get recent posts since the last mention time
            List<FacebookMention> recentPosts = facebook
                    .getRecentPosts(lastMentionTime);
            mentions.addAll(recentPosts);
            // Get recent comments on previous posts
            // Comments on post in other people's feed
            List<FacebookMention> repliesInOtherFeeds = facebook.getPostReplies();
            mentions.addAll(repliesInOtherFeeds);
            // Comments on posts in my own feed
            List<FacebookMention> repliesInMyFeed = new ArrayList<FacebookMention>();
            for (String postId : processedPosts) {
                List<FacebookMention> postComments = facebook.getPostComments(postId,
                        lastMentionTime);
                repliesInMyFeed.addAll(postComments);
            }
            mentions.addAll(repliesInMyFeed);

            // This one holds the associations mentions/process
            List<Message> incomings = new ArrayList<Message>();
            // This one holds the mentionId of last processed mention
            String lastProcessedIncoming = null;
            // This one holds the mentionId's of the last processed posts
            List<String> lastProcessedPosts = new ArrayList<String>();

            // Ordering from the oldest to the newest, that way when post and reply
            // to that arrive at the same time they can be processed in the correct
            // order, furthermore the lastProcessedIncoming is set to the last
            // processed mention
            Collections.sort(mentions);

            // Loop over recent posts
            for (FacebookMention mention : mentions) {
                if (lastMentionTime != null
                        && lastMentionTime
                                .compareTo(mention.getCreationTimestamp() / 1000L) > 0) {
                    continue;
                }
                Message m = new Message(mention.getScreenName(), mention.getText(), System.currentTimeMillis(), userId, TransportType.Facebook);
                if (mention.getInReplyToId() != FacebookMention.DEFAULT_REPLY_TO) {
                    // It is a comment
                    m.setInReplyToMessageId("facebook:" + mention.getInReplyToId());
                } else {
                    // It is a post
                    lastProcessedPosts.add(mention.getId());
                }
                incomings.add(m);
                lastProcessedIncoming = mention.getId();
            }

            // If there are no new posts i will continue pending on comments on
            // previously processed posts, therefore including the last processed
            // posts as processed incomings
            // if (recentPosts.isEmpty()) {
            // processedIncomings.addAll(lastMentions);
            // }
            lastProcessedPosts.addAll(processedPosts);

            routing.handleIncomings(incomings);

            if (lastProcessedIncoming != null) {
                lastProcessedPosts.add(0, lastProcessedIncoming);
                try {
                    datastore.updateTransportCursor(TransportType.Facebook, Base64
                            .encode(SerializableUtils
                                    .serialiseObject(lastProcessedPosts)));
                } catch (IOException e) {
                    LOG.error("Error while serializing list of last processed posts");
                }
            }

        }
	}
	
    private FacebookTransportClient getFacebookClient(TransportAccessToken token) {
        FacebookTransportClient c = createFacebookClient();
        c.setCredentials(token);
        return c;
    }
    
    protected abstract FacebookTransportClient createFacebookClient();
	
}
