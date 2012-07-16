package net.ex337.scriptus.server.frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * Servlet reponsible for settings page. This servlet doesn't use
 * openID but will only respond to requests from 127.0.0.1.
 * 
 * The page is simple enough that it should be usable in links/lynx.
 * TODO A better security system might be a good idea in the future.
 *   
 * @author ian
 *
 */
public class SettingsServlet extends HttpServlet {

	private static final long serialVersionUID = -5553735938511251323L;
	private static final Log LOG = LogFactory.getLog(SettingsServlet.class);
	
	private XmlWebApplicationContext ctx;
	private Set<String> localAddresses = new HashSet<String>();

	@Override
	public void init() {
	    ctx = (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	    // Initializing local variable containing addresses associated with localhost
	    try {
			for (InetAddress inetAddress : InetAddress.getAllByName("localhost")) {
			    localAddresses.add(inetAddress.getHostAddress());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			LOG.error("Unable to lookup local addresses", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!localAddresses.contains(req.getRemoteAddr())) {
			resp.sendError(403);
			return;
		}

		if (req.getParameter("state") != null && req.getParameter("code") != null) {
			// Validate state
			ScriptusConfig cfg = (ScriptusConfig) ctx.getBean("config");
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(
					"https://graph.facebook.com/oauth/access_token?"
							+ "client_id=" + cfg.getFacebookAppKey()
							+ "&client_secret=" + cfg.getFacebookAppSecret()
							+ "&code=" + req.getParameter("code")
							+ "&redirect_uri=" + URLEncoder.encode("http://localhost:8080"
									+ req.getContextPath() + "/settings","UTF-8"));
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			// If there is some error during the execution
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
				InputStream instream = entity.getContent();
				try {

					BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					// do something useful with the response
					String responseBody = reader.readLine();
					Pattern p = Pattern.compile("access_token=(\\w*)&expires=(\\d*)");
					Matcher m = p.matcher(responseBody);
					if (m.find() && m.groupCount() == 2) {
						cfg.setFacebookAccessToken(m.group(1));
					} else {
						LOG.error("Problem parsing Facebook access token response=[" + responseBody + "]");
					}
				} catch (IOException ex) {
					// In case of an IOException the connection will be released
					// back to the connection manager automatically
					throw ex;
				} catch (RuntimeException ex) {
					// In case of an unexpected exception you may want to abort
					// the HTTP request in order to shut down the underlying
					// connection and release it back to the connection manager.
					httpget.abort();
					throw ex;
				} finally {
					// Closing the input stream will trigger connection release
					instream.close();
				}

				// When HttpClient instance is no longer needed,
				// shut down the connection manager to ensure
				// immediate deallocation of all system resources
				httpclient.getConnectionManager().shutdown();
			} else {
				LOG.info("Failure exchanging facebook code for a facebook user access token. Request=["
						+ httpget.getURI().toString()
						+ "] Status code=["
						+ response.getStatusLine().getStatusCode()
						+ "] Reason=["
						+ response.getStatusLine().getReasonPhrase() + "]");
			}
		}
		req.setAttribute("config", ctx.getBean("config"));

		req.getRequestDispatcher("/WEB-INF/jsp/settings.jsp").forward(req, resp);
		return;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (!localAddresses.contains(req.getRemoteAddr())) {
			resp.sendError(403);
			return;
		}

		ScriptusConfig cfg = (ScriptusConfig) ctx.getBean("config");
		
		cfg.setAwsAccessKeyId(req.getParameter("awsAccessKeyId"));
		cfg.setAwsSecretKey(req.getParameter("awsSecretKey"));
		cfg.setS3Bucket(req.getParameter("s3Bucket"));
		cfg.setTwitterAccessToken(req.getParameter("twitterAccessToken"));
		cfg.setTwitterAccessTokenSecret(req.getParameter("twitterAccessTokenSecret"));
		cfg.setTwitterConsumerKey(req.getParameter("twitterConsumerKey"));
		cfg.setTwitterConsumerSecret(req.getParameter("twitterConsumerSecret"));
		cfg.setDatastoreType(DatastoreType.valueOf(req.getParameter("datastore")));
		cfg.setTransportType(TransportType.valueOf(req.getParameter("transport")));
		cfg.setFacebookAppKey(req.getParameter("facebookAppKey"));
		cfg.setFacebookAppSecret(req.getParameter("facebookAppSecret"));
		cfg.setFacebookAccessToken(req.getParameter("facebookAccessToken"));
		
		cfg.save();
		
		ctx.refresh();
				
		resp.sendRedirect(req.getContextPath()+"/scripts/list");
	}

}
