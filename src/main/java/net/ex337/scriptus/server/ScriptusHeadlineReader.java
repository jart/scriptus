package net.ex337.scriptus.server;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.ex337.scriptus.config.ScriptusConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScriptusHeadlineReader {
    
    private static final Log LOG = LogFactory.getLog(ScriptusHeadlineReader.class);

    private static final long RSS_POLL = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);

    private String lastNewsItemHeadline;
    private String lastNewsItemLink;
    
    private String atomFeed;
    
    private Timer t;
    
    @PostConstruct
    public void init() {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try{
                    String xmlFeed = IOUtils.toString(new URL(atomFeed).openStream(), ScriptusConfig.CHARSET_STR);
                    if( ! xmlFeed.contains("<link href=\"") || ! xmlFeed.contains("<title>")){
                        return;
                    }
                    //get to the first entry
                    xmlFeed = xmlFeed.substring(xmlFeed.indexOf("<entry>"));
                    lastNewsItemLink = StringUtils.substringBetween(xmlFeed, "<link href=\"", "\"/>");
                    lastNewsItemHeadline = StringUtils.substringBetween(xmlFeed, "<title>", "</title>");
                    //FIXME do some safing here to ensure we don't import an XSS...
                } catch(Exception e) {
                    //do nothing
                    LOG.info("Could not get rss feed", e);
                }
                
            }
            
        }, 0L, RSS_POLL);
    }

    @PreDestroy
    public void destroy(){
        
    }
    
    public void setAtomFeed(String atomFeed) {
        this.atomFeed = atomFeed;
    }

    public String getLastNewsItemHeadline() {
        return lastNewsItemHeadline;
    }

    public String getLastNewsItemLink() {
        return lastNewsItemLink;
    }

}
