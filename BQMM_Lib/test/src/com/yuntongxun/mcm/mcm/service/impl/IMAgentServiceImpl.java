package com.yuntongxun.mcm.mcm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;
import org.springframework.http.HttpMethod;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.model.ASDataInfo;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.CcpCustomData;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.ReservedInfo;
import com.yuntongxun.mcm.mcm.model.SeqInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.quartz.AgentDialogTimeOutJob;
import com.yuntongxun.mcm.mcm.quartz.QuartzManager;
import com.yuntongxun.mcm.mcm.service.DataAsyncService;
import com.yuntongxun.mcm.mcm.service.IMAgentService;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

public class IMAgentServiceImpl implements IMAgentService{

	public static final Logger logger = LogManager.getLogger(IMAgentServiceImpl.class);
	
	private RMServerRequestService rmServerRequestService;
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private PushService pushService;
	
	private VersionDao versionDao;
	
	private DataAsyncService dataAsyncService;
	
	private WeiXinGWService weixinService;
	
	private String fileServerDownloadUrl;
	
	private HttpClient httpClient;
	
	private int resendTimeNum;
	 
	@Override
	public void onWork(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentInfo()){
			logger.warn("agentInfoInner is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENTINFO_EMPTY_ERROR); 
		}
		
		MCMEventData.AgentInfoInner agentInfoInner = sendMsg.getAgentInfo();
		if(agentInfoInner == null){
			logger.warn("agentInfoInner is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENTINFO_EMPTY_ERROR); 
		}
		
		if(!agentInfoInner.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = agentInfoInner.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		int serviceCap = 4;
		if(sendMsg.hasServiceCap()){
			serviceCap = sendMsg.getServiceCap();	
		}
		logger.info("serviceCap: {}.", serviceCap);
		
		// 请求rmServer
		sendOnWorkRequestToRm(connector, agentId, serviceCap, userAcc, sendMsg.getMCMEvent(), 
				protoClientNo, agentInfoInner);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", agentId);
	}
	
	/**
	 * @Description: 发送签入请求至rm server
	 * @param connector 
	 * @param agentId
	 * @param serviceCap
	 * @param userAcc
	 * @param mcmEvent 
	 * @throws CCPServiceException
	 */
	private void sendOnWorkRequestToRm(Connector connector, String agentId, int serviceCap, 
			String userAcc, int mcmEvent, int protoClientNo, 
			MCMEventData.AgentInfoInner agentInfoInner) throws CCPServiceException{
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_AGENT_ON_WORK.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setAgentid(String.valueOf(agentId));
		
		// 采用掩码方式 服务能力。表示座席的服务能力。1语音、2 IM、4视频，默认值为1。能力值相加就是能力组合。例如3语音+IM、5音视频、6视频+IM、7语音+视频+IM。
		rmServerTransferData.setServicecap(String.valueOf(serviceCap)); 
		
		// 对照信息
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(mcmEvent);
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		// 座席号码，手机号或座机号或voip帐号(标准voip号)
		// rmServerTransferData.setNumber(agentInfoInner.getNumber());
		
		// 自定义voip账号。默认址为false
		rmServerTransferData.setCustomaccnum(connector.getUserName());
		
		// 0无效、1顺振、2同振。默认值为0
		// rmServerTransferData.setAcdcalltype(agentInfoInner); 
		
		// 0优先呼number、1优先呼customaccnum。默认值为0
		// rmServerTransferData.setFirstnumber(agentInfoInner); 
		
		// 座席优先级；默认为10，值越大优先级越高；座席在不同的队列中可以有不同的优先级，使用逗号分隔。若优先参数与服务队列数量不等，则左对齐方式
		String queuePriority = agentInfoInner.getQueuePriority();
		int qp = 10;
		if(StringUtils.isNotBlank(queuePriority) && StringUtils.isNumeric(queuePriority)){
			qp = Integer.parseInt(queuePriority);
		}
		rmServerTransferData.setQueuepriority(qp);
		
		// 座席电话线路状态：0准备中 1准备就绪　2用户锁定 3咨询通话中　4线路忙。有效值为0或1，默认值为0。
		// rmServerTransferData.setAgentstate(0);
		
		// 2016-03-12 去掉im state 改为传agentState true false
		// 座席im状态：10离线状态、11在线、12上班空闲中、13有新用户锁定、14服务中、15服务中且已饱和、16服务暂停（当前已接用户继续服务，不再为新用户提供服务）。默认值为12
		//int imState = agentInfoInner.getImState() > 0 ? agentInfoInner.getImState() : 10;
		// rmServerTransferData.setImagentstate(imState);
		
		int agentState = agentInfoInner.getImState() > 0 ? 1 : 0;
		logger.info("agentState: {}.", agentState);
		rmServerTransferData.setAgentstate(agentState);
		
		int maxImUser = agentInfoInner.getMaxImUser() > 0 ? agentInfoInner.getMaxImUser() : 1;
		logger.info("maxImUser: {}.", maxImUser);
		rmServerTransferData.setMaximuser(maxImUser); // 同时最多服务IM用户的数量，默认值为1
		
		// 推送弹屏voip帐号；默认值为空
		rmServerTransferData.setPushvoipacc(agentInfoInner.getPushVoipacc());
		
		// 座席服务队列类型，与队列类型一致，默认值为
		String queueType = agentInfoInner.getQueueType();
		logger.info("queueType: {}.", queueType);
		//座席服务队列类型，与队列类型一致，默认值为0。可以有多个值，使用逗号分隔即可。or serverqueuetype
		rmServerTransferData.setAgenttype(queueType);
		// rmServerTransferData.setServerqueuetype(queueType);
		
		// 呼叫座席延迟时间，单位是秒；当pushvoipacc有值时，此参数有效；默认值为0秒，不延迟呼叫座席
		// rmServerTransferData.setDelaycall(delaycall); 
		
		// 用户信息获取回调地址；默认为空；pushvoipacc参数有值时此参数有效；所有座席应使用同一地址
		// rmServerTransferData.setUserinfocallbackurl(userinfocallbackurl)
		
		// 座席接听超时时长，单位秒，默认为-1，等待到运营商挂机。最小值为5秒。超时后用户将被转下一个空闲座席，或被加入到队列头。
		// rmServerTransferData.setAnswertimeout(answertimeout)
		
		// 座席服务模式。0 互斥模式电话优先、1 互斥模式IM先模式、2 共享模式电话优先、3共享模式IM优先，默认值为0。 互斥模式：座席同时只能提供一种服务 共享模式：座席同时可提供电话与IM的并发服务
		// rmServerTransferData.setAgentservermode(agentservermode);
		
		// 上班就绪后优先接听的用户。agentstate值为0或1时有效。
		// rmServerTransferData.setCallidfirst(callidfirst);
	
		String cmdMessage = rmServerTransferData.toJsonForCmdAgentOnWork();
		rmServerRequestService.doPushMessage(cmdMessage);
	}
	
	@Override
	public void offWork(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		String agentId = sendMsg.getAgentId();
		
		logger.info("agentId: {}.", agentId);
		
		// 请求rmServer
		sendOffWorkRequestToRm(connector, agentId, userAcc, sendMsg.getMCMEvent(), protoClientNo);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}
	
	/**
	 * @Description: 发送签出请求至rm server
	 * @param connector
	 * @param agentId
	 * @param userAcc
	 * @param mcmEvent
	 * @throws CCPServiceException
	 */
	private void sendOffWorkRequestToRm(Connector connector, String agentId, String userAcc, 
			int mcmEvent, int protoClientNo) throws CCPServiceException{
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_AGENT_OFF_WORK.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL);
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(mcmEvent);
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());

		// serverQueueId 下班队列 可选默认为空下班全部队列
		// 座席号码，手机号或座机号或voip帐号
		// rmServerTransferData.setNumber(number);
		
		rmServerTransferData.setAgentid(String.valueOf(agentId));
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAgentOffWork();
		rmServerRequestService.doPushMessage(cmdMessage);
	}

	@Override
	public void stateOpt(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
	}
	
	@Override
	public void ready(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		logger.info("agentId: {}.", agentId);
		
		readyOrNotReady(connector, sendMsg, agentId, true, userAcc, protoClientNo);
	}

	@Override
	public void notReady(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		readyOrNotReady(connector, sendMsg, agentId, false, userAcc, protoClientNo);
	}

	/**
	 * @Description: 坐席就绪或未就绪
	 * @param connector
	 * @param sendMsg
	 * @param agentId
	 * @param imState true: 就绪  false: 未就绪
	 * @throws CCPServiceException
	 */
	private void readyOrNotReady(Connector connector, MCMDataInner sendMsg, String agentId, 
			boolean imState, String userAcc, int protoClientNo) throws CCPServiceException{
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_AGENT_READY.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL);
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setReady(imState);
		seqInfo.setAgentId(agentId);
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setAgentid(String.valueOf(agentId));
		
		// 设置座席电话状态，true表示状更新为准备就绪，false表示状态更新为准备中，当imagentstate为空时默认值为true；imagentstate不为空此值可为空
		logger.info("state: {}.", imState);
		rmServerTransferData.setState(imState ? "true" : "false"); 
		
		// 是优先接听客户电话，默认值为false，顺序接听
		// rmServerTransferData.setPriority(priority);
		
		// 是否强制设置状态，默认值为false。
		// rmServerTransferData.setForce(force);
		
		// 就绪后优先接听的用户。agentstate值为0或1时有效
		// rmServerTransferData.setCallidfirst(callidfirst);
		
		// true 表示状态就绪，状态值为12，若当前状态值为13、14或15，则值不变，且就绪成功；false表示状态未就绪，状态值为16，暂停。
		// rmServerTransferData.setImagentstate(imAgetnState);
		// rmServerTransferData.setImstate(imState);
		// rmServerTransferData.setAgentstate();
		
		// IM状态就绪后优先服务的用户会话ID
		// rmServerTransferData.setSidfirst(sidfirst); 
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAgentReady();
		rmServerRequestService.doPushMessage(cmdMessage);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", agentId);
	}
	
	@Override
	public void startSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		String ccpCustomData = "";
		if(sendMsg.hasCcpCustomData()){
			ccpCustomData = sendMsg.getCcpCustomData();
		}
		logger.info("ccpCustomData: {}.", ccpCustomData);
		
		// 发送欢迎语及修改会话
		sendWelcomeToUser(connector, sendMsg, agentId, userAccountAcc, userAcc, 
				protoClientNo, ccpCustomData);
	}

	/**
	 * @Description: 发送欢迎语至用户
	 * @param connector
	 * @param sendMsg
	 * @param agentId
	 * @param userAccountAcc
	 * @param userAcc
	 * @throws CCPServiceException
	 */
	private void sendWelcomeToUser(Connector connector, MCMDataInner sendMsg, String agentId, 
			String userAccount, String userAcc, int protoClientNo, 
			String ccpCustomData) throws CCPServiceException{
		// 通知坐席发送响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_SendMCMResp_VALUE);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
				
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.warn("start ser with user fail, get user and agent dialog is null.");
			return;
		}
		
		// 给用户发送消息
		// long version = versionDao.getMessageVersion(userAccountAcc);
		String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, connector.getConnectorId());
		
		MSGDataInner.Builder msgData = MSGDataInner.newBuilder();
		
		//如果会话的AS侧服务模式不为空,并且AS侧欢迎语不为空，回复该欢迎语
		if(StringUtils.isNotEmpty(userAndAgentDialog.getAsServiceMode())&&StringUtils.isNotEmpty(userAndAgentDialog.getAsWelcome())){
			msgData.setMsgContent(userAndAgentDialog.getAsWelcome());
		}else{
			msgData.setMsgContent("您好, 工号: " + agentId + "为您服务");
		}
		msgData.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
		
		//判断chanType类型，如果IM类型，回复IM消息；如果微信类型，回复微信消息
		if(MCMChannelTypeInner.MCType_im_VALUE == userAndAgentDialog.getChanType()){
			// 回复IM消息
			mcmMessageInfo = buildMCMMessageInfo(sendMsg, msgData.build(), connector, 
					0, msgId, MCMEventDefInner.AgentEvt_SendMCM_VALUE, "", agentId);
			pushService.doPushMsg(userAccount, mcmMessageInfo);
			
		}else if(MCMChannelTypeInner.MCType_wx_VALUE == userAndAgentDialog.getChanType()){
			//回复微信消息
			McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
			weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
			String userID = StringUtil.getUserNameFormUserAcc(userAccount);
			weiXinMsgInfo.setUserID(userID);
			weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
			weiXinMsgInfo.setContent(msgData.getMsgContent());
			weixinService.sendWeiXinMsg(weiXinMsgInfo);
		}

		notifyTransAgent(ccpCustomData, userAndAgentDialog, connector);
		
		// 修改会话记录，坐席已接起
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData.build(), 0, connector, 
				userAndAgentDialog, msgId, null, userAccount, agentId);
	}

	/**
	 * @Description: 转接坐席，转接队列，被转接者接起会话后，通知转接者转接成功，及结束服务 
	 * @param ccpCustomData
	 * @param userAndAgentDialog
	 * @throws CCPServiceException
	 */
	private void notifyTransAgent(String ccpCustomData, UserAndAgentDialog userAndAgentDialog, 
			Connector connector) throws CCPServiceException{
		CcpCustomData ccd = null;
		try {
			if(StringUtils.isNotBlank(ccpCustomData)){
				String tempCcpCustomData = EncryptUtil.base64Decoder(ccpCustomData);
				logger.info("tempCcpCustomData: {}.", tempCcpCustomData);
				
				if(tempCcpCustomData.contains("{") && tempCcpCustomData.contains("}")){
					ccd = (CcpCustomData)JSONUtil.jsonToObj(tempCcpCustomData, CcpCustomData.class);
				}
			}
		} catch (Exception e) {
			logger.error("parser ccpCustomData error.", e);
		}
		
		if(ccd != null){
			if(MCMEventDefInner.AgentEvt_TransKF_VALUE == ccd.getMcmEvent() || 
					MCMEventDefInner.AgentEvt_ForceTransfer_VALUE == ccd.getMcmEvent()){
				 
				String agentCount = ccd.getTransferAgentCount();
				logger.info("transferAgentCount: {}.", agentCount);
				
				String agentId = ccd.getTransferAgentId();
				logger.info("transferAgentId: {}.", agentId);
				
				String transferAllocAgentCount = ccd.getTransferAllocAgentCount();
				logger.info("transferAllocAgentCount: {}.", transferAllocAgentCount);
				
				String transferAllocAgentId = ccd.getTransferAllocAgentId();
				logger.info("transferAllocAgentId: {}.", transferAllocAgentId);
				
				// 结束坐席
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
				rmServerTransferData.setAppid(connector.getAppId());
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setMcmEvent(ccd.getMcmEvent());
				seqInfo.setAgentAccount(agentCount);
				seqInfo.setAgentId(agentId);
				seqInfo.setTransferAllocAgentCount(transferAllocAgentCount);
				seqInfo.setTransferAllocAgentId(transferAllocAgentId);
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerTransferData.setSid(userAndAgentDialog.getSid());
				rmServerTransferData.setAgentid(agentId);
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
			}
		}
	}
		
	@Override
	public void stopSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		// 请求rmServer 
		sendStopRequestToRm(sendMsg, connector, userAccountAcc, connector.getAppId(), 
				userAcc, protoClientNo);
	}

	/**
	 * @Description: 发送结束咨询响应至用户
	 * @param sendMsg
	 * @param connector
	 * @param userAccountAcc
	 * @param appId
	 * @param userAcc 
	 * @throws CCPServiceException
	 */
	private void sendStopRequestToRm(MCMDataInner sendMsg, Connector connector, String userAccountAcc, 
			String appId, String userAcc, int protoClientNo) throws CCPServiceException{
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog != null){
			
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			
			if(agentInfoSet.isEmpty()){
				userAgentDialogRedisDao.deleteDialog(userAccountAcc);
				userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
			}
			
			for(AgentInfo agentInfo : agentInfoSet){
				String otherAgentAccount = agentInfo.getAgentAccount();
				String otherAgentId = agentInfo.getAgentId();
				if(!otherAgentAccount.equals(userAcc)){
					continue;
				}
				String sid = userAndAgentDialog.getSid(); 
				
				// 通知rm server结束服务
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
				rmServerTransferData.setAppid(appId);
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setMcmEvent(MCMEventDefInner.AgentEvt_StopSerWithUser_VALUE);
				seqInfo.setAgentAccount(otherAgentAccount);
				seqInfo.setAgentId(otherAgentId);
				seqInfo.setProtoClientNo(protoClientNo);
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerTransferData.setSid(sid);
				rmServerTransferData.setAgentid(otherAgentId);
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
				
				// 添加历史消息
				dataAsyncService.saveM3CSMessageHistory(sendMsg, null, 0, connector, userAndAgentDialog, 
						"", null, userAccountAcc, otherAgentId);
			}

			// 发送停止的请求到as
			sendStopMessageToAs(userAndAgentDialog);
		}else{
			logger.warn("stopSerWithUser fail, get user and agent dialog is null.");
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_StopSerWithUserResp_VALUE);
			pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		}
	}
	
	private void sendStopMessageToAs(UserAndAgentDialog userAndAgentDialog){
		String notifyUrl = userAndAgentDialog.getMcm_notify_url();
		if(StringUtils.isBlank(notifyUrl)){
			logger.info("notify url is null.");
			return;
		}
		logger.info("notify url is: {}.", notifyUrl);
		
		ASDataInfo asDataInfo = new ASDataInfo();
		asDataInfo.setAction(Constants.AS_ACTION_STOP_MESSAGE);
		asDataInfo.setSid(userAndAgentDialog.getSid());
		asDataInfo.setAppId(userAndAgentDialog.getAppId());
		asDataInfo.setCreateTime(String.valueOf(System.currentTimeMillis()));
		asDataInfo.setOsUnityAccount(userAndAgentDialog.getOsUnityAccount());
		asDataInfo.setCustomAppID(userAndAgentDialog.getCustomAppId());
		asDataInfo.setChanType(String.valueOf(userAndAgentDialog.getChanType()));
		
		List<String> agentIds = new ArrayList<String>();
		for(AgentInfo agentInfo:userAndAgentDialog.getAgentInfoSet()){
			agentIds.add(agentInfo.getAgentId());
		}
		asDataInfo.setAgentIds(agentIds);
		
		String messageBody = asDataInfo.toEndAskJson();
		
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			logger.info("stopSerWithUser send stopMsgRequest to AS, url: {}, body: {}.", notifyUrl, messageBody);
			
			Map<String,String> httpResult = httpClient.sendPacket(notifyUrl, httpMethod, header,messageBody);
			String statusCode = httpResult.get("statusCode");
			String content = httpResult.get("content");
			// String dataFormat = httpResult.get("dataFormat");
			
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				for (int i = 1; i <= resendTimeNum; i++) {
					httpResult = httpClient.sendPacket(notifyUrl, httpMethod, header, messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode != null&&Integer.parseInt(statusCode) < 500){
						content = httpResult.get("content");
						// dataFormat = httpResult.get("dataFormat");
						break;
					}
				}
			}
			
			logger.info("sendStopMessageToAs return content: {}.", content);
			
			if(statusCode == null || (statusCode!=null&&Integer.parseInt(statusCode) >= 500)){
				logger.info("sendStopMessageToAs error, statusCode:"+statusCode);
			}else{
				logger.info("sendStopMessageToAs success.");
			}
			
		}catch (CCPServiceException e) {
			logger.error("sendStopMessageToAs#error()", e);
		}
	}
	
	@Override
	public void rejectUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		rejectUserEndService(sendMsg, connector, userAccountAcc, userAcc, protoClientNo);
	}
	
	/**
	 * @Description: 拒接
	 * @param sendMsg
	 * @param connector
	 * @param userAccountAcc
	 * @param userAcc
	 * @param protoClientNo 
	 * @throws CCPServiceException
	 */
	private void rejectUserEndService(MCMDataInner sendMsg, Connector connector, String userAccountAcc,
			String userAcc, int protoClientNo) throws CCPServiceException{
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog != null){
			
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			
			if(agentInfoSet.isEmpty()){
				userAgentDialogRedisDao.deleteDialog(userAccountAcc);
				userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
				return;
			}
			
			for(AgentInfo agentInfo : agentInfoSet){
				String agentAccount = agentInfo.getAgentAccount();
				String agentId = agentInfo.getAgentId();
				if(agentAccount.equals(userAcc)){
					// 结束会话
					RMServerTransferData rmServerTransferData = new RMServerTransferData();
					rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
					rmServerTransferData.setAppid(connector.getAppId());
					
					SeqInfo seqInfo = new SeqInfo();
					seqInfo.setMcmEvent(sendMsg.getMCMEvent());
					seqInfo.setAgentAccount(userAcc);
					seqInfo.setAgentId(agentId);
					seqInfo.setLogSessionId(ThreadContext.peek());
					seqInfo.setProtoClientNo(protoClientNo);
					rmServerTransferData.setSeq(seqInfo.toString());
					
					rmServerTransferData.setSid(userAndAgentDialog.getSid());
					rmServerTransferData.setAgentid(agentId);
					
					rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
					
					// 添加历史消息
					dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
							null, "", agentId);
					
					break;
				}
			}
		}else{
			logger.warn("rejectUser fail, get user and agent dialog is null.");
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_RejectUserResp_VALUE);
			pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		}
	}
	
	@Override
	public void sendMCM(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(sendMsg.getMSGDataCount() <= 0){
			logger.warn("MSGData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_MSGDAtA_EMPTY);
		}
		
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		// 发送消息
		sendMcmMessageToUser(sendMsg, connector, userAcc, userAccountAcc, protoClientNo);
	}
	
	/**
	 * @Description: 发送消息给用户
	 * @param sendMsg
	 * @param connector
	 * @param agentAccount
	 * @param userAccountAcc  
	 * @throws
	 */
	private void sendMcmMessageToUser(MCMDataInner sendMsg, Connector connector, String agentAccount, 
			String userAccountAcc, int protoClientNo) throws CCPServiceException{
		// 通知坐席发送响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_SendMCMResp_VALUE);
		pushService.doPushMsg(agentAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
					
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog != null){
			// 给其他坐席发送消息
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			
			// 发送者坐席Id 
			String agentId = checkAgentIsExistDialog(agentAccount, agentInfoSet);
			if(StringUtils.isBlank(agentId)){
				return;
			}
			
			String userAccount = StringUtil.getUserNameFormUserAcc(userAccountAcc);
			
			List<MSGDataInner> msgDataList = sendMsg.getMSGDataList();
			for(MSGDataInner msgData : msgDataList){
				long version = -1; // versionDao.getMessageVersion(userAccountAcc);
				String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, connector.getConnectorId());
				
				//根据不同的用户来源，向用户发送消息
				if(MCMChannelTypeInner.MCType_im_VALUE==userAndAgentDialog.getChanType()){
					// 推送给用户消息
					mcmMessageInfo = buildMCMMessageInfo(sendMsg, msgData, connector, 
							version, msgId, sendMsg.getMCMEvent(), "", agentId);
					pushService.doPushMsg(userAccountAcc, mcmMessageInfo);
					
				}else if(MCMChannelTypeInner.MCType_wx_VALUE==userAndAgentDialog.getChanType()){
					McmWeiXinMsgInfo weixinData = new McmWeiXinMsgInfo();
					weixinData.setOpenID(userAndAgentDialog.getOsUnityAccount());
					weixinData.setUserID(StringUtil.getUserNameFormUserAcc(userAccountAcc));
					weixinData.setMsgType(weixinData.getWeixinMsgType(msgData.getMsgType()));
					String content = msgData.getMsgContent();
					weixinData.setContent(content);
					if(StringUtils.isNotEmpty(msgData.getMsgFileUrl())){
						weixinData.setUrl(fileServerDownloadUrl+msgData.getMsgFileUrl());
					}
					weixinService.sendWeiXinMsg(weixinData);
				}
				
				// 给坐席推消息
				for(AgentInfo agentInfo : agentInfoSet){
					if(agentAccount.equals(agentInfo.getAgentAccount())){
						continue;
					}
					
					// 推送消息
					mcmMessageInfo = buildMCMMessageInfo(sendMsg, msgData, connector, 
							version, msgId, MCMEventDefInner.NotifyAgent_AgentSendMsg_VALUE, 
							userAccount, agentId);
					pushService.doPushMsg(agentInfo.getAgentAccount(), mcmMessageInfo);
					
					// 添加历史消息
					dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData, version, 
							connector, userAndAgentDialog, msgId, null, 
							agentInfo.getAgentAccount(), agentInfo.getAgentId());
				}
				
				// 添加历史消息
				dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData, version, 
						connector, userAndAgentDialog, msgId, null, 
						userAccountAcc, agentId);
				
				//如果存在监控者，需要处理监控者消息
				/*if(monitorFlag){
					processMonitorMsg(userAndAgentDialog, sendMsg, connector, msgId, msgData);
				}*/
			}// for msgDataList
			
		}else{
			logger.warn("sendMSG fail, get user and agent dialog is null.");
			return;
		}
	}
	
	/**
	 * @Description: 判断坐席是否存在会话中
	 * @param agentAccount
	 * @param agentInfoList 
	 */
	private String checkAgentIsExistDialog(String agentAccount, Set<AgentInfo> agentInfoSet){
		for (Iterator<AgentInfo> iter = agentInfoSet.iterator(); iter.hasNext();) {  
			AgentInfo agentInfo = iter.next();  
			if(agentAccount.equals(agentInfo.getAgentAccount())){
				logger.info("agentAccount: {} exist dialog.", agentAccount);
			    return agentInfo.getAgentId();
            }  
        }  
		return "";
	}
	
	/**
	 * @Description: 超监控者推送消息
	 * @param userAndAgentDialog
	 * @param sendMsg
	 * @param connector
	 * @param msgId
	 * @param msgData
	 * @throws CCPServiceException
	 */
	/*private void processMonitorMsg(UserAndAgentDialog userAndAgentDialog, MCMDataInner sendMsg, Connector connector,
			String msgId, MSGDataInner msgData) throws CCPServiceException {
		AgentEventInfo agentEventInfo = null;
		String monitorAgentAccount = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		MCMMessageInfo mcmMessageInfo = null;
		for(String agentId:userAndAgentDialog.getMonitorAgentIds()){
			//根据监控者座席ID获取座席帐号
			agentEventInfo = agentEventInfoRedisDao.getEventInfo(Integer.parseInt(agentId));
			if(agentEventInfo==null){
				logger.info("processMonitorMsg fail, get agentInfo is null.");
				throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_DIALOG_NOT_EXIST);
			}
			//获取监控者的版本号
			long version = versionDao.getMessageVersion(agentEventInfo.getUserAcc());
			
			// 推送消息
			mcmMessageInfo = buildMCMMonitorMessageInfo(sendMsg, msgData, version, connector, msgId);
			pushService.doPushMsg(monitorAgentAccount, mcmMessageInfo);
			
			// 异步保存消息数据导数据库
			dataAsyncService.saveM3CSMessageHistory(sendMsg, msgData, version, 
					connector, userAndAgentDialog, msgId, 1,agentId);
		}
	}*/
	
	/**
	 * @Description: 封装发送消息对象
	 * @param sendMsg  
	 * @param msgData
	 * @param connector
	 * @param version
	 * @param msgId
	 */
	private MCMMessageInfo buildMCMMessageInfo(MCMDataInner sendMsg, MSGDataInner msgData, 
			Connector connector, long version, String msgId, int mcmEvent, 
			String userAccount, String agentId) {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMsgId(msgId);
		mcmMessageInfo.setAppId(connector.getAppId());
		//mcmMessageInfo.setVersion(version);	
		mcmMessageInfo.setMCMEvent(mcmEvent);
		mcmMessageInfo.setOsUnityAccount(Constants.OS_UNITY_ACCOUNT);
		
		if(StringUtils.isNotBlank(userAccount)){
			mcmMessageInfo.setUserAccount(userAccount);
		}
		
		if(StringUtils.isNotBlank(agentId)){
			mcmMessageInfo.setAgentId(agentId);
		}
		
		String chanType = sendMsg.getChanType();
		int ct = 0;
		if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			ct = Integer.parseInt(sendMsg.getChanType());
		}
		mcmMessageInfo.setChanType(ct);
		
		mcmMessageInfo.setMsgType(msgData.getMsgType());
		mcmMessageInfo.setMsgContent(msgData.getMsgContent());
		mcmMessageInfo.setMsgFileName(msgData.getMsgFileName());
		mcmMessageInfo.setMsgFileUrl(msgData.getMsgFileUrl());
		mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
		
		mcmMessageInfo.setMailTitle(msgData.getMailTitle());
		
		return mcmMessageInfo;
	}
	
	/**
	 * @Description: 封装发送消息对象
	 * @param sendMsg  
	 * @param msgData
	 * @param version
	 * @param connector
	 */
	/*private MCMMessageInfo buildMCMMonitorMessageInfo(MCMDataInner sendMsg, MSGDataInner msgData, Long version, 
			Connector connector, String msgId) {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMsgId(msgId);
		mcmMessageInfo.setUserAccount(sendMsg.getUserAccount());
		mcmMessageInfo.setVersion(version);
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_MonitorUserMsg_VALUE);
		mcmMessageInfo.setOsUnityAccount(sendMsg.getOsUnityAccount());
		
		mcmMessageInfo.setMsgType(msgData.getMsgType());
		mcmMessageInfo.setMsgContent(msgData.getMsgContent());
		mcmMessageInfo.setMsgFileName(msgData.getMsgFileName());
		mcmMessageInfo.setMsgFileUrl(msgData.getMsgFileUrl());
		mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
		
		mcmMessageInfo.setChanType(MCMChannelTypeInner.MCType_im_VALUE);
		mcmMessageInfo.setAppId(connector.getAppId());
		
		return mcmMessageInfo;
	}*/
	
	@Override
	public void transKF(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}, .", userAccountAcc);
		
		if(!sendMsg.hasTransAgentId()){
			logger.warn("trans agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_TRANS_AGENT_ID_EMPTY);
		}
		
		String transAgentId = sendMsg.getTransAgentId();
		if(StringUtils.isBlank(transAgentId)){
			logger.warn("trans agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_TRANS_AGENT_ID_EMPTY);
		}
		logger.info("transAgentId: {}.", transAgentId);
		
		// 根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 申请获取坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		
		if(sendMsg.hasQueueType() && sendMsg.getQueueType() > 0){
			rmServerTransferData.setQueuetype(sendMsg.getQueueType());
		}
		
		String agentId = "";
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			if(agentInfo.getAgentAccount().equals(userAcc)){
				agentId = agentInfo.getAgentId();
				break;
			}
		}
	
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setAgentid(transAgentId); // 锁定指定服务座席工号
		rmServerTransferData.setForce(true);
		rmServerTransferData.setUseraccount(userAccount);
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = null;
		if(sendMsg.hasChanType()){
			chanType = sendMsg.getChanType();
		}
		int ct = 0;
		if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			ct = Integer.parseInt(chanType);
		}
		rmServerTransferData.setKeytype(ct);
		// 自定义文本内容。对于微信用户此字段会有内容
		// rmServerTransferData.setCustomcontent(customcontent);
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", agentId);
	}
	
	@Override
	public void transferQueue(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasQueueType()){
			logger.warn("transferQueue error, queueType is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_TRANSFER_QUEUE_QUEUETYPE_EMPTY);
		}
		
		int queueType = sendMsg.getQueueType();
		logger.info("queueType: {}.", queueType);
		/*if(queueType < 0){
			logger.info("transferQueue error,queueType is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_TRANSFER_QUEUE_QUEUETYPE_EMPTY);
		}*/
		
		// 根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 申请获取坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		
		String agentId = "";
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			if(agentInfo.getAgentAccount().equals(userAcc)){
				agentId = agentInfo.getAgentId();
				break;
			}
		}
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setQueuetype(queueType); 
		rmServerTransferData.setUseraccount(sendMsg.getUserAccount());
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = null;
		if(sendMsg.hasChanType()){
			chanType = sendMsg.getChanType();
		}
		int ct = 0;
		if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			ct = Integer.parseInt(chanType);
		}
		rmServerTransferData.setKeytype(ct);
		// 自定义文本内容。对于微信用户此字段会有内容
		// rmServerTransferData.setCustomcontent(customcontent);
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", agentId);
	}

	@Override
	public void enterCallService(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
	}
	
	@Override
	public void forceTransfer(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasTransAgentId()){
			logger.warn("trans agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_TRANS_AGENT_ID_EMPTY);
		}
		
		String transAgentId = sendMsg.getTransAgentId();
		if(StringUtils.isBlank(transAgentId)){
			logger.warn("trans agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_TRANS_AGENT_ID_EMPTY);
		}
		logger.info("transAgentId: {}.", transAgentId);
		
		if(!sendMsg.hasSuperAgentId()){
			logger.warn("super agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		
		String superAgentId = sendMsg.getSuperAgentId();
		if(StringUtils.isBlank(superAgentId)){
			logger.warn("super agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		logger.info("superAgentId: {}.", superAgentId);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		
		String srcAgentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(srcAgentId)){
			logger.warn("srcAgentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("srcAgentId: {}.", srcAgentId);
		
		//根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 申请获取坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setAgentId(srcAgentId);
		seqInfo.setSuperAgentId(superAgentId);
		seqInfo.setSuperAccount(userAcc);
		seqInfo.setLogSessionId(ThreadContext.peek());		
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setAgentid(transAgentId);
		rmServerTransferData.setForce(true);
		rmServerTransferData.setUseraccount(userAccount);
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = null;
		if(sendMsg.hasChanType()){
			chanType = sendMsg.getChanType();
		}
		int ct = 0;
		if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			ct = Integer.parseInt(chanType);
		}
		rmServerTransferData.setKeytype(ct);
		// 自定义文本内容。对于微信用户此字段会有内容
		// rmServerTransferData.setCustomcontent(customcontent);
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", transAgentId);
	}

	@Override
	public void forceEndService(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasSuperAgentId()){
			logger.warn("super agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		
		String superAgentId = sendMsg.getSuperAgentId();
		if(StringUtils.isBlank(superAgentId)){
			logger.warn("super agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		logger.info("superAgentId: {}.", superAgentId);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			
			// 只结束被强拆的坐席, 如果存在多个, 其他的不用结束
			if(agentInfo.getAgentId().equals(agentId)){
				//向RMserver发送结束服务命令CmdImAgentServiceEnd
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
				rmServerTransferData.setAppid(connector.getAppId());
				rmServerTransferData.setSid(userAndAgentDialog.getSid());
				rmServerTransferData.setAgentid(agentId);
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setAgentId(agentInfo.getAgentId());
				seqInfo.setAgentAccount(agentInfo.getAgentAccount());
				seqInfo.setMcmEvent(sendMsg.getMCMEvent());
				seqInfo.setSuperAccount(userAcc);
				seqInfo.setProtoClientNo(protoClientNo);
				seqInfo.setSuperAgentId(superAgentId);
				
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdAllocImAgent());
			}
		}
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
				null, "", agentId);
	}

	@Override
	public void startSessionTimer(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("start ser with user fail, get user and agent dialog or agentInfoList is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_StartSessionTimerResp_VALUE);
		mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
		
		if(QuartzManager.isExistJob(userAndAgentDialog.getSid())){
			mcmMessageInfo.setOptRetDes("timer already exi.");
			
		}else{
			// 启动定时器
			QuartzManager.addJob(userAndAgentDialog.getSid(), new AgentDialogTimeOutJob());
			
			mcmMessageInfo.setOptRetDes("timer success.");
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			for(AgentInfo agentInfo : agentInfoSet){
				if(userAcc.equals(agentInfo.getAgentAccount())){
					dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", 
							null, "", agentInfo.getAgentId());
				}
				// 向RM发送CmdImAgentServiceEnd消息
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
				rmServerTransferData.setAppid(connector.getAppId());
				rmServerTransferData.setSid(userAndAgentDialog.getSid());
				rmServerTransferData.setAgentid(agentInfo.getAgentId());
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setAgentAccount(userAcc);
				seqInfo.setMcmEvent(sendMsg.getMCMEvent());
				seqInfo.setProtoClientNo(protoClientNo);
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
			}
		}
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
	}

	@Override
	public void monitorAgent(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);

		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 向会话数据中添加监听者座席id
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setAgentAccount(userAcc);
		agentInfo.setAgentId(agentId);
		agentInfo.setType(Constants.MONITOR_AGENT_TYPE);
		agentInfoSet.add(agentInfo);
		userAgentDialogRedisDao.saveDialog(userAccountAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		
		// 向座席回复监听响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_MonitorAgentResp_VALUE);
		mcmMessageInfo.setUserAccount(userAccount);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}

	@Override
	public void cancelMonitorAgent(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 修改会话
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for (Iterator<AgentInfo> iter = agentInfoSet.iterator(); iter.hasNext();) {  
			AgentInfo agentInfo = iter.next();  
			if(agentId.equals(agentInfo.getAgentId()) && agentInfo.getType() == Constants.MONITOR_AGENT_TYPE){
				logger.info("start remove monitor agentId: {}.", agentId);
			    iter.remove();  
            }  
        }  
		userAndAgentDialog.setAgentInfoSet(agentInfoSet);
		userAgentDialogRedisDao.saveDialog(userAccountAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		
		// 向座席回复监听响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_CancelMonitorAgentResp_VALUE);
		mcmMessageInfo.setUserAccount(userAccount);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}

	@Override
	public void startConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 申请获取坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		rmServerTransferData.setUseraccount(userAccount);
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setAgentid(String.valueOf(agentId)); // 锁定指定服务座席工号
		rmServerTransferData.setForce(true);
		
		if(sendMsg.hasQueueType()){
			rmServerTransferData.setQueuetype(sendMsg.getQueueType()); // 锁定指定服务座席工号
		}
		
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = null;
		if(sendMsg.hasChanType()){
			chanType = sendMsg.getChanType();
		}
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
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}

	@Override
	public void joinConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		// 根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 加入会议响应
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_JoinConfResp_VALUE);
		pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
				
		// 通知会议成员
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		for(AgentInfo agentInfo : agentInfoSet){
			if(userAcc.equals(agentInfo.getAgentAccount())){
				continue;
			}
			mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_AgentJoinIM_VALUE);
			mcmMessageInfo.setAgentId(agentId);
			mcmMessageInfo.setUserAccount(userAccount);
			mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
			mcmMessageInfo.setOptRetDes("have agent join conf.");
			
			pushService.doPushMsg(agentInfo.getAgentAccount(), mcmMessageInfo);
		}
		
		// 通知用户
		mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartConf_VALUE);
		mcmMessageInfo.setAgentAccount(userAcc);
		mcmMessageInfo.setAgentId(agentId);
		mcmMessageInfo.setAgentAccount(connector.getUserName());
		pushService.doPushMsg(userAccount, mcmMessageInfo);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}
	
	@Override
	public void exitConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		//根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 结束会话
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setLogSessionId(ThreadContext.peek());
		seqInfo.setProtoClientNo(protoClientNo);
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setSid(userAndAgentDialog.getSid());
		rmServerTransferData.setAgentid(agentId);
		
		rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}
	
	@Override
	public void forceJoinConf(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		//根据用户useracc查询当前用户会话状态
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccountAcc);
		if(userAndAgentDialog == null || userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.warn("userAndAgentDialog is null or agentInfoList is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_USER_DIALOG_NOT_EXIST);
		}
		
		// 申请获取坐席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
		rmServerTransferData.setUseraccount(userAccount);
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setAgentId(agentId);
		seqInfo.setLogSessionId(ThreadContext.peek());
		seqInfo.setProtoClientNo(protoClientNo);
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setAgentid(agentId); // 锁定指定服务座席工号
		rmServerTransferData.setForce(true);
		
		// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
		String chanType = null;
		if(sendMsg.hasChanType()){
			chanType = sendMsg.getChanType();
		}
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
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}
	
	@Override
	public void queryQueueInfo(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		
		// 向RMServer发送请求，查询信息排队人数和空闲座席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_QUERY_QUEUE_INFO.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		if(sendMsg.hasQueueType()){
			rmServerTransferData.setQueuetype(sendMsg.getQueueType());
		}else{
			rmServerTransferData.setQueuetype(-1);
		}
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setProtoClientNo(protoClientNo);
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		
		String cmdMessage = rmServerTransferData.toJsonForCmdQueryQueueInfo();
		rmServerRequestService.doPushMessage(cmdMessage);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", "");
	}

	@Override
	public void queryAgentInfo(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
	}

	@Override
	public void serWithTheUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasUserAccount()){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccount = sendMsg.getUserAccount();
		if(StringUtils.isBlank(userAccount)){
			logger.warn("userAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
		}
		
		String userAccountAcc = userAccount;
		if(!userAccount.contains("#")){
			userAccountAcc = StringUtil.getUserAcc(connector.getAppId(), userAccount);	
		}
		logger.info("userAccountAcc: {}.", userAccountAcc);
		
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		if(!sendMsg.hasChanType()){
			logger.warn("chanType is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SER_WITH_THE_USER_CHAN_TYPE_EMPTY);
		}
		
		String chanType = sendMsg.getChanType();
		if(StringUtils.isBlank(chanType)){
			logger.warn("chanType is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_SER_WITH_THE_USER_CHAN_TYPE_EMPTY);
		}
		logger.info("chanType: {}.", chanType);
		
		String sid = StringUtil.generateSid(Constants.M3C_SERIAL, connector.getConnectorId());
		
		// 向RMServer发送请求，分配IM座席
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		rmServerTransferData.setForce(true);
		rmServerTransferData.setAgentid(agentId);
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setConnectorId(connector.getConnectorId());
		seqInfo.setLogSessionId(ThreadContext.peek());
		rmServerTransferData.setSeq(seqInfo.toString());
		
		rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
		rmServerTransferData.setUseraccount(userAccount);
		rmServerTransferData.setSid(sid);
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
		rmServerRequestService.doPushMessage(cmdMessage);	
		
		// 生成会话记录
		saveDialog(sendMsg.getCCSType(), Constants.OS_UNITY_ACCOUNT, sid, userAccountAcc, Integer.parseInt(chanType));
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				userAccountAcc, agentId);
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
	public void reservedForUser(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		if(!sendMsg.hasCcpCustomData()){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		
		String ccpCustomData = sendMsg.getCcpCustomData();
		if(StringUtils.isBlank(ccpCustomData)){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		logger.info("ccpCustomData: {}.", ccpCustomData);
		
		ReservedInfo reservedInfo = parseCcpCustomDataToReservedInfo(ccpCustomData);
		if(reservedInfo == null){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		
		if(StringUtils.isBlank(reservedInfo.getReservedKey())){
			logger.warn("reservedKey is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		
		sendReservedServiceReqToRmServer(sendMsg, connector, true, userAcc, protoClientNo, 
				reservedInfo, agentId);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}
	
	@Override
	public void cancelReserved(MCMDataInner sendMsg, Connector connector, String userAcc, 
			int protoClientNo) throws CCPServiceException {
		if(!sendMsg.hasAgentId()){
			logger.warn("agentId is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isBlank(agentId)){
			logger.warn("agentId is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		logger.info("agentId: {}.", agentId);
		
		if(!sendMsg.hasCcpCustomData()){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		
		String ccpCustomData = sendMsg.getCcpCustomData();
		if(StringUtils.isBlank(ccpCustomData)){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		logger.info("ccpCustomData: {}.", ccpCustomData);
		
		ReservedInfo reservedInfo = parseCcpCustomDataToReservedInfo(ccpCustomData);
		if(reservedInfo == null){
			logger.warn("ccpCustomData is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY);
		}
		
		sendReservedServiceReqToRmServer(sendMsg, connector, false, userAcc, protoClientNo, 
				reservedInfo, agentId);
		
		// 添加历史消息
		dataAsyncService.saveM3CSMessageHistory(sendMsg, null, -1, connector, null, "", null, 
				"", agentId);
	}

	/**
	 * @Description: 获取预留的相关参数信息
	 * @param ccpCustomData 
	 */
	private ReservedInfo parseCcpCustomDataToReservedInfo(String ccpCustomData){
		try {
			ReservedInfo reservedInfo = (ReservedInfo)JSONUtil.jsonToObj(ccpCustomData, ReservedInfo.class);
			return reservedInfo;
		} catch (Exception e) {
			logger.error("parseCcpCustomDataToReservedInfo#error: ", e);
			return null;
		} 
	}
	
	/**
	 * @Description: 坐席发送预留请求至rm server
	 * @param sendMsg
	 * @param connector
	 * @param optType true表示启用预留服务；false取消预留服务
	 * @param userAcc
	 * @param protoClientNo
	 * @param reservedInfo reservedkey为空时是取消全部预留，有值则取消指定用户服务
	 * @throws
	 */
	private void sendReservedServiceReqToRmServer(MCMDataInner sendMsg, Connector connector, boolean optType, 
			String userAcc, int protoClientNo, ReservedInfo reservedInfo, 
			String agentId) throws CCPServiceException{
		// 申请预留
		RMServerTransferData rmServerTransferData = new RMServerTransferData();
		rmServerTransferData.setCommand(CommandEnum.CMD_AGENT_RESERVED_SERVICE.getValue());
		rmServerTransferData.setAppid(connector.getAppId());
		
		SeqInfo seqInfo = new SeqInfo();
		seqInfo.setMcmEvent(sendMsg.getMCMEvent());
		seqInfo.setAgentAccount(userAcc);
		seqInfo.setLogSessionId(ThreadContext.peek());
		seqInfo.setProtoClientNo(protoClientNo);
		seqInfo.setReady(optType);
		seqInfo.setUserAccount(reservedInfo.getReservedKey());
		seqInfo.setConnectorId(connector.getConnectorId());
		seqInfo.setAppId(connector.getAppId());
		seqInfo.setChanType(String.valueOf(reservedInfo.getKeyType()));
		rmServerTransferData.setSeq(seqInfo.toString());

		rmServerTransferData.setAgentid(agentId);
		rmServerTransferData.setKeytype(reservedInfo.getKeyType());
		rmServerTransferData.setOpttype(optType);
		rmServerTransferData.setReservedkey(reservedInfo.getReservedKey());
		
		String cmdMessage = rmServerTransferData.toJsonForCmdAgentReservedService();
		rmServerRequestService.doPushMessage(cmdMessage);
	}
	
	@Override
	public void agentDisconnect(MCMDataInner sendMsg, Connector connector,
			String userAcc, int protoClientNo) throws CCPServiceException {
		pushService.doPushMsg(userAcc, null, protoClientNo, Constants.SUCC);
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

	public void setFileServerDownloadUrl(String fileServerDownloadUrl) {
		this.fileServerDownloadUrl = fileServerDownloadUrl;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setResendTimeNum(int resendTimeNum) {
		this.resendTimeNum = resendTimeNum;
	}
	
}
