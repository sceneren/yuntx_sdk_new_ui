package com.yuntongxun.mcm.mcm.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.CcpCustomData;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MsgJsonData;
import com.yuntongxun.mcm.mcm.model.RMServerAgent;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.SeqInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.service.DataAsyncService;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.mcm.service.RMServerResponseService;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.RMServerErrorCode;
import com.yuntongxun.mcm.util.StringUtil;

public class RMServerResponseServiceImpl implements RMServerResponseService{

	public static final Logger logger = LogManager.getLogger(RMServerResponseServiceImpl.class);
	
	private PushService pushService;
	
	private RMServerRequestService rmServerRequestService;
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private AsService asService;
	
	private WeiXinGWService weixinService;
	
	private DataAsyncService dataAsyncService;
	
	@Override
	public void respMCCCModuleLogIn(RMServerTransferData rmServerTransferData) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		logger.info("@respMCCCModuleLogIn statusCode: {}.", statusCode);
		
		if(!Constants.RESPONSE_OK.equals(statusCode)){
			logger.info("login RM Server fail, error code: {}", statusCode);
		}else{
			logger.info("login RM Server success.");	
		}
	}

	@Override
	public void respAgentOnWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		logger.info("@respAgentOnWork statusCode: {}.", statusCode);
		
		if(seqInfo != null){
			String agentAccount = seqInfo.getAgentAccount();
			int mcmEvent = MCMEventDefInner.NotifyAgent_KFOnWorkResp_VALUE;
			int protoClientNo = seqInfo.getProtoClientNo();
			int errorCode = getResultCode(statusCode);
			
			// 坐席签入响应通知
			sendNotifyResponseMessage(mcmEvent, agentAccount, protoClientNo, errorCode);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(mcmEvent, agentAccount, seqInfo.getAgentId(), "", statusCode);
		}
	}
	
	@Override
	public void respAgentOffWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		logger.info("@respAgentOffWork statusCode: {}.", statusCode);
		
		if(seqInfo != null){
			String agentAccount = seqInfo.getAgentAccount();
			int mcmEvent = MCMEventDefInner.NotifyAgent_KFOffWorkResp_VALUE;
			int protoClientNo = seqInfo.getProtoClientNo();
			int errorCode = getResultCode(statusCode);
			
			// 坐席签出响应通知
			sendNotifyResponseMessage(mcmEvent, agentAccount, protoClientNo, errorCode);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(mcmEvent, agentAccount, seqInfo.getAgentId(), "", statusCode);
			
			String tempUserAccount = rmServerTransferData.getUseraccounts();
			logger.info("@respAgentOffWork userAccounts: {}.", tempUserAccount);
			
			if(StringUtils.isNotBlank(tempUserAccount)){
				String[] userAccounts = tempUserAccount.split(",");
				for(String userAccount : userAccounts){
					if(!userAccount.contains("#")){
						userAccount = StringUtil.getUserAcc(seqInfo.getAppId(), userAccount);
					}
					
					UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
					if(userAndAgentDialog != null){
						Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
						String agentId = seqInfo.getAgentId();
						
						// 删除会话
						if(agentInfoSet.size() > 1){
							logger.info("agent offwork start modify dialog");
							
							removeAgentInfo(agentId, agentInfoSet);
							userAndAgentDialog.setAgentInfoSet(agentInfoSet);
							
							userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
									Constants.DIALOG_VALID_TIME);	
						}else{
							logger.info("agent offwork start delete dialog");
							
							// 通知用户会话结束
							sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", 
									null, -1, Constants.SUCC, null, agentId);
							
							userAgentDialogRedisDao.deleteDialog(userAccount);
							userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
						}	
					}else {
						logger.info("agent offwork get dialog is null.");
					}
				}
			}
		}
	}

	@Override
	public void respAgentReady(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		logger.info("@respAgentReady statusCode: {}.", statusCode);
		
		if(seqInfo != null){
			boolean isReady = seqInfo.isReady();
			int event = isReady ? MCMEventDefInner.NotifyAgent_ReadyResp_VALUE : 
				  				  MCMEventDefInner.NotifyAgent_NotReadyResp_VALUE;
			String agentAccount = seqInfo.getAgentAccount();
			int protoClientNo = seqInfo.getProtoClientNo();
			int errorCode = getResultCode(statusCode);
			
			// 坐席就绪未就绪响应通知
			sendNotifyResponseMessage(event, agentAccount, protoClientNo, errorCode);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(event, agentAccount, seqInfo.getAgentId(), "", statusCode);
		}
	}
	
	@Override
	public void respAllocImAgent(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		logger.info("@respAllocImAgent statusCode: {}.", statusCode);

		int resultCode = getResultCode(statusCode);
		logger.info("get resultCode: {}.", resultCode);
		
		// 用户排队参数，返回目前排队人数（包括当前排队用户），为0，表示不用排队
		int queueCount = rmServerTransferData.getQueuecount();
		logger.info("queueCount: {}.", queueCount);
		
		// 分配的坐席Id
		String agentId = rmServerTransferData.getAgentid();
		logger.info("agentId: {}.", agentId);
		
		String sid = rmServerTransferData.getSid();
		logger.info("sid: {}.", sid);
		
		logger.info("userAccount: {}.", rmServerTransferData.getUseraccount());
		
		// 分配的坐席账号
		String customaccount = rmServerTransferData.getCustomaccount();
		logger.info("customaccount: {}.", customaccount);
		
		String welcome = rmServerTransferData.getWelcome();
		logger.info("welcome: {}.", welcome);
		
		String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		
		logger.info("alloc im agentId: {} - customaccount: {} to userAccount: {}.", agentId, 
				customaccount, userAccount);
		
		if(seqInfo != null && userAndAgentDialog != null){
			int mcmEvent = seqInfo.getMcmEvent();
			String asFlag = seqInfo.getAsFlag();
			String optResultCBUrl = userAndAgentDialog.getOptResultCBUrl();
			
			switch(mcmEvent){
				case MCMEventDefInner.UserEvt_StartAsk_VALUE:
					logger.info("user start ask response...");
					
					// 用户开始咨询响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, 
							seqInfo.getUserAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						// 如果排队人数为0, 则分配成功, 否则发送排队通知
						if(queueCount == 0){
							startAskAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, 
									queueCount, seqInfo.getMsgJsonData());	
						} else{
							// 保存排队的人数
							userAndAgentDialog.setQueueCount(queueCount);
							
							userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
									Constants.DIALOG_VALID_TIME);
							
							if(MCMChannelTypeInner.MCType_im_VALUE == userAndAgentDialog.getChanType()){
								sendMessage(MCMEventDefInner.AgentEvt_SendMCM_VALUE, userAccount, "正在排队中...", 
										Constants.OS_SYSTEM_ACCOUNT, seqInfo.getConnectorId());
							}else if(MCMChannelTypeInner.MCType_wx_VALUE == userAndAgentDialog.getChanType()){
								//回复微信消息
								McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
								weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
								String userID = StringUtil.getUserNameFormUserAcc(userAccount);
								weiXinMsgInfo.setUserID(userID);
								weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
								weiXinMsgInfo.setContent("正在排队中...");
								weixinService.sendWeiXinMsg(weiXinMsgInfo);
							}
						}
					} else{
						logger.warn("alloc im Agent fail, statusCode: {}, start delete dialog.", statusCode);
						// 分配失败，清空会话记录
						String tempUserAccount = seqInfo.getUserAccount();
						userAndAgentDialog = userAgentDialogRedisDao.getDialog(tempUserAccount);
						if(userAndAgentDialog != null){
							userAgentDialogRedisDao.deleteDialog(tempUserAccount);
							userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
						}
					}
					
					//如果是AS侧相关的开始咨询，需要判断AS侧是否设置了操作回调方法地址
					if(Constants.AS_FLAG_YES.equals(asFlag) && StringUtils.isNotEmpty(optResultCBUrl)){
						//调用AS侧操作回调方法
						asService.optCallbackRequestAS(userAndAgentDialog, rmServerTransferData);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_TransKF_VALUE:
					logger.info("agent trans kf response...");
					
					// 转接坐席响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_TransKFResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						transKfAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_TransferQueue_VALUE:
					logger.info("agent transfer queue response.");
					
					// 转接队列响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_TransferQueueResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						transferQueueAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_ForceTransfer_VALUE:
					logger.info("agent force transfer response.");
					
					// 强转响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_ForceTransferResp_VALUE, 
							seqInfo.getSuperAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						forceTransferAllocImAgentResp(userAccount, customaccount, seqInfo, agentId, userAndAgentDialog);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_StartConf_VALUE:
					logger.info("agent start conf response.");
					
					// 开始会议响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_StartConfResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						startConfAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_ForceJoinConf_VALUE:
					logger.info("agent force join conf response.");
					
					// 强插响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_ForceJoinConfResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						forceJoinConfAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);
					}
					break;
					
				case MCMEventDefInner.UserEvt_SendMSG_VALUE:
					logger.info("cancel timer send msg response.");
					
					// 用户发送消息，如果存在定时器，分配坐席响应通知
					if(Constants.RESPONSE_OK.equals(statusCode)){
						cancelTimeAllocImAgentResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);	
					}
					break;
					
				case MCMEventDefInner.AgentEvt_SerWithTheUser_VALUE:
					logger.info("agent ser with the user response.");
					
					// 坐席响应通知
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_SerWithTheUserResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						serWithTheUserResp(userAccount, customaccount, agentId, userAndAgentDialog, seqInfo);	
					}
					break;
					
				default:
					logger.info("alloc im Agent unknown resp.");
					break;
			}
			
		} else {
			logger.info("seqInfo or userAndAgentDialog is null");
			
		}// if seqInfo and userAndAgentDialog  
	}
	
	/**
	 * @Description: 为指定用户服务响应通知
	 * @param userAccount 用户账号，含appId
	 * @param customaccount 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param seqInfo 请求rm server透传信息
	 * @throws CCPServiceException
	 */
	private void serWithTheUserResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException {
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		
		//判断chanType类型，如果IM类型，回复IM消息；如果微信类型，回复微信消息
		int chanType = userAndAgentDialog.getChanType();
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			if(MCMChannelTypeInner.MCType_im_VALUE == chanType){
				sendMessage(MCMEventDefInner.AgentEvt_SendMCM_VALUE, userAccount, "您好, 工号: " + agentId + "为您服务", 
						Constants.OS_UNITY_ACCOUNT, seqInfo.getConnectorId());
				
			}else if(MCMChannelTypeInner.MCType_wx_VALUE == chanType){
				//回复微信消息
				McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
				weiXinMsgInfo.setOpenID(seqInfo.getOsUnityAccount());
				String userID = StringUtil.getUserNameFormUserAcc(userAccount);
				weiXinMsgInfo.setUserID(userID);
				weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
				weiXinMsgInfo.setContent("您好, 工号: " + agentId + "为您服务");
				weixinService.sendWeiXinMsg(weiXinMsgInfo);
			}
			
			// 保存会话
			logger.info("serWithTheUserResp after modify user and agent dialog.");
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);	
			userAndAgentDialog.setQueueCount(0);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}
	}

	/**
	 * @Description: 用户开始咨询，分配坐席响应
	 * @param userAccount 用户userAcc
 	 * @param customaccount 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @param queueCount 排队人数
	 * @param statusCode
	 * @throws
	 */
	private void startAskAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, int queueCount, String msgJsonData) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		
		CcpCustomData ccpCustomData = new CcpCustomData();
		ccpCustomData.setMcmEvent(MCMEventDefInner.UserEvt_StartAsk_VALUE);
		
		// 修改实时会话记录
		logger.info("start modify user and agent dialog.");
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			int mcmEvent = MCMEventDefInner.NotifyAgent_NewUserAsk_VALUE;
			int isReserved = userAndAgentDialog.getIsReserved();
			// 预留用户
			if(isReserved == Constants.IS_RESERVED){
				mcmEvent = MCMEventDefInner.NotifyAgent_ReservedUserAsk_VALUE;
			}
			
			// 通知坐席，有新用户咨询
			sendNotifyMessage(mcmEvent, agentAccount, userName, null, -1, 
					Constants.SUCC, ccpCustomData, agentId, msgJsonData);
			
			if(StringUtils.isBlank(userAndAgentDialog.getHistoryAgentIds())){
				userAndAgentDialog.setHistoryAgentIds(agentId);
			}else{
				userAndAgentDialog.setHistoryAgentIds(","+agentId);
			}
			
			agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);	
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(mcmEvent, agentAccount, agentId, userAccount, Constants.RESPONSE_OK);
		}
		
		userAndAgentDialog.setQueueCount(queueCount);
		
		userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
				Constants.DIALOG_VALID_TIME);
	}
	
	/**
	 * @Description: 坐席转接会话给其他坐席，分配坐席响应 
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含AppId
	 * @param agentId 分配的坐席账号
	 * @param userAndAgentDialog 用户咨询会话
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void transKfAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
	
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			CcpCustomData ccpCustomData = new CcpCustomData();
			ccpCustomData.setMcmEvent(MCMEventDefInner.AgentEvt_TransKF_VALUE);
			ccpCustomData.setTransferAgentCount(seqInfo.getAgentAccount());
			ccpCustomData.setTransferAgentId(seqInfo.getAgentId());
			ccpCustomData.setTransferAllocAgentCount(agentAccount);
			ccpCustomData.setTransferAllocAgentId(agentId);
			
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_TransferNewUser_VALUE, agentAccount, 
					userName, null, -1, Constants.SUCC, ccpCustomData, seqInfo.getAgentId());	
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_TransferNewUser_VALUE, 
					agentAccount, agentId, userAccount, "");
		}
	}
	
	/**
	 * @Description: 坐席转接用户到其它队列，分配坐席响应 
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含AppId
	 * @param agentId 分配的坐席账号
	 * @param userAndAgentDialog 用户咨询会话
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void transferQueueAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		
		// 删除转接坐席 
		removeAgentInfo(seqInfo.getAgentId(), agentInfoSet);
					
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			// 通知坐席，有新用户咨询
			CcpCustomData ccpCustomData = new CcpCustomData();
			ccpCustomData.setMcmEvent(MCMEventDefInner.AgentEvt_TransferQueue_VALUE);
			/*ccpCustomData.setTransferAgentCount(seqInfo.getAgentAccount());
			ccpCustomData.setTransferAgentId(seqInfo.getAgentId());
			ccpCustomData.setTransferAllocAgentCount(agentAccount);
			ccpCustomData.setTransferAllocAgentId(agentId);
			logger.info("ccpCustomData: {}.", ccpCustomData.toString());
			*/
			
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_TransferNewUser_VALUE, agentAccount, 
					userName, null, -1, Constants.SUCC, ccpCustomData, seqInfo.getAgentId());
			
			// 修改实时会话, 保存加入者的相关信息
			logger.info("transfer queue after start modify user and agent dialog.");
			
			// 记录分配的坐席
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);	
			
			if(StringUtils.isBlank(userAndAgentDialog.getHistoryAgentIds())){
				userAndAgentDialog.setHistoryAgentIds(agentId);
			}else{
				userAndAgentDialog.setHistoryAgentIds(","+agentId);
			}
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_TransferNewUser_VALUE, 
					agentAccount, agentId, userAccount, "");
		}
		
		// 结束转接的坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
		rmServerTransferData.setAppid(appId);
		
		seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(MCMEventDefInner.AgentEvt_TransferQueue_VALUE);
		seqInfo.setAgentAccount(seqInfo.getAgentAccount());
		seqInfo.setAgentId(seqInfo.getAgentId());
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		//rmServerTransferData.setSid(userAndAgentDialog.getSid());
		rmServerTransferData.setAgentid(seqInfo.getAgentId());
		
		rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
	}
	
	/**
	 * @Description: 坐席强转，分配坐席响应 
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含AppId
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void forceTransferAllocImAgentResp(String userAccount, String customaccount, SeqInfo seqInfo, 
			String agentId, UserAndAgentDialog userAndAgentDialog) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		String agentAccount = StringUtil.getUserAcc(appId, customaccount); // 转接的坐席
		String srcAgentId = seqInfo.getAgentId(); // 源坐席ID
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			String transferAgentAccount = "";
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			for(AgentInfo agentInfo : agentInfoSet){
				if(agentInfo.getAgentId().equals(srcAgentId)){
					transferAgentAccount = agentInfo.getAgentAccount();
				}
			}
			
			CcpCustomData ccpCustomData = new CcpCustomData();
			ccpCustomData.setMcmEvent(MCMEventDefInner.AgentEvt_ForceTransfer_VALUE);
			ccpCustomData.setTransferAgentCount(transferAgentAccount);
			ccpCustomData.setTransferAgentId(srcAgentId);
			ccpCustomData.setTransferAllocAgentCount(agentAccount);
			ccpCustomData.setTransferAllocAgentId(agentId);
			logger.info("ccpCustomData: {}.", ccpCustomData.toString());
			
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_ForceTransfernewUser_VALUE, agentAccount, 
					userName, null, -1, Constants.SUCC, ccpCustomData, seqInfo.getSuperAgentId());		
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_ForceTransfernewUser_VALUE, 
					agentAccount, agentId, userAccount, "");
		}
	}
	
	/**
	 * @Description: 坐席开启会议，分配坐席响应
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void startConfAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			// 获取会议发起者坐席Id
			String startConfAgentId = "";
			for(AgentInfo agentInfo : agentInfoSet){
				if(agentInfo.getAgentAccount().equals(seqInfo.getAgentAccount())){
					startConfAgentId = agentInfo.getAgentId();
					break;
				}
			}
			
			// 通知坐席加入会议
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_InviteJoinConf_VALUE, agentAccount, userName, 
					null, -1, Constants.SUCC, null, startConfAgentId);
			
			// 修改实时会话, 保存加入者的相关信息
			logger.info("start modify user and agent conf dialog.");
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_InviteJoinConf_VALUE, 
					agentAccount, startConfAgentId, userAccount, "");
		}
	}
	
	/**
	 * @Description: 坐席强插，分配坐席响应
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void forceJoinConfAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			for(AgentInfo agentInfo : agentInfoSet){
				if(agentInfo.getAgentId().equals(agentId)){
					continue;
				}
				
				// 通知坐席有强插
				sendNotifyMessage(MCMEventDefInner.NotifyAgent_ForceStartConf_VALUE, agentInfo.getAgentAccount(), 
						userName, null, -1, Constants.SUCC, null, agentInfo.getAgentId());	
				
				// 被动行为统计数据入库
				saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_ForceStartConf_VALUE, 
						agentInfo.getAgentAccount(), agentInfo.getAgentId(), userAccount, "");
			}
			
			// 通知用户强插响应
			sendNotifyMessage(MCMEventDefInner.NotifyUser_StartConf_VALUE, userAccount, "", null, -1, 
					Constants.SUCC, null, seqInfo.getAgentId());
			
			// 修改会话记录
			logger.info("start modify user and agent forceJoinConf dialog.");
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			// 修改实时会话, 保存加入者的相关信息
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}
	}
	
	/**
	 * @Description: 定时器未到期，用户发送消息，重新分配坐席给用户，分配成功发送对应的消息给坐席
	 * @param userAccount 用户userAcc
	 * @param customaccount 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @param seqInfo 请求rm server 透传信息
	 * @throws CCPServiceException
	 */
	private void cancelTimeAllocImAgentResp(String userAccount, String customaccount, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String agentAccount = StringUtil.getUserAcc(appId, customaccount);
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccount)){
			// 推送消息
			int msgType = seqInfo.getMsgType();
			String msgContent = seqInfo.getMsgContent();
			String msgFileName = seqInfo.getMsgFileName();
			String msgFileUrl = seqInfo.getMsgFileUrl();
			String userAppId = seqInfo.getAppId();
			String connectorId = seqInfo.getConnectorId();
			int mcmEvent = seqInfo.getMcmEvent();
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setUserAccount(StringUtil.getUserNameFormUserAcc(userAccount));
			mcmMessageInfo.setMCMEvent(mcmEvent);
			mcmMessageInfo.setMsgType(msgType);
			mcmMessageInfo.setMsgContent(msgContent);
			mcmMessageInfo.setMsgFileName(msgFileName);
			
			mcmMessageInfo.setMsgFileUrl(msgFileUrl);
			mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
			mcmMessageInfo.setChanType(MCMChannelTypeInner.MCType_im_VALUE);
			mcmMessageInfo.setAppId(userAppId);
			
			// 添加会话Id
			MsgJsonData msgJsonData = new MsgJsonData();
			msgJsonData.setSessionId(userAndAgentDialog.getSid());
			mcmMessageInfo.setMsgJsonData(msgJsonData.toJson());
			
			pushService.doPushMsg(agentAccount, mcmMessageInfo);
			
			// 异步保存消息数据导数据库
			MCMEventData.MSGDataInner msgData = MCMEventData.MSGDataInner.newBuilder()
					.setMsgType(msgType)
					.setMsgFileName(msgFileName)
					.setMsgFileUrl(msgFileUrl)
					.setMsgContent(msgContent)
					.build();
			MCMEventData.MCMDataInner sendMsg = MCMEventData.MCMDataInner.newBuilder()
					.setCCSType(0)
					.setMCMEvent(mcmEvent)
					.build();
			
			Connector connector = new Connector();
			connector.setAppId(StringUtil.getAppIdFormUserAcc(userAccount));
			connector.setUserName(StringUtil.getUserNameFormUserAcc(userAccount));
			
			String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, connectorId);
			
			dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData, -1, 
					connector, userAndAgentDialog, msgId, 
					"", agentAccount, agentId);
		}
	}
	
	@Override
	public void cmdWakeUpUser(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		logger.info("@cmdWakeUpUser...");
		String agentId = rmServerTransferData.getAgentid();
		logger.info("agentId: {}.", agentId);
		
		String sid = rmServerTransferData.getSid();
		String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
		logger.info("sid: {}.", sid);
		
		String appId = rmServerTransferData.getAppid();
		logger.info("appId: {}.", appId);
		
		String customaccnum = rmServerTransferData.getCustomaccnum();
		logger.info("customaccnum: {}.", customaccnum);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && StringUtils.isNotBlank(agentId) 
				&& StringUtils.isNotBlank(customaccnum)){
			logger.info("@cmdWakeUpUser agent: {}-{} to userAccount: {}.", agentId, customaccnum, userAccount);
			
			cmdWakeUpResp(userAccount, customaccnum, agentId, userAndAgentDialog, seqInfo);
			
		} else{
			logger.info("{}: get userAndAgentDialog is null.", userAccount);
		}
	}
	
	/**
	 * @Description: 唤醒用户响应
	 * @param userAccount 用户userAcc
	 * @param customaccnum 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @throws CCPServiceException
	 */
	private void cmdWakeUpResp(String userAccount, String customaccnum, String agentId, 
			UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo) throws CCPServiceException{
		logger.info("@cmdWakeUpResp");
		
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		String agentAccount = StringUtil.getUserAcc(appId, customaccnum);
		String msgJsonData = seqInfo == null ? "" : seqInfo.getMsgJsonData();
		
		// 通知坐席，有新用户咨询
		sendNotifyMessage(MCMEventDefInner.NotifyAgent_NewUserAsk_VALUE, agentAccount, 
				userName, null, -1, Constants.SUCC, null, "", msgJsonData);
		
		// 响应
		logger.info("send cmdWakeUpResp to rm server.");
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setStatuscode("0");
		rmServerTransferData.setCommand(CommandEnum.RESP_WAKE_UP_USER.getValue());
		
		rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForResp());
		
		// 被动行为统计数据入库
		saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_NewUserAsk_VALUE, 
				agentAccount, agentId, userAccount, "");
				
		// 修改实时会话记录
		logger.info("wakeUp start modify user and agent dialog.");
		if(StringUtils.isBlank(userAndAgentDialog.getHistoryAgentIds())){
			userAndAgentDialog.setHistoryAgentIds(agentId);
		}else{
			userAndAgentDialog.setHistoryAgentIds("," + agentId);
		}
		
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setAgentId(agentId);
		agentInfo.setAgentAccount(agentAccount);
		agentInfoSet.add(agentInfo);
		userAndAgentDialog.setAgentInfoSet(agentInfoSet);
		userAndAgentDialog.setQueueCount(0);
		
		userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
				Constants.DIALOG_VALID_TIME);
	}
	
	@Override
	public void respImAgentServiceEnd(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		int resultCode = getResultCode(statusCode);
		String seq = rmServerTransferData.getSeq();
		String sid = rmServerTransferData.getSid();
		
		logger.info("@respImAgentServiceEnd statuscode: {}, sid: {}.", statusCode, seq, sid);
		logger.info("get resultCode: {}.", resultCode);
		
		String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.warn("respImAgentServiceEnd get userAndAgentDialog is null.");
			return;
		}
		
		if(seqInfo != null && userAndAgentDialog != null){
			int mcmEvent = seqInfo.getMcmEvent();
			
			switch(mcmEvent){
				case MCMEventDefInner.UserEvt_EndAsk_VALUE:
					logger.info("user end ask response.");
				
					// 用户结束会话响应通知
					userEndAskResp(userAndAgentDialog, seqInfo, userAccount, rmServerTransferData);
					break;
					
				case MCMEventDefInner.AgentEvt_ForceEndService_VALUE:
					logger.info("agent force end service response.");
					
					// 强拆结束服务响应
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_ForceEndServiceResp_VALUE, 
							seqInfo.getSuperAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						forceEndServiceResp(userAndAgentDialog, seqInfo, userAccount, rmServerTransferData);
					}
					break;
					
				case MCMEventDefInner.AgentEvt_StopSerWithUser_VALUE:
					logger.info("agent stop ser with user response.");
					
					// 坐席结束会话响应, 有可能是会议，坐席结束后，其它坐席被动结束，这个时候不用回响应
					if(StringUtils.isNotBlank(seqInfo.getAgentAccount())){
						sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_StopSerWithUserResp_VALUE, 
								seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);	
					}
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						agentStopSerWithUserResp(userAndAgentDialog, seqInfo, userAccount, rmServerTransferData);	
					}
					break;
					
				case MCMEventDefInner.AgentEvt_StartSessionTimer_VALUE:
					// 定时器结束会话响应， 不用清理会话，在定时器过期处理会话
					logger.info("agent start session timer end service response.");
					break;
					
				case MCMEventDefInner.AgentEvt_TransKF_VALUE:
					logger.info("trans KF end service response.");
					
					// 转接坐席结束会话响应
					transferKfResp(userAccount, seqInfo, userAndAgentDialog, getOptResultMap(statusCode));
					break;
				
				case MCMEventDefInner.AgentEvt_TransferQueue_VALUE:
					// 转接队列结束会话响应
					logger.info("transfer queue end service response");
					break;
					
				case MCMEventDefInner.AgentEvt_ExitConf_VALUE:
					// 退出会议结束会话响应
					logger.info("exit conf end service response");
					
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_ExitConfResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						exitConfResp(userAccount, seqInfo, userAndAgentDialog);	
					}
					break;
				
				case MCMEventDefInner.AgentEvt_RejectUser_VALUE:
					logger.info("rejectuser end service response");
					
					// 拒接响应
					sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_RejectUserResp_VALUE, 
							seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);
					
					if(Constants.RESPONSE_OK.equals(statusCode)){
						rejectUserResp(userAccount, seqInfo, userAndAgentDialog);
					}
					break;
				
				case MCMEventDefInner.AgentEvt_ForceTransfer_VALUE:
					logger.info("force transfer end service response");
					
					// 强转坐席结束强转坐席会话响应
					forceTransferResp(userAccount, seqInfo, userAndAgentDialog, getOptResultMap(statusCode));
					break;
				
				case MCMEventDefInner.ConnectorNotify_UserDisconnect_VALUE:
					logger.info("connector user disconnect response.");
				
					// 用户异常结束会话响应通知
					userEndAskResp(userAndAgentDialog, seqInfo, userAccount, rmServerTransferData);
					break;
					
				default :
					logger.info("end service unknown resp.");
					break;
					
			}
			
		}// if seqInfo and userAndAgentDialog
	}
	
	/**
	 * @Description: 强转结束会话响应
	 * @param userAccount 用户账号，含appId
	 * @param seqInfo 请求rm server透传信息
	 * @param userAndAgentDialog 会话记录
	 * @param map rm server返回响应结果
	 * @throws CCPServiceException
	 */
	private void forceTransferResp(String userAccount, SeqInfo seqInfo, UserAndAgentDialog userAndAgentDialog, 
			Map<Integer, String> map) throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		
		String agentId = seqInfo.getAgentId(); // 强转者坐席Id
		String agentAccount = seqInfo.getAgentAccount(); // 强转者坐席账号
		String transferAllocAgentCount = seqInfo.getTransferAllocAgentCount(); // 被强转坐席账号
		String transferAllocAgentId = seqInfo.getTransferAllocAgentId(); // 被强转坐席Id

		// 通知坐席, 会话已强转给其他坐席
		sendNotifyMessage(MCMEventDefInner.NotifyAgent_TransferResult_VALUE, agentAccount, 
				userName, map, -1, Constants.SUCC, null, transferAllocAgentId);
			
		if(agentInfoSet != null){
			logger.info("force transfer after start modify dialog");
			
			removeAgentInfo(agentId, agentInfoSet);
			
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentAccount(transferAllocAgentCount);
			agentInfo.setAgentId(transferAllocAgentId);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}
	}
	
	/**
	 * @Description: 坐席拒接会话响应
	 * @param seqInfo 请求 rm server透传信息
	 * @throws CCPServiceException
	 */
	private void rejectUserResp(String userAccount, SeqInfo seqInfo, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException{
		String agentId = seqInfo.getAgentId();
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		
		// 通知用户
		sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", 
				null, -1, Constants.SUCC, null, agentId);
		
		if(agentInfoSet.size() > 1){
			logger.info("reject user start modify user and agent dialog.");
			
			removeAgentInfo(agentId, agentInfoSet);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);	
		} else{
			logger.info("reject user start delete user and agent dialog.");
			
			userAgentDialogRedisDao.deleteDialog(userAccount);
			userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
		}
	}
	
	/**
	 * @Description: 退出会议结束会话响应
	 * @param userAccount 用户账号, 含appId
	 * @param seqInfo 请求rm server透传信息
	 * @param userAndAgentDialog 会话记录
	 * @throws CCPServiceException
	 */
	private void exitConfResp(String userAccount, SeqInfo seqInfo, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		String agentId = seqInfo.getAgentId();
		
		// 通知会议成员
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			String otherAgentId = agentInfo.getAgentId();
			String otherAgentAccount = agentInfo.getAgentAccount();
			if((otherAgentId).equals(agentId)){
				continue;
			}
			
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_ExitConf_VALUE, otherAgentAccount, 
					userName, null, -1, Constants.SUCC, null, agentId);
		}
				
		// 通知用户
		if(agentInfoSet.size() > 1){
			// 修改实时会话, 删除加入者的相关信息
			logger.info("exit conf start modify user and agent dialog.");
			
			removeAgentInfo(agentId, agentInfoSet);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);	
		} else{
			sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", 
					null, -1, Constants.SUCC, null, agentId);
			
			// 清除会话
			userAgentDialogRedisDao.deleteDialog(userAccount);
			userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
		}
	}
	
	/**
	 * @Description: 用户结束咨询通知
	 * @param userAndAgentDialog 用户会话
	 * @param seqInfo 请求rm server透传信息
	 * @param userAccount 用户账号，带appId
	 * @param rmServerTransferData
	 * @throws CCPServiceException
	 */
	private void userEndAskResp(UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo, String userAccount, 
			RMServerTransferData rmServerTransferData) throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();		
		
		// 通知坐席用户会话结束
		for(AgentInfo agentInfo : agentInfoSet){
			if(seqInfo.getAgentId() == agentInfo.getAgentId()){
				sendNotifyMessage(MCMEventDefInner.NotifyAgent_UserEndAsk_VALUE, agentInfo.getAgentAccount(), 
						userName, null, -1, Constants.SUCC, null, "");		
				
				// 被动行为统计数据入库
				saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_UserEndAsk_VALUE, 
						agentInfo.getAgentAccount(), seqInfo.getAgentId(), userAccount, "");
			}
		}
		
		//如果是AS侧相关的结束咨询，并且mcmNotifyUrl不为空
		if(Constants.AS_FLAG_YES.equals(seqInfo.getAsFlag())){
			//向AS侧发送用户结束咨询通知
			asService.stopMsgRequestAS(userAndAgentDialog);
		}
		
		// 删除会话
		logger.info("start modify user and agent user end ask dialog.");
		if(agentInfoSet.size() > 1){
			removeAgentInfo(seqInfo.getAgentId(), agentInfoSet);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}else{
			userAgentDialogRedisDao.deleteDialog(userAccount);
			userAgentDialogRedisDao.deleteDialogSid(rmServerTransferData.getSid());
		}
	}
	
	/**
	 * @Description: 转接坐席, 转接者结束响应通知
	 * @param userAccount 用户账号，含appId
	 * @param seqInfo 请求rm server透传信息
	 * @param userAndAgentDialog 会话记录
	 * @param optMap rm server返回响应结果
	 * @throws CCPServiceException
	 */
	private void transferKfResp(String userAccount, SeqInfo seqInfo, UserAndAgentDialog userAndAgentDialog, 
			Map<Integer, String> optMap) throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		// 转接者坐席账号
		String agentAccount = seqInfo.getAgentAccount();
		// 转接者坐席Id
		String agentId = seqInfo.getAgentId();
		// 被转接的坐席账号
		String transferAllocAgentCount = seqInfo.getTransferAllocAgentCount();
		// 被转接的坐席Id
		String transferAllocAgentId = seqInfo.getTransferAllocAgentId(); 

		// 通知转接坐席结果通知
		sendNotifyMessage(MCMEventDefInner.NotifyAgent_TransferResult_VALUE, agentAccount, 
				userName, optMap, -1, Constants.SUCC, null, agentId);

		// 被动行为统计数据入库
		saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_TransferResult_VALUE, 
				agentAccount, agentId, userAccount, "");	
		
		if(agentInfoSet != null){
			logger.info("transfer kf after start modify dialog");
			
			removeAgentInfo(agentId, agentInfoSet);
			
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentAccount(transferAllocAgentCount);
			agentInfo.setAgentId(transferAllocAgentId);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}
	}
	
	/**
	 * @Description: 强拆结束会话
	 * @param userAndAgentDialog 坐席用户会话记录
	 * @param seqInfo 请求rm server透传信息
	 * @param userAccount 用户账号不带appId
	 * @param rmServerTransferData rm server返回数据
	 * @throws CCPServiceException
	 */
	private void forceEndServiceResp(UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo, 
			String userAccount, RMServerTransferData rmServerTransferData) throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();		
		String agentAccount = seqInfo.getAgentAccount(); // 被拆者坐席账号
		String superAgentId = seqInfo.getSuperAgentId(); // 管理者坐席Id
		String agentId = seqInfo.getAgentId(); // 被拆者坐席Id
		
		// 向座席发送消息通知，告知其会话已结束
		sendNotifyMessage(MCMEventDefInner.NotifyAgent_AgentEndIMService_VALUE, agentAccount, 
				userName, null, -1, Constants.SUCC, null, superAgentId);
		
		// 被动行为统计数据入库
		saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_AgentEndIMService_VALUE, 
				agentAccount, superAgentId, userAccount, "");	
				
		// 删除会话
		if(agentInfoSet.size() > 1){
			logger.info("force end service start modify dialog");
			
			removeAgentInfo(seqInfo.getAgentId(), agentInfoSet);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);	
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);
		}else{
			logger.info("force end service start delete dialog");
			
			sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", null, 
					-1, Constants.SUCC, null, agentId);
			
			userAgentDialogRedisDao.deleteDialog(userAccount);
			userAgentDialogRedisDao.deleteDialogSid(rmServerTransferData.getSid());
		}	
	}
	
	/**
	 * @Description: 坐席结束会话通知
	 * @param userAndAgentDialog 坐席用户会话
	 * @param seqInfo 请求rm server 透传信息
	 * @param userAccount 用户账号，带appId
	 * @param rmServerTransferData rm server返回数据
	 * @throws CCPServiceException
	 */
	private void agentStopSerWithUserResp(UserAndAgentDialog userAndAgentDialog, SeqInfo seqInfo, 
			String userAccount, RMServerTransferData rmServerTransferData) throws CCPServiceException{
		String userName = StringUtil.getUserNameFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();		
		String agentId = seqInfo.getAgentId();
		
		// 通知其他坐席有坐席结束会话
		for(AgentInfo agentInfo : agentInfoSet){
			String otherAgentId = agentInfo.getAgentId();
			String otherAgentAccount = agentInfo.getAgentAccount();
			if(otherAgentId.equals(agentId)){
				continue;
			}
			
			// 通知其它坐席有坐席结束服务
			sendNotifyMessage(MCMEventDefInner.NotifyAgent_AgentEndIMService_VALUE, otherAgentAccount, userName, 
					null, -1, Constants.SUCC, null, agentId);
			
			// 被动行为统计数据入库
			saveM3CSMessageHistory(MCMEventDefInner.NotifyAgent_AgentEndIMService_VALUE, 
					otherAgentAccount, agentId, userAccount, "");
			
			// 通知rm server结束服务
			RMServerTransferData tempRmServerTransferData = new RMServerTransferData();
			tempRmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
			tempRmServerTransferData.setAppid(rmServerTransferData.getAppid());
			
			seqInfo = new SeqInfo();
			seqInfo.setMcmEvent(MCMEventDefInner.AgentEvt_StopSerWithUser_VALUE);
			seqInfo.setAgentId(otherAgentId);
			tempRmServerTransferData.setSeq(seqInfo.toString());
			
			tempRmServerTransferData.setSid(rmServerTransferData.getSid());
			tempRmServerTransferData.setAgentid(otherAgentId);
			
			rmServerRequestService.doPushMessage(tempRmServerTransferData.toJsonForCmdImAgentServiceEnd());
		}
			
		// 删除会话
		if(agentInfoSet.size() > 1){
			logger.info("agent stop serWithUser start modify dialog");
			
			removeAgentInfo(agentId, agentInfoSet);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			
			userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
					Constants.DIALOG_VALID_TIME);	
		}else{
			logger.info("agent stop serWithUser start delete dialog");
			
			// 通知用户会话结束
			sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", 
					null, -1, Constants.SUCC, null, agentId);
			
			userAgentDialogRedisDao.deleteDialog(userAccount);
			userAgentDialogRedisDao.deleteDialogSid(rmServerTransferData.getSid());
		}	
	}
	
	/**
	 * @Description: 删除会话中坐席相关信息
	 * @param agentId
	 * @param agentInfoList 
	 */
	private void removeAgentInfo(String agentId, Set<AgentInfo> agentInfoSet){
		for (Iterator<AgentInfo> iter = agentInfoSet.iterator(); iter.hasNext();) {  
			AgentInfo agentInfo = iter.next();  
			if(agentId.equals(agentInfo.getAgentId())){
				logger.info("start remove agentId: {}.", agentId);
			    iter.remove();  
            }  
        }  
	}
	
	@Override
	public void respCMUnexpectedRestart(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		
	}

	@Override
	public void respLockAgent(RMServerTransferData rmServerTransferData, SeqInfo seqInfo) 
			throws CCPServiceException {
		
	}

	@Override
	public void respAgentStateSwitch(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		
	}

	@Override
	public void respEnterCCS(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		
	}

	@Override
	public void respExitCCSQueue(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		int resultCode = getResultCode(statusCode);
		String sid = rmServerTransferData.getCallid();
		int queueCount = rmServerTransferData.getQueuecount();
		
		logger.info("@respExitCCSQueue statuscode: {}, sid: {}, queueCount: {}.", statusCode, 
				 sid, queueCount);
		logger.info("get resultCode: {}.", resultCode);
		
		String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.warn("get userAndAgentDialog is null.");
			return;
		}
		
		logger.info("exit queue start delete dialog.");
		
		userAgentDialogRedisDao.deleteDialog(userAccount);
		userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
	}

	@Override
	public void respAgentReservedService(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		int resultCode = getResultCode(statusCode);
		
		logger.info("@respAgentReservedService statuscode: {}, resultCode: {}.", statusCode, resultCode);

		if(seqInfo.isReady()){
			// 预留响应通知
			sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_ReservedForUserResp_VALUE, 
					seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);	
		} else {
			// 取消预留响应通知
			sendNotifyResponseMessage(MCMEventDefInner.NotifyAgent_CancelReservedResp_VALUE, 
					seqInfo.getAgentAccount(), seqInfo.getProtoClientNo(), resultCode);	
		}
		
		if(Constants.RESPONSE_OK.equals(statusCode)){
			reservedServiceResp(seqInfo);
		}
	}
	
	/**
	 * @Description: 坐席预留响应通知
	 * @param seqInfo 
	 */
	private void reservedServiceResp(SeqInfo seqInfo){
		boolean optType = seqInfo.isReady();
		String userAccount = seqInfo.getUserAccount();
		String connectorId = seqInfo.getConnectorId();
		String appId = seqInfo.getAppId();
		
		if(userAccount.contains("#")){
			userAccount = StringUtil.getUserAcc(appId, userAccount);
		}
		
		String sid = StringUtil.generateSid(Constants.M3C_SERIAL, connectorId);
		
		// 预留成功， 建立会话
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(optType){
			if(userAndAgentDialog == null){
				userAndAgentDialog = new UserAndAgentDialog();
				userAndAgentDialog.setCCSType(0);
				userAndAgentDialog.setChannel(String.valueOf(Constants.M3C_SERIAL));
				userAndAgentDialog.setSid(sid);
				userAndAgentDialog.setDateCreated(System.currentTimeMillis());
				userAndAgentDialog.setChanType(Integer.parseInt(seqInfo.getChanType()));
				userAndAgentDialog.setAppId(appId);
				userAndAgentDialog.setIsReserved(Constants.IS_RESERVED);
				
				userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
				userAgentDialogRedisDao.saveDialogSid(sid, userAccount, Constants.DIALOG_VALID_TIME);
			} else {
				userAndAgentDialog.setIsReserved(Constants.IS_RESERVED);
				userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
			}
		} else{
			if(userAndAgentDialog != null){
				userAndAgentDialog.setIsReserved(Constants.IS_NOT_RESERVED);
				userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
			}
		}
	}
	
	@Override
	public void respQueryQueueInfo(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		String statusCode = rmServerTransferData.getStatuscode();
		int resultCode = getResultCode(statusCode);
		logger.info("@respQueryQueueInfo statuscode: {}, get resultCode: {}.", statusCode, resultCode);
		
		// 当前队列的空闲座席
		int idleCount = rmServerTransferData.getIdlecount(); 
		logger.info("idleCount: {}.", idleCount);
		
		List<RMServerAgent> idleAgents = rmServerTransferData.getIdleagents();
		logger.info("idleAgents: {}.", Arrays.toString(idleAgents.toArray()));
		
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_QueryQueueInfoResp_VALUE);
		mcmMessageInfo.setIdlecount(idleCount);
		pushService.doPushMsg(seqInfo.getAgentAccount() , mcmMessageInfo, seqInfo.getProtoClientNo(), resultCode);
	}

	@Override
	public void cmdNotifyAgentOffWork(RMServerTransferData rmServerTransferData, SeqInfo seqInfo)
			throws CCPServiceException {
		logger.info("@cmdNotifyAgentOffWork...");
		
		String agentId = rmServerTransferData.getAgentid();
		logger.info("agentId: {}.", agentId);
		
		String appId = rmServerTransferData.getAppid();
		logger.info("appId: {}.", appId);
		
		String tempUserAccounts = rmServerTransferData.getUseraccounts();
		logger.info("userAccounts: {}.", tempUserAccounts);
		
		if(StringUtils.isNotBlank(tempUserAccounts) && StringUtils.isNotBlank(agentId)){
			String[] userAccounts = tempUserAccounts.split(",");
			for(String userAccount : userAccounts){
				if(!userAccount.contains("#")){
					userAccount = StringUtil.getUserAcc(appId, userAccount);
				}
				
				UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
				if(userAndAgentDialog != null){
					Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
					
					// 删除会话
					if(agentInfoSet.size() > 1){
						logger.info("notify agent off work start modify dialog");
						
						removeAgentInfo(agentId, agentInfoSet);
						userAndAgentDialog.setAgentInfoSet(agentInfoSet);
						
						userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, 
								Constants.DIALOG_VALID_TIME);	
					}else{
						logger.info("notify agent off work start delete dialog");
						
						// 通知用户会话结束
						sendNotifyMessage(MCMEventDefInner.NotifyUser_EndAsk_VALUE, userAccount, "", 
								null, -1, Constants.SUCC, null, agentId);
						
						userAgentDialogRedisDao.deleteDialog(userAccount);
						userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
					}	
				}else {
					logger.info("notify agent off work get dialog is null.");
				}
			}
			
			// 响应
			rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setStatuscode("0");
			rmServerTransferData.setCommand(CommandEnum.RESP_NOTIFY_AGENT_OFF_WORK.getValue());
			
			rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForResp());
		}
	}
	
	/**
	 * @Description: 发送响应通知 
	 * @param event 触发事件
	 * @param userAcc 接收消息对象
	 * @param protoClientNo 客户端请求流水号
	 * @param errorCode 请求错误码
	 * @throws CCPServiceException 
	 */
	private void sendNotifyResponseMessage(int event, String userAcc, int protoClientNo, 
			int errorCode) throws CCPServiceException{
		sendNotifyMessage(event, userAcc, "", null, protoClientNo, errorCode, null, "");
	}
	
	/**
	 * @Description: 发送通知消息 
	 * @param event 触发事件
	 * @param userAcc 接收消息对象
	 * @param userName 不含AppId
	 * @param optResultMap 成功失败结果
	 * @param protoClientNo 客户端请求流水号
	 * @param errorCode 请求错误码
	 * @param ccpCustomData 随由数据，sdk-m3透传
	 * @param agentId 坐席Id
	 * @throws CCPServiceException 
	 */
	private void sendNotifyMessage(int event, String userAcc, String userName, 
			Map<Integer, String> optResultMap, int protoClientNo, int errorCode, 
			CcpCustomData ccpCustomData, String agentId) throws CCPServiceException{
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(event);
		
		if(StringUtils.isNotBlank(userName)){
			mcmMessageInfo.setUserAccount(userName);	
		}
		
		if(StringUtils.isNotBlank(agentId)){
			mcmMessageInfo.setAgentId(agentId);	
		}
		
		if(ccpCustomData != null){
			mcmMessageInfo.setCcpCustomData(ccpCustomData.toString());
		}
		
		if(optResultMap != null){
			 for (Entry<Integer, String> entry : optResultMap.entrySet()) {
			 	String optRetDes = optResultMap.get(entry.getKey());
				mcmMessageInfo.setOptResult(entry.getKey());
				mcmMessageInfo.setOptRetDes(optRetDes);
			 }
		}
		
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, errorCode);
	}
	
	/**
	 * @Description: 发送通知消息 
	 * @param event 触发事件
	 * @param userAcc 接收消息对象
	 * @param userName 不含AppId
	 * @param optResultMap 成功失败结果
	 * @param protoClientNo 客户端请求流水号
	 * @param errorCode 请求错误码
	 * @param ccpCustomData 随由数据，sdk-m3透传
	 * @param agentId 坐席Id
	 * @param msgJsonData
	 * @throws CCPServiceException 
	 */
	private void sendNotifyMessage(int event, String userAcc, String userName, 
			Map<Integer, String> optResultMap, int protoClientNo, int errorCode, 
			CcpCustomData ccpCustomData, String agentId, String msgJsonData) throws CCPServiceException{
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(event);
		
		if(StringUtils.isNotBlank(userName)){
			mcmMessageInfo.setUserAccount(userName);	
		}
		
		if(StringUtils.isNotBlank(agentId)){
			mcmMessageInfo.setAgentId(agentId);	
		}
		
		if(ccpCustomData != null){
			mcmMessageInfo.setCcpCustomData(ccpCustomData.toString());
		}
		
		if(StringUtils.isNotBlank(msgJsonData)){
			mcmMessageInfo.setMsgJsonData(msgJsonData);
		}
		
		if(optResultMap != null){
			 for (Entry<Integer, String> entry : optResultMap.entrySet()) {
			 	String optRetDes = optResultMap.get(entry.getKey());
				mcmMessageInfo.setOptResult(entry.getKey());
				mcmMessageInfo.setOptRetDes(optRetDes);
			 }
		}
		
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, errorCode);
	}
	
	/**
	 * @Description: 发送文本消息
	 * @param event 
	 * @param userAcc 接收者
	 * @param msgContent 消息内容
	 * @throws CCPServiceException
	 */
	private void sendMessage(int event, String userAcc, String msgContent, String osUnityAccount, 
			String connectorId) throws CCPServiceException{
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(event);
		mcmMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
		mcmMessageInfo.setMsgContent(msgContent);
		
		if(StringUtils.isNotBlank(osUnityAccount)){
			mcmMessageInfo.setOsUnityAccount(osUnityAccount);	
		}
		
		String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, connectorId);
		mcmMessageInfo.setMsgId(msgId);
		// long version = versionDao.getMessageVersion(userAccountAcc);
		// mcmMessageInfo.setVersion(version);
		mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
		
		pushService.doPushMsg(userAcc, mcmMessageInfo, -1, Constants.SUCC);
	}
	
	/**
	 * @Description: 获取响应码
	 * @param statusCode rm server返回错误码 
	 */
	private int getResultCode(String statusCode){
		int resultCode = Constants.SUCC;
		if(StringUtils.isNotBlank(statusCode)){
			if("000000".equals(statusCode)){
				resultCode = Constants.SUCC;
			}else {
				if(StringUtils.isNumeric(statusCode)){
					resultCode = Integer.parseInt(statusCode);	
				}	
			}
		}	
		
		return resultCode;
	}
	
	/**
	 * @Description: rm server 错误码对应错误码描述
	 * @param statusCode rm server返回错误码 
	 */
	private Map<Integer, String> getOptResultMap(String statusCode){
		Map<Integer, String> map = new HashMap<Integer, String>();
		int resultCode  = getResultCode(statusCode);
		String value = RMServerErrorCode.getErrorCodeByRMServerCodeNo(statusCode);
		map.put(resultCode, value);
		
		return map;
	}
	
	/**
	 * @Description: 被动行为统计数据入库
	 * @param mcmEvent
	 * @param userAcc
	 * @param agentId
	 * @param receiverUserAcc
	 * @param resultCode
	 * @throws CCPServiceException
	 */
	private void saveM3CSMessageHistory(int mcmEvent, String userAcc, String agentId, 
			String receiverUserAcc, String resultCode) throws CCPServiceException{
		MCMEventData.MCMDataInner sendMsg = MCMEventData.MCMDataInner.newBuilder()
				.setCCSType(0)
				.setMCMEvent(mcmEvent)
				.build();
				
		Connector connector = new Connector();
		connector.setAppId(StringUtil.getAppIdFormUserAcc(userAcc));
		connector.setUserName(StringUtil.getUserNameFormUserAcc(userAcc));
				
		dataAsyncService.saveM3CSMessageHistory(sendMsg, connector, receiverUserAcc, agentId, resultCode);
	}
	
	/**
	 * set inject
	 */
	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setRmServerRequestService(RMServerRequestService rmServerRequestService) {
		this.rmServerRequestService = rmServerRequestService;
	}

	public void setUserAgentDialogRedisDao(
			UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}

	public void setAsService(AsService asService) {
		this.asService = asService;
	}

	public void setWeixinService(WeiXinGWService weixinService) {
		this.weixinService = weixinService;
	}

	public void setDataAsyncService(DataAsyncService dataAsyncService) {
		this.dataAsyncService = dataAsyncService;
	}
	
}
