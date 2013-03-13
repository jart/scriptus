package net.ex337.scriptus.transport.impl;

import java.util.UUID;

import javax.annotation.Resource;

import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.datastore.impl.jpa.dao.PersonalTransportMessageDAO;
import net.ex337.scriptus.transport.MessageRouting;
import net.ex337.scriptus.transport.Transport;

public class PersonalTransport implements Transport {

    @Resource
    private ScriptusDatastore datastore;
    @Resource
    private MessageRouting routing;
    
    @Override
    public String send(String userId, String to, String msg) {
        PersonalTransportMessageDAO m = new PersonalTransportMessageDAO();
        m.from=to; 
        m.message = msg;
        m.userId = userId;
        UUID id = datastore.savePersonalTransportMessage(m);
        
        return id.toString();
        
//        /*
//         * we have to p
//         */
//        Message mm = new Message(null, msg, System.currentTimeMillis(), userId, TransportType.Personal);
//        // TODO Auto-generated method stub
//        return null;
    }

}
