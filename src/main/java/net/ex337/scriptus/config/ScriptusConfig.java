package net.ex337.scriptus.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Acts as the interface to the configuration store.
 * 
 * The configuration is loaded by default from ~/.scriptus/config.properties.
 * 
 * If the system property scriptus.config is supplied, then this property
 * is taken as a URL (file-system, relative or absolute, or HTTP etc.)
 * from which to load the properties file.
 * 
 * The configuration includes what datastore and transport to use.
 * 
 * If no configuration file is found, we default to using the in-memory
 * datastore and the command-line transport.
 * 
 * The properties in the config file can, with one exception, be modified
 * via the "/settings" page. The one exception is the boolean setting 
 * "disableOpenID", which has to be set manually by editing the file
 * itself, for reasons of security. This setting is useful when debugging
 * scripts offline, where openID authentication is impossible.
 * 
 * @author ian
 *
 */
public class ScriptusConfig {

	public static final String SCRIPTUS_CONFIG_SYSVAR = "scriptus.config";

    public static enum TransportType {Twitter, CommandLine, Dummy};
	public static enum DatastoreType {Db, Embedded, Memory};

	public static final String DURATION_FORMAT="([0-9]+)[\\ ,]*([smhdwMqyDC])";

	public static final String DATE_FORMAT="yyyy-MM-dd HH:mm";

	public static final String CHARSET_STR = "UTF-8";
	
	public static final Charset CHARSET = Charset.forName(ScriptusConfig.CHARSET_STR);
	
	public static final String SCRIPTUS_DIR = System.getProperty("user.home")+"/.scriptus";

	private int DEFAULT_TIMEOUT_LENGTH=24;
	private TimeUnit WAIT_SLEEP_UNIT = TimeUnit.HOURS;
	
	//also defines on what multiple of the time unit to execute, e.g.
	//10minutes = execution at 00, 10, 20, 
	private int SCHEDULER_POLL_INTERVAL = 1;
	private TimeUnit SCHEDULER_TIME_UNIT=TimeUnit.MINUTES;
	
	private String twitterConsumerKey="";
	private String twitterConsumerSecret="";

	private String twitterAccessToken="";
	private String twitterAccessTokenSecret="";

	private TransportType transportType;
	
	private DatastoreType datastoreType;

	public static boolean FORCE_CLEAN_INSTALL = false;

	/**
	 * Set to true during init() iff no config file exists
	 * at specified (or default) location.
	 */
	private boolean cleanInstall;
	
	private String configLocation;
	
	private boolean disableOpenID;
	
	@PostConstruct
	public void init() throws IOException {
		/*
		 * is there a system property? if so use it
		 * is there a ~/.scriptus/config.properties? if so use it
		 * use a default
		 */
		File scriptusDir = new File(SCRIPTUS_DIR);
		
//		if( ! scriptusDir.exists() && ! scriptusDir.mkdir()) {
//			throw new IOException("Unable to create directory "+SCRIPTUS_DIR);
//		}
		
		
		File localConfig;
		
		String defaultConfigLocation = SCRIPTUS_DIR+"/config.properties";
		
		configLocation = defaultConfigLocation;

		if(System.getProperty(SCRIPTUS_CONFIG_SYSVAR) != null) {
		    
			configLocation = System.getProperty(SCRIPTUS_CONFIG_SYSVAR);
			
			Properties props = new Properties();

			/*
			 * We need to figure out if we can load from this location,
			 * because if we're running for the first time and the
			 * config file doesn't exist then we might end up in a
			 * sticky situation.
			 */
			InputStream configStream = null;
			
			try {
				
				configStream = new URL(configLocation).openStream();
				
			} catch(MalformedURLException mfe) {
			    
				configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configLocation);
				
			}
			
			if(configStream == null) try {
			    configStream = new FileInputStream(configLocation);
			} catch(FileNotFoundException fnfe) {
			    //do nothing...
			}
			
			boolean canLoadConfig = (configStream != null);
			
			if(canLoadConfig) {

				props.load(configStream);
				load(props);
				
			} else {

				cleanInstall = true;
				
			}
			
		} else if((localConfig = new File(configLocation)).exists()) {
			
			Properties props = new Properties();

			InputStream fin = new FileInputStream(localConfig);
			props.load(fin);
			fin.close();
			
			load(props);

		} else {

			cleanInstall = true;
			
		}
		
		if(cleanInstall) {

			transportType = TransportType.CommandLine;
			
			datastoreType = DatastoreType.Memory;
			
		}
		
		/*
		 * Try not to touch the local filesystem unless we know we're 
		 * going to  need to.
		 */
		//put DB initialisatoin here
		if(configLocation.equals(defaultConfigLocation) || datastoreType == DatastoreType.Db) {
			
			if( ! scriptusDir.exists()) {
				scriptusDir.mkdir();
			}
			
		}
		

	}
	
	private void load(Properties props) {
		twitterConsumerKey = props.getProperty("twitterConsumerKey");
		twitterConsumerSecret = props.getProperty("twitterConsumerSecret");
		twitterAccessToken = props.getProperty("twitterAccessToken");
		twitterAccessTokenSecret = props.getProperty("twitterAccessTokenSecret");
		datastoreType = DatastoreType.valueOf(props.getProperty("datastore"));
		transportType = TransportType.valueOf(props.getProperty("transport"));
		disableOpenID = Boolean.parseBoolean(props.getProperty("disableOpenID"));
	}
	
	public void save() throws IOException {
		
		Properties props = new Properties();
		
		props.put("twitterConsumerKey",			twitterConsumerKey);
		props.put("twitterConsumerSecret",		twitterConsumerSecret);
		props.put("twitterAccessToken",			twitterAccessToken);
		props.put("twitterAccessTokenSecret",	twitterAccessTokenSecret);
		props.put("transport",       			transportType.toString());
		props.put("datastore", 					datastoreType.toString());
		/*
		 * not written out automatically
		 *  - (a) no option in GUI,
		 *  - (b) dangerous! so has to be done manually
		 */
		//props.put("disableOpenID",				Boolean.toString(disableOpenID));

		FileOutputStream fout = new FileOutputStream(new File(configLocation));
		
		props.store(fout, "Scriptus configuration file");
		
		fout.close();
		
	}


	public String getTwitterConsumerKey() {
		return twitterConsumerKey;
	}

	public void setTwitterConsumerKey(String twitterConsumerKey) {
		this.twitterConsumerKey = twitterConsumerKey;
	}

	public String getTwitterAccessToken() {
		return twitterAccessToken;
	}

	public void setTwitterAccessToken(String twitterAccessToken) {
		this.twitterAccessToken = twitterAccessToken;
	}

	public String getTwitterAccessTokenSecret() {
		return twitterAccessTokenSecret;
	}

	public void setTwitterAccessTokenSecret(String twitterAccessTokenSecret) {
		this.twitterAccessTokenSecret = twitterAccessTokenSecret;
	}
	
	
	public String getTwitterConsumerSecret() {
		return twitterConsumerSecret;
	}

	public void setTwitterConsumerSecret(String twitterConsumerSecret) {
		this.twitterConsumerSecret = twitterConsumerSecret;
	}

	public TransportType getTransportType() {
		return transportType;
	}

	public void setTransportType(TransportType transportType) {
		this.transportType = transportType;
	}

	public DatastoreType getDatastoreType() {
		return datastoreType;
	}

	public void setDatastoreType(DatastoreType datastoreType) {
		this.datastoreType = datastoreType;
	}


	public boolean isCleanInstall() {
	    if(FORCE_CLEAN_INSTALL) {
	        return true;
	    }
		return cleanInstall;
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public boolean getDisableOpenID() {
		return disableOpenID;
	}

	public void setDisableOpenID(boolean offlineMode) {
		this.disableOpenID = offlineMode;
	}

	public int getDefaultTimeoutLength() {
		return DEFAULT_TIMEOUT_LENGTH;
	}

	public int getSchedulerPollInterval() {
		return SCHEDULER_POLL_INTERVAL;
	}

	public TimeUnit getWaitSleepUnit() {
		return WAIT_SLEEP_UNIT;
	}

	public TimeUnit getSchedulerTimeUnit() {
		return SCHEDULER_TIME_UNIT;
	}

	public String getMemoryStoreClass() {
	    return "net.ex337.scriptus.datastore.impl.ScriptusDatastoreMemoryImpl";
	}
	
	
}
