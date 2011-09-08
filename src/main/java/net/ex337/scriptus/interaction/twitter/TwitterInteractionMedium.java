package net.ex337.scriptus.interaction.twitter;

import java.math.BigInteger;
import java.security.SecureRandom;
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
import net.ex337.scriptus.config.ScriptusConfig.Medium;
import net.ex337.scriptus.dao.ScriptusDAO;
import net.ex337.scriptus.dao.TwitterCorrelation;
import net.ex337.scriptus.interaction.InteractionMedium;
import net.ex337.scriptus.model.api.Message;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TwitterInteractionMedium implements InteractionMedium {

	private static final int MAX_CID = 0xFFFFFF;

	private static final Log LOG = LogFactory.getLog(TwitterInteractionMedium.class);

	@Resource
	private ScriptusDAO dao;

	@Resource
	private ScriptusConfig config;
	
	private MessageReceiver londonCalling;
	
	private ScheduledExecutorService scheduledTwitterChecker;
	
	private SecureRandom rnd = new SecureRandom();

    //private Twitter twitter;

	private TwitterClient twitter;
	
    @PostConstruct
	public void init() {
		
		if(config.getMedium() != Medium.Twitter) {
			return;
		}
		
		twitter = new TwitterClientImpl(config);

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
					TwitterInteractionMedium.this.checkMessages();
				} catch(Exception e) {
					LOG.error("exception checking for messaged on Twitter", e);
				}
			}
			
		}, delay, pollIntervalSeconds, TimeUnit.SECONDS);

	}

    @PreDestroy
	public void destroy() {
    	scheduledTwitterChecker.shutdown();
	}

	@Override
	public void registerReceiver(MessageReceiver londonCalling) {
		this.londonCalling = londonCalling;
	}


	protected void checkMessages() {
		
		List<Long> lastMentions = dao.getTwitterLastMentions();
		
		for(Long l : lastMentions) {
			System.out.println("lastm:"+snowflakeDate(getSecond(l)));
		}
		
		List<Tweet> mentions = twitter.getMentions();
	
		
		long ageThreshold = getAgeThreshold(lastMentions);

		System.out.println("ageThreshold:"+snowflakeDate(ageThreshold));

		if(mentions.isEmpty()) {
			return;
		}
		
		List<Message> incomings =  new ArrayList<Message>(); 
		List<String> correlationsToUnregister =  new ArrayList<String>(); 
		//a bit ugly...
		List<Object[]> listenersToUnregister =  new ArrayList<Object[]>(); 
		List<Long> processedIncomings =  new ArrayList<Long>(); 
		
		Collections.sort(mentions);
		
		for(final Tweet s : mentions) {
			/*
			 * dealt with this one
			 */
			if(lastMentions.contains(s.getSnowflake())) {
				continue;
			}
			/*
			 * i.e. we've gone beyond the last mention and a bit beyond
			 */
			long snAgeSecs = getSecond(s.getSnowflake());
			
			System.out.println("snAgeSecs:"+snowflakeDate(snAgeSecs));
	
			/*
			 * we've passed last processed tweet
			 */
			if(snAgeSecs < ageThreshold) {
				
				break;
			}
			
			boolean foundPid = false;
			
			for(final String e : s.getHashtags()) {
				
				/*
				 * if in reply to ask()
				 */
				
				TwitterCorrelation c = dao.getTwitterCorrelationByID(e);

				/*
				 * if I've setup scriptus with my own account,
				 * we need to make sure that 'request' and 'reply'
				 * tweets don't get mixed up
				 */
				if(c != null && 
				   s.getSnowflake() != c.getSourceSnowflake() && 
				   s.getScreenName().equals(c.getUser())) {
						incomings.add(new Message(c.getPid(), s.getScreenName(), cleanTweet(s, e, c.getUser())));
						correlationsToUnregister.add(e);
						foundPid = true;
				}
			}
			
			/*
			 * Then we need to check that a pid might be listening to
			 * this user. There might be one or more pids listening to the same user,
			 * so we put listeners in a stack and pop them on a FIFO basis.
			 */
			if( ! foundPid ) {
				UUID pid = dao.getMostRecentTwitterListener(s.getScreenName());
				if(pid != null) {
					incomings.add(new Message(pid, s.getScreenName(), cleanTweet(s, null, s.getScreenName())));
					listenersToUnregister.add(new Object[]{pid, s.getScreenName()});
					foundPid = true;
				}
				
			}
			
			processedIncomings.add(s.getSnowflake());
		}
		
		londonCalling.handleIncomings(incomings);
		
		if( ! processedIncomings.isEmpty()) {
			dao.updateTwitterLastMentions(processedIncomings);
		}
		for(String s : correlationsToUnregister) {
			dao.unregisterTwitterCorrelation(s);
		}
		for(Object[] o : listenersToUnregister) {
			dao.unregisterTwitterListener((UUID)o[0], (String)o[1]);
		}
		
		
	}

	private String cleanTweet(Tweet s, final String cid, String screenName) {
		/*
		 * remove trailing comments
		 */
		
		String text = s.getText();
		if(text.contains("//")) {
			text = text.substring(0, text.indexOf("//"));
		}
		
		/*
		 * remove hashtag
		 */
		if(cid != null) {
			text = StringUtils.remove(text, "#"+cid);
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
	
	private long getAgeThreshold(List<Long> snowflakes) {
		long current = Long.MIN_VALUE;
		
		for(long snowflake : snowflakes) {

			//round down
			long second = getSecond(snowflake);
			
			//10-second window - one order of magnitude
			//outside of the 1-second window for twitters
			//k-sorted tweets
			
			if(second > current) {
				current = second;
			}
			
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
	public void say(String to, String msg) {
		
		long id = twitter.tweet("@"+to+" "+msg);
		
		System.out.println(id+" : "+"@"+to+" "+msg);
			
	}

	@Override
	public void ask(UUID pid, String to, String msg) {
		
		String next = transformNumber(new BigInteger(Integer.toString(Math.abs(rnd.nextInt(MAX_CID)))), 62);

		LOG.info("registering cid "+next+" for pid "+pid.toString().substring(30));

		long id = twitter.tweet("@"+to+" #"+next+" "+msg);

		dao.registerTwitterCorrelation(new TwitterCorrelation(pid, to, next, id));

	}

	@Override
	public void listen(UUID pid, String to) {
		
		dao.registerTwitterListener(pid, to);
		

	}

	public static String transformNumber(BigInteger decimal, final int baseInt) {
		
		char[] alphabet = new char[baseInt];
		int aNum = 0;
		for(int i = '0'; i != '9'+1 && aNum < baseInt; i++) {
			alphabet[aNum++] = (char) i;
		}
		if(aNum < baseInt) for(int i = 'A'; i != 'Z'+1 && aNum < baseInt; i++) {
			alphabet[aNum++] = (char) i;
		}
		if(aNum < baseInt) for(int i = 'a'; i != 'z'+1 && aNum < baseInt; i++) {
			alphabet[aNum++] = (char) i;
		}
		
		if(baseInt > aNum ) {
			throw new RuntimeException("Run out of alphabet for base "+baseInt);
		}
		//populate
		
		StringBuilder b = new StringBuilder();
		
		final BigInteger firstBase = new BigInteger(Integer.toString(baseInt));
		
		BigInteger base = new BigInteger(firstBase.toByteArray());

		BigInteger lastBase = BigInteger.ONE;
		
		for(int pow = 2; ; pow++) {
			
			BigInteger digit = decimal.mod(base);
			
			char c = alphabet[digit.divide(lastBase).intValue()];
			
			b.append(c);
			
			decimal = decimal.subtract(digit);
			
			if(decimal.equals(BigInteger.ZERO)) {
				break;
			}
			
			lastBase = base;
			base = firstBase.pow(pow);
			
		}
		
		return b.reverse().toString();
		
	}

	public static void main(String[] args) {
		
		/*
		 * 10^3  1000 * 4 = 4000
		 * 10^2   100 * 2 =  200
		 * 10^1    10 * 3 =   30
		 * 10^0     1 * 5 =    5
		 * 
		 * 4235    = 
		 * 
		 * 
		 * 2^7  128 * 1 = 128
		 * ...
		 * ...
		 * ...
		 * 2^3    8 * 1 =   8
		 * 2^2    4 * 0 =   0
		 * 2^1    2 * 1 =   2
		 * 2^0    1 * 1 =   1
		 * 			    	____
		 * 			       139
		 * 
		 * 
		 * 10001011
		 *  1000000
		 */
		SecureRandom r = new SecureRandom();
		for(int i = 0; i != 10; i++) {
			
			String s = Long.toString(Math.abs(r.nextLong()));
			
			System.out.println(s);
			
			BigInteger b= new BigInteger(s);

			System.out.println(transformNumber(b, 10));
			System.out.println(transformNumber(b, 62));
		}
		
		
	}
	
}
