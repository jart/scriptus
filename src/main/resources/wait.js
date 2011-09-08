function waitCtor(wait_impl, getlastpid_impl) {
	
	return function(fn, pid) {
//		var log = org.apache.commons.logging.LogFactory.getLog("net.ex337.scriptus.model.api.Wait");
		if( ! pid) {
			pid = getlastpid_impl();
		}
		if( ! pid) {
			return -1;
		}
		//error conditions?
//		log.debug("before wait_impl, pid="+pid);
//		log = null;
		var result = wait_impl(pid);
//		log = org.apache.commons.logging.LogFactory.getLog("net.ex337.scriptus.model.api.Wait");
//		log.debug("after wait_impl, result="+result+", fn="+fn);
		if(result && fn) {
//			log.debug("in result && fn");
			fn(result);
		}
		return pid;
	}
}

