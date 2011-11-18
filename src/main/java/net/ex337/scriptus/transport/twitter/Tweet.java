package net.ex337.scriptus.transport.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tweet implements Comparable<Tweet> {
	
	private static final Pattern HASHTAG_REGEXP = Pattern.compile("#([A-Za-z0-9]*)");
	
	private long snowflake;
	private String text;
	private String screenName;
	private List<String> hashtags = new ArrayList<String>();
	
	public Tweet(long snowflake, String text, String screenName) {
		super();
		this.snowflake = snowflake;
		this.text = text;
		this.screenName = screenName;
		
		Matcher m = HASHTAG_REGEXP.matcher(text);
		while(m.find()) {
			hashtags.add(m.group(1));
		}
	}
	
	public long getSnowflake() {
		return snowflake;
	}
	public String getText() {
		return text;
	}
	public List<String> getHashtags() {
		return hashtags;
	}
	public String getScreenName() {
		return screenName;
	}
	
	public String toString() {
		return "tweet from "+screenName+":"+snowflake+":"+text;
	}

	@Override
	public int compareTo(Tweet t) {
		if(t == null) {
			return 1;
		}
		return - Long.valueOf(snowflake).compareTo(t.getSnowflake());
	}
	public int hashCode() {
		return Long.valueOf(snowflake).hashCode();
	}
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if( ! (o instanceof Tweet)) {
			return false;
		}
		if(snowflake == ((Tweet)o).getSnowflake()) {
			return true;
		}
		return false;
	}

	

}
