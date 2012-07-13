package net.ex337.scriptus.tests;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;

/**
 * 
 * @author ian
 *
 */
public class Testcase_AWS extends BaseTestCase {
	
    private ScriptusConfig config;

	
    private static final String CORRELATION_IDS = "scriptus-correlation-ids";

	@Override
	protected void setUp() throws Exception {

//		System.setProperty("scriptus.config", "test-scriptus.properties");
//		System.setProperty("scriptus.config", "filesystem-based-scriptus.properties");
		
		super.setUp();
		
		config = (ScriptusConfig) appContext.getBean("config");
		
		
	}

	public void testSDB() throws IOException {

	    String fromUser = "user:781924273";
	    String messageId="tweet:13196802";

	       AmazonSimpleDBClient sdb = new AmazonSimpleDBClient(config);

        String select = 
            "select itemName() from "+CORRELATION_IDS+" where "+
            "messageId is null";// and userId is null ";
        
//        if(messageId != null) {
//            select +=
//                "  or (messageId = :msgId and userId is null) "+
//                "  or (messageId = :msgId and userId = :user) ";
//        }
//        
//        //user is never null
//        select += 
//            "  or (messageId is null and userId = :user)";
        
        select = StringUtils.replace(select, ":msgId", "'"+StringEscapeUtils.escapeSql(messageId)+"'");
        select = StringUtils.replace(select, ":user", "'"+StringEscapeUtils.escapeSql(fromUser)+"'");

        System.out.println(select);
        
        ListDomainsResult r = sdb.listDomains();
        System.out.println(r.getDomainNames());
        
        SelectRequest s = new SelectRequest();
        s.setSelectExpression(select);
        
        SelectResult rs = sdb.select(s);
	
	}
	
}
