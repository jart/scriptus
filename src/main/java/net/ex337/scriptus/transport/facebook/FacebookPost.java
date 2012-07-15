package net.ex337.scriptus.transport.facebook;

public class FacebookPost implements Comparable<FacebookPost> {

	private String id;
	private String screenName;
	private String text;
	private long creationTimestamp;
	private String inReplyToId;
	public static final String DEFAULT_REPLY_TO = null;

	public FacebookPost(String id, String text, String screenName, long creationTimestamp, String inReplyToId) {
		super();
		this.id = id;
		this.inReplyToId = inReplyToId;
		this.text = text;
		this.creationTimestamp = creationTimestamp;
		this.screenName = screenName;
	}

	public FacebookPost(String id, String text, String screenName, long creationTimestamp) {
		this(id, text, screenName, creationTimestamp, String.valueOf(FacebookPost.DEFAULT_REPLY_TO));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public String getInReplyToId() {
		return inReplyToId;
	}

	public void setInReplyToId(String inReplyToId) {
		this.inReplyToId = inReplyToId;
	}

	@Override
	public int compareTo(FacebookPost p) {
		if (p == null) {
			return 1;
		}
		return Long.valueOf(this.creationTimestamp).compareTo(p.getCreationTimestamp());
	}

}
