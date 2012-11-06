package net.ex337.scriptus.server.frontend;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;

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
public class WelcomeServlet extends HttpServlet {

	private static final long serialVersionUID = -5553735938511251323L;
	
	private XmlWebApplicationContext ctx;

	@Override
	public void init() {

	    ctx = (XmlWebApplicationContext) WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		req.getRequestDispatcher("/WEB-INF/jsp/welcome.jsp").forward(req, resp);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
        req.getRequestDispatcher("/WEB-INF/jsp/welcome.jsp").forward(req, resp);
        
	}

	
	
}
