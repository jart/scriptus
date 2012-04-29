package net.ex337.scriptus.datastore.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.TwitterCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

/**
 * 
 * AWS-backed implementation of Scriptus datastore.
 * 
 * Storage is separated into three parts:
 * 
 * 1) an S3 bucket that has to be named in {@link ScriptusConfig#}, since 
 * bucket names have to be globally unique across S3. If it doesn't exist
 * then it will be created. Used for blob data.
 * 
 * 2) A set of three SDB domains which will also be created if they don't
 * exist, all prefixed with "scriptus-". Used for structured data.
 * 
 * @author ian
 *
 */
public abstract class ScriptusDatastoreAWSImpl extends BaseScriptusDatastore implements ScriptusDatastore {
	
	private static final Log LOG = LogFactory.getLog(ScriptusDatastoreAWSImpl.class);
	
	private static final String CORRELATION_IDS = "scriptus-correlation-ids";
	private static final String SCHEDULED_TASKS = "scriptus-scheduled-tasks";
	private static final String LISTENERS = "scriptus-listeners";
	
	@Resource
	private ScriptusConfig config;

	private AmazonSimpleDB sdb;

	private AmazonS3 s3;
    
    private String s3bucket;
    
    private static final List<String> DOMAINS =
        Collections.unmodifiableList(new ArrayList<String>(){{
            add(CORRELATION_IDS);
            add(SCHEDULED_TASKS);
            add(LISTENERS);
        }});

	@PostConstruct
	public void init() throws IOException {
		
		if(config.getDatastoreType() != DatastoreType.Aws) {
			return;
		}
		
		
//		PropertiesCredentials config = new PropertiesCredentials(
//                this.getClass().getClassLoader().getResource("AwsCredentials.properties").openStream());
		
        s3 = new AmazonS3Client(config);
		
		sdb = new AmazonSimpleDBClient(config);
		
        for(String s : DOMAINS) {
            sdb.createDomain(new CreateDomainRequest(s));
        }
        
        boolean hasB = false;
        
        s3bucket = this.config.getS3Bucket();
        
        for(Bucket b : s3.listBuckets()) {
        	if(b.getName().equals(s3bucket)){
        		hasB = true;
        		break;
        	}
        }
        if( ! hasB ){
        	s3.createBucket(s3bucket);
        }
        

		//confirm that the domain exists for pidcid mapping
		//create the bucket if it doesn't exist
	}
	
	private InputStream get(String path) {
		LOG.info("GET "+path);
		return s3.getObject(s3bucket, path).getObjectContent();
	}

	@Override
	public void writeProcess(UUID pid, byte[] process) {
		ObjectMetadata m = new ObjectMetadata();
		s3.putObject(s3bucket, "/process/"+pid, new ByteArrayInputStream(process), m);
		
	}

	@Override
	public byte[] loadProcess(UUID pid) {
		try {
			return IOUtils.toByteArray(get("/process/"+pid));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
	}


    public void setS3bucket(String s3bucket) {
		this.s3bucket = s3bucket;
	}

	@Override
	public String loadScriptSource(String userId, String name) {

		try {
			return IOUtils.toString(get("/user/"+URLEncoder.encode(userId, ScriptusConfig.CHARSET)+"/scripts/"+name), ScriptusConfig.CHARSET);
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}

	}

	@Override
	public void saveScriptSource(String userId, String name, String source) {
		
		ObjectMetadata m = new ObjectMetadata();
		try {
			s3.putObject(s3bucket, "/user/"+URLEncoder.encode(userId, ScriptusConfig.CHARSET)+"/scripts/"+name, new ByteArrayInputStream(source.getBytes(ScriptusConfig.CHARSET)), m);
		} catch (UnsupportedEncodingException e) {
			throw new ScriptusRuntimeException(e);
		}
	}

	@Override
	public Set<String> listScripts(String userId) {
		
		String prefix;
		try {
			prefix = "/user/"+URLEncoder.encode(userId, ScriptusConfig.CHARSET)+"/scripts/";
		} catch (UnsupportedEncodingException e) {
			//never happens
			throw new ScriptusRuntimeException(e);
		}
		
		ListObjectsRequest r = new ListObjectsRequest(this.s3bucket, prefix, null, "/", 100);
		
		ObjectListing l = s3.listObjects(r);
		
		Set<String> results = new HashSet<String>();
		
		for(S3ObjectSummary s : l.getObjectSummaries()) {
			results.add(s.getKey().substring(prefix.length()));
		}
		
		return results;
	}

	@Override
	public void deleteScript(String userId, String script) {
		
		DeleteObjectRequest delete;
		try {
			delete = new DeleteObjectRequest(s3bucket, "/user/"+URLEncoder.encode(userId, ScriptusConfig.CHARSET)+"/scripts/"+script);
		} catch (UnsupportedEncodingException e) {
			throw new ScriptusRuntimeException(e);
		}
		
		s3.deleteObject(delete);
		
	}

	@Override
	public void deleteProcess(UUID pid) {

		s3.deleteObject(new DeleteObjectRequest(s3bucket, "/process/"+pid.toString()));
		
	}

	@Override
	public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
		
		String select = 
			"select * from `"+
			SCHEDULED_TASKS+"` where when <= '"+dueDate.getTimeInMillis()+"'";
		
		SelectRequest r = new SelectRequest(select, true);
		
		SelectResult s = sdb.select(r);
		
		List<ScheduledScriptAction> result = new ArrayList<ScheduledScriptAction>();
		
		//FIXME nextToken
		
		for(Item i : s.getItems()) {
		    String task = SerializableUtils.getAttribute(i.getAttributes(), "task").getValue();
						
			result.add(ScheduledScriptAction.readFromString(task));
		}
		
		return result;
	}

	@Override
	public void deleteScheduledTask(UUID pid, long nonce) {
		
		sdb.deleteAttributes(new DeleteAttributesRequest(SCHEDULED_TASKS, pid+"/"+nonce));
		
	}

	@Override
	public void saveScheduledTask(final ScheduledScriptAction task) {
		
		List<ReplaceableAttribute> atts = new ArrayList<ReplaceableAttribute>(){{
			add(new ReplaceableAttribute("pid", task.getPid().toString(), false));
			//not padding number because all system.currentTimeMillis() is always the same,
			//At least until the year 2286...
            add(new ReplaceableAttribute("when", Long.toString(task.getWhen()), false));
            add(new ReplaceableAttribute("task", task.toString(), false));
		}};
		
		PutAttributesRequest r = new PutAttributesRequest(SCHEDULED_TASKS, task.getPid()+"/"+task.getNonce(), atts);
		sdb.putAttributes(r);
	}

	@Override
	public void registerTwitterCorrelation(final TwitterCorrelation correlation) {
		
		List<ReplaceableAttribute> atts = new ArrayList<ReplaceableAttribute>(){{
			add(new ReplaceableAttribute("pid", correlation.getPid().toString(), false));
			if(correlation.getUser() != null) {
	            add(new ReplaceableAttribute("user", correlation.getUser(), false));
			}
		}};
		PutAttributesRequest r = new PutAttributesRequest(CORRELATION_IDS, correlation.getMessageId(), atts);

		sdb.putAttributes(r);
	}

	@Override
	public TwitterCorrelation getTwitterCorrelationByID(final String messageId) {
		
		GetAttributesRequest r = new GetAttributesRequest(CORRELATION_IDS, messageId);
		r.setConsistentRead(true);
		r.setAttributeNames(new HashSet<String>() {{
			add("pid");
			add("user");
		}});
		List<Attribute> atts = sdb.getAttributes(r).getAttributes();
		if(atts.size() == 0){
			return null;
		}
		
		String foundUser = null;
		UUID pid = null;
		
		for(Attribute a : atts) {
			if("pid".equals(a.getName())) {
				pid = UUID.fromString(a.getValue());
			} else if("user".equals(a.getName())) {
				foundUser = a.getValue();
			}
		}
		
		if(pid == null) {
			return null;
		}
		
		LOG.info("found atts:"+atts.toString());
		
		return new TwitterCorrelation(pid, foundUser, messageId);
	}

	@Override
	public void unregisterTwitterCorrelation(final String snowflake) {
		sdb.deleteAttributes(new DeleteAttributesRequest(CORRELATION_IDS, snowflake));
		
	}
	
	@Override
	public List<Long> getTwitterLastMentions() {
			try {
				return (List<Long>) SerializableUtils.deserialiseObject(IOUtils.toByteArray(get("/twitter/mentions")));
			} catch (AmazonS3Exception e) {
				if(e.getStatusCode() == 404) {
					return new ArrayList<Long>();
				}
				throw e;
			} catch (IOException e) {
				throw new ScriptusRuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new ScriptusRuntimeException(e);
			}
	}

	@Override
	public void updateTwitterLastMentions(List<Long> processedIncomings) {
		ObjectMetadata m = new ObjectMetadata();
		try {
			s3.putObject(new PutObjectRequest(s3bucket, "/twitter/mentions", new ByteArrayInputStream(SerializableUtils.serialiseObject(processedIncomings)), m));
		} catch (IOException e) {
			throw new ScriptusRuntimeException(e);
		}
		
	}

	@Override
	public void registerTwitterListener(UUID pid, final String to) {
		
		List<ReplaceableAttribute> atts = new ArrayList<ReplaceableAttribute>(){{
			add(new ReplaceableAttribute("to", to, false));
			add(new ReplaceableAttribute("timestamp", Long.toString(System.currentTimeMillis()), false));
		}};
		
		PutAttributesRequest r = new PutAttributesRequest(LISTENERS, pid.toString(), atts);
		sdb.putAttributes(r);
		
	}
	
	@Override
	public UUID getMostRecentTwitterListener(String screenName) {
		
		String select = 
			"select * from `"+
			LISTENERS+"` where to = '"+StringEscapeUtils.escapeSql(screenName)+"'";
		
		SelectRequest r = new SelectRequest(select, true);
		
		SelectResult s = sdb.select(r);
		
		long mostRecentTime = Long.MIN_VALUE;
		UUID mostRecentPID = null;
		
		for(Item i : s.getItems()) {
			
			long when = Long.MIN_VALUE;
			
			for(Attribute a : i.getAttributes()) {
				if("when".equals(a.getName())) {
					when = Long.parseLong(a.getValue());
				}
			}
			
			if(when >= mostRecentTime) {
				mostRecentPID = UUID.fromString(i.getName());
				mostRecentTime = when;
			}

		}

		return mostRecentPID;
	}

	@Override
	public void unregisterTwitterListener(UUID uuid, String to) {
		
		sdb.deleteAttributes(new DeleteAttributesRequest(LISTENERS, uuid.toString()));
		
	}


}
