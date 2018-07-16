package net.rails.support.job.worker;

import java.util.List;
import org.quartz.JobListener;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.ext.AbsGlobal;
import net.rails.support.Support;

public abstract class DefaultScheduleWorker {

	protected Logger log;
	protected AbsGlobal g;

	public abstract List<JobObject> getScheduleJobs();
	public abstract TriggerListener getTriggerListener();
	public abstract JobListener getJobListener();
	
	public DefaultScheduleWorker(AbsGlobal g) {
		super();
		this.g = g;
		log = LoggerFactory.getLogger(getClass());
	}

	protected String getHostname() {
		return Support.env().getHostname();
	}

}
