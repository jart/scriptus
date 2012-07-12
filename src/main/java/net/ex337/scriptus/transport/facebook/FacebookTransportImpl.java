package net.ex337.scriptus.transport.facebook;

import java.util.ArrayList;
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
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.transport.Transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FacebookTransportImpl implements Transport {

	private static final Log LOG = LogFactory
			.getLog(FacebookTransportImpl.class);

	@Resource
	private ScriptusDatastore datastore;

	@Resource
	private ScriptusConfig config;

	@Resource(name = "facebookClient")
	private FacebookClientInterface facebook;

	private MessageReceiver londonCalling;

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
		long id = facebook.publish(to, msg); // TODO Don't publish if already
												// present !!
		LOG.debug(id + " : " + "@" + to + " " + msg);
		return "facebook:" + id;
	}

	@Override
	public void listen(UUID pid, String to) {
		datastore.registerTwitterListener(pid, to);
	}

	@Override
	public void registerReceiver(MessageReceiver londonCalling) {
		this.londonCalling = londonCalling;
	}

	public void checkMessages() {
		// Get processed mentions
		List<Long> lastMentions = datastore.getTwitterLastMentions();
		// Get the time of the most recent processed post (whose comments where
		// already processed)
		Long lastMentionTime = facebook.getTime(lastMentions);

		// Get recent posts
		List<FacebookPost> recentPosts = facebook
				.getRecentPosts(lastMentionTime);

		List<Message> incomings = new ArrayList<Message>();
		List<String> correlationsToUnregister = new ArrayList<String>();
		List<Object[]> listenersToUnregister = new ArrayList<Object[]>();
		// This one holds the mentionId's of the recent mentions
		List<Long> processedIncomings = new ArrayList<Long>();

		// Loop over recent posts
		for (FacebookPost post : recentPosts) {
			// if (lastMentions.contains(post.getId().split("_")[1])) {
			// This post was already processed, get next !!
			// continue;
			// }
			// Check if there is a listening process related to this user
			UUID pid = datastore.getMostRecentTwitterListener(post
					.getScreenName());
			// If there is such a process then add a new incoming
			if (pid != null) {
				incomings.add(new Message(pid, post.getScreenName(), post
						.getText()));
				listenersToUnregister.add(new Object[] { pid,
						post.getScreenName() });
			}
			processedIncomings.add(Long.valueOf(post.getId().split("_")[1]));
		}

		// If there are no new posts i will continue pending on comments on
		// previously processed posts
		// if (recentPosts.isEmpty()) {
		// Therefore including the last mentions as processed incomings
		processedIncomings.addAll(lastMentions);
		// }

		/*
		 * Condition to search for comments in previous posts done by the
		 * scriptus-transport facebook application
		 */

		if (lastMentionTime != null) {
			// Loop over previous mentions (they may have comments that some
			// process is waiting for)
			// comments for the recent mentions (processedIncomings) will be
			// done the next time this method is called
			// Process comments only for registered posts after the first
			// handling of incomings
			List<FacebookPost> comments = new ArrayList<FacebookPost>();
			for (Long mentionId : lastMentions) {
				comments.addAll(facebook.getPostComments(mentionId));
			}
			comments.addAll(facebook.getPostReplies());
			for (FacebookPost comment : comments) {
				Long commentedPostId = Long
						.valueOf(comment.getId().split("_")[1]);
				Long commentId = Long.valueOf(comment.getId().split("_")[2]);
				if (!lastMentions.contains(Long.valueOf(commentId))) {
					TwitterCorrelation c = datastore
							.getTwitterCorrelationByID("facebook:"
									+ commentedPostId);
					if (c != null)
					// && (c.getUser() == null || (c.getUser() != null &&
					// post.getScreenName().equals(c.getUser())))
					// si el dueño del post no es igual al user del c
					{
						incomings.add(new Message(c.getPid(), comment
								.getScreenName(), comment.getText()));
						correlationsToUnregister.add("facebook:" + commentId);
					}
					processedIncomings.add(commentId);
				}
			}
		}

		londonCalling.handleIncomings(incomings);

		if (!processedIncomings.isEmpty()) {
			// TODO Useful to change this to store Strings or Objects, with the
			// actual way, i may confuse post with comment
			datastore.updateTwitterLastMentions(processedIncomings);
		}
		for (String s : correlationsToUnregister) {
			datastore.unregisterTwitterCorrelation(s);
		}
		for (Object[] o : listenersToUnregister) {
			datastore.unregisterTwitterListener((UUID) o[0], (String) o[1]);
		}
	}
}
