package net.ex337.scriptus.transport.facebook;

import java.util.List;

public interface FacebookClientInterface {

	public String getScreenName();

	/**
	 * 
	 * @param untilTime
	 * @return
	 */
	public List<FacebookPost> getRecentPosts(Long untilTime);

	/**
	 * Returns the comments of a post in my own wall
	 * 
	 * @param postId
	 * @return
	 */
	public List<FacebookPost> getPostComments(Long postId);

	/**
	 * Return the time of the last processed post
	 * 
	 * @param id
	 * @return
	 */
//	public Long getTime(Long id);
	public Long getTime(List<Long> lastMentions);

	public long publish(String to, String message);

	public List<FacebookPost> getPostReplies();

}
