package com.yuntongxun.mcm.mcm.quartz;

import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {

	public static final Logger logger = LogManager.getLogger(QuartzManager.class);
	
	private static SchedulerFactory sf = new StdSchedulerFactory();
	
	private static String JOB_GROUP_NAME = "agentDialogGroup";
	
	private static String TRIGGER_GROUP_NAME = "agentDialogTrigger";
	
	//private static String DEFAULT_TIME = "0 23/2 * * * ?";
	
	private static long DEFAULT_TIME2 = 1000 * 60 * 5;
	
	public static void addJob(String jobName, Job job) {
		if(!isExistJob(jobName)){
			//addJob(jobName, job, DEFAULT_TIME);	
			addSimpleJob(jobName, job, DEFAULT_TIME2);
		}
	}

	/**
	 * @Description：添加一个定时任务，使用默认的任务组名，触发器名，触发器组名
	 * @param jobName 任务名
	 * @param job 任务
	 * @param time 时间设置
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public static void addJob(String jobName, Job job, String time) {
		logger.info("quartz manager add job, jobName: {}.", jobName);
		
		try {
			Scheduler sched = sf.getScheduler();
			JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME, job.getClass());// 任务名，任务组，任务执行类
			// 触发器
			CronTrigger trigger = new CronTrigger(jobName, TRIGGER_GROUP_NAME);
			// 触发器时间设定
			trigger.setCronExpression(time);
			sched.scheduleJob(jobDetail, trigger);
			// 启动
			if (!sched.isShutdown()){
				sched.start();
			}
		} catch (Exception e) {
			logger.error("addJob[" + jobName + "] error: ", e);
		}
	}

	public static void addSimpleJob(String jobName, Job job, long time) {
		logger.info("quartz manager add simple job, jobName: {}.", jobName);
		
		try {
			Scheduler sched = sf.getScheduler();
			// 任务名，任务组，任务执行类
			JobDetail jobDetail = new JobDetail(jobName, JOB_GROUP_NAME, job.getClass());
			// 触发器
			Trigger trigger = new SimpleTrigger(jobName, TRIGGER_GROUP_NAME, 
					new Date(System.currentTimeMillis() + time), null, 0, 1000L);
			sched.scheduleJob(jobDetail, trigger);
			// 启动
			if (!sched.isShutdown()){
				sched.start();
			}
		} catch (Exception e) {
			logger.error("addSimpleJob[" + jobName + "] error: ", e);
		}
	}
	
	/**
	 * @Description：修改一个任务的触发时间(使用默认的任务组名，触发器名，触发器组名)
	 * @param jobName
	 * @param time
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	public static void modifyJobTime(String jobName, String time) 
			throws SchedulerException, ParseException {
		logger.info("quartz manager modify job time, jobName: {}.", jobName);
		
		Scheduler sched = sf.getScheduler();
		Trigger trigger = sched.getTrigger(jobName, TRIGGER_GROUP_NAME);
		if (trigger != null) {
			CronTrigger ct = (CronTrigger) trigger;
			ct.setCronExpression(time);
			sched.resumeTrigger(jobName, TRIGGER_GROUP_NAME);
		}
	}

	/**
	 * @Description：移除一个任务(使用默认的任务组名，触发器名，触发器组名)
	 * @param jobName
	 * @throws SchedulerException
	 */
	public static void removeJob(String jobName) {
		logger.info("quartz removeJob, jobName: {}.", jobName);
		
		try {
			Scheduler sched = sf.getScheduler();
			sched.pauseTrigger(jobName, TRIGGER_GROUP_NAME);// 停止触发器
			sched.unscheduleJob(jobName, TRIGGER_GROUP_NAME);// 移除触发器
			sched.deleteJob(jobName, JOB_GROUP_NAME);// 删除任务
		} catch (Exception e) {
			logger.error("remove job error: ", e);
		}
	}

	/**
	 * @Description: 判断是否存在 
	 * @throws
	 */
	public static boolean isExistJob(String jobName) {
		boolean isResult = false;
		try {
			Scheduler sched = sf.getScheduler();
			JobDetail jobDetail = sched.getJobDetail(jobName, JOB_GROUP_NAME);
			if(jobDetail != null){
				isResult = true;
			}
			
			logger.info("jobName: {}, isExist: {}.", jobName, isResult);
		} catch (Exception e) {
			logger.error("is exist job error: ", e);
			return false;
		}
		return isResult;
	}
	
}
