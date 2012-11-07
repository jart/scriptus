package net.ex337.scriptus.datastore.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.annotation.Resource;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.scheduler.ProcessLocks;

import org.apache.commons.io.FileUtils;
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

	@Override
	public void createSamples() {
		URL u = this.getClass().getClassLoader().getResource("samples");

		if(u == null) {
			throw new ScriptusRuntimeException("cannot find samples resource");
		}
		
		File samples = new File(u.getFile());
		
		if( ! samples.exists()) {
            throw new ScriptusRuntimeException("samples file doesn't exist");
		}

		for(File f : samples.listFiles()) {
			try {
				saveScriptSource(ScriptusDatastore.SAMPLE_USER, f.getName(), FileUtils.readFileToString(f));
			} catch (IOException e) {
				throw new ScriptusRuntimeException(e);
			}
		}
	}



	@Override
	public final ScriptProcess newProcess(String userId, String source, String args, String owner) {

		ScriptProcess result = createScriptProcess();

		result.init(userId, source, args, owner);

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
