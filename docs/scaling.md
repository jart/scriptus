
#Scaling Scriptus, or 'YAGNI: an interesting academic excercise' 

Scriptus currently scales vertically only, i.e: to process more load, use a bigger computer. Adjusting the properties of the executor that runs scripts can control the number of concurrent scripts that may run on one machine.

If necessary, Scriptus could be made to scale horizontally, partitioning workload across servers. This would be done by partitioning up the PID namespace into as many slices as necessary to bring the load down on the server hosting each slice.

Unresolved issues that would have to be fixed in the process of implementing this are:
 * The frontend openId authentication process currently stores things in the HTTPSession, but should use cookies only.
 * The correlation ID namespace would also need to be partitioned.

Beyond statically configured clustering, it should be possible to implement 'elastic' scaling using the "[hotrepart](http://code.google.com/p/hotrepart/)" process I demonstrated using PL/Proxy and PostgresQL.

