package net.ex337.scriptus.transport.twitter;


public class Tweet implements Comparable<Tweet> {

    private long snowflake;
    private long creation;
	private long inReplyToId;
	private String text;
	private String screenName;

    public Tweet(long snowflake, String text, String screenName) {
        this(snowflake, text, screenName, -1);
    }

	public Tweet(long snowflake, String text, String screenName, long inReplyTo) {
		super();
		this.snowflake = snowflake;
		this.text = text;
		this.screenName = screenName;
		this.inReplyToId = inReplyTo;
	}
	
	public long getSnowflake() {
		return snowflake;
	}
	public String getText() {
		return text;
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
		/*
		 * default sort is DESCENDING, newest first.
		 */
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

    public long getInReplyToId() {
        return inReplyToId;
    }

    public void setInReplyToId(long inReplyToId) {
        this.inReplyToId = inReplyToId;
    }

    public long getCreation() {
        return creation;
    }

    public void setCreation(long creation) {
        this.creation = creation;
    }

	

}
