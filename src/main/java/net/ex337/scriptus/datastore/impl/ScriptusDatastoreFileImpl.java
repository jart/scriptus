
package net.ex337.scriptus.datastore.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.MessageCorrelation;
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
    private File cursorsDir;
	
	/*
	 * scheduled tasks are stored in a file named <timeMillis>_pid_nonce,
	 * to effectively enable them to be found abck via time or pid.
	 * 
	 * Ugly, but effective for now... 
	 * 
	 * ./scriptus/correlation/
	 * 
	 * pid-
	 * usridx/
	 * pididx/
	 * msgidx/
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
		cursorsDir = new File(scriptusHome, "cursors");
		
		checkDirsExist(scriptusHome, processDir, scriptsDir, correlationDir, schedulerDir, incomingsDir, cursorsDir);
		
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
			return;
			//throw new ScriptusRuntimeException("Process not found: "+pid);
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
	public void deleteScheduledTask(final UUID pid, final long nonce) {
		
		File[] foundFiles = schedulerDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("_"+pid+"_"+nonce);
			}
		});
		
		if(foundFiles.length != 1) {
			throw new ScriptusRuntimeException("could not find scheduled task "+pid+"/"+nonce);
		}
		
		if( ! foundFiles[0].delete()) {
			throw new ScriptusRuntimeException("could not delete scheduled task "+pid+"/"+nonce);
		}
		
	}

	@Override
	public void saveScheduledTask(ScheduledScriptAction task) {
		
		try {
			FileUtils.writeStringToFile(new File(schedulerDir, task.getWhen()+"_"+task.getPid()+"_"+task.getNonce()), task.toString());
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
	public void registerMessageCorrelation(MessageCorrelation correlation) {
		try {
	        File f = new File(correlationDir, correlation.getPid()+"-"+correlation.getMessageId()+"-"+correlation.getUser());
	        FileUtils.writeByteArrayToFile(f, SerializableUtils.serialiseObject(correlation));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public Set<MessageCorrelation> getMessageCorrelations(final String messageId, final String fromUser) {
	    
	    Set<MessageCorrelation> result = new HashSet<MessageCorrelation>();
	    
		try {
		    for(File f : correlationDir.listFiles(new FilenameFilter() {
                
		        /*
                 * listen({to:"foo",messageId:X}); -X-foo
                 * 
                 * any message from that user in rlepy to that
                 * 
                 * listen({messageId:X}); -X-null
                 * listen({to:"foo"}); null-foo
		         * listen() -null-null
		         */
		        
                @Override
                public boolean accept(File dir, String name) {
                    
                    boolean accept = false;
                    
                    accept |= name.contains("-null-null");
                    
                    
                    if(messageId != null) {
                        accept |= name.contains("-"+messageId+"-null");
                        accept |= name.contains("-"+messageId+"-"+fromUser);
                    } else if(messageId == null) {
                        accept |= name.contains("-null-"+fromUser);
                    }
                    
                    return accept;
                }
            })) {
		        result.add((MessageCorrelation) SerializableUtils.deserialiseObject(FileUtils.readFileToByteArray(f)));
		    }
			
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new ScriptusRuntimeException(e);
		}
		
		return result;
	}

	@Override
	public void unregisterMessageCorrelation(MessageCorrelation correlation) {
		new File(correlationDir, correlation.getPid()+"-"+correlation.getMessageId()+"-"+correlation.getUser()).delete();
	}

    @Override
    public String getTransportCursor(TransportType transport) {
        File cursor = new File(cursorsDir, transport.toString());
        if( ! cursor.exists()) {
            return null;
        }
        try {
            return FileUtils.readFileToString(cursor);
        } catch (IOException e) {
            throw new ScriptusRuntimeException(e);
        }
    }

    @Override
    public void updateTransportCursor(TransportType transport, String cursor) {
        File cursorF = new File(cursorsDir, transport.toString());
        try {
            FileUtils.writeStringToFile(cursorF, cursor);
        } catch (IOException e) {
            throw new ScriptusRuntimeException(e);
        }
    }

}
