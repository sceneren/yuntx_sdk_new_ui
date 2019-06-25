package com.yuntongxun.mcm.mcm.quartz;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.dao.UserRedisDao;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.SpringUtil;

public class AgentDialogTimeOutJob implements Job{

	public static final Logger logger = LogManager.getLogger(AgentDialogTimeOutJob.class);
	
	public AgentDialogTimeOutJob() {
		super();
	}

	private UserRedisDao userRedisDao = (UserRedisDao)SpringUtil.getBean("userRedisDao");
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao = (UserAgentDialogRedisDao)SpringUtil.getBean("userAgentDialogRedisDao");
	
	private PushService pushService = (PushService)SpringUtil.getBean("pushService"); 
	
	private AsService asService = (AsService)SpringUtil.getBean("asService"); 
	
	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		String sid = jec.getJobDetail().getName();
		logger.info("@AgentDialogTimeOutJob..." + sid);
		
		try {
			String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
			UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
			if(userAndAgentDialog == null){
				logger.warn("userAndAgentDialog is null.");
				
			}else{
				String notifyUrl = userAndAgentDialog.getMcm_notify_url();
				if(StringUtils.isNotBlank(notifyUrl)){
					logger.info("out time send stop message to :{}.", notifyUrl);
					
					// 结束咨询
					boolean result = asService.stopMsgRequestAS(userAndAgentDialog);
					if(result){
						logger.info("stop message to as return result: {}.", result);
					}
				}
				
				List<UserLoginInfo> userLoginInfoList = null;
				
				// 查询当前座席在线状态
				Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
				for(AgentInfo agentInfo : agentInfoSet){
					String agentAccount = agentInfo.getAgentAccount();
					userLoginInfoList = userRedisDao.getUserLoginInfo(agentAccount);
					boolean agentIsOnline = false;
					for(int i=0;i<userLoginInfoList.size();i++){
						if(userLoginInfoList.get(i)!=null){
							agentIsOnline = true;
						}
					}
					
					if(agentIsOnline){
						sendNotify(agentAccount, MCMEventDefInner.NotifyAgent_STExpired_VALUE, userAccount);
					}
				}
				
				// 查询用户在线状态
				userLoginInfoList = userRedisDao.getUserLoginInfo(userAccount);
				boolean userIsOnline = false;
				for(int i=0;i<userLoginInfoList.size();i++){
					if(userLoginInfoList.get(i)!=null){
						userIsOnline = true;
					}
				}
				
				if(userIsOnline){
					sendNotify(userAccount, MCMEventDefInner.NotifyUser_EndAsk_VALUE, "");
				}
				
				// 删除会话
				userAgentDialogRedisDao.deleteDialog(userAccount);
				userAgentDialogRedisDao.deleteDialogSid(sid);
			}
			
		} catch (CCPServiceException e) {
			logger.error("AgentDialogTimeOutJob#error()", e);
		} 
		
	}
	
	private void sendNotify(String userAcc, int mcmEvent, String userAccount) 
			throws CCPServiceException {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(mcmEvent);
		mcmMessageInfo.setUserAccount(userAccount);
		
		pushService.doPushMsg(userAcc, mcmMessageInfo);
	}

}
