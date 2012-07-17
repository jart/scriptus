package net.ex337.scriptus.transport;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.api.HasTimeout;
import net.ex337.scriptus.model.api.Message;
import net.ex337.scriptus.scheduler.ProcessLocks;
import net.ex337.scriptus.scheduler.ProcessScheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageRouting {

    @Resource
    private ProcessLocks locks;
    
    @Resource
    private ScriptusDatastore datastore;

    @Resource
    private ProcessScheduler scheduler;
    
    private static final Log LOG = LogFactory.getLog(MessageRouting.class);

    private void executeWithMessage(final UUID pid, final Message m) {

        LOG.info("msg "+m.getMsg()+" from "+m.getFrom()+" in reply to "+m.getInReplyToMessageId());
        
        try {
            locks.runWithLock(pid, new Runnable() {
                @Override
                public void run() {
                    ScriptProcess p = datastore.getProcess(pid);

                    if(p.getState() instanceof HasTimeout) {
                        //delete wake if it exists, should fail silently
                        datastore.deleteScheduledTask(pid, ((HasTimeout)p.getState()).getNonce());
                    }

                    datastore.updateProcessState(pid, m);
                }
            });
            
            scheduler.execute(pid);
        } catch(Exception e) {
            LOG.error("could not run process "+pid, e);
            //continue
        }
         
    }
    
    public void handleIncomings(List<Message> messages) {

        for(Message s : messages) {
            
            if(s.getInReplyToMessageId() != null){
                //then it could be a reply to an ask();
                Set<MessageCorrelation> cc = datastore.getMessageCorrelations(s.getInReplyToMessageId(), s.getFrom());

                /*
                 * if I've setup scriptus with my own account,
                 * we need to make sure that 'request' and 'reply'
                 * tweets don't get mixed up
                 */
                for(MessageCorrelation c : cc) {
                    executeWithMessage(c.getPid(), s);
                    datastore.unregisterMessageCorrelation(c);
                }
            }
            
        }

    }

}
