package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.TransportAccessToken;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;

/**
 * 
 * Servlet responsible for doing Twitter oAuth token requesting and saving.
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class TwitterConnectionServlet extends BaseServlet {

    private static final long serialVersionUID = -2332232512812118451L;

    private static final String SOCIAL_AUTH_MANAGER = "socialAuthManager";
    
    private ScriptusConfig config;
    
    private SocialAuthConfig socialAuthConfig;
    
    private boolean notConfigured;
    
    @Override
    public void init() {
        super.init();
        
        config = (ScriptusConfig) super.ctx.getBean("config");
        
        Properties socialAuthProps = new Properties();
        
        if(config.getTwitterConsumerKey() == null) {
            notConfigured = true;
            return;
        }
        
        socialAuthProps.put("twitter.com.consumer_key", config.getTwitterConsumerKey());
        socialAuthProps.put("twitter.com.consumer_secret", config.getTwitterConsumerSecret());
        
        socialAuthConfig = SocialAuthConfig.getDefault();
        try{
            socialAuthConfig.load(socialAuthProps);
        } catch(Exception e)  {
            throw new ScriptusRuntimeException(e);
        }
        

    }
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
		
		String path = req.getPathInfo();
		
	    SocialAuthManager manager = (SocialAuthManager) req.getSession().getAttribute(SOCIAL_AUTH_MANAGER);
	    
	    if(manager == null) {
	        /*
	         * then either the session expired or the user is getting the 
	         * link they were posted to.
	         */
	        resp.sendRedirect(req.getContextPath()+"/connect");
	    }
	    
	    Map<String, String> paramsMap = SocialAuthUtil.getRequestParametersMap(req); 
	    
	    AuthProvider provider;
        try {
            provider = manager.connect(paramsMap);
        } catch (Exception e) {
            throw new ScriptusRuntimeException(e);
        }

        String tokenKey = provider.getAccessGrant().getKey();
        String tokenSecret = provider.getAccessGrant().getSecret();

        TransportAccessToken twitterT = new TransportAccessToken(openid, TransportType.Twitter, tokenKey, tokenSecret);
        
        ((ScriptusDatastore)ctx.getBean("datastore")).saveTransportAccessToken(twitterT);
        
        req.getSession().removeAttribute(SOCIAL_AUTH_MANAGER);
        
        resp.sendRedirect(req.getContextPath()+"/connect");

	}

	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {

        SocialAuthManager manager = new SocialAuthManager();
        try {
            
            manager.setSocialAuthConfig(socialAuthConfig);
            
            String domain = req.getServletPath();
            
            String successURL = domain+"/twitter";
            
            String url = manager.getAuthenticationUrl(TransportType.Twitter.toString().toLowerCase(), successURL);
            
            req.getSession().setAttribute(SOCIAL_AUTH_MANAGER, manager);

            resp.sendRedirect(successURL);
            
        } catch (Exception e) {
            throw new ScriptusRuntimeException(e);
        }
        
	}

    @Override
    protected String getPageLabel() {
        return "scripts";
    }

}
