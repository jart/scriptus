package net.ex337.scriptus.server.frontend;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.scheduler.ProcessScheduler;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;

/**
 * 
 * Servlet responsible for CRUDL admin tasks on scripts.
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class ScriptsServlet extends BaseServlet {

	private static final long serialVersionUID = 50869801033071491L;
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
		
		String path = req.getPathInfo();

        req.setAttribute("config", ctx.getBean("config"));

		if("/list/yours".equals(path) || "/list".equals(path)){

			Set<String> scripts = ((ScriptusDatastore) ctx.getBean("datastore")).listScripts(openid);
			
			if(scripts == null || scripts.isEmpty()){
			    
	            resp.sendRedirect(req.getContextPath()+"/scripts/list/samples");
	            
			} else {
			    
                req.setAttribute("scripts", scripts);

	            getServletContext().getRequestDispatcher("/WEB-INF/jsp/listScripts.jsp").forward(req, resp);
			}
			
			
			return;

		} else if("/list/samples".equals(path)){

            Set<String> scripts = ((ScriptusDatastore) ctx.getBean("datastore")).listScripts(ScriptusDatastore.SAMPLE_USER);
            
            req.setAttribute("scripts", scripts);
            req.setAttribute("samples", Boolean.TRUE);

            getServletContext().getRequestDispatcher("/WEB-INF/jsp/listScripts.jsp").forward(req, resp);
		    

		} else if("/edit".equals(path)) {
			
			String scriptId = req.getParameter("script");
			
			String scriptSource = null;
			
			if(StringUtils.isNotEmpty(scriptId)) {
			    
			    String user = openid;
			    
			    if(Boolean.TRUE.toString().equalsIgnoreCase(req.getParameter("sample"))){
			        user = ScriptusDatastore.SAMPLE_USER;
			        req.setAttribute("sample", Boolean.TRUE);
			    }
				
				scriptSource = ((ScriptusDatastore) ctx.getBean("datastore")).loadScriptSource(user, scriptId);
				
				if(scriptSource == null) {
					resp.sendError(404);
					return;
				}
			}
			
			req.setAttribute("scriptId", scriptId);
			req.setAttribute("scriptSource", scriptSource);
			
			getServletContext().getRequestDispatcher("/WEB-INF/jsp/editScript.jsp").forward(req, resp);
			
			return;
			
		}

		//if redirect to list, then errors could cause infinite redirects
		resp.sendError(404);
		return;
	}

	//saveScript
	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {


		String path = req.getPathInfo();

		if("/edit".equals(path)) {
			
			String scriptId = req.getParameter("scriptid");
			String script = req.getParameter("source");
			
			((ScriptusDatastore) ctx.getBean("datastore")).saveScriptSource(openid, scriptId, script);
			
			resp.sendRedirect("list");
			return;
			
		} else if("/delete".equals(path)) {
			
			((ScriptusDatastore) ctx.getBean("datastore")).deleteScript(openid, req.getParameter("deleteid"));
			resp.sendRedirect("list");
			return;
			
		} else if("/run".equals(path)) {
			
			String script = req.getParameter("runid");
			String args = req.getParameter("args");
			String owner = req.getParameter("owner");
			
			((ProcessScheduler) ctx.getBean("scheduler")).executeNewProcess(openid, script, args, owner);

			resp.sendRedirect("list");
			return;

		}

		resp.sendError(404);
	}

    @Override
    protected String getPageLabel() {
        return "scripts";
    }

}
