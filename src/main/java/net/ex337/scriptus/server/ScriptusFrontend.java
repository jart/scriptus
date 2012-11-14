package net.ex337.scriptus.server;

import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ScriptusFrontend {

    private static final int DEFAULT_PORT = 8080;
    
    public static void main(String[] args) throws Exception {
        
        int port = DEFAULT_PORT;
        
        String customConfig = null;
        
        if(args.length != 0) {
            for(int i = 0; i != args.length; i++) {
                if("-p".equals(args[i]) || "--port".equals(args[i])) {
                    
                    if(i == args.length -1) {
                        System.out.println("please specify a port, or omit the flag to use the default (8080)");
                        return;
                    } else if(i < args.length-1) {
                        String portStr = args[i+1];
                        try {
                            port = Integer.parseInt(portStr);
                            if(port > 65535 || port < 0) {
                                System.out.println("port arg "+portStr+" must be between 0 and 65535");
                                return;
                            }
                        } catch(NumberFormatException nfe) {
                            System.out.println("port arg "+portStr+" is not an integer");
                            return;
                        }
                    }
                    
                } else if("-c".equals(args[i]) || "--config".equals(args[i])) {
                    if(i == args.length -1) {
                        System.out.println("please specify a configuration file, or omit the flag to use the default (~/.scriptus/config.properties)");
                        return;
                    }
                    customConfig = args[i+1];
                    System.setProperty("scriptus.config", customConfig);
                }

            }
        }

        System.out.println("Starting Scriptus...");

        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        context.getInitParams().put("keepGenerated", "true"); 
        
        ProtectionDomain protectionDomain = ScriptusFrontend.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        
        context.setWar(location.toExternalForm());
        context.setExtractWAR(false);

        server.setHandler(context);
        
        server.start();

        System.out.println("Serving on http://localhost:"+port+"/");
        
        server.join();

    }

}
