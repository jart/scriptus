
#Known problems

Scriptus does generally work as expected, however there are some quirks, bugs and niggles that are good to know about. Below are listed all such problems of which I'm currently aware. This page will be updated to reflect the status of the latest build.

##Scheduler timing

The scheduler currently polls the storage for tasks to execute every minute, on the minute. This means that timeouts and durations of a minute or less will take up to a minute plus the time to next poll. e.g. a `sleep()` of 1 minute at 04:34:40 will wake the process at roughly 04:36:00.

##Listen timeout

The `listen()` call works by registering a process as a listener inside the Twitter transport. We then poll Twitter every minute for mentions by the user being listened to.

However, if the user 'speaks' after the last poll but before the timeout, then this tweet is not picked up before the process is woken and null is returned to the program.

In addition, the 'listen' is not unregistered on waking, causing a needless process load & nonce check.

##Process re-parenting & storage economy

In UNIX, when a root process terminates its children if any are 're-parented' to a caretaker (or something) and continue execution. In Scriptus, child processes continue execution, but when they terminate, it is assumed that a root process will wait on them at some point and so their state & result is persisted.

This could balloon storage over time, which would cost money if AWS is being used as the datastore. Good hygiene and tidiness is not yet completely enforced.

##kill() during process execution

If a process is killed during its execution, a flag is set on the `ScriptProcess` to avoid saving the process or executing any resultant `ScriptAction`. However, this check is by no means watertight, and there are several conceivable race conditions between `ProcessExecutor.run` and `ProcessSchedulerImpl.markAsKilledIfRunning` under which a nominally killed process is not present to receive the `kill` flag, but executes and persists anyway.
 
##kill, wait and deleting processes

The implementation of kill is pretty ugly because the management of child processes itself is ugly - see above, but also the child processes aren't deleted when they should be, removed from the list of children, etc. None of this prevents Scriptus from functioning correctly, but leaves it looking pretty hairy around the edges.
