package net.rails.web;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.PropertyConfigurator;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.Define;
import net.rails.active_record.Adapter;
import net.rails.ext.AbsGlobal;
import net.rails.log.LogPoint;
import net.rails.support.Support;
import net.rails.support.job.worker.DefaultScheduleWorker;
import net.rails.support.job.worker.JobObject;

@WebListener
public class ApplicationListener implements ServletContextListener {

	private Logger log;
	private ServletContextEvent context;

	public ApplicationListener() {
		super();
		log = LoggerFactory.getLogger(ApplicationListener.class);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.info("ApplicationListener Destroying...");
		shutdownScheduler();
		try{
			Adapter.cleaupDataSource();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
		log.info("ApplicationListener Destroyed.");
	}

	@Override
	public void contextInitialized(final ServletContextEvent context) {
		this.context = context;
		log.debug("ApplicationListener Initialized!");
		if (Define.CONFIG_PATH == null) {
			String webinf = new File(String.format("%s/WEB-INF/", context.getServletContext().getRealPath("/")))
					.getAbsolutePath();
			Define.CONFIG_PATH = String.format("%s/config/", webinf);
			log.debug("Set Define.CONFIG_PATH = {}", Define.CONFIG_PATH);
		}
		if (Define.VIEW_PATH == null) {
			String webinf = new File(String.format("%s/WEB-INF/", context.getServletContext().getRealPath("/")))
					.getAbsolutePath();
			Define.VIEW_PATH = String.format("%s/view/", webinf);
			log.debug("Set Define.VIEW_PATH = {}", Define.VIEW_PATH);
		}
		File logCnfFile = new File(String.format("%s/log4j.properties", Define.CONFIG_PATH));
		if (logCnfFile.exists()) {
			log.debug("Use {}",logCnfFile.getAbsolutePath());
			try {
				PropertyConfigurator.configure(logCnfFile.toURI().toURL());
			} catch (MalformedURLException e) {
				log.error(e.getMessage(), e);
			}
		}
		logCnfFile = new File(String.format("%s/log4j.xml", Define.CONFIG_PATH));
		if (logCnfFile.exists()) {
			log.debug("Use {}",logCnfFile.getAbsolutePath());
			try {
				PropertyConfigurator.configure(logCnfFile.toURI().toURL());
			} catch (MalformedURLException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		File filterPropFile = new File(String.format("%s/filter.properties", Define.CONFIG_PATH));
		if(filterPropFile.exists()){
			try{
				Properties ps = new Properties();
				ps.load(new FileInputStream(filterPropFile));
				LogPoint.configure(ps);
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}

		final AbsGlobal g = new AbsGlobal() {

			@Override
			public void setUserId(Object userId) {

			}

			@Override
			public Object getUserId() {
				return null;
			}

			@Override
			public void setSessionId(Object sessionId) {

			}

			@Override
			public Object getSessionId() {
				return null;
			}

			@Override
			public String getRealPath() {
				return context.getServletContext().getRealPath("/");
			}

		};
		startJobs(g);
	}

	private void startJobs(AbsGlobal g) {
		if (!Support.env().getRoot().containsKey("jobs")) {
			return;
		}
		
		DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
		Scheduler scheduler = null;
		Object o = Support.env().get("jobs");
		DefaultScheduleWorker scheduleWorker = null;
		try {
			if (o instanceof List) {
				scheduleWorker = Support.job().defaultSchedule(g);
			} else {
				scheduleWorker = (DefaultScheduleWorker) Class.forName(o.toString()).getConstructor(AbsGlobal.class)
						.newInstance(g);
			}
			List<JobObject> jobs = scheduleWorker.getScheduleJobs();
			log.info("Starting Jobs");
			if (jobs != null) {
				SimpleThreadPool threadPool = new SimpleThreadPool();
				threadPool.setThreadCount(Support.env().getOrDefault("quartz.system_thread_count",10));
				threadPool.setThreadPriority(Thread.NORM_PRIORITY);
				threadPool.setThreadNamePrefix(Define.SYSTEM_SCHEDULER);
				threadPool.initialize();
				factory.createScheduler(Define.SYSTEM_SCHEDULER,Define.SYSTEM_SCHEDULER,threadPool,new RAMJobStore());
				scheduler = factory.getScheduler(Define.SYSTEM_SCHEDULER);
				scheduler.start();
				for (JobObject jobObject : jobs) {
					String jobName = jobObject.getJobName();
					String jobClass = jobObject.getClassify();
					String cronExpression = jobObject.getCronExpression();
					log.info("Starting: {}", jobName);
					log.info("Class: {}", jobClass);
					log.info("Cron Expression: {}", cronExpression);
					org.quartz.Job job = (org.quartz.Job) Class.forName(jobClass).newInstance();
					JobDetail jobDetail = JobBuilder.newJob(job.getClass())
							.withIdentity(jobName, jobObject.getJobGroup()).build();
					jobDetail.getJobDataMap().put("AbsGlobal", g);
					Trigger trigger = TriggerBuilder.newTrigger()
							.withIdentity(jobObject.getTriggerName(), jobObject.getTriggerGroup())
							.withSchedule(CronScheduleBuilder.cronSchedule(jobObject.getCronExpression())).build();
					JobListener jobListener = scheduleWorker.getJobListener();
					TriggerListener triggerListener = scheduleWorker.getTriggerListener();
					if (jobListener != null) {
						scheduler.getListenerManager().addJobListener(scheduleWorker.getJobListener());
					}
					if (triggerListener != null) {
						scheduler.getListenerManager()
								.addTriggerListener(scheduleWorker.getTriggerListener());
					}
					scheduler.scheduleJob(jobDetail, trigger);
				}
			}
			log.info("Started Jobs");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void shutdownScheduler() {
		DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
		Scheduler sysScheduler = null;
		try {
			Collection<Scheduler> schedulers = factory.getAllSchedulers();
			log.info("Shutdown Scheduler Total: {}",schedulers.size());
			for (Iterator<Scheduler> iterator = schedulers.iterator(); iterator.hasNext();) {
				Scheduler scheduler = iterator.next();
				if(scheduler.getSchedulerName().equals(Define.SYSTEM_SCHEDULER)){
					sysScheduler = scheduler;
				}else{
					shutdownScheduler(scheduler);
				}
			}
			if(sysScheduler != null){
				shutdownScheduler(sysScheduler);
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	private void shutdownScheduler(Scheduler scheduler){
		try {

			if (scheduler != null) {
				List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
				for (String groupName : triggerGroupNames) {
					log.info("UnscheduleJobs: {}",groupName);
					Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
					scheduler.unscheduleJobs(new ArrayList<TriggerKey>(triggerKeys));
				}
				List<String> jobGroupNames = scheduler.getTriggerGroupNames();
				for (Iterator<String> iterator = jobGroupNames.iterator(); iterator.hasNext();) {
					String groupName = iterator.next();
					log.info("DeleteJobs: {}",groupName);
					Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
					scheduler.deleteJobs(new ArrayList<JobKey>(jobKeys));
				}
				scheduler.shutdown();
			}
			log.info("Scheduler Shutdown Status: {} {}",scheduler.getSchedulerName(), scheduler.isShutdown());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
