package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.datastore.impl.jpa.dao.LogMessageDAO;
import net.ex337.scriptus.scheduler.ProcessScheduler;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Servlet responsible for CRUDL admin tasks on scripts.
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class LogsServlet extends BaseServlet {
	
	/**
     * 
     */
    private static final long serialVersionUID = -1270175176564125292L;

    @Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
		
		String path = req.getPathInfo();

        req.setAttribute("config", ctx.getBean("config"));
        
        List<LogMessageDAO> logs = ((ScriptusDatastore)ctx.getBean("datastore")).getLogMessages(openid);

		getServletContext().getRequestDispatcher("/WEB-INF/jsp/logs.jsp").forward(req, resp);

	}

	//saveScript
	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {


		String op = req.getParameter("op");

		if("delete".equals(op)) {
			
			String logId = req.getParameter("logid");
			
			((ScriptusDatastore) ctx.getBean("datastore")).deleteLogMessage(logId, openid);
			
			resp.sendRedirect("/logs");
			return;
		}
		
		resp.sendError(404);
	}

    @Override
    protected String getPageLabel() {
        return "logs";
    }

}
