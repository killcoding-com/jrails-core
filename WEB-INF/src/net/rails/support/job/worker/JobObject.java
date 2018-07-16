package net.rails.support.job.worker;

public class JobObject {
	
	private String classify;
	private String jobGroup;
	private String jobName;
	private String triggerGroup;
	private String triggerName;
	private String cronExpression;
	private String hostnames;
	
	public JobObject() {
		super();
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getTriggerGroup() {
		return triggerGroup;
	}

	public void setTriggerGroup(String triggerGroup) {
		this.triggerGroup = triggerGroup;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getClassify() {
		return classify;
	}

	public void setClassify(String classify) {
		this.classify = classify;
	}

	public String getHostnames() {
		return hostnames;
	}

	public void setHostnames(String hostnames) {
		this.hostnames = hostnames;
	}

}
