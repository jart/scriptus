package net.ex337.scriptus.config;

import static net.ex337.scriptus.CryptUtils.hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;

import net.ex337.scriptus.CryptUtils;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;

/**
 * Acts as the interface to the configuration store.
 * 
 * The configuration is loaded by default from ~/.scriptus/config.properties.
 * 
 * If the system property scriptus.config is supplied, then this property is
 * taken as a URL (file-system, relative or absolute, or HTTP etc.) from which
 * to load the properties file.
 * 
 * The configuration includes what datastore and transport to use.
 * 
 * If no configuration file is found, we default to using the in-memory
 * datastore and the command-line transport.
 * 
 * The properties in the config file can, with one exception, be modified via
 * the "/settings" page. The one exception is the boolean setting
 * "disableOpenID", which has to be set manually by editing the file itself, for
 * reasons of security. This setting is useful when debugging scripts offline,
 * where openID authentication is impossible.
 * 
 * @author ian
 * 
 */
public class ScriptusConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String DB_PARAMETERS = "dbParameters";
    private static final String DB_NAME = "dbName";
    private static final String DB_USERNAME = "dbUsername";
    private static final String DB_PASSWORD = "dbPassword";
    private static final String DB_PORT = "dbPort";
    private static final String DB_HOST = "dbHost";

    private static final String HASH_ALGO = "SHA-256";

    private static final String SYMETRIC_CIPHER = "AES";

    public static final String SCRIPTUS_CONFIG_SYSVAR = "scriptus.config";

    public static enum TransportType {
        Twitter(true), Personal(false), Dummy(false),

        ;

        private boolean isPublic;

        private TransportType(boolean isPublic) {
            this.isPublic = isPublic;
        }

        public boolean isPublic() {
            return isPublic;
        }
    };

    public static enum DatastoreType {
        PostgreSQL, Embedded, Memory
    };

    public static final String DURATION_FORMAT = "([0-9]+)[\\ ,]*([smhdwMqyDC])";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String CHARSET_STR = "UTF-8";

    public static final Charset CHARSET = Charset.forName(ScriptusConfig.CHARSET_STR);

    public static final String SCRIPTUS_DIR = System.getProperty("user.home") + "/.scriptus";

    private int DEFAULT_TIMEOUT_LENGTH = 24;
    private TimeUnit WAIT_SLEEP_UNIT = TimeUnit.HOURS;

    // also defines on what multiple of the time unit to execute, e.g.
    // 10minutes = execution at 00, 10, 20,
    private int SCHEDULER_POLL_INTERVAL = 1;
    private TimeUnit SCHEDULER_TIME_UNIT = TimeUnit.MINUTES;

    private String twitterConsumerKey = "";
    private String twitterConsumerSecret = "";

    // private String twitterAccessToken="";
    // private String twitterAccessTokenSecret="";

    private TransportType transportType;

    private DatastoreType datastoreType;

    private byte[] salt;
    private transient Map<String, byte[]> keys = new HashMap<String, byte[]>();
    private transient String lastKey;
    
    private String dbHost, dbPort, dbUsername, dbPassword, dbName, dbParameters;

    public static boolean FORCE_CLEAN_INSTALL = false;

    /**
     * Set to true during init() iff no config file exists at specified (or
     * default) location.
     */
    private boolean cleanInstall;

    private boolean disableOpenID;

    private String defaultConfigLocation = SCRIPTUS_DIR + "/config.properties";;

    private String configLocation = defaultConfigLocation;

    @PostConstruct
    public void init() throws IOException {

        /*
         * is there a system property? if so use it is there a
         * ~/.scriptus/config.properties? if so use it use a default
         */
        File scriptusDir = new File(SCRIPTUS_DIR);

        // if( ! scriptusDir.exists() && ! scriptusDir.mkdir()) {
        // throw new IOException("Unable to create directory "+SCRIPTUS_DIR);
        // }

        Properties props = getProperties();
        
        if(props == null){
            cleanInstall = true;
        } else {
            load(props);
        }

        if (cleanInstall || FORCE_CLEAN_INSTALL) {

            //transportType = TransportType.CommandLine;

            //datastoreType = DatastoreType.Memory;

            SecureRandom r = new SecureRandom();

            salt = new byte[32];
            byte[] firstKey = new byte[32];

            r.nextBytes(salt);
            r.nextBytes(firstKey);

            lastKey = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

            keys.put(lastKey, firstKey);

        }

        /*
         * Try not to touch the local filesystem unless we know we're going to
         * need to.
         */
        // put DB initialisatoin here
        if (configLocation.equals(defaultConfigLocation)) {

            if (!scriptusDir.exists()) {
                scriptusDir.mkdir();
            }

        }

        /*
         * save config file with salt & key. But if we're forcing, we don't want
         * to save a new config file (every test-case)
         */
        if (cleanInstall && !FORCE_CLEAN_INSTALL) {
            save();
        }

    }

    private Properties getProperties() throws IOException {

        Properties props = new Properties();

        File localConfig;

        if (System.getProperty(SCRIPTUS_CONFIG_SYSVAR) != null) {

            configLocation = System.getProperty(SCRIPTUS_CONFIG_SYSVAR);

            /*
             * We need to figure out if we can load from this location, because
             * if we're running for the first time and the config file doesn't
             * exist then we might end up in a sticky situation.
             */
            InputStream configStream = null;

            try {

                configStream = new URL(configLocation).openStream();

            } catch (MalformedURLException mfe) {

                configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configLocation);

            }

            if (configStream == null)
                try {
                    configStream = new FileInputStream(configLocation);
                } catch (FileNotFoundException fnfe) {
                    // do nothing...
                }

            boolean canLoadConfig = (configStream != null);

            if (canLoadConfig) {

                props.load(configStream);

            } else {

                return null;

            }

        } else if ((localConfig = new File(configLocation)).exists()) {

            InputStream fin = new FileInputStream(localConfig);
            props.load(fin);
            fin.close();

        } else {

            return null;

        }
        
        return props;

    }

    private void load(Properties props) {
        twitterConsumerKey = props.getProperty("twitterConsumerKey");
        twitterConsumerSecret = props.getProperty("twitterConsumerSecret");
        // twitterAccessToken = props.getProperty("twitterAccessToken");
        // twitterAccessTokenSecret =
        // props.getProperty("twitterAccessTokenSecret");
        datastoreType = DatastoreType.valueOf(props.getProperty("datastore"));
        transportType = TransportType.valueOf(props.getProperty("transport"));
        disableOpenID = Boolean.parseBoolean(props.getProperty("disableOpenID"));

        dbHost = props.getProperty(DB_HOST);
        dbPort = props.getProperty(DB_PORT);
        dbUsername = props.getProperty(DB_USERNAME);
        dbPassword = props.getProperty(DB_PASSWORD);
        dbName = props.getProperty(DB_NAME);
        dbParameters = props.getProperty(DB_PARAMETERS);
        
        String[] keyIdList = StringUtils.split(props.getProperty("transportKeys"), ",");

        if (keyIdList == null) {
            return;
        }

        for (String k : keyIdList) {
            this.keys.put(k, CryptUtils.fromHex(props.getProperty("transportKey." + k)));

            if (lastKey == null || k.compareTo(lastKey) > 0) {
                this.lastKey = k;
            }
        }

        String saltHex = props.getProperty("transportKeys.salt");

        if (saltHex == null) {
            return;
        }

        this.salt = CryptUtils.fromHex(saltHex);

    }

    public void save() throws IOException {

        Properties props = dumpConfigToProperties();

        /*
         * not written out automatically - (a) no option in GUI, - (b)
         * dangerous! so has to be done manually
         */
        // props.put("disableOpenID", Boolean.toString(disableOpenID));

        FileOutputStream fout = new FileOutputStream(new File(configLocation));

        props.store(fout, "Scriptus configuration file");

        fout.close();
    }

    public Properties dumpConfigToProperties() {

        Properties props = new Properties();

        props.put("twitterConsumerKey", twitterConsumerKey);
        props.put("twitterConsumerSecret", twitterConsumerSecret);
        // props.put("twitterAccessToken", twitterAccessToken);
        // props.put("twitterAccessTokenSecret", twitterAccessTokenSecret);
        props.put("transport", transportType.toString());
        props.put("datastore", datastoreType.toString());

        List<String> keys = new ArrayList<String>();

        props.put("transportKeys.salt", CryptUtils.toHex(this.salt));

        for (Map.Entry<String, byte[]> e : this.keys.entrySet()) {
            keys.add(e.getKey());
            props.put("transportKey." + e.getKey(), CryptUtils.toHex(e.getValue()));
        }

        props.put("transportKeys", StringUtils.join(keys, ','));

        return props;

    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public void setTwitterConsumerKey(String twitterConsumerKey) {
        this.twitterConsumerKey = twitterConsumerKey;
    }

    // public String getTwitterAccessToken() {
    // return twitterAccessToken;
    // }
    //
    // public void setTwitterAccessToken(String twitterAccessToken) {
    // this.twitterAccessToken = twitterAccessToken;
    // }
    //
    // public String getTwitterAccessTokenSecret() {
    // return twitterAccessTokenSecret;
    // }
    //
    // public void setTwitterAccessTokenSecret(String twitterAccessTokenSecret)
    // {
    // this.twitterAccessTokenSecret = twitterAccessTokenSecret;
    // }

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
        if (FORCE_CLEAN_INSTALL) {
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

    public String decrypt(byte[] ciphertext, String keyId) {

        byte[] keymat = hash(HASH_ALGO, ArrayUtils.addAll(hash(HASH_ALGO, getKey(keyId)), getSalt()));

        Key key = new SecretKeySpec(keymat, 0, 16, SYMETRIC_CIPHER);

        return new String(CryptUtils.decrypt(SYMETRIC_CIPHER, ciphertext, key), CHARSET);
    }

    private byte[] getSalt() {
        return salt;
    }

    private byte[] getKey(String keyId) {
        byte[] key = keys.get(keyId);
        if (key == null) {
            throw new ScriptusRuntimeException("key with ID " + keyId + " not found");
        }
        return Arrays.copyOf(key, key.length);
    }

    public String getLatestKeyId() {
        return lastKey;
    }

    public byte[] encrypt(String plaintext, String keyId) {

        byte[] keymat = hash(HASH_ALGO, ArrayUtils.addAll(hash(HASH_ALGO, getKey(keyId)), getSalt()));

        Key key = new SecretKeySpec(keymat, 0, 16, SYMETRIC_CIPHER);

        return CryptUtils.encrypt(SYMETRIC_CIPHER, plaintext.getBytes(CHARSET), key);
    }

    @Override
    public void initialize(ConfigurableApplicationContext c) {
        
        
        /*
         * also load config here to configure bean container itself
         */
        
        Properties r;
        try {
            r = getProperties();
        } catch (IOException e) {
            throw new ScriptusRuntimeException(e);
        }
        
        load(r);
        
        /*
        <property name="url" value="jdbc:postgresql://${dbServer}:${dbPort}/${dbName}?${dbParameters}"/>
        <property name="username" value="${dbUsername}"/>
        <property name="password" value="${dbUsername}"/>
         */

        c.getEnvironment().getPropertySources().addFirst(new ScriptusConfigPropertySource("Scriptus config", this));
    }
    
    public class ScriptusConfigPropertySource extends PropertySource<ScriptusConfig>{
        
        public ScriptusConfigPropertySource(String name, ScriptusConfig source) {
            super(name, source);
        }

        @Override
        public Object getProperty(String name) {
            if(DatastoreType.class.getSimpleName().equals(name)){
                return getDatastoreType().toString();
            }
            if(DB_HOST.equals(name)){
                return dbHost;
            } else if(DB_PORT.equals(name)){
                return dbPort;
            } else if(DB_PASSWORD.equals(name)){
                return dbPassword;
            } else if(DB_USERNAME.equals(name)){
                return dbUsername;
            } else if(DB_NAME.equals(name)){
                return dbName;
            } else if(DB_PARAMETERS.equals(name)){
                return dbParameters == null ? "" : dbParameters;
            }
            
            return null;
        }
        
    }
}
