package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.datastore.impl.jpa.dao.LogMessageDAO;

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

        req.setAttribute("config", f);
        
        List<LogMessageDAO> logs = d.getLogMessages(openid);

        req.setAttribute("logs", logs);
        
		getServletContext().getRequestDispatcher("/WEB-INF/jsp/logs.jsp").forward(req, resp);

	}

	//saveScript
	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {


		String op = req.getParameter("op");

		if("delete".equals(op)) {
			
			String logId = req.getParameter("id");
			
			d.deleteLogMessage(logId, openid);
			
			resp.sendRedirect(req.getContextPath()+"/processes/logs");
			return;
		}
		
		resp.sendError(404);
	}

    @Override
    protected String getPageLabel() {
        return "logs";
    }

}
