package net.ex337.scriptus.datastore.impl.jpa;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.ex337.scriptus.SerializableUtils;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.config.ScriptusConfig.TransportType;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.datastore.impl.BaseScriptusDatastore;
import net.ex337.scriptus.datastore.impl.jpa.dao.ChildProcessPIDDAO;
import net.ex337.scriptus.datastore.impl.jpa.dao.MessageCorrelationDAO;
import net.ex337.scriptus.datastore.impl.jpa.dao.ProcessDAO;
import net.ex337.scriptus.datastore.impl.jpa.dao.ScheduledScriptActionDAO;
import net.ex337.scriptus.datastore.impl.jpa.dao.ScriptDAO;
import net.ex337.scriptus.datastore.impl.jpa.dao.TransportCursorDAO;
import net.ex337.scriptus.exceptions.ProcessNotFoundException;
import net.ex337.scriptus.exceptions.ScriptusRuntimeException;
import net.ex337.scriptus.model.MessageCorrelation;
import net.ex337.scriptus.model.ScriptProcess;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;
import net.ex337.scriptus.model.scheduler.Wake;
import net.ex337.scriptus.model.support.ScriptusClassShutter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.transaction.annotation.Transactional;

public abstract class ScriptusDatastoreJPAImpl extends BaseScriptusDatastore implements ScriptusDatastore {
    
    private static final Log LOG = LogFactory.getLog(ScriptusDatastoreJPAImpl.class);
    
    @PersistenceContext(unitName="jpa-pu")
    private EntityManager em;

    @Override
    @Transactional(readOnly=true)
    public ScriptProcess getProcess(UUID pid) {
        if (pid == null) {
            throw new ScriptusRuntimeException("Cannot load null pid");
        }

        LOG.debug("loading " + pid.toString().substring(30));

        Context cx = Context.enter();
        cx.setClassShutter(new ScriptusClassShutter());
        cx.setOptimizationLevel(-1); // must use interpreter mode
        
        try {
            
            ProcessDAO d = em.find(ProcessDAO.class, pid.toString());

            if(d == null) {
                throw new ProcessNotFoundException(pid.toString());
            }

            ScriptProcess result = createScriptProcess();
            
            result.setPid(UUID.fromString(d.pid));
            result.setWaiterPid(UUID.fromString(d.waitingPid));
            result.setSource(new String(d.source, Charset.forName(ScriptusConfig.CHARSET)));
            result.setSourceName(d.sourceId);
            result.setUserId(d.userId);
            result.setArgs(d.args);
            result.setState(SerializableUtils.deserialiseObject(d.state));
            if(result.getChildren() == null) {
                result.setChildren(new ArrayList<UUID>());
            }
            if(d.children != null) for(ChildProcessPIDDAO c : d.children) {
                result.getChildren().add(UUID.fromString(c.child));
            }
            result.setCompiled((Function)SerializableUtils.deserialiseObject(d.compiled));
            result.setOwner(d.owner);
            result.setRoot(d.isRoot);
            result.setVersion(d.version);
            
            result.setGlobalScope((ScriptableObject) SerializableUtils.deserialiseObject(d.globalScope));
            result.setContinuation(SerializableUtils.deserialiseObject(d.continuation));
            
            return result;

        } catch (ScriptusRuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw new ScriptusRuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new ScriptusRuntimeException(e);
        } finally {
            Context.exit();
        }
        
    }
    
    @Override
    @Transactional(readOnly=false)
    public void writeProcess(ScriptProcess p) {
        
        if (p.getPid() == null) {
            p.setPid(UUID.randomUUID());
        } else {
            p.setVersion(p.getVersion()+1);
        }

        LOG.debug("saving " + p.getPid().toString().substring(30));

        Context cx = Context.enter();
        cx.setClassShutter(new ScriptusClassShutter());
        cx.setOptimizationLevel(-1); // must use interpreter mode
        
        try {
            ProcessDAO d = null;
            
            if(p.getVersion() == 0) {
                d = new ProcessDAO();
                d.pid = p.getPid().toString();
            } else {
                d = em.find(ProcessDAO.class, p.getPid());
                if(d == null) {
                    throw new ScriptusRuntimeException("Process not found for pid "+p.getPid());
                }
            }
            
            d.args = p.getArgs();
            d.compiled = SerializableUtils.serialiseObject(p.getCompiled());
            d.continuation = SerializableUtils.serialiseObject(p.getContinuation());
            d.globalScope = SerializableUtils.serialiseObject(p.getGlobalScope());
            d.isRoot = p.isRoot();
            d.owner = p.getOwner();
            d.source = p.getSource().getBytes(Charset.forName(ScriptusConfig.CHARSET));
            d.sourceId = p.getSourceName();
            d.state = SerializableUtils.serialiseObject(p.getState());
            d.userId = p.getUserId();
            if(p.getWaiterPid() != null) {
                d.waitingPid = p.getWaiterPid().toString();
            }
            
            if(p.getChildren() != null && ! p.getChildren().isEmpty()) {
                if(d.children == null) {
                    d.children = new ArrayList<ChildProcessPIDDAO>();
                } else {
                    d.children.clear();
                }
                for(UUID c : p.getChildren()) {
                    d.children.add(new ChildProcessPIDDAO(d, c.toString()));
                    
                }
            }
            
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            ObjectOutputStream out = new ObjectOutputStream(bout);
//            out.writeObject(this);
//            out.writeObject(getGlobalScope());
//            out.writeObject(getContinuation());
//            out.close();
            
            em.persist(d);

            //datastore.writeProcess(getPid(), bout.toByteArray());
                
        } catch (ScriptusRuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw new ScriptusRuntimeException(e);
        } finally {
            Context.exit();
        }

    }

    @Override
    @Transactional(readOnly=false)
    public void deleteProcess(UUID pid) {
        
        Query q = em.createQuery("delete from ProcessDAO d where d.pid = :pid");
        q.setParameter("pid", pid.toString());
        
        q.executeUpdate();
        
    }

    @Override
    @Transactional(readOnly=true)
    public Set<String> listScripts(String userId) {
        
        Query q = em.createQuery("select name from ScriptDAO d where d.userId = :userId");
        q.setParameter("userId", userId);
        
        List<Object> oo = q.getResultList();
        
        Set<String> result = new HashSet<String>();
        
        for(Object o : oo) {
            result.add(o.toString());
        }

        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public String loadScriptSource(String userId, String name) {
        
        Query q = em.createQuery("select s from ScriptDAO s where s.userId = :userId and s.name = :name");
        q.setParameter("userId", userId);
        q.setParameter("name", name);
        
        try {
            ScriptDAO d = (ScriptDAO) q.getSingleResult();
            return new String(d.source, Charset.forName(ScriptusConfig.CHARSET));
        } catch(NoResultException nre) {
            return null;
        }

    }

    @Override
    @Transactional(readOnly=false)
    public void saveScriptSource(String userId, String name, String source) {
        
        ScriptDAO d = new ScriptDAO();
        d.name = name;
        d.userId = userId;
        d.source = source.getBytes(Charset.forName(ScriptusConfig.CHARSET));
        em.persist(d);

    }

    @Override
    @Transactional(readOnly=false)
    public void deleteScript(String userId, String name) {
        
        Query q = em.createQuery("delete from ScriptDAO d where d.userId = :userId and d.name = :name");
        q.setParameter("userId", userId);
        q.setParameter("name", name);
        
        q.executeUpdate();

    }

    @Override
    @Transactional(readOnly=true)
    public List<ScheduledScriptAction> getScheduledTasks(Calendar dueDate) {
        
        Query q = em.createQuery("select s from ScheduledScriptActionDAO s where s.when <= :when");
        q.setParameter("when", dueDate.getTimeInMillis());
        
        List<ScheduledScriptActionDAO> daos = q.getResultList();
        
        List<ScheduledScriptAction> result = new ArrayList<ScheduledScriptAction>(daos.size());
        
        for(ScheduledScriptActionDAO d : daos) {
            result.add(toScheduledAction(d));
        }
            
        return result;
        
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteScheduledTask(UUID pid, long nonce) {
        
        Query q = em.createQuery("delete from ScheduledScriptActionDAO s where s.pid  = :pid and s.nonce = :nonce");
        q.setParameter("pid", pid.toString());
        q.setParameter("nonce", nonce);
        
        q.executeUpdate();

    }

    @Override
    @Transactional(readOnly=false)
    public void saveScheduledTask(ScheduledScriptAction task) {
        if(task instanceof Wake) {
            Wake w = (Wake) task;
            
            ScheduledScriptActionDAO d = new ScheduledScriptActionDAO();
            d.action="wake";
            d.nonce = w.getNonce();
            d.pid = w.getPid().toString();
            d.when = task.getWhen();

            em.persist(d);
            
        } else {
            throw new ScriptusRuntimeException("unknown scheduled action "+task);
        }

    }

    @Override
    @Transactional(readOnly=false)
    public void registerMessageCorrelation(MessageCorrelation cid) {
        MessageCorrelationDAO d = new MessageCorrelationDAO();
        d.pid = cid.getPid().toString();
        d.timestamp = cid.getTimestamp();
        d.messageId = cid.getMessageId();
        d.user = cid.getUser();
        em.persist(d);
    }

    @Override
    @Transactional(readOnly=true)
    public Set<MessageCorrelation> getMessageCorrelations(String inReplyToMessageId, String fromUser) {
        
        StringBuilder b = new StringBuilder(
                "select d from MessageCorrelationDAO d" +
                " where (d.messageId is null and d.user is null)" +
                " or (d.messageId is null and d.user = :user)");
        
        if(inReplyToMessageId != null) {
            b.append(" or (d.messageId = :messageId and d.user is null)" +
            		 " or (d.messageId = :messageId and d.user = :user)");
        }
        Query q = em.createQuery(b.toString());
        q.setParameter("user", fromUser);
        if(inReplyToMessageId != null) {
            q.setParameter("messageId", inReplyToMessageId);
        }
        
        List<MessageCorrelationDAO> dd = q.getResultList();
        
        Set<MessageCorrelation> result = new HashSet<MessageCorrelation>();
       
        for(MessageCorrelationDAO d : dd) {
            result.add(toMessageCorrelation(d));
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly=false)
    public void unregisterMessageCorrelation(MessageCorrelation correlation) {
        
        MessageCorrelationDAO d = em.find(MessageCorrelationDAO.class, correlation.getPid().toString());
        if(d != null) {
            em.remove(d);
        }

    }

    @Override
    @Transactional(readOnly=true)
    public String getTransportCursor(TransportType transport) {
        TransportCursorDAO d = em.find(TransportCursorDAO.class, transport.toString());
        if(d == null){
            return null;
        }
        return d.cursor;
    }

    @Override
    @Transactional(readOnly=false)
   public void updateTransportCursor(TransportType transport, String cursor) {
        TransportCursorDAO d = em.find(TransportCursorDAO.class, transport.toString());
        if(d == null){
            d = new TransportCursorDAO();
            d.transport = transport.toString();
        }
        d.cursor = cursor;
        
        em.persist(d);
    }


    
    private MessageCorrelation toMessageCorrelation(MessageCorrelationDAO dao) {
        MessageCorrelation r = new MessageCorrelation();
        r.setPid(UUID.fromString(dao.pid));
        r.setUser(dao.user);
        r.setMessageId(dao.messageId);
        r.setTimestamp(dao.timestamp);
        return r;
        
    }
    private ScheduledScriptAction toScheduledAction(ScheduledScriptActionDAO dao) {
        ScheduledScriptAction r = null;
        
        if(dao.action.equalsIgnoreCase("wake")) {
            Wake w = new Wake(UUID.fromString(dao.pid), dao.nonce, dao.when);
            r = w;
        } else {
            throw new ScriptusRuntimeException("unkown type of action "+dao.action);
        }
        
        return r;
    }
    
    @Override
    @Transactional(readOnly=false)
    public void createTestSources() {
        super.createTestSources();        
    }


}
