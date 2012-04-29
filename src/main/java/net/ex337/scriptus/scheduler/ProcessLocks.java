package net.ex337.scriptus.scheduler;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.MapMaker;

/**
 * @author ian
 *
 */
public class ProcessLocks {
	
	private static final Log LOG = LogFactory.getLog(ProcessLocks.class);

	private ConcurrentMap<UUID, Lock> locks;
	
	@PostConstruct
	public void init() {

		//TODO add l0-minute timeout on script execution+extract constant
		locks = new MapMaker()
			.expireAfterAccess(11, TimeUnit.MINUTES)
			.makeMap();

	}

	public final void runWithLock(final UUID pid, Runnable r) {
		Lock lock;
		
		locks.putIfAbsent(pid, new ReentrantLock());

		(lock = locks.get(pid)).lock();
		
		try {
			r.run();
		} finally {
			lock.unlock();
		}
	}


	
}
