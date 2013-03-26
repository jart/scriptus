package net.ex337.scriptus.server.frontend.auth.transports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.server.frontend.auth.BaseServlet;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Servlet responsible for listing connections to social networks etc.
 * 
 * Uses openID for identifying users.
 * 
 * @author ian
 *
 */
public class TransportsServlet extends BaseServlet {

    private static final long serialVersionUID = -4615310743688687976L;

    @Override
    public void init() {
        super.init();

    }
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
	    
	    List<TransportType> installedTransports = d.getInstalledTransports(openid);
	    
	    List<TransportType> freeTransports = new ArrayList<TransportType>(Arrays.asList(TransportType.values()));
	    
	    for(Iterator<TransportType> ts = freeTransports.iterator(); ts.hasNext();) {
	        TransportType t = ts.next();
	        if(installedTransports.contains(t) || ! t.isPublic()){
	            ts.remove();
	        }
	    }
	    
        req.setAttribute("installedTransports", installedTransports);
        req.setAttribute("freeTransports", freeTransports);

	    req.getRequestDispatcher("/WEB-INF/jsp/transports.jsp").forward(req, resp);

	}

	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
	    
        TransportType t = TransportType.valueOf(req.getParameter("transport"));
        
        if(t == TransportType.Twitter){
            
            if(StringUtils.equals("del", req.getParameter("op"))) {
                d.deleteTransportAccessToken(openid, t);
            }
            
            req.getRequestDispatcher("/transports/twitter").forward(req, resp);
            return;
        }
        
        resp.sendRedirect(req.getContextPath()+"/transports");
	}

    @Override
    protected String getPageLabel() {
        return "transports";
    }

}
