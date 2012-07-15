package net.ex337.scriptus.transport.facebook;

import java.util.List;

public interface FacebookClientInterface {

	/**
	 * 
	 * @param sinceTime
	 * @return
	 */
	public List<FacebookPost> getRecentPosts(Long sinceTime);

	/**
	 * Returns the comments of a post
	 * 
	 * @param postId
	 *            facebook id of a post
	 * @param sinceTime
	 * @return a list of comments in reply to a post
	 */
	public List<FacebookPost> getPostComments(String postId, Long sinceTime);

	/**
	 * Returns recent replies to posts of mine.
	 * 
	 * @return a list of replies to my facebook posts
	 */
	public List<FacebookPost> getPostReplies();

	/**
	 * Returns the creation time of a mention (post/comment)
	 * 
	 * @param mentionId
	 *            identifier of the mention (post/comment)
	 * @return Unix timestamp of the mention's creation time
	 */
	public Long getTime(String mentionId);

	/**
	 * Publishes a message to in a user's feed or in my own feed
	 * 
	 * @param to
	 *            username of the message receiver
	 * @param message
	 *            the message to publish
	 * @return facebook object id of the published message
	 */
	public String publish(String to, String message);

}
