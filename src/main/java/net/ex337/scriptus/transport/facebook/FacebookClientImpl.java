package net.ex337.scriptus.transport.facebook;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.Parameter;
import com.restfb.json.JsonObject;
import com.restfb.types.Comment;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;
import com.restfb.types.User;

public class FacebookClientImpl implements FacebookClientInterface {

	private static final Log LOG = LogFactory.getLog(FacebookClientImpl.class);

	private FacebookClient facebookClient = null;

	@Resource
	private ScriptusConfig config;

	public FacebookClientImpl() {
//		this.init();
	}

	@PostConstruct
	public void init() {
		facebookClient = new DefaultFacebookClient(
//				"AAADC80HpsZAYBABTRlc5jb6jGHNIXbtlZCFIRLKXlgbfruUcN5zSeLmsaMRcqmb9jM996Eihu16SZCZAF7m7GmIlwUbcDczGYLftEzTl5QZDZD");
				config.getFacebookAccessToken());
	}

	@Override
	public String getScreenName() {
		User user = facebookClient.fetchObject("me", User.class);
		return user.getName();
	}

	@Override
	public List<FacebookPost> getRecentPosts(Long untilTime) {
		List<FacebookPost> mentions = new ArrayList<FacebookPost>();

		/**
		 * Get mentions
		 */
		Connection<Post> connections = null;
		List<Post> tags = new ArrayList<Post>();
		List<Post> posts = new ArrayList<Post>();
		if (untilTime == null) {
//			connections = facebookClient.fetchConnection("me/tagged", Post.class);
//			tags = new ArrayList<Post>(connections.getData());
			connections = facebookClient.fetchConnection("me/posts", Post.class);
			posts = connections.getData();
			tags.addAll(posts);
		} else {
//			connections = facebookClient.fetchConnection("me/tagged", Post.class, Parameter.with("limit", "25"), Parameter.with("since", untilTime));
//			tags = new ArrayList<Post>(connections.getData());
			connections = facebookClient.fetchConnection("me/posts", Post.class, Parameter.with("limit", "25"), Parameter.with("since", untilTime));
			posts = connections.getData();
			tags.addAll(posts);
		}
		for (Post p : tags) {
			// Ghhr !! facebook graph api is not working well !! until parameter is not being taken in count, i am handling it
			if (p.getMessage() != null && (untilTime == null || untilTime != null && Long.valueOf(p.getCreatedTime().getTime()/1000L).compareTo(untilTime) >= 0)) {
				User sender = facebookClient.fetchObject(p.getFrom().getId(), User.class);
				String screenName = sender.getUsername().isEmpty() ? sender.getId() : sender.getUsername();
				FacebookPost fbp = new FacebookPost(p.getId(), p.getMessage(), screenName, p.getCreatedTime().getTime());
				mentions.add(fbp);
			}
		}
		// When untiltime is null limit results to the most recent one
		if(untilTime == null){
			// Retain last post and discard all others
			FacebookPost[] lastPostToRetain = { Collections.max(mentions) };
			mentions.retainAll(Arrays.asList(lastPostToRetain));
		}
		return mentions;
	}

	@Override
	// TODO handle comment retrieving of comments of posts on somebody else wall
	public List<FacebookPost> getPostComments(Long postId) {
		User user = facebookClient.fetchObject("me", User.class);
		List<FacebookPost> messageReplies = new ArrayList<FacebookPost>();
		Connection<JsonObject> connections = facebookClient.fetchConnection(
				user.getId() + "_" + postId + "/comments", JsonObject.class);
		List<JsonObject> objs = connections.getData();
		for (JsonObject obj : objs) {
			JsonMapper jsonMapper = new DefaultJsonMapper();
			Comment c = jsonMapper.toJavaObject(obj.toString(), Comment.class);
			if (c.getMessage() != null) {
				User sender = facebookClient.fetchObject(c.getFrom().getId(), User.class);
				String screenName = sender.getUsername().isEmpty() ? sender.getId() : sender.getUsername();
				FacebookPost fbp = new FacebookPost(c.getId(), c.getMessage(), screenName, c.getCreatedTime().getTime());
				messageReplies.add(fbp);
			}
		}
		return messageReplies;
	}

	// The comment retrieving is wrong cause the comment ids form is userid_postid_commentid and not userid_commentid
	@Override
//public Long getTime(Long id) {
	public Long getTime(List<Long> lastMentions) {
		if(lastMentions.isEmpty()){
			return null;
		}
		String userId = facebookClient.fetchObject("me", User.class).getId();
		List<Long> postsTime = new ArrayList<Long>();
		for(Long id: lastMentions){
			Post p = facebookClient.fetchObject(userId + "_" + id, Post.class);
			if (p == null) {
				continue;
			} else {
				postsTime.add(p.getCreatedTime().getTime() / 1000L);
			}
		}
		return Collections.max(postsTime);
//		if(p!=null){
//			return p.getCreatedTime().getTime()/1000L;
//		}else{
//			Comment c = jsonMapper.toJavaObject(obj.toString(), Comment.class);
//			if(c!=null){
//				return c.getCreatedTime().getTime()/1000L;
//			}else{
//				return null;
//			}
//		}
	}

	@Override
	public long publish(String to, String message) {
//		String messageToPost = "@[" + user.getId() + ":1:" + user.getName() + "] " + message;
		if (to != null) {
			User user = facebookClient.fetchObject(to, User.class);
			FacebookType publishMessageResponse = facebookClient.publish(user.getId() + "/feed", FacebookType.class, Parameter.with("message", message));
			return Long.valueOf(publishMessageResponse.getId().split("_")[1]);
		} else {
			FacebookType publishMessageResponse = facebookClient.publish("me/feed", FacebookType.class, Parameter.with("message", message));
			return Long.valueOf(publishMessageResponse.getId().split("_")[1]);
		}
	}

	/**
	 * facebook object id of the comment to which a notification is referring to
	 * 
	 * @param postId
	 *            the facebook id of the post where to look for the comment id
	 * @param notif
	 *            the notification
	 * @return the id of the comment or null if not found
	 */
	private String getCommentId(Post p, Notification notif) {
		// Iterate over post's comments looking for the one which notification is referring to
		for (Comment comment: p.getComments().getData()) {
			Calendar commentCalendar = new GregorianCalendar();
			commentCalendar.setTime(comment.getCreatedTime());
			Calendar notifCalendar = new GregorianCalendar();
			notifCalendar.setTime(notif.getCreatedTime());
			// Check if the comment is the right one
			if (comment.getFrom().equals(notif.getFrom())
					&& commentCalendar.get(Calendar.DAY_OF_YEAR) == notifCalendar.get(Calendar.DAY_OF_YEAR)
					&& commentCalendar.get(Calendar.HOUR_OF_DAY) == notifCalendar.get(Calendar.HOUR_OF_DAY)
					&& commentCalendar.get(Calendar.MINUTE) == notifCalendar.get(Calendar.MINUTE)) {
				return comment.getId();
			}
		}
		return null;
	}

	public List<FacebookPost> getPostReplies() {
		List<FacebookPost> postReplies = new ArrayList<FacebookPost>();

		// Fetch all the new notifications
		List<Notification> notifications = facebookClient.fetchConnection("me/notifications", Notification.class).getData();

		// Iterate over them looking for notifications of replies to a comment
		for (Notification notif : notifications) {
			// Check if the notification refer to the feed comment application
			if (notif.getApplication().getId().equals("19675640871")) {
				// Check if the post (to which the notification refers to) is mine
				String[] linkPathSplitted = notif.getLink().split("/");
				String postId = notif.getTo().getId() + "_" + linkPathSplitted[linkPathSplitted.length - 1];
				Post p = facebookClient.fetchObject(postId, Post.class);
				if (p == null) {
					// If the post is not mine we skip to the next notification
					continue;
				}
				// Get comment id
				String comment_id = getCommentId(p, notif);
				// Get replier's screen name
				String replierScreenName = facebookClient.fetchObject(notif.getFrom().getId(), User.class).getUsername();
				// Construct facebook post object and return
				FacebookPost fbp = new FacebookPost(comment_id.toString(),
						notif.getMessage(),
						replierScreenName,
						notif.getCreatedTime().getTime());
				postReplies.add(fbp);
				// TODO Log this response
				Boolean publishMessageResponse = facebookClient.publish(notif.getId(), Boolean.class, Parameter.with("unread", "0"));
			}
		}
		return postReplies;
	}

	public static void main(String[] args) throws MalformedURLException {
		FacebookClientImpl facebook = new FacebookClientImpl();
		List<FacebookPost> comments = facebook.getPostReplies();
//		for (FacebookPost c: comments) {
//			System.out.println(c.getId());
//			System.out.println(c.getScreenName());
//			System.out.println(c.getText());
//			System.out.println(c.getCreationTimestamp());
//		}
	}
}
