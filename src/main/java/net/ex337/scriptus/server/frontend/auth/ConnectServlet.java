package net.ex337.scriptus.server.frontend.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;

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
public class ConnectServlet extends BaseServlet {

    private static final long serialVersionUID = -4615310743688687976L;

    private ScriptusConfig config;
    
    @Override
    public void init() {
        super.init();
        
        config = (ScriptusConfig) super.ctx.getBean("config");

    }
	
	@Override
	protected void doAuthGet(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
	    
	    List<TransportType> installedTransports = ((ScriptusDatastore)ctx.getBean("datastore")).getInstalledTransports(openid);
	    
	    List<TransportType> freeTransports = new ArrayList<TransportType>(Arrays.asList(TransportType.values()));
	    
	    for(Iterator<TransportType> ts = freeTransports.iterator(); ts.hasNext();) {
	        TransportType t = ts.next();
	        if(installedTransports.contains(t) || ! t.isPublic()){
	            ts.remove();
	        }
	    }
	    
        req.setAttribute("installedTransports", installedTransports);
        req.setAttribute("freeTransports", freeTransports);

	    req.getRequestDispatcher("/WEB-INF/jsp/connect.jsp").forward(req, resp);

	}

	@Override
	protected void doAuthPost(HttpServletRequest req, HttpServletResponse resp, String openid) throws ServletException, IOException {
	    
        TransportType t = TransportType.valueOf(req.getParameter("transport"));
        
        if(t == TransportType.Twitter){
            
            if(StringUtils.equals("del", req.getParameter("op"))) {
                ((ScriptusDatastore)ctx.getBean("datastore")).deleteTransportAccessToken(openid, t);
            }
            
            req.getRequestDispatcher("/connect/twitter").forward(req, resp);
            return;
        }
        
        resp.sendRedirect(req.getContextPath()+"/connect");
	}

    @Override
    protected String getPageLabel() {
        return "connect";
    }

}
