package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * 
 * Servlet reponsible for home page, the first thing user sees when logged in
 *   
 * @author ian
 *
 */
public class HomeServlet extends BaseServlet {

	private static final long serialVersionUID = -5553735938511251323L;
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String user) throws ServletException, IOException {

	    ScriptusConfig f = (ScriptusConfig) ctx.getBean("config");

	    if(f.isCleanInstall()) {
	        req.setAttribute("clean", Boolean.TRUE);
	    }
	    	    
        Set<String> scripts = ((ScriptusDatastore) ctx.getBean("datastore")).listScripts(user);
        
        if(scripts.isEmpty()){
            req.setAttribute("noscripts", Boolean.TRUE);
        }
        
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
		
	}

	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String user) throws ServletException, IOException {
		
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
        
	}

    @Override
    protected String getPageLabel() {
        return "";
    }

	
	
}
