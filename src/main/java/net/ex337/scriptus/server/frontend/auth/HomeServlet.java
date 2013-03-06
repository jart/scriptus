package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.server.ScriptusHeadlineReader;

/**
 * 
 * Servlet reponsible for home page, the first thing user sees when logged in
 *   
 * @author ian
 *
 */
public class HomeServlet extends BaseServlet {

	private static final long serialVersionUID = -5553735938511251323L;
    
    private ScriptusHeadlineReader r;
    
    @Override
    public void init() {
        super.init();
        r = (ScriptusHeadlineReader) ctx.getBean("scriptusHeadlineReader");
    }
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String user) throws ServletException, IOException {


	    if(f.isCleanInstall()) {
	        req.setAttribute("clean", Boolean.TRUE);
	    }
	    
	    int countScripts = d.countSavedScripts(user);
	    int countProcesses = d.countRunningProcesses(user);
	    	    
        if(countScripts == 0){
            req.setAttribute("noscripts", Boolean.TRUE);
        }
        req.setAttribute("countScripts", countScripts);
        req.setAttribute("countProcesses", countProcesses);
        
        req.setAttribute("lastNewsItemHeadline", r.getLastNewsItemHeadline());
        req.setAttribute("lastNewsItemLink", r.getLastNewsItemLink());
        
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
