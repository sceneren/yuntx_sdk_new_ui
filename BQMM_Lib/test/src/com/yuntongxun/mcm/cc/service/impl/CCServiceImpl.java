package com.yuntongxun.mcm.cc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.cc.form.WakeupUserForm;
import com.yuntongxun.mcm.cc.model.StartMessage;
import com.yuntongxun.mcm.cc.model.StopMessage;
import com.yuntongxun.mcm.cc.service.ICCService;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.CcpCustomData;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.ScriptManager;
import com.yuntongxun.mcm.util.StringUtil;

public class CCServiceImpl implements ICCService{

	public static final Logger logger = LogManager.getLogger(CCServiceImpl.class);
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private PushService pushService;
	
	private int resendTimeNum;
	
	private HttpClient httpClient;
	
	private WeiXinGWService weixinService;
	  
	@Override
	public void startMessage(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) 
			throws CCPServiceException {
		String moduleCode = ScriptManager.getScriptManager().getBaseCache(Constants.KEY_CONFIG_MCM_SERVER_ID);
		String msgId = StringUtil.generateMessageMsgId(moduleCode, connector.getConnectorId());
		String userAccount = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		
		if(!sendMsg.hasOsUnityAccount()){
			logger.info("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}

		String osUnityAccount = sendMsg.getOsUnityAccount(); 
		logger.info("osUnityAccount: {}.", osUnityAccount);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() > 0){
			logger.info("user already ask, have agentInfo.");

			// 回响应
			sendNotifyResponseMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, userAccount, protoClientNo, 
					Constants.SUCC);
			
		} else if (userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() == 0){
			logger.info("user already ask, not exist agentInfo.");
			
			sendNotifyResponseMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, userAccount, protoClientNo, 
					Constants.SUCC);
			
			if(userAndAgentDialog.getQueueCount() > 0){
				logger.info("already queue.");
				
				MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
				mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
				mcmMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
				mcmMessageInfo.setMsgContent("正在分配客服...");
				pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
			}
			
		} else {
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, connector.getConnectorId());
			String chanType = String.valueOf(MCMChannelTypeInner.MCType_im_VALUE);
			
			if(sendMsg.hasChanType()){
				chanType = sendMsg.getChanType();
			}
			
			// 获取企业Id
			String msgJsonData = "";
			if(sendMsg.hasMsgJsonData()) {
				msgJsonData = sendMsg.getMsgJsonData();
			}
			logger.info("cc start ask msgJsonData: {}.", msgJsonData);
			
			String companyId = "";
			try {
				if(StringUtils.isNotBlank(msgJsonData)){
					JSONObject jsonObject = JSONObject.fromObject(msgJsonData);
					
					Object ctObj = jsonObject.get("companyId");
					if(ctObj != null){
						companyId = String.valueOf(ctObj);
					}
				}
			} catch (Exception e) {
				logger.error("parse msgJsonData error: " + msgJsonData);
			}
			logger.info("companyId: {}.", companyId);
			
			// 发起开始咨询
			StartMessage startMessage = new StartMessage();
			startMessage.setUserAccount(userAccount);
			startMessage.setSid(sid);
			startMessage.setOsUnityAccount(osUnityAccount);
			startMessage.setAppId(connector.getAppId());
			startMessage.setChanType(chanType);
			startMessage.setMsgId(msgId);
			startMessage.setCompanyId(companyId);
			startMessage.setCreateTime(String.valueOf(System.currentTimeMillis()));
			
			String url = appInfo.getMcm_notify_url();
			String content = sendRequest(url, startMessage.toJson());
			
			JSONObject jsonObject = JSONObject.fromObject(content);
			if(jsonObject.has(Constants.CC_RESPONSE_CODE)){
				String code = jsonObject.getString(Constants.CC_RESPONSE_CODE);
				if(Constants.RESPONSE_OK.equals(code)){
					Object obj = parseBody(content);
					WakeupUserForm wakeupUserForm = (WakeupUserForm) obj;
					
					String agentId = wakeupUserForm.getAgentId();
					String customaccnum = wakeupUserForm.getCustomaccnum();
					
					// 生成会话记录 排队中跟分配坐席保存，其他情况不用保存会话记录
					saveDialog(sendMsg.getCCSType(), osUnityAccount, sid, userAccount, url, Integer.parseInt(chanType), 
							connector.getAppId(), agentId, customaccnum);
					
				} else {
					sendNotifyResponseMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, userAccount, protoClientNo, 
							Integer.parseInt(code));
				}
			}
		}
	}
	
	/**
	 * @Description: 保存会话
	 * @param ccsType
	 * @param osUnityAccount
	 * @param sid
	 * @param userAcc
	 * @param mcmNotifyUrl
	 * @param chanType
	 * @param appId 
	 * @throws
	 */
	private void saveDialog(int ccsType, String osUnityAccount, String sid, String userAcc, String mcmNotifyUrl, 
			Integer chanType, String appId, String agentId, String agentAccount){
		UserAndAgentDialog userAndAgentDialog = new UserAndAgentDialog();
		userAndAgentDialog.setCCSType(ccsType);
		userAndAgentDialog.setChannel(String.valueOf(Constants.M3C_SERIAL));
		userAndAgentDialog.setOsUnityAccount(osUnityAccount);
		userAndAgentDialog.setSid(sid);
		userAndAgentDialog.setDateCreated(System.currentTimeMillis());
		userAndAgentDialog.setMcm_notify_url(mcmNotifyUrl);
		userAndAgentDialog.setChanType(chanType);
		userAndAgentDialog.setAppId(appId);
		
		if(StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(agentAccount)){
			HashSet<AgentInfo> agentInfoSet = new HashSet<AgentInfo>();
			AgentInfo agentInfo = new AgentInfo();
			agentInfo.setAgentId(agentId);
			agentInfo.setAgentAccount(agentAccount);
			agentInfoSet.add(agentInfo);
			userAndAgentDialog.setAgentInfoSet(agentInfoSet);
			userAndAgentDialog.setQueueCount(0);
		}
		
		userAgentDialogRedisDao.saveDialog(userAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		userAgentDialogRedisDao.saveDialogSid(sid, userAcc, Constants.DIALOG_VALID_TIME);
	}
	
	@Override
	public void stopmessage(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) 
			throws CCPServiceException {
		String userAccount = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.info("endAsk fail, get user and agent dialog is null.");
			
		} else {
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			if(agentInfoSet.isEmpty()){
				logger.info("agentInfoList is empty, start exit queue.");
				
			} else {
				logger.info("agentInfoList is not empty, start end server.");
				endAsk(userAccount, userAndAgentDialog);
				
				sendNotifyResponseMessage(MCMEventDefInner.NotifyUser_EndAskResp_VALUE, userAccount, protoClientNo, 
						Constants.SUCC);
			}
		}
	}
	
	/**
	 * @Description: 结束咨询
	 * @param userAndAgentDialog
	 * @throws CCPServiceException 
	 */
	private void endAsk(String userAccount, UserAndAgentDialog userAndAgentDialog) throws CCPServiceException{
		StopMessage stopMessage = new StopMessage();
		stopMessage.setSid(userAndAgentDialog.getSid());
		stopMessage.setAppId(userAndAgentDialog.getAppId());
		stopMessage.setOsUnityAccount(userAndAgentDialog.getOsUnityAccount());
		stopMessage.setCreateTime(String.valueOf(System.currentTimeMillis()));
		stopMessage.setChanType(String.valueOf(userAndAgentDialog.getChanType()));
		
		List<String> agentIds = new ArrayList<String>();
		for(AgentInfo agentInfo:userAndAgentDialog.getAgentInfoSet()){
			agentIds.add(agentInfo.getAgentId());
		}
		stopMessage.setAgentIds(agentIds);
		
		String messageBody = stopMessage.toJson();
		
		String content = sendRequest(userAndAgentDialog.getMcm_notify_url(), messageBody);
		logger.info("end Ask return content: {}.", content);
		
		// 删除会话
		userAgentDialogRedisDao.deleteDialog(userAccount);
		userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
	}

	/**
	 * @Description: 获取响应内容
	 * @param body 
	 * @throws CCPServiceException
	 */
	private Object parseBody(String body) throws CCPServiceException{
		WakeupUserForm wakeupUserForm = null;
		if(StringUtils.isBlank(body)){
			throw new CCPServiceException(Constants.ERROR_MCM_IM_CCS_BODY_EMPTY);
		}
		
		logger.info("body: {}.", body);
		
		try {
			JSONObject jsonObject = JSONObject.fromObject(body);
			if(jsonObject.has(Constants.CC_RESPONSE_CODE)) {
				String code = jsonObject.getString(Constants.CC_RESPONSE_CODE);
				if(code.equals(Constants.RESPONSE_OK) && jsonObject.has(Constants.CC_RESPONSE_DATA)){
					String data = jsonObject.getString(Constants.CC_RESPONSE_DATA);
					if(StringUtils.isNotBlank(data)){
						wakeupUserForm = (WakeupUserForm)JSONUtil.jsonToObj(data, WakeupUserForm.class);
					} 
				} 
			}
		} catch (Exception e) {
			logger.error("parseBody#error()", e);
		}
		
		if(wakeupUserForm == null){
			logger.info("parser get wakeupUserForm is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_CCS_BODY_EMPTY);
		}
		
		return wakeupUserForm;
	}
	
	/**
	 * @Description: 发送http请求
	 * @param url
	 * @param messageBody
	 */
	private String sendRequest(String url, String messageBody) {
		HttpMethod httpMethod = HttpMethod.POST;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			Map<String, String> httpResult = httpClient.sendPacket(url, httpMethod, header, messageBody);
			String statusCode = httpResult.get("statusCode");
			String content = null;
			if(statusCode == null || (statusCode != null && Integer.parseInt(statusCode) >= 500)){
				for (int i = 1; i <= resendTimeNum; i++) {
					httpResult = httpClient.sendPacket(url, httpMethod, header,messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
						content = httpResult.get("content");
						break;
					}
				}
			}
			content = httpResult.get("content");
			
			if(statusCode == null || (statusCode!=null && Integer.parseInt(statusCode) >= 500)){
				return null;
			}else{
				return content;
			}
		}catch (CCPServiceException e) {
			logger.error("sendRequest#error()", e);
			return null;
		}
	}

	@Override
	public void userDisconnect(Connector connector, String userAccount, AppAttrs appAttrs, UserAndAgentDialog userAndAgentDialog)
			throws CCPServiceException {
		logger.info("as userDisconnect userAccount: {}.", userAccount);
		
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		if(agentInfoSet.isEmpty()){
			logger.info("agentInfoList is empty, start exit queue.");
		} else {
			logger.info("agentInfoList is not empty, start end server.");
			endAsk(userAccount, userAndAgentDialog);
		}
	}

	@Override
	public void wechatStartMessage(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException {
		String moduleCode = ScriptManager.getScriptManager().getBaseCache(Constants.KEY_CONFIG_MCM_SERVER_ID);
		String msgId = StringUtil.generateMessageMsgId(moduleCode,weixinMsg.getOpenID());
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() > 0){
			//回复微信消息
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
				weiXinMsgInfo.setContent("正在分配客服...");
				weixinService.sendWeiXinMsg(weiXinMsgInfo);
			}
			return;
			
		}else{
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, weixinMsg.getOpenID());
			
			//准备向AS侧发送开始咨询对象
			StartMessage startMessage = new StartMessage();
			startMessage.setSid(sid);
			startMessage.setOsUnityAccount(weixinMsg.getOpenID());
			startMessage.setAppId(weixinMsg.getAppId());
			startMessage.setChanType(String.valueOf(MCMChannelTypeInner.MCType_wx_VALUE));
			startMessage.setMsgId(msgId);
			startMessage.setCreateTime(String.valueOf(System.currentTimeMillis()));
			startMessage.setUserAccount(userAccount);
			
			String url = appAttrs.getMcm_notify_url();
			//向cc侧发送 HTTP请求
			String content = sendRequest(url, startMessage.toJson());
			
			if(StringUtils.isNotBlank(content)) {
				JSONObject jsonObject = JSONObject.fromObject(content);
				if(jsonObject.has(Constants.CC_RESPONSE_CODE)){
					String code = jsonObject.getString(Constants.CC_RESPONSE_CODE);
					if(Constants.RESPONSE_OK.equals(code)){
						Object obj = parseBody(content);
						WakeupUserForm wakeupUserForm = (WakeupUserForm) obj;
						String agentId = wakeupUserForm.getAgentId();
						String customaccnum = wakeupUserForm.getCustomaccnum();
						
						//回复微信消息
						String userId = StringUtil.getUserNameFormUserAcc(userAccount);
						sendWeixinMessage(userId, "您好, 工号: " + agentId + "为您服务");
						
						// 生成会话记录
						saveDialog(0, weixinMsg.getOpenID(), sid, userAccount, url, MCMChannelTypeInner.MCType_wx_VALUE, 
								weixinMsg.getAppId(), agentId, customaccnum);
					} 
				}
				
			} else {
				//回复微信消息
				sendWeixinMessage(weixinMsg.getUserID(), "座席暂时无法提供服务...");
			}
		}
	}

	@Override
	public void wakeUpUser(WakeupUserForm wakeupUserForm) throws CCPServiceException {
		String agentId = wakeupUserForm.getAgentId();
		logger.info("agentId: {}.", agentId);
		
		String sid = wakeupUserForm.getSid();
		String userAccount = userAgentDialogRedisDao.getUserAccBySid(sid);
		logger.info("sid: {}.", sid);
		
		String appId = wakeupUserForm.getAppId();
		logger.info("appId: {}.", appId);
		
		String customaccnum = wakeupUserForm.getCustomaccnum();
		logger.info("customaccnum: {}.", customaccnum);
		
		String customData = wakeupUserForm.getCustomData();
		logger.info("customData: {}.", customData);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && StringUtils.isNotBlank(agentId) && StringUtils.isNotBlank(customaccnum)){
			logger.info("@cmdWakeUpUser agent: {}-{} to userAccount: {}.", agentId, customaccnum, userAccount);
			
			cmdWakeUpResp(userAccount, customaccnum, agentId, userAndAgentDialog);
		} else{
			logger.info("{}: get userAndAgentDialog is null.", userAccount);
		}
	}
	
	/**
	 * @Description: 唤醒用户
	 * @param userAccount 用户userAcc
	 * @param customaccnum 分配的坐席账号，不含appId
	 * @param agentId 分配的坐席Id
	 * @param userAndAgentDialog 用户会话
	 * @throws CCPServiceException
	 */
	private void cmdWakeUpResp(String userAccount, String customaccnum, String agentId, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException{
		String appId = StringUtil.getAppIdFormUserAcc(userAccount);
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		String agentAccount = StringUtil.getUserAcc(appId, customaccnum);
		
		// 修改实时会话记录
		logger.info("wakeUp start modify user and agent dialog.");
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setAgentId(agentId);
		agentInfo.setAgentAccount(agentAccount);
		agentInfoSet.add(agentInfo);
		userAndAgentDialog.setAgentInfoSet(agentInfoSet);
		userAndAgentDialog.setQueueCount(0);
		userAgentDialogRedisDao.saveDialog(userAccount, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		
		// 给用户推送消息
		sendMessageToUser("", userAndAgentDialog.getChanType(), agentId, userAccount, appId);
	}
	
	/**
	 * @Description: 发送消息至用户侧
	 * @param userAndAgentDialog 会话记录
	 * @param welcome 欢迎语
	 * @param agentId 坐席
	 * @param userAccount 用户账号
	 * @throws CCPServiceException 
	 */
	private void sendMessageToUser(String welcome, int chanType, String agentId, String userAccount, String appId) 
			throws CCPServiceException {
		String moduleCode = ScriptManager.getScriptManager().getBaseCache(Constants.KEY_CONFIG_MCM_SERVER_ID);
		String msgId = StringUtil.generateMessageMsgId(Constants.M3C_SERIAL, moduleCode);
		
		MSGDataInner.Builder msgData = MSGDataInner.newBuilder();
		
		if(StringUtils.isNotBlank(welcome)){
			msgData.setMsgContent(welcome);
		}else{
			msgData.setMsgContent("您好, 工号: " + agentId + "为您服务");
		}
		msgData.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
		
		if(MCMChannelTypeInner.MCType_im_VALUE == chanType) { // IM
			MCMMessageInfo mcmMessageInfo = buildMCMMessageInfo(msgData.build(), -1, msgId, MCMEventDefInner.AgentEvt_SendMCM_VALUE, 
					null, agentId, chanType, appId);
			pushService.doPushMsg(userAccount, mcmMessageInfo);
		}else if(MCMChannelTypeInner.MCType_wx_VALUE == chanType){ // wechat
			String userId = StringUtil.getUserNameFormUserAcc(userAccount);
			sendWeixinMessage(userId, msgData.getMsgContent());
		}
	}
	
	/**
	 * @Description: 封装发送消息对象
	 * @param sendMsg  
	 * @param msgData
	 * @param connector
	 * @param version
	 * @param msgId
	 */
	private MCMMessageInfo buildMCMMessageInfo(MSGDataInner msgData, long version, String msgId, int mcmEvent, 
			String userAccount, String agentId, int chanType, String appId) {
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMsgId(msgId);
		mcmMessageInfo.setAppId(appId);
		//mcmMessageInfo.setVersion(version);	
		mcmMessageInfo.setMCMEvent(mcmEvent);
		mcmMessageInfo.setOsUnityAccount(Constants.OS_UNITY_ACCOUNT);
		
		if(StringUtils.isNotBlank(userAccount)){
			mcmMessageInfo.setUserAccount(userAccount);
		}
		
		if(StringUtils.isNotBlank(agentId)){
			mcmMessageInfo.setAgentId(agentId);
		}
		
		mcmMessageInfo.setChanType(chanType);
		
		mcmMessageInfo.setMsgType(msgData.getMsgType());
		mcmMessageInfo.setMsgContent(msgData.getMsgContent());
		mcmMessageInfo.setMsgFileName(msgData.getMsgFileName());
		mcmMessageInfo.setMsgFileUrl(msgData.getMsgFileUrl());
		mcmMessageInfo.setMsgDateCreated(String.valueOf(System.currentTimeMillis()));
		
		return mcmMessageInfo;
	}
	
	/**
	 * @Description: 发送响应通知 
	 * @param event 触发事件
	 * @param userAcc 接收消息对象
	 * @param protoClientNo 客户端请求流水号
	 * @param errorCode 请求错误码
	 * @throws CCPServiceException 
	 */
	private void sendNotifyResponseMessage(int event, String userAcc, int protoClientNo, int errorCode) 
			throws CCPServiceException{
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
	private void sendNotifyMessage(int event, String userAcc, String userName, Map<Integer, String> optResultMap, 
			int protoClientNo, int errorCode, CcpCustomData ccpCustomData, String agentId) throws CCPServiceException{
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
	 * @Description: 发送微信消息
	 * @param userAccount
	 * @param msgContent 
	 */
	private void sendWeixinMessage(String userId, String msgContent) {
		McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
		weiXinMsgInfo.setOpenID(Constants.OS_UNITY_ACCOUNT);
		weiXinMsgInfo.setUserID(userId);
		weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
		weiXinMsgInfo.setContent(msgContent);
		
		weixinService.sendWeiXinMsg(weiXinMsgInfo);
	}

	/**
	 * set inject 
	 */
	public void setUserAgentDialogRedisDao(UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setResendTimeNum(int resendTimeNum) {
		this.resendTimeNum = resendTimeNum;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setWeixinService(WeiXinGWService weixinService) {
		this.weixinService = weixinService;
	}
}
