package net.ex337.scriptus.transport.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;

public class FacebookTransportImpl implements Transport {

	private static final Log LOG = LogFactory
			.getLog(FacebookTransportImpl.class);

	@Resource
	private ScriptusDatastore datastore;

	@Resource
	private ScriptusConfig config;

	@Resource(name = "facebookClient")
	private FacebookClientInterface facebook;

	@Resource
	private MessageRouting routing;

	private ScheduledExecutorService scheduler;

	@PostConstruct
	public void init() {

		if (config.getTransportType() != TransportType.Facebook) {
			return;
		}

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
				} catch (Exception e) {
					LOG.error("exception checking for messages on Facebook"
							+ this.getClass().getSimpleName(), e);
				}
			}

		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);

	}

	@PreDestroy
	public void destroy() {
		scheduler.shutdown();
	}

	@Override
	public String send(String to, String msg) {
		// TODO Don't publish if already present !!
		String id = facebook.publish(to, msg);
		LOG.debug(id + " : " + "@" + to + " " + msg);
		return "facebook:" + id;
	}

	@SuppressWarnings("unchecked")
	public void checkMessages() {
		// Get most recently processed mention (post/comment)
		List<String> processedPosts = new ArrayList<String>();
		try {
			processedPosts = (List<String>) SerializableUtils
					.deserialiseObject(Base64.decode(datastore
							.getTransportCursor(TransportType.Facebook)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String lastMention = processedPosts.get(0);
		processedPosts.remove(0);

		// Get the time of the most recently processed mention (post/comment)
		Long lastMentionTime = facebook.getTime(lastMention);

		List<FacebookPost> mentions = new ArrayList<FacebookPost>();
		// Get recent posts since the last mention time
		List<FacebookPost> recentPosts = facebook
				.getRecentPosts(lastMentionTime);
		mentions.addAll(recentPosts);
		// Get recent comments on previous posts
		// Comments on post in other people's feed
		List<FacebookPost> repliesInOtherFeeds = facebook.getPostReplies();
		mentions.addAll(repliesInOtherFeeds);
		// Comments on posts in my own feed
		List<FacebookPost> repliesInMyFeed = new ArrayList<FacebookPost>();
		for (String postId : processedPosts) {
			List<FacebookPost> postComments = facebook.getPostComments(postId,
					lastMentionTime);
			repliesInMyFeed.addAll(postComments);
		}

		// This one holds the mentions to process in association to its related
		// process
		List<Message> incomings = new ArrayList<Message>();
		// This one holds the mentionId's of the recent mentions
		String lastProcessedIncoming = null;
		List<String> lastProcessedPosts = new ArrayList<String>();

		Collections.sort(mentions);

		// Loop over recent posts
		for (FacebookPost mention : mentions) {
			if (lastMentionTime.compareTo(mention.getCreationTimestamp()) > 0) {
				continue;
			}
			Message m = new Message(mention.getScreenName(), mention.getText());
			if (mention.getInReplyToId() != FacebookPost.DEFAULT_REPLY_TO) {
				m.setInReplyToMessageId(mention.getInReplyToId());
			} else {
				lastProcessedPosts.add(mention.getId());
			}
			incomings.add(m);
			lastProcessedIncoming = mention.getId();
		}

		// If there are no new posts i will continue pending on comments on
		// previously processed posts
		// if (recentPosts.isEmpty()) {
		// Therefore including the last mentions as processed incomings
		// processedIncomings.addAll(lastMentions);
		// }

		routing.handleIncomings(incomings);

		if (lastProcessedIncoming != null) {
			lastProcessedPosts.add(0, lastProcessedIncoming);
			try {
				datastore.updateTransportCursor(TransportType.Facebook, Base64
						.encode(SerializableUtils
								.serialiseObject(lastProcessedPosts)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
