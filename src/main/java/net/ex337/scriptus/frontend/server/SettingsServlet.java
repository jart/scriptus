package net.ex337.scriptus.frontend.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.Dao;
import net.ex337.scriptus.config.ScriptusConfig.Medium;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class SettingsServlet extends HttpServlet {

	private static final long serialVersionUID = -5553735938511251323L;
	
	private XmlWebApplicationContext ctx;

	@Override
	public void init() {

	    ctx = (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		if( ! req.getRemoteAddr().equals("127.0.0.1")) {
			resp.sendError(403);
		}
		
		req.setAttribute("config", ctx.getBean("config"));
		
		req.getRequestDispatcher("/WEB-INF/jsp/settings.jsp").forward(req, resp);
		return;
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ScriptusConfig cfg = (ScriptusConfig) ctx.getBean("config");
		
		cfg.setAwsAccessKeyId(req.getParameter("awsAccessKeyId"));
		cfg.setAwsSecretKey(req.getParameter("awsSecretKey"));
		cfg.setS3Bucket(req.getParameter("s3Bucket"));
		cfg.setTwitterAccessToken(req.getParameter("twitterAccessToken"));
		cfg.setTwitterAccessTokenSecret(req.getParameter("twitterAccessTokenSecret"));
		cfg.setTwitterConsumerKey(req.getParameter("twitterConsumerKey"));
		cfg.setTwitterConsumerSecret(req.getParameter("twitterConsumerSecret"));
		cfg.setDao(Dao.valueOf(req.getParameter("dao")));
		cfg.setMedium(Medium.valueOf(req.getParameter("medium")));
		
		cfg.save();
		
		ctx.refresh();
				
		resp.sendRedirect(req.getContextPath()+"/scripts/list");
	}

	
	
}
