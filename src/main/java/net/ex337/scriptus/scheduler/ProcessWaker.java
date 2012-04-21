package net.ex337.scriptus.scheduler;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.ex337.scriptus.ScriptusFacade;
import net.ex337.scriptus.config.ScriptusConfig;
import net.ex337.scriptus.datastore.ScriptusDatastore;
import net.ex337.scriptus.model.scheduler.ScheduledScriptAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessWaker {

    private static final Log LOG = LogFactory.getLog(ProcessWaker.class);

    @Resource
    private ScriptusDatastore datastore;

    @Resource
    private ProcessLocks locks;

    @Resource
    private ScriptusFacade facade;

    @Resource
    private ScriptusConfig config;

    // private ConcurrentMap<Long,Set<ScheduledScriptAction>> scheduledWakes;
    //
    private ScheduledExecutorService scheduledTasksChecker;

    //
    // @PostConstruct
    // public void init() {
    //
    // scheduledWakes = new MapMaker()
    // .expireAfterWrite(6, TimeUnit.MINUTES)
    // .makeMap();
    //
    // scheduledTasksChecker = new ScheduledThreadPoolExecutor(4);
    //
    // long now = System.currentTimeMillis();
    // long ms = now % 1000;
    //
    // scheduledTasksChecker.scheduleAtFixedRate(new Runnable(){
    //
    // @Override
    // public void run() {
    // try {
    // ProcessWaker.this.checkScheduledWakes();
    // } catch(Exception e) {
    // LOG.error("error waking scheduled tasks", e);
    // }
    // }
    //
    // }, ms, 1000, TimeUnit.MILLISECONDS);
    //
    // scheduledTasksChecker.scheduleAtFixedRate(new Runnable() {
    //
    // @Override
    // public void run() {
    // try {
    // Calendar nowPlus = Calendar.getInstance();
    // nowPlus.add(Calendar.MINUTE, 5);
    // List<ScheduledScriptAction> tasks = datastore.getScheduledTasks(nowPlus);
    // for(final ScheduledScriptAction task : tasks) {
    // queueTask(task);
    // }
    // } catch(Exception e) {
    // LOG.error("error retrieving scheduled tasks", e);
    // }
    // }
    //
    // }, 1, 1, TimeUnit.MINUTES);
    //
    // }
    //
    // public void scheduleTask(ScheduledScriptAction task) {
    // long delta = task.getWhen() - System.currentTimeMillis();
    // if(delta < 1000 * 60 * 5) {
    // queueTask(task);
    // }
    // datastore.saveScheduledTask(task);
    // }
    //
    // @PostConstruct
    // public void destroy() {
    // scheduledTasksChecker.shutdown();
    // }
    //
    // private void checkScheduledWakes() {
    //
    // Set<ScheduledScriptAction> tasks =
    // scheduledWakes.get(getSystemTimeSeconds());
    //
    // for(final ScheduledScriptAction task : tasks) {
    // locks.runWithLock(task.getPid(), new Runnable() {
    // @Override
    // public void run() {
    // try {
    // task.visit(facade, datastore.getProcess(task.getPid()));
    // } catch(Exception e) {
    // //FIXME should set state of task as "error" (and add != clause to query)
    // LOG.error("Exception when executing scheduled task", e);
    // } finally {
    // //no retry
    // //FIXME move this to after visit above
    // datastore.deleteScheduledTask(task.getPid(), task.getNonce());
    // }
    // }
    // });
    // }
    //
    // }
    //
    // private long getSystemTimeSeconds() {
    // return removeMillis(System.currentTimeMillis());
    // }
    // private long removeMillis(long time) {
    // return time - (time % 1000);
    // }
    //
    // private void queueTask(final ScheduledScriptAction task) {
    // long when = removeMillis(task.getWhen());
    // scheduledWakes.putIfAbsent(when, new HashSet<ScheduledScriptAction>());
    // final Set<ScheduledScriptAction> actions = scheduledWakes.get(when);
    // locks.runWithLock(task.getPid(), new Runnable() {
    // @Override
    // public void run() {
    // actions.add(task);
    // }
    //
    // });
    // }

    @PostConstruct
    public void init() {

        scheduledTasksChecker = new ScheduledThreadPoolExecutor(2);

        /*
         * everything is converted into seconds so that we can avoid Calendar
         * and use TimeUnit for everything.
         */
        long pollIntervalSeconds = TimeUnit.SECONDS.convert(config.getSchedulerPollInterval(),
                config.getSchedulerTimeUnit());

        long delay = pollIntervalSeconds - (System.currentTimeMillis() / 1000 % pollIntervalSeconds);

        scheduledTasksChecker.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    ProcessWaker.this.checkForScheduledTasks();
                } catch (Exception e) {
                    LOG.error("error checking for scheduled tasks", e);
                }
            }

        }, delay, pollIntervalSeconds, TimeUnit.SECONDS);

    }

    @PreDestroy
    public void destroy() {
        scheduledTasksChecker.shutdown();
    }

    protected void checkForScheduledTasks() {

        List<ScheduledScriptAction> tasks = datastore.getScheduledTasks(Calendar.getInstance());

        for (final ScheduledScriptAction t : tasks) {
            locks.runWithLock(t.getPid(), new Runnable() {
                @Override
                public void run() {
                    try {
                        t.visit(facade, datastore.getProcess(t.getPid()));
                    } catch (Exception e) {
                        // FIXME should set state of task as "error" (and add !=
                        // clause to query)
                        LOG.error("Exception when executing scheduled task", e);
                    } finally {
                        // no retry
                        // FIXME move this to after visit above
                        datastore.deleteScheduledTask(t.getPid(), t.getNonce());
                    }
                }
            });
        }

    }

}
