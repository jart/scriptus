package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.ProcessListItem;
import net.ex337.scriptus.scheduler.ProcessScheduler;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Servlet responsible for displating user info
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class YouServlet extends BaseServlet {

	
	/**
     * 
     */
    private static final long serialVersionUID = 152768278537175495L;

    @Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {

		getServletContext().getRequestDispatcher("/WEB-INF/jsp/you.jsp").forward(req, resp);
		
		return;
	}

	//saveScript
	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {


		resp.sendError(404);
	}

    @Override
    protected String getPageLabel() {
        return "you";
    }

}
