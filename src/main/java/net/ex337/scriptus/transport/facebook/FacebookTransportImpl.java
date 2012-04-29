package net.ex337.scriptus.transport.facebook;

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
	private FacebookClient facebook;

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
					/*
					 * Used as a cursor to keep track of the tweets we've
					 * already processed.
					 * 
					 * @return a list of tweet IDs for tweets processed at the
					 * last poll of the Twitter API.
					 */
					// getTwitterLastMentions

					/*
					 * @see #registerTwitterCorrelation(TwitterCorrelation)
					 */
					// getTwitterCorrelationByID

					/*
					 * Used for tracking processes listening to twitter users.
					 * Because no correlation ID is sent to the user, if
					 * multiple processes listen to the same user, they receive
					 * that users mentions in the reverse order in which the
					 * processes listened.
					 * 
					 * Once a process has received a mention from a user, or if
					 * the listen() times out, the listener is unregistered.
					 */
					// getMostRecentTwitterListener

					/*
					 * @see #getTwitterLastMentions()
					 */
					// updateTwitterLastMentions

					/*
					 * @see #registerTwitterCorrelation(TwitterCorrelation)
					 */
					// unregisterTwitterCorrelation

					/*
					 * see {@link #getMostRecentTwitterListener(String)}
					 */
					// unregisterTwitterListener

				} catch (Exception e) {
					LOG.error("exception checking for messaged on Twitter", e);
				}
			}

		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);

	}

	@PreDestroy
	public void destroy() {
		scheduler.shutdown();
	}

	@Override
	public long send(String to, String msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void listen(UUID pid, String to) {
		/*
		 * see {@link #getMostRecentTwitterListener(String)}
		 */
		// registerTwitterListener
	}

	@Override
	public void registerReceiver(MessageReceiver londonCalling) {
		this.londonCalling = londonCalling;
	}

}
