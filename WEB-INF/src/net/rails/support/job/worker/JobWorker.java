package net.rails.support.job.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.Trigger.CompletedExecutionInstruction;

import net.rails.ext.AbsGlobal;
import net.rails.support.Support;

public final class JobWorker {
	
	public DefaultScheduleWorker defaultSchedule(AbsGlobal g){
		return new DefaultScheduleWorker(g) {
			final List<JobObject> SCHEDULE_JOBS = new ArrayList<JobObject>();
			@Override
			public List<JobObject> getScheduleJobs() {
				JobObject jobObject = new JobObject();
				Object o = Support.env().get("jobs");
				if (o instanceof List) {
					List<Map<String, Object>> jobs = (List<Map<String, Object>>) o;
					for (Map<String, Object> job : jobs) {
						String jobName = (String) Support.map(job).keys().get(0);
						Map<String, String> jobItem = (Map<String, String>) job.get(jobName);
						String jobClass = jobItem.get("classify");
						String hostnames = jobItem.get("hostnames") == null ? "%" : jobItem.get("hostnames");
						String cronExpression = (String) jobItem.get("cron_expression");
						String jobGroup = "DEFAULT_JOB_GROUP";
						String triggerGroup = "DEFAULT_TRIGGER_GROUP";
						String triggerName = "Trigger_" + jobName;
						jobObject = new JobObject();
						jobObject.setClassify(jobClass);
						jobObject.setCronExpression(cronExpression);
						jobObject.setJobGroup(jobGroup);
						jobObject.setJobName(jobName);
						jobObject.setTriggerGroup(triggerGroup);
						jobObject.setTriggerName(triggerName);
						jobObject.setHostnames(hostnames);
						SCHEDULE_JOBS.add(jobObject);
					}
				}
				return SCHEDULE_JOBS;
			}
			
			@Override
			public TriggerListener getTriggerListener() {
				return new TriggerListener() {

					@Override
					public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
						boolean veto = false;
						String local = null;
						String currentTriggerName = null;
						String currentTriggerGroup = null;
						for (Iterator<JobObject> iterator = SCHEDULE_JOBS.iterator(); iterator.hasNext();) {
							JobObject jobObject = iterator.next();
							List<String> hosts = Arrays.asList(jobObject.getHostnames().split(","));
							local = getHostname();
							currentTriggerGroup = trigger.getKey().getGroup();
							currentTriggerName = trigger.getKey().getName();
							if (currentTriggerGroup.equals(jobObject.getTriggerGroup()) && currentTriggerName.equals(jobObject.getTriggerName())) {
								if (hosts.contains("%") || hosts.contains(local)) {
									veto = false;
								} else {
									veto = true;
								}
								log.debug(String.format("Job %s(%s.%s) veto status: %s", local,currentTriggerGroup,currentTriggerName, veto));
							}
						}
						return veto;
					}

					@Override
					public void triggerMisfired(Trigger trigger) {

					}

					@Override
					public void triggerFired(Trigger trigger, JobExecutionContext context) {

					}

					@Override
					public void triggerComplete(Trigger trigger, JobExecutionContext context,
							CompletedExecutionInstruction triggerInstructionCode) {
						
					}

					@Override
					public String getName() {
						return "DefaultScheduleWorker-TriggerListener";
					}
				};
			}

			@Override
			public JobListener getJobListener() {
				return null;
			}
		};
	}

}
