package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ProcessListItem;

/**
 * 
 * Servlet responsible for CRUDL admin tasks on processes.
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class ProcessAdminServlet extends BaseServlet {

	private static final long serialVersionUID = 50869801033071491L;
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
		
		String path = req.getPathInfo();

        req.setAttribute("config", ctx.getBean("config"));

		List<ProcessListItem> processes = ((ScriptusDatastore) ctx.getBean("datastore")).getProcessesForUser(openid);
		
		req.setAttribute("processes", processes);

		getServletContext().getRequestDispatcher("/WEB-INF/jsp/listProcesses.jsp").forward(req, resp);
		
		return;
	}

	//saveScript
	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {


		String op = req.getParameter("op");

		if("kill".equals(op)) {
			
			String pid = req.getParameter("pid");
			
			((ScriptusDatastore) ctx.getBean("datastore")).deleteProcess(UUID.fromString(pid));
			
            resp.sendRedirect(req.getContextPath()+"/processes/list");
			return;
			
		}

		resp.sendError(404);
	}

    @Override
    protected String getPageLabel() {
        return "processes";
    }

}
