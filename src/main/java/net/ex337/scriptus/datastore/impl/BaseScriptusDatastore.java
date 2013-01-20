package net.ex337.scriptus.datastore.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;

import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.datastore.impl.jpa.dao.TransportTokenDAO;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.TransportAccessToken;
import net.ex337.scriptus.scheduler.ProcessLocks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Base class for all DAO implementations. Contains method implementations
 * common to all subclasses.
 * 
 * @author ian
 *
 */
public abstract class BaseScriptusDatastore implements ScriptusDatastore {
	
	private static final Log LOG = LogFactory.getLog(BaseScriptusDatastore.class);
	
	@Resource
	protected ProcessLocks locks;

	/**
	 * to be overridden by Spring to do autowiring
	 */
	public abstract ScriptProcess createScriptProcess();
	
	public abstract TransportAccessToken createTransportAccessToken();

	@Override
	public void createSamples() {
	    
        ProtectionDomain protectionDomain = ScriptusConfig.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        
        if("file".equalsIgnoreCase(location.getProtocol())) {
            
            if(location.getFile() == null) {
                LOG.warn("no samples can be loaded from location "+location);
                return;
            }
            
            File[] samples = new File(location.getFile()+"samples/").listFiles();
            
            for(File f : samples){
                try {
                    saveScriptSource(ScriptusDatastore.SAMPLE_USER, f.getName(), FileUtils.readFileToString(f));
                } catch (IOException e) {
                    LOG.warn("Could not load/save sample script", e);
                }
            }
            
        } else if("jar".equalsIgnoreCase(location.getProtocol())) {

            /*open as zip file, could be JAR or WAR file*/
            
          try {
              ZipInputStream p = new ZipInputStream(location.openStream());
              ZipEntry e = null;
              while((e = p.getNextEntry()) != null) {
                  String path = e.getName();
                  if(path.endsWith("/")) {
                      continue;
                  }
                  if(path.startsWith("WEB-INF/classes/samples/") || path.startsWith("samples/")) {
                      String name = path.substring(path.lastIndexOf("/"));
                      saveScriptSource(ScriptusDatastore.SAMPLE_USER, name, IOUtils.toString(p));
                  }
                  p.closeEntry();
              }
          } catch (IOException e) {
              LOG.warn("Could not load/save sample scripts from JAR file", e);
          }
            
            
        } else {
            LOG.warn("samples not loaded, unknown code location "+location);
        }

	}



	@Override
	public final ScriptProcess newProcess(String userId, String sourceId, boolean sample, String args, String owner) {

		ScriptProcess result = createScriptProcess();

		result.init(userId, sourceId, sample, args, owner);

		return result;
	}

	   /* (non-Javadoc)
     * @see net.ex337.scriptus.ProcessScheduler#updateProcessState(java.util.UUID, java.lang.Object)
     */
    @Override
    public void updateProcessState(final UUID pid, final Object o) {
        locks.runWithLock(pid, new Runnable() {
            @Override
            public void run() {
                ScriptProcess script = getProcess(pid);
                script.setState(o);
                script.save();
            }
            
        });
    }

}
