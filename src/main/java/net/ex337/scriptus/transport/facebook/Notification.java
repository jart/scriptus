package net.ex337.scriptus.transport.facebook;

import static com.restfb.util.DateUtils.toDateFromLongFormat;

import java.util.Date;

import com.restfb.Facebook;
import com.restfb.types.NamedFacebookType;

public class Notification extends NamedFacebookType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5238833909999405640L;

	@Facebook
	private NamedFacebookType from;

	@Facebook
	private NamedFacebookType to;

	@Facebook("created_time")
	private String createdTime;

	@Facebook
	private String message;

	@Facebook
	private String link;

	@Facebook
	private NamedFacebookType application;

	/**
	 * User who posted the comment.
	 * 
	 * @return User who posted the comment.
	 */
	public NamedFacebookType getFrom() {
		return from;
	}

	/**
	 * The user who receives the comment.
	 * 
	 * @return User who receives the comment.
	 */
	public NamedFacebookType getTo() {
		return to;
	}

	/**
	 * Date on which the comment was created.
	 * 
	 * @return Date on which the comment was created.
	 */
	public Date getCreatedTime() {
		return toDateFromLongFormat(createdTime);
	}

	/**
	 * Text contents of the comment.
	 * 
	 * @return Text contents of the comment.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * The link attached to this post.
	 * 
	 * @return The link attached to this post.
	 */
	public String getLink() {
		return link;
	}

	/**
	 * The application used to create this post.
	 * 
	 * @return The application used to create this post.
	 */
	public NamedFacebookType getApplication() {
		return application;
	}

}
