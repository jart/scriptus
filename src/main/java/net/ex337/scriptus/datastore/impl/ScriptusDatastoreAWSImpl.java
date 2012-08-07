package net.ex337.scriptus.datastore.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.annotation.Resource;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.DatastoreType;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
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
	
	private static final String PID = "pid";
    private static final String TIMESTAMP = "timestamp";
    private static final String MESSAGE_ID = "messageId";
    private static final String USER_ID = "userId";

    private static final Log LOG = LogFactory.getLog(ScriptusDatastoreAWSImpl.class);
	
    private static final String CORRELATION_IDS = "scriptus-correlation-ids";
    private static final String TRANSPORT_CURSORS = "scriptus-transport-cursors";
	private static final String SCHEDULED_TASKS = "scriptus-scheduled-tasks";
	
	@Resource
	private ScriptusConfig config;

	private AmazonSimpleDB sdb;

	private AmazonS3 s3;
    
    private String s3bucket;
    
    private static final List<String> DOMAINS =
        Collections.unmodifiableList(new ArrayList<String>(){{
            add(CORRELATION_IDS);
            add(SCHEDULED_TASKS);
            add(TRANSPORT_CURSORS);
        }});

	@PostConstruct
	public void init() throws IOException {
		
		if(config.getDatastoreType() != DatastoreType.Aws) {
			return;
		}
		
		
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
			add(new ReplaceableAttribute(PID, task.getPid().toString(), false));
			//not padding number because all system.currentTimeMillis() is always the same,
			//At least until the year 2286...
            add(new ReplaceableAttribute("when", Long.toString(task.getWhen()), false));
            add(new ReplaceableAttribute("task", task.toString(), false));
		}};
		
		PutAttributesRequest r = new PutAttributesRequest(SCHEDULED_TASKS, task.getPid()+"/"+task.getNonce(), atts);
		sdb.putAttributes(r);
	}

	@Override
	public void registerMessageCorrelation(final MessageCorrelation correlation) {

		List<ReplaceableAttribute> atts = new ArrayList<ReplaceableAttribute>();
		atts.add(new ReplaceableAttribute(PID, correlation.getPid().toString(), false));
		atts.add(new ReplaceableAttribute(TIMESTAMP, Long.toString(correlation.getTimestamp()), false));
		if(correlation.getUser() != null) {
		    atts.add(new ReplaceableAttribute(USER_ID, correlation.getUser(), false));
		}
		if(correlation.getMessageId() != null) {
		    atts.add(new ReplaceableAttribute(MESSAGE_ID, correlation.getMessageId(), false));
		}
		PutAttributesRequest r = new PutAttributesRequest(CORRELATION_IDS, correlation.getPid().toString(), atts);

		sdb.putAttributes(r);
	}

	@Override
	public Set<MessageCorrelation> getMessageCorrelations(final String messageId, String fromUser) {
	    
	    List<String> selects = new ArrayList<String>();
	    
        selects.add( 
                "select * from `"+CORRELATION_IDS+"` where "+
                "     ("+MESSAGE_ID+" is null and "+USER_ID+" is null) ");
        selects.add( 
                "select * from `"+CORRELATION_IDS+"` where "+
                "     ("+MESSAGE_ID+" is null and "+USER_ID+" = :user)");
        
        
        //FIXME parallelise
        //FIXME RDBS!
        //too many predicates if we do it all at once
        if(messageId != null) {
            selects.add("select * from `"+CORRELATION_IDS+"` where "+MESSAGE_ID+" = :msgId and "+USER_ID+" is null");
            selects.add("select * from `"+CORRELATION_IDS+"` where "+MESSAGE_ID+" = :msgId and "+USER_ID+" = :user");
        }
        
        Set<MessageCorrelation> result = new HashSet<MessageCorrelation>();
        
        for(String select : selects) {
            select = StringUtils.replace(select, ":msgId", "'"+StringEscapeUtils.escapeSql(messageId)+"'");
            select = StringUtils.replace(select, ":user", "'"+StringEscapeUtils.escapeSql(fromUser)+"'");
            
            SelectRequest s = new SelectRequest(select, true);
            
            SelectResult rs = sdb.select(s);
            
            if(rs == null || rs.getItems() == null || rs.getItems().isEmpty()){
                continue;
            }
            
            //FIXME cursors!!
            for(Item i : rs.getItems()) {

                String foundUser = null;
                String foundMessageId = null;
                UUID pid = null;
                long timestamp = 0;
                
                for(Attribute a : i.getAttributes()) {
                    if(PID.equals(a.getName())) {
                        pid = UUID.fromString(a.getValue());
                    } else if(USER_ID.equals(a.getName())) {
                        foundUser = a.getValue();
                    } else if(MESSAGE_ID.equals(a.getName())) {
                        foundMessageId = a.getValue();
                    } else if(TIMESTAMP.equals(a.getName())) {
                        timestamp = Long.parseLong(a.getValue());
                    }
                }
                
                if(pid == null) {
                    continue;
                }
                
                LOG.info("found atts:"+i.getAttributes().toString());
                
                result.add(new MessageCorrelation(pid, foundUser, foundMessageId, timestamp));
            }
        }
        		
		return result;
		
	}

	@Override
	public void unregisterMessageCorrelation(final MessageCorrelation correlation) {
		sdb.deleteAttributes(new DeleteAttributesRequest(CORRELATION_IDS, correlation.getPid().toString()));
		
	}

    @Override
    public String getTransportCursor(TransportType transport) {
        
        GetAttributesRequest gar = new GetAttributesRequest(TRANSPORT_CURSORS, transport.toString());
        gar.setAttributeNames(Arrays.asList(new String[]{"cursor"}));
        
        GetAttributesResult r = sdb.getAttributes(gar);
        
        
        if(r == null || r.getAttributes() == null || r.getAttributes().isEmpty()) {
            return null;
        }
        return r.getAttributes().get(0).getValue();
    }

    @Override
    public void updateTransportCursor(TransportType transport, String cursor) {
        PutAttributesRequest par = new PutAttributesRequest(TRANSPORT_CURSORS, transport.toString(), new ArrayList<ReplaceableAttribute>());
        ReplaceableAttribute rat = new ReplaceableAttribute("cursor", cursor, true);
        par.getAttributes().add(rat);
       sdb.putAttributes(par);
    }
	
	

}
