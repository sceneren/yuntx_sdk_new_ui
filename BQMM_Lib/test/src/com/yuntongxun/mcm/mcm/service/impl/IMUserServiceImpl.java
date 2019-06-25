package com.yuntongxun.mcm.mcm.service.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MsgJsonData;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.SeqInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.quartz.QuartzManager;
import com.yuntongxun.mcm.mcm.service.DataAsyncService;
import com.yuntongxun.mcm.mcm.service.IMUserService;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

public class IMUserServiceImpl implements IMUserService {

	public static final Logger logger = LogManager.getLogger(IMUserServiceImpl.class);
	
	private RMServerRequestService rmServerRequestService;
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private PushService pushService;
	
	private VersionDao versionDao;
	
	private DataAsyncService dataAsyncService;
	
	private WeiXinGWService weixinService;
	
	private Integer defaultQueueType;
	
	@Override
	public void imStartAsk(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasOsUnityAccount()){
			logger.warn("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}

		String osUnityAccount = sendMsg.getOsUnityAccount();
		if(StringUtils.isBlank(osUnityAccount)){
			logger.warn("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}
		
		logger.info("osUnityAccount: {}.", osUnityAccount);
		
		String agentId = "";
		if(sendMsg.hasAgentId()){
			agentId = sendMsg.getAgentId();
			logger.info("agentId: {}.", agentId);
		}
		
		int queueType = -1;
		if(sendMsg.hasQueueType()){
			queueType = sendMsg.getQueueType();
			logger.info("queueType: {}.", queueType);
		}
		
		sendImStartAskRequestToRm(connector, userAcc, sendMsg, protoClientNo, 
				agentId, queueType);
	}
	
	/**
	 * @Description: 发送分配坐席请求至rm server
	 * @param connector
	 * @param userAcc
	 * @param sendMsg
	 * @param protoClientNo
	 * @param agentId
	 * @param queueType
	 * @throws CCPServiceException
	 */
	private void sendImStartAskRequestToRm(Connector connector, String userAcc, MCMDataInner sendMsg, 
			int protoClientNo, String agentId, int queueType) throws CCPServiceException{
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAcc);
		
		if(userAndAgentDialog != null && userAndAgentDialog.getIsReserved() == Constants.IS_NOT_RESERVED){
			int queueCount = userAndAgentDialog.getQueueCount();
			logger.info("user already ask, queueCount: {}.", queueCount);
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
			pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
			
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			if(!agentInfoSet.isEmpty()){
				logger.info("exist agentInfo...");
				// 看能不能再这再次发送又新用户咨询的请求至坐席端
				
			}else{
				logger.info("not exist agentInfo...");
				
				if(queueCount > 0){
					mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE);
					mcmMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
					mcmMessageInfo.setMsgContent("正在排队中...");
					mcmMessageInfo.setOsUnityAccount(Constants.OS_SYSTEM_ACCOUNT);
					
					pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
				}
			}
			
		} else{
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, connector.getConnectorId());
			if(userAndAgentDialog != null && userAndAgentDialog.getIsReserved() == Constants.IS_RESERVED){
				sid = userAndAgentDialog.getSid();
			} else {
				// 生成会话记录
				saveDialog(sendMsg.getCCSType(), sendMsg.getOsUnityAccount(), sid, userAcc, 
						MCMChannelTypeInner.MCType_im_VALUE);	
			}
			
			// 申请获取坐席
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setSid(sid); // 用户session ID
			
			// 锁定指定服务座席工号
			if(StringUtils.isNotBlank(agentId)){
				logger.info("start ask lock agentId: {}.", agentId);
				
				rmServerTransferData.setAgentid(agentId); 	
				// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
				rmServerTransferData.setForce(true);
			}
			
			// 进入队列的类型，默认值为0
			if(queueType >= 0){
				rmServerTransferData.setQueuetype(queueType);  
			} else {
				if(StringUtils.isBlank(agentId)){
					// 2016-03-23 add by leizi 
					logger.info("set default queue type: {}.", defaultQueueType);
					if(defaultQueueType == null){
						logger.warn("default queue type is null.");
					} else{
						rmServerTransferData.setQueuetype(defaultQueueType);	
					}
				}
			}
			
			rmServerTransferData.setUseraccount(userAcc);
	
			if(!sendMsg.hasChanType()){
				// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
				String chanType = sendMsg.getChanType();
				int ct = 0;
				if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
					ct = Integer.parseInt(chanType);
				}
				rmServerTransferData.setKeytype(ct);
			}
			
			// 自定义文本内容。对于微信用户此字段会有内容
			// rmServerTransferData.setCustomcontent(customcontent);
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setMcmEvent(sendMsg.getMCMEvent());
			seqInfo.setAppId(connector.getAppId());
			seqInfo.setProtoClientNo(protoClientNo);
			seqInfo.setUserAccount(userAcc);
			seqInfo.setLogSessionId(ThreadContext.peek());
			seqInfo.setConnectorId(connector.getConnectorId());
			
			String msgJsonData = "";
			if(sendMsg.hasMsgJsonData()){
				msgJsonData = sendMsg.getMsgJsonData();
			}
			logger.info("im user start ask msgJsonData: {}.", msgJsonData);
			seqInfo.setMsgJsonData(msgJsonData);
			
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
			rmServerRequestService.doPushMessage(cmdMessage);
		}
	}
	
	@Override
	public void weixinStartAsk(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) 
			throws CCPServiceException {
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
		String osUnityAccount = weixinMsg.getOpenID();
		if(StringUtils.isBlank(osUnityAccount)){
			logger.info("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}

		logger.info("osUnityAccount: {}.", osUnityAccount);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() > 0){
			logger.info("user already ask, have agentInfo.");
			
			McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
			weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
			weiXinMsgInfo.setUserID(weixinMsg.getUserID());
			weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
			weiXinMsgInfo.setContent("会话已建立...");
			weixinService.sendWeiXinMsg(weiXinMsgInfo);
			
			return;
			
		} else if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() == 0){
			logger.info("user already ask, not exist agentInfo.");
			
			if(userAndAgentDialog.getQueueCount() > 0){
				logger.info("already queue.");
				McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
				weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
				weiXinMsgInfo.setUserID(weixinMsg.getUserID());
				weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
				weiXinMsgInfo.setContent("会话已建立...");
				weixinService.sendWeiXinMsg(weiXinMsgInfo);
			}
			
			return;
			
		}else{
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, weixinMsg.getOpenID());
			
			// 生成会话记录
			saveDialog(0, osUnityAccount, sid, userAccount, MCMChannelTypeInner.MCType_wx_VALUE);
			
			// 用户开始咨询响应
			McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
			weiXinMsgInfo.setOpenID(osUnityAccount);
			weiXinMsgInfo.setUserID(weixinMsg.getUserID());
			weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
			weiXinMsgInfo.setContent("会话已建立...");
			weixinService.sendWeiXinMsg(weiXinMsgInfo);
			
			// 申请获取坐席
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
			rmServerTransferData.setAppid(weixinMsg.getAppId());
			rmServerTransferData.setSid(sid); // 用户session ID
			
			// 锁定指定服务座席工号
			//	if(!sendMsg.hasAgentId() && StringUtils.isNotEmpty(sendMsg.getAgentId())){
			//		logger.info("start ask lock agentId: {}.", sendMsg.getAgentId());
			//		rmServerTransferData.setAgentid(sendMsg.getAgentId()); 	
			//	}
						
			// 进入队列的类型，默认值为0
			//	if(!sendMsg.hasQueueType() && sendMsg.getQueueType() > 0){
			//		rmServerTransferData.setQueuetype(sendMsg.getQueueType());  
			//	}
			
			// 2016-03-23 add by leizi 
			logger.info("set default queue type: {}.", defaultQueueType);
			if(defaultQueueType == null){
				logger.warn("default queue type is null.");
			} else{
				rmServerTransferData.setQueuetype(defaultQueueType);	
			}
			
			// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
			// rmServerTransferData.setForce(force);
			rmServerTransferData.setUseraccount(userAccount);
	
			rmServerTransferData.setKeytype(MCMChannelTypeInner.MCType_wx_VALUE);
			
			// 自定义文本内容。对于微信用户此字段会有内容
			// rmServerTransferData.setCustomcontent(customcontent);
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setMcmEvent(weixinMsg.getMCMEvent());
			seqInfo.setAppId(weixinMsg.getAppId());
			seqInfo.setUserAccount(userAccount);
			seqInfo.setLogSessionId(ThreadContext.peek());
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
			rmServerRequestService.doPushMessage(cmdMessage);
		}
	}

	/**
	 * @Description: 生成实时会话记录
	 * @param ccsType
	 * @param osUnityAccount
	 * @param connectorId
	 * @param sid
	 * @param userAcc
	 */
	private void saveDialog(int ccsType, String osUnityAccount, String sid, String userAcc, 
			Integer chanType){
		UserAndAgentDialog userAndAgentDialog = new UserAndAgentDialog();
		userAndAgentDialog.setCCSType(ccsType);
		userAndAgentDialog.setChannel(String.valueOf(Constants.M3C_SERIAL));
		userAndAgentDialog.setOsUnityAccount(osUnityAccount);
		userAndAgentDialog.setSid(sid);
		userAndAgentDialog.setDateCreated(System.currentTimeMillis());
		userAndAgentDialog.setChanType(chanType);
		
		String appId = StringUtil.getAppIdFormUserAcc(userAcc);
		userAndAgentDialog.setAppId(appId);
		
		userAgentDialogRedisDao.saveDialog(userAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		userAgentDialogRedisDao.saveDialogSid(sid, userAcc, Constants.DIALOG_VALID_TIME);
	}
	
	@Override
	public void endAsk(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_EndAskResp_VALUE);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAcc);
		if(userAndAgentDialog == null){
			logger.warn("endAsk fail, get user and agent dialog is null.");
		} else{
			serviceEnd(userAndAgentDialog, connector, sendMsg.getMCMEvent(), userAcc);
		}
	}

	/**
	 * @Description: 用户结束会话
	 * @param userAndAgentDialog
	 * @param connector
	 * @param mcmEvent
	 * @param userAcc 
	 * @throws CCPServiceException
	 */
	private void serviceEnd(UserAndAgentDialog userAndAgentDialog, Connector connector, int mcmEvent, 
			String userAcc) throws CCPServiceException{
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		if(agentInfoSet.isEmpty()){
			logger.info("agentInfoList is empty, start exit queue.");
			
			// 退出排队
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_EXIT_CCS_QUEUE.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			rmServerTransferData.setCallid(userAndAgentDialog.getSid());
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setLogSessionId(ThreadContext.peek());
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdExitCCSQueue();
			rmServerRequestService.doPushMessage(cmdMessage);
			
		} else {
			logger.info("agentInfoList is not empty, start end server.");
			// 通知rm server结束服务
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setSid(userAndAgentDialog.getSid());
			
			for(AgentInfo agentInfo : agentInfoSet){
				rmServerTransferData.setAgentid(agentInfo.getAgentId());
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setMcmEvent(mcmEvent);
				seqInfo.setUserAccount(userAcc);
				seqInfo.setAgentId(agentInfo.getAgentId());
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
			}
		}
	}
	
	@Override
	public void sendMSG(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasOsUnityAccount()){
			logger.warn("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}
		
		if(sendMsg.getMSGDataCount() <= 0){
			logger.warn("MSGData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_MSGDAtA_EMPTY);
		}
		
		String osUnityAccount = sendMsg.getOsUnityAccount(); 
		logger.info("osUnityAccount: {}.", osUnityAccount);
		
		// 通知用户发送消息响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_SendMSGResp_VALUE);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAcc);
		if(userAndAgentDialog == null){
			logger.warn("sendMSG fail, get user and agent dialog is null.");
			return;
		}
		
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		// 判断是否存在定时器，存在，则取消，
		String jobName = userAndAgentDialog.getSid();
		if(QuartzManager.isExistJob(jobName)){
			QuartzManager.removeJob(jobName);
			
			for(AgentInfo agentInfo : agentInfoSet){
				allocImAgent(connector, sendMsg, userAndAgentDialog.getSid(), String.valueOf(agentInfo.getAgentId()), 
						userAcc, protoClientNo);
			}
		}else{
			// 推送消息
			for(AgentInfo agentInfo : agentInfoSet){
				List<MSGDataInner> msgDataList = sendMsg.getMSGDataList();
				String agentAccount = agentInfo.getAgentAccount();
				String agentId = agentInfo.getAgentId();
				
				for(MSGDataInner msgData : msgDataList){
					long version = -1; // versionDao.getMessageVersion(agentAccount);
					String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, connector.getConnectorId());
					
					mcmMessageInfo = buildMCMMessageInfo(sendMsg, msgData, version, connector, msgId, 
							agentAccount, sendMsg.getMCMEvent(), MCMChannelTypeInner.MCType_im_VALUE, userAndAgentDialog);
					pushService.doPushMsg(agentAccount, mcmMessageInfo, userAndAgentDialog, userAcc);
					
					// 异步保存消息数据导数据库
					dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData, version, 
							connector, userAndAgentDialog, msgId, null, agentAccount, agentId);
					
				}// for msgDataList
			}
		}
	}

	/**
	 * @Description: 取消定时器,重新分配坐席
	 * @param connector
	 * @param sendMsg
	 * @param sid
	 * @param agentId
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	private void allocImAgent(Connector connector, MCMDataInner sendMsg, String sid, 
			String agentId, String userAcc, int protoClientNo) throws CCPServiceException{
		// 重新分配制度坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setConnectorId(connector.getConnectorId());
		seqInfo.setLogSessionId(ThreadContext.peek());
		
		List<MSGDataInner> msgDataList = sendMsg.getMSGDataList();
		if(msgDataList != null && !msgDataList.isEmpty()){
			MSGDataInner msgData = msgDataList.get(0);
			
			seqInfo.setMsgContent(msgData.getMsgContent());
			seqInfo.setMsgFileUrl(msgData.getMsgFileUrl());
			seqInfo.setMsgType(msgData.getMsgType());
			seqInfo.setMsgFileName(msgData.getMsgFileName());
		}
		
		rmServerTransferData.setSeq(seqInfo.toString());
		rmServerTransferData.setSid(sid); // 用户session ID
		rmServerTransferData.setAgentid(agentId); // 锁定指定服务座席工号
		
		rmServerTransferData.setQueuetype(0);
		// rmServerTransferData.setQueuetype(sendMsg.getQueueType()); // 进入队列的类型，默认值为0。
		// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
		rmServerTransferData.setForce(true);
		rmServerTransferData.setUseraccount(userAcc);
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = sendMsg.getChanType();
		int ct = 0;
		if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			ct = Integer.parseInt(chanType);
		}
		
		rmServerTransferData.setKeytype(ct);
		// 自定义文本内容。对于微信用户此字段会有内容
		// rmServerTransferData.setCustomcontent(customcontent);
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);
	}
	
	/**
	 * @Description: 封装发送消息对象
	 * @param sendMsg  
	 * @param msgData
	 * @param version
	 * @param connector
	 */
	private MCMMessageInfo buildMCMMessageInfo(MCMDataInner sendMsg, MSGDataInner msgData, Long version, 
			Connector connector, String msgId, String agentAccount, int mcmEvent, int chanType,
			UserAndAgentDialog userAndAgentDialog) {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMsgId(msgId);
		
		String agentAppId = StringUtil.getAppIdFormUserAcc(agentAccount);
		String userAppId = connector.getAppId();
		String userName = connector.getUserName();
		if(!agentAppId.equals(userAppId)){
			userName = StringUtil.getUserAcc(userAppId, userName);
		}
		mcmMessageInfo.setUserAccount(userName);
		
		//mcmMessageInfo.setVersion(version);
		mcmMessageInfo.setMCMEvent(mcmEvent);
		mcmMessageInfo.setOsUnityAccount(sendMsg.getOsUnityAccount());
		
		mcmMessageInfo.setMsgType(msgData.getMsgType());
		mcmMessageInfo.setMsgContent(msgData.getMsgContent());
		mcmMessageInfo.setMsgFileName(msgData.getMsgFileName());
		mcmMessageInfo.setMsgFileUrl(msgData.getMsgFileUrl());
		mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
		
		mcmMessageInfo.setChanType(chanType);
		mcmMessageInfo.setAppId(connector.getAppId());
		
		// 添加会话Id
		MsgJsonData msgJsonData = new MsgJsonData();
		msgJsonData.setSessionId(userAndAgentDialog.getSid());
		mcmMessageInfo.setMsgJsonData(msgJsonData.toJson());
		
		return mcmMessageInfo;
	}
	
	@Override
	public void getAGList(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
	}
	
	@Override
	public void ircn(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		
	}
	
	@Override
	public void sendWeixinMSG(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) 
			throws CCPServiceException {
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
		if(StringUtils.isBlank(weixinMsg.getOpenID())){
			logger.warn("sendWeixinMSG error: openId is empty.");
		}
		
		String openId = weixinMsg.getOpenID(); 
		logger.info("@userAccount: {}.", userAccount);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.info("sendMSG fail, get user and agent dialog is null.");
			McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
			weiXinMsgInfo.setOpenID(openId);
			weiXinMsgInfo.setUserID(weixinMsg.getUserID());
			weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
			weiXinMsgInfo.setContent("请先点击开始咨询，待分配客服后才可以为您服务.");
			weixinService.sendWeiXinMsg(weiXinMsgInfo);
			
			return;
		}
		
		// 推送消息
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			String agentAccount = agentInfo.getAgentAccount();
			String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, weixinMsg.getOpenID());
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMsgId(msgId);
			
			//String agentAppId = StringUtil.getAppIdFormUserAcc(agentAccount);
			//String userAppId = weixinMsg.getAppId();
			String userName = weixinMsg.getUserID();
			mcmMessageInfo.setUserAccount(userName);
			
			//mcmMessageInfo.setVersion(version);
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
			mcmMessageInfo.setOsUnityAccount(weixinMsg.getOpenID());
			
			mcmMessageInfo.setMsgType(weixinMsg.getIMMsgType(weixinMsg.getMsgType()));
			mcmMessageInfo.setMsgContent(weixinMsg.getContent());
			mcmMessageInfo.setMsgFileUrl(weixinMsg.getUrl());
			mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
			
			mcmMessageInfo.setChanType(MCMChannelTypeInner.MCType_wx_VALUE);
			mcmMessageInfo.setAppId(weixinMsg.getAppId());
			
			// 添加会话Id
			MsgJsonData msgJsonData = new MsgJsonData();
			msgJsonData.setSessionId(userAndAgentDialog.getSid());
			mcmMessageInfo.setMsgJsonData(msgJsonData.toJson());
			
			pushService.doPushMsg(agentAccount, mcmMessageInfo, userAndAgentDialog, userAccount);
		}
		
		// 判断是否存在定时器，存在，则取消，
		String jobName = userAndAgentDialog.getSid();
		if(QuartzManager.isExistJob(jobName)){
			QuartzManager.removeJob(jobName);
			
			for(AgentInfo agentInfo : agentInfoSet){
				allocImAgentForWeixin(weixinMsg,appAttrs,userAndAgentDialog,agentInfo);
			}
		}
	}

	/**
	 * @param userAndAgentDialog 
	 * @Description: 分配坐席
	 * @param connector
	 * @param sendMsg
	 * @param sid
	 * @param agentId
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	private void allocImAgentForWeixin(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs, 
			UserAndAgentDialog userAndAgentDialog, AgentInfo agentInfo) throws CCPServiceException{
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
		if(userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("allocImAgentForWeixin error, agentInfo is empty.");
		}
		
		// 重新分配制度坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(weixinMsg.getAppId());
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
		seqInfo.setAppId(weixinMsg.getAppId());
		seqInfo.setMsgType(StringUtils.isNumeric(weixinMsg.getMsgType()) ? Integer.parseInt(weixinMsg.getMsgType()) : 0);
		seqInfo.setMsgContent(weixinMsg.getContent());
		seqInfo.setMsgFileUrl(weixinMsg.getUrl());
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		rmServerTransferData.setAgentid(agentInfo.getAgentId()); // 锁定指定服务座席工号
		
		rmServerTransferData.setQueuetype(0);
		// rmServerTransferData.setQueuetype(sendMsg.getQueueType()); // 进入队列的类型，默认值为0。
		// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
		rmServerTransferData.setForce(true);
		rmServerTransferData.setUseraccount(userAccount);
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		
		rmServerTransferData.setKeytype(MCMChannelTypeInner.MCType_wx_VALUE);
		// 自定义文本内容。对于微信用户此字段会有内容
		// rmServerTransferData.setCustomcontent(customcontent);
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);
	}
	
	@Override
	public void userDisconnect(Connector connector, String userAcc, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException {
		serviceEnd(userAndAgentDialog, connector, MCMEventDefInner.ConnectorNotify_UserDisconnect_VALUE, userAcc);
	}
	
	/**
	 * set inject
	 */
	public void setUserAgentDialogRedisDao(
			UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}

	public void setRmServerRequestService(
			RMServerRequestService rmServerRequestService) {
		this.rmServerRequestService = rmServerRequestService;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}

	public void setDataAsyncService(DataAsyncService dataAsyncService) {
		this.dataAsyncService = dataAsyncService;
	}
	
	public void setWeixinService(WeiXinGWService weixinService) {
		this.weixinService = weixinService;
	}

	public void setDefaultQueueType(Integer defaultQueueType) {
		this.defaultQueueType = defaultQueueType;
	}
	
}
