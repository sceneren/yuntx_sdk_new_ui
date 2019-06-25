package com.yuntongxun.mcm.mcm.service.impl;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yuntongxun.tools.protocol.codec.ProtobufCodecManager;

import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.connection.ModuleProducter;
import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MsgLite;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.dao.AppRedisDao;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.service.AgentService;
import com.yuntongxun.mcm.mcm.service.MCMService;
import com.yuntongxun.mcm.mcm.service.UserService;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.sevenmoor.service.SevenMoorService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.RedisKeyConstant;
import com.yuntongxun.mcm.util.StringUtil;
import com.yuntongxun.mcm.util.cryptos.Cryptos;

public class MCMServiceImpl implements MCMService {

	public static final Logger logger = LogManager.getLogger(MCMServiceImpl.class);

	private String cryptosKey;

	private AgentService agentService;

	private UserService userService;

	private AppRedisDao appRedisDao;
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private BaseRedisDao baseRedisDao;
	
	private SevenMoorService sevenMoorService;
	
	private ModuleProducter moduleProducter;
	
	@Override
	public void handleMcmReceivedMessage(final MsgLiteInner msgLite, final Connector connector) 
			throws CCPServiceException {
		String eventName = "unknown event";
		try {
			MCMDataInner sendMsg = getSendMsg(msgLite);

			if (sendMsg == null) {
				throw new CCPServiceException(Constants.ERROR_MCM_MCMDAtA_EMPTY);
			} else {
				// 判断事件是否合法
				int mcmEvent = sendMsg.getMCMEvent();
				
				MCMEventData.MCMEventDefInner medi = MCMEventDefInner.valueOf(mcmEvent);
				if (medi == null) {
					logger.warn("unknown mcmEvent: {}.", mcmEvent);
					throw new CCPServiceException(Constants.ERROR_MCM_UNKNOWN_EVENT);
				}

				eventName = MCMEventDefInner.valueOf(mcmEvent).name();
				if (StringUtils.isBlank(eventName)) {
					logger.warn("unknown mcmEvent: {}.", mcmEvent);
					throw new CCPServiceException(Constants.ERROR_MCM_UNKNOWN_EVENT);
				}
				
				PrintUtil.printStartTag(eventName);
				
				String appId = connector.getAppId();
				if(StringUtils.isBlank(appId)){
					logger.warn("get appId is null.");
					throw new CCPServiceException(Constants.ERROR_MCM_IM_APP_ATTRS_EMPTY);
				}
				
				// 获取appAttrs
				AppAttrs appAttrs = appRedisDao.getAppAttrsByAppkey(appId);
				if(appAttrs == null){
					logger.warn("get appAttrs is null.");
					throw new CCPServiceException(Constants.ERROR_MCM_IM_APP_ATTRS_EMPTY);
				}
				
				if(appAttrs.getIsAgent() == 0){
					logger.warn("isAgent is 0.");
					throw new CCPServiceException(Constants.ERROR_MCM_IM_ISAGENT_EMPTY_OR_ZERO);
				}
				
				// 开始处理事件请求
				processRequest(sendMsg, connector, mcmEvent, msgLite.getProtoClientNo(), appAttrs);
				
			}//if sendMsg
		} catch (IOException e) {
			logger.error("handleMcmReceivedMessage#IOException()", e);
			throw new CCPServiceException(Constants.ERROR_MCM_SERVER_ISSUE);
		} catch (CCPRedisException e) {
			logger.error("handleMcmReceivedMessage#CCPRedisException()", e);
			throw new CCPServiceException(Constants.ERROR_MCM_SERVER_ISSUE);
		} finally{
			PrintUtil.printEndTag(eventName);
		}
	}

	/**
	 * @Description: 开始处理请求
	 * @param sendMsg
	 * @param connector
	 * @param mcmEvent
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	private void processRequest(MCMDataInner sendMsg, Connector connector, int mcmEvent, 
			int protoClientNo, AppAttrs appAttrs) throws CCPServiceException {
		switch(mcmEvent){
			case MCMEventDefInner.UserEvt_StartAsk_VALUE:
				// 用户开始咨询
				userService.startAsk(sendMsg, connector, protoClientNo, appAttrs);
				break;
				
			case MCMEventDefInner.UserEvt_EndAsk_VALUE:
				// 用户结束咨询
				userService.endAsk(sendMsg, connector, protoClientNo, appAttrs);
				break;
				
			case MCMEventDefInner.UserEvt_SendMSG_VALUE:
				// 用户发送消息
				userService.sendMSG(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.UserEvt_GetAGList_VALUE:
				// 用户获取客服分组列表
				userService.getAGList(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.UserEvt_IRCN_VALUE:
				// 用户平台交互控制命令与通知
				userService.ircn(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_KFOnWork_VALUE:
				// 客服(坐席)上班(签入)
				agentService.onWork(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_KFOffWork_VALUE:
				// 客服(坐席)下班(签出)
				agentService.offWork(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_KFStateOpt_VALUE:
				// 客服(坐席)状态变化
				agentService.stateOpt(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_Ready_VALUE:
				// 客服(坐席)坐席就绪
				agentService.ready(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_NotReady_VALUE:
				// 客服(坐席)未就绪
				agentService.notReady(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_StartSerWithUser_VALUE:
				// 客服(坐席)开始为用户服务(接起会话)
				agentService.startSerWithUser(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_StopSerWithUser_VALUE:
				// 客服(坐席)结束为用户服务(挂断会话)
				agentService.stopSerWithUser(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_RejectUser_VALUE:
				// 客服(坐席)拒绝为用户服务
				agentService.rejectUser(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_SendMCM_VALUE:
				// 客服(坐席)发送普通消息
				agentService.sendMCM(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_SendNotify_VALUE:
				// 客服(坐席)发送通知信息
				agentService.sendNotify(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_TransKF_VALUE:
				// 客服(坐席)将用户转接到其他客服
				agentService.transKF(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_EnterCallService_VALUE:
				// 客服(坐席)转入电话服务
				agentService.enterCallService(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_MakeCall_VALUE:
				// 客服(坐席)发送请求外呼事件
				agentService.makeCall(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_AnswerCall_VALUE:
				// 客服(坐席)发送请求应答事件
				agentService.answerCall(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ReleaseCall_VALUE:
				// 客服(坐席)发送请求挂机事件
				agentService.releaseCall(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_StartConf_VALUE:
				// 客服(坐席)开始会议
				agentService.startConf(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_JoinConf_VALUE:
				// 客服(坐席)加入会议
				agentService.joinConf(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ExitConf_VALUE:
				// 客服(坐席)退出会议
				agentService.exitConf(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_TransferQueue_VALUE:
				// 客服(座席)转接队列
				agentService.transferQueue(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ForceJoinConf_VALUE:
				// 客服(座席)强插
				agentService.forceJoinConf(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ForceTransfer_VALUE:
				// 客服(座席)强转
				agentService.forceTransfer(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ForceEndService_VALUE:
				// 客服(座席)强拆
				agentService.forceEndService(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_StartSessionTimer_VALUE:
				// 启动会话定时器
				agentService.startSessionTimer(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_MonitorAgent_VALUE:
				// 管理者监听座席
				agentService.monitorAgent(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_CancelMonitorAgent_VALUE:
				// 管理者取消监听座席
				agentService.cancelMonitorAgent(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_QueryQueueInfo_VALUE:
				// 查询队列接口
				agentService.queryQueueInfo(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_QueryAgentInfo_VALUE:
				// 查询座席信息接口
				agentService.queryAgentInfo(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_SerWithTheUser_VALUE:
				// 座席为指定用户服务
				agentService.serWithTheUser(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_ReservedForUser_VALUE:
				// 用户预留服务
				agentService.reservedForUser(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.AgentEvt_CancelReserved_VALUE:
				// 取消用户预留服务
				agentService.cancelReserved(sendMsg, connector, protoClientNo);
				break;
				
			case MCMEventDefInner.UserEvt_SubmitInvestigate_VALUE:
				// 用户提交满意度数据
				userService.submitInvestigate(sendMsg, connector, protoClientNo);
				break;
		}
	}
	
	@Override
	public void handlerConnectorCloseReceivedMessage(MsgLiteInner msgLite, Connector connector) 
			throws CCPServiceException {
		try {
			PrintUtil.printStartTag("ConnectorClose");			
				
			MsgLite.MsgLiteInner returnMsgLite = MsgLiteFactory.getMsgLite(msgLite.getProtoType(), msgLite.getProtoClientNo(),
					connector.getSessionId(), Constants.SUCC);
			moduleProducter.replyBytesMessage(connector.getChannelId(), returnMsgLite);
			
			String appId = connector.getAppId();
			if(StringUtils.isBlank(appId)){
				logger.warn("get appId is null.");
				return;
			}
			
			// 获取appAttrs
			AppAttrs appAttrs = appRedisDao.getAppAttrsByAppkey(connector.getAppId());
			if(appAttrs == null){
				logger.warn("get appAttrs is null.");
				return;
			}
			
			if(appAttrs.getIsAgent() == 0){
				logger.warn("isAgent is 0.");
				return;
			}
			
			String userName = connector.getUserName();
			String userAcc = StringUtil.getUserAcc(appId, userName);
			logger.info("userAcc: {}.", userAcc);
			
			UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAcc);
			if(userAndAgentDialog == null){
				logger.info("get userAndAgentDialog is null.");
			} else {
				userService.userDisconnect(connector, appAttrs, userAcc, userAndAgentDialog);
				return;
			}
			
			String key = RedisKeyConstant.YTX_7MOOR_LOGIN_INFO+userAcc;
			String connectionId = baseRedisDao.getRedisValue(key);
			logger.info("get redis key:{},value:{}",key,connectionId);
			if(StringUtils.isNotEmpty(connectionId)){
				//向七陌发送用户离开
				sevenMoorService.userDisconnect(connectionId);
			}
			
		} catch (Exception e) {
			logger.error("handlerConnectorCloseReceivedMessage#error()", e);
			return;
		} finally{
			PrintUtil.printEndTag("ConnectorClose");
		}
	}
	
	/**
	 * @Description: 处理接收到的消息内容
	 * @param msgLite
	 * @return
	 * @throws IOException
	 */
	private MCMDataInner getSendMsg(MsgLiteInner msgLite) throws IOException {
		byte[] data = msgLite.getProtoData().toByteArray();
		// 消息解密 Decode
		byte[] decodeDataCut = data;

		// 注意加解密 问价服务器不解密，文本服务器需解密
		// 加密先注释了
		if (msgLite.hasProtoEncrypt()) {
			int len = msgLite.getProtoEncrypt();
			if (len > 0) {
				byte[] decodeData = Cryptos.toQESDecode(cryptosKey, data);
				decodeDataCut = new byte[len];
				for (int i = 0; i < len; i++) {
					decodeDataCut[i] = decodeData[i];
				}
			}
		}

		return (MCMDataInner) ProtobufCodecManager.decoder(
				MCMDataInner.getDefaultInstance(), decodeDataCut);
	}

	/**
	 * set inject
	 */
	public void setCryptosKey(String cryptosKey) {
		this.cryptosKey = cryptosKey;
	}

	public void setAppRedisDao(AppRedisDao appRedisDao) {
		this.appRedisDao = appRedisDao;
	}

	public void setAgentService(AgentService agentService) {
		this.agentService = agentService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	public void setSevenMoorService(SevenMoorService sevenMoorService) {
		this.sevenMoorService = sevenMoorService;
	}

	public void setModuleProducter(ModuleProducter moduleProducter) {
		this.moduleProducter = moduleProducter;
	}

	public void setUserAgentDialogRedisDao(
			UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}
	
}
