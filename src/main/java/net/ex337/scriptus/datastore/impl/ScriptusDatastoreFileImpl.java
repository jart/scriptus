
package net.ex337.scriptus.datastore.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

import org.apache.commons.io.FileUtils;

/**
 * 
 * Filesystem-backed implementation of Scriptus datastore.
 * 
 * Stores everything in ~/.scriptus/.
 * 
 * @author ian
 *
 */
public abstract class ScriptusDatastoreFileImpl extends BaseScriptusDatastore {
	
	private File scriptusHome;
	private File processDir;
	private File scriptsDir;
	private File correlationDir;
	private File schedulerDir;
	private File incomingsDir;
	
	/*
	 * scheduled tasks are stored in a file named <timeMillis>_pid_nonce,
	 * to effectively enable them to be found abck via time or pid.
	 * 
	 * Ugly, but effective for now... 
	 * 
	 */
	
	@PostConstruct
	public void init() throws IOException {
		
		scriptusHome = new File(ScriptusConfig.SCRIPTUS_DIR);
		
		processDir = new File(scriptusHome, "processes");
		scriptsDir = new File(scriptusHome, "scripts");
		correlationDir = new File(scriptusHome, "correlationIds");
		schedulerDir = new File(scriptusHome, "scheduler");
		incomingsDir = new File(scriptusHome, "incomings");
		
		checkDirsExist(scriptusHome, processDir, scriptsDir, correlationDir, schedulerDir, incomingsDir);
		
	}

	@Override
	public void writeProcess(UUID pid, byte[] process) {
		try {
			FileUtils.writeByteArrayToFile(new File(processDir, pid.toString()), process);
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}
	

	@Override
	public byte[] loadProcess(UUID pid) {
		File process = new File(processDir, pid.toString());
		if( ! process.exists()) {
			throw new ScriptusRuntimeException("Process not found: "+pid);
		}
		try {
			return FileUtils.readFileToByteArray(process);
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void deleteProcess(UUID pid) {
		File process = new File(processDir, pid.toString());
		if( ! process.exists()) {
			throw new ScriptusRuntimeException("Process not found: "+pid);
		}
		if( ! process.delete()) {
			throw new ScriptusRuntimeException("Could not delete process: "+pid);
		}
	}


	@Override
	public Set<String> listScripts(String userId) {
		
		final HashSet<String> result = new HashSet<String>();
		

		try {
			File userDir = new File(scriptsDir, URLEncoder.encode(userId, ScriptusConfig.CHARSET));
			
			//not good I know, but hey... gimme functional programming!
			if( userDir.exists()) userDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					result.add(name);
					return false;
				}
			});

		} catch (UnsupportedEncodingException e) {
			throw new ScriptusRuntimeException(e);
		}

		return result;
	}

	@Override
	public String loadScriptSource(String userId, String name) {

		try {
			
			File userDir = new File(scriptsDir, URLEncoder.encode(userId, ScriptusConfig.CHARSET));
			
			File script = new File(userDir, name);
			
			if(! userDir.exists() || ! script.exists()) {
				throw new ScriptusRuntimeException("script not found:"+name);
			}
			
			return FileUtils.readFileToString(script, ScriptusConfig.CHARSET);
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void saveScriptSource(String userId, String name, String source) {
		
		try {
			File userDir = new File(scriptsDir, URLEncoder.encode(userId, ScriptusConfig.CHARSET));
			
			checkDirsExist(userDir);
			
			File script = new File(userDir, name);
			
			FileUtils.writeStringToFile(script, source, ScriptusConfig.CHARSET);
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
		
	}

	@Override
	public void deleteScript(String userId, String name) {
		
		
		try {
			File userDir = new File(scriptsDir, URLEncoder.encode(userId, ScriptusConfig.CHARSET));

			if( ! userDir.exists()){
				return;
			}

			File script = new File(userDir, name);
			
			if( ! script.exists()) {
				return;
			}
			
			if( ! script.delete()) {
				throw new IOException("could nto delete "+script);
			}

		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
		
	}

	@Override
	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
		
		final List<ScheduledScriptAction> result = new ArrayList<ScheduledScriptAction>();
		
		final long dueDateMillis = dueDate.getTimeInMillis();

		schedulerDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				
				String n = arg0.getName();
				
				String timePortion = n.substring(0, n.indexOf("_"));
				
				long fileTime = Long.parseLong(timePortion);
				
				if(fileTime <= dueDateMillis) {
					try {
						result.add(ScheduledScriptAction.readFromString(FileUtils.readFileToString(arg0, ScriptusConfig.CHARSET)));
					} catch (IOException e) {
						throw new ScriptusRuntimeException(e);
					}
				}
				return false;
			}
		});
		
		return result;
	}

	@Override
	public void deleteScheduledTask(final ScheduledScriptAction t) {
		
		File[] foundFiles = schedulerDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_"+t.getPid()+"_"+t.getNonce());
			}
		});
		
		if(foundFiles.length != 1) {
			throw new ScriptusRuntimeException("could not find scheduled task "+t.toString());
		}
		
		if( ! foundFiles[0].delete()) {
			throw new ScriptusRuntimeException("could not delete scheduled task "+t.toString());
		}
		
	}

	@Override
	public void scheduleTask(Calendar when, ScheduledScriptAction task) {
		
		try {
			FileUtils.writeStringToFile(new File(schedulerDir, when.getTimeInMillis()+"_"+task.getPid()+"_"+task.getNonce()), task.toString());
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}

	}

	private void checkDirsExist(File... dirs) throws IOException {
		for(File f : dirs){
			if( f.exists() && ! f.isDirectory()) {
				throw new IOException(f+" should be directory but isn't");
			}
			if( ! f.exists() && ! f.mkdir()) {
				throw new IOException("Could not create directory "+f);
			}
		}
	}

	@Override
	public void registerTwitterCorrelation(TwitterCorrelation correlation) {
		try {
			FileUtils.writeByteArrayToFile(new File(correlationDir, correlation.getId()), SerializableUtils.serialiseObject(correlation));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public TwitterCorrelation getTwitterCorrelationByID(String cid) {
		try {
			File cidf = new File(correlationDir, cid);
			
			if( ! cidf.exists()) {
				return null;
			}
			
			return (TwitterCorrelation) SerializableUtils.deserialiseObject(FileUtils.readFileToByteArray(cidf));
			
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void unregisterTwitterCorrelation(String cid) {
		new File(correlationDir, cid).delete();
	}

	@Override
	public List<Long> getTwitterLastMentions() {
		try {
			File mentions = new File(correlationDir, "mentions");
			if( ! mentions.exists()) {
				return new ArrayList<Long>();
			}
			
			return (List<Long>) SerializableUtils.deserialiseObject(FileUtils.readFileToByteArray(mentions));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void updateTwitterLastMentions(List<Long> processedIncomings) {
		try {
			FileUtils.writeByteArrayToFile(new File(correlationDir, "mentions"), SerializableUtils.serialiseObject(processedIncomings));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void registerTwitterListener(UUID pid, String to) {
		File toDir = new File(correlationDir, "listenTo"+to);

		try {
			checkDirsExist(toDir);

			FileUtils.writeStringToFile(new File(toDir, Long.toString(System.currentTimeMillis())), pid.toString());

		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
		
		
	}

	@Override
	public UUID getMostRecentTwitterListener(String screenName) {
		
		File toDir = new File(correlationDir, "listenTo"+screenName);
		
		if( ! toDir.exists()){
			return null;
		}
		
		List<String> found = Arrays.asList(toDir.list());
		
		if(found.isEmpty()) {
			return null;
		}
		
		Collections.sort(found);
		
		try {
			return UUID.fromString(FileUtils.readFileToString(new File(toDir, found.get(found.size()-1))));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public void unregisterTwitterListener(UUID pid, String to) {
		
		File toDir = new File(correlationDir, "listenTo"+to);
		
		if( ! toDir.exists()){
			return;
		}
		
		File[] found = toDir.listFiles();
		
		if(found.length == 0) {
			return;
		}
		
		for(File f : found) {
			String s;
			try {
				s = FileUtils.readFileToString(f, ScriptusConfig.CHARSET);
			} catch (IOException e) {
				throw new ScriptusRuntimeException(e);
			}
			
			if(UUID.fromString(s).equals(pid)) {
				f.delete();
				return;
			}
		}
		
	}


}
