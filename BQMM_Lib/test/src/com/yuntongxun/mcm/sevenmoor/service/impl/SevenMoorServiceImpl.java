package com.yuntongxun.mcm.sevenmoor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpMethod;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.mcm.model.MsgJsonData;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.sevenmoor.model.JsonMsg;
import com.yuntongxun.mcm.sevenmoor.model.SdkLoginJsonData;
import com.yuntongxun.mcm.sevenmoor.model.SdkNewMsgData;
import com.yuntongxun.mcm.sevenmoor.model.SdkNewMsgJsonData;
import com.yuntongxun.mcm.sevenmoor.model.SevenMoorLoginRInfo;
import com.yuntongxun.mcm.sevenmoor.model.SevenMoorMsg;
import com.yuntongxun.mcm.sevenmoor.model.TransferData;
import com.yuntongxun.mcm.sevenmoor.service.SevenMoorService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.JsonUtils;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.RedisKeyConstant;
import com.yuntongxun.mcm.util.StringUtil;

public class SevenMoorServiceImpl implements SevenMoorService {

	public static final Logger logger = LogManager.getLogger(SevenMoorServiceImpl.class);
	
	private ExecutorService executorService;
	
	private String sevenMoorNodeServerUrl;
	
	private HttpClient httpClient;
	
	private PushService pushService;
	
	private BaseRedisDao baseRedisDao;
	
	private String sevenmoorAccessId;

	private String localServerUrl;
	
	private VersionDao versionDao;

	public void init() {
		executorService = Executors.newFixedThreadPool(32);
	}
	
	@Override
	public void processAction(TransferData transferData) throws CCPServiceException {
		String actionName = transferData.getAction();
		logger.info("actionName: {}.", actionName);
		
		if(Constants.SEVEN_MOOR_ACTION_NAME_NEW_MESSAGE.equals(actionName)){
			sdkGetMsg(transferData);
		} else if(Constants.SEVEN_MOOR_ACTION_NAME_NEW_SYS_MESSAGE.equals(actionName)){
			if(Constants.SEVEN_MOOR_SYS_MSG_TYPE_FINISH.equals(transferData.getType())){
				agentEndAsk(transferData);
			}
		}
	}
	
	@Override
	public void sdkLogin(final MCMDataInner sendMsg, final Connector connector, final String userAcc, final int protoClientNo) {
		logger.info("@7moor sdkLogin");
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_login");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("7moor sdkLogin");
				try {
					//先从redis查询当前useracc的用户是否存在
					String connectionId = baseRedisDao.getRedisValue(RedisKeyConstant.YTX_7MOOR_LOGIN_INFO + userAcc);
					SevenMoorLoginRInfo sevenMoorLoginRInfo = null;
					TransferData requestData = new TransferData();
					SdkLoginJsonData loginJsonData = new SdkLoginJsonData();
					if(StringUtils.isNotEmpty(connectionId)){
						requestData.setConnectionId(connectionId);
						requestData.setKey(connectionId);
						requestData.setIsNewVisitor(false);
						
						loginJsonData.setKey(connectionId);
						
						String sevenMoorLoginRInfoValue = baseRedisDao.getRedisValue(RedisKeyConstant.YTX_7MOOR_LOGIN_INFO_R + connectionId);
						if(StringUtils.isNotEmpty(sevenMoorLoginRInfoValue)){
							sevenMoorLoginRInfo = (SevenMoorLoginRInfo)JsonUtils.jsonToObj(sevenMoorLoginRInfoValue, SevenMoorLoginRInfo.class);
						}
						
					} else {
						String key = StringUtil.getUUID();
						requestData.setConnectionId(key);
						requestData.setKey(key);
						requestData.setIsNewVisitor(true);
						loginJsonData.setKey(key);
					}
					
					MsgJsonData jsonData = (MsgJsonData)JsonUtils.jsonToObj(sendMsg.getMsgJsonData(), MsgJsonData.class);
					
					//String userId = connector.getAppId() + "-" + connector.getUserName();
					StringBuilder sBuilder = new StringBuilder();
					sBuilder.append(connector.getAppId()).append("-").append(connector.getUserName());
					String userId = sBuilder.toString();
					
					loginJsonData.setCommand("Action");
					loginJsonData.setNewVersion("true");
					
					requestData.setAccessId(jsonData.getAccessId());
					loginJsonData.setAccessId(jsonData.getAccessId());
					
					requestData.setUserId(userId);
					loginJsonData.setUserId(userId);
					
					String userName = sendMsg.hasUserData() ? sendMsg.getUserData() : "";
					
					loginJsonData.setUserName(userName);
					requestData.setDeviceId(connector.getDeviceNo());
					loginJsonData.setDeviceId(connector.getDeviceNo());

					requestData.setConnectServer(localServerUrl);
					loginJsonData.setConnectServer(localServerUrl);
					
					String actionId = StringUtil.getUUID();
					requestData.setActionId(actionId);
					loginJsonData.setActionId(actionId);

					requestData.setIoSessionId(6);
					loginJsonData.setIoSessionId(6);
					
					requestData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_LOGIN);
					loginJsonData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_LOGIN);
					
					if(Integer.parseInt(connector.getDeviceType()) %2 == 0){
						requestData.setPlatform("ios");
						loginJsonData.setPlatform("ios");
					}else{
						requestData.setPlatform("android");
						loginJsonData.setPlatform("android");
					}
					
					JsonMsg jsonMsg = new JsonMsg();
					jsonMsg.setData(JsonUtils.toJson(loginJsonData));

					Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, JsonUtils.toJson(jsonMsg));
					
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);

					if (Constants.SUCC == Integer.parseInt(statusCode)) {
						logger.info("login to 7moor node server success");
						
						if (StringUtils.isNotEmpty(content)) {
							TransferData responseData = (TransferData) JsonUtils.jsonToObj(content, TransferData.class);
							MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
							mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
							
							// 向SDK推送登录成功消息
							pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);

							// 获取用户满意度（模拟七陌SDK步骤，必须调用，不然客服无法接到看到用户会话）
							sdkGetInvestigate(requestData,protoClientNo,userAcc);
							
							// 开启新会话（模拟七陌SDK步骤，必须调用，不然客服无法接到看到用户会话）
							sdkBeginNewChatSession(requestData);

							// 需要持久化到数据库（redis）失效时间24小时
							/*Map<byte[], byte[]> msetMap = new Hashtable<byte[], byte[]>();
							msetMap.put((RedisKeyConstant.YTX_7MOOR_LOGIN_INFO + userAcc).getBytes(), requestData.getConnectionId().getBytes());
							if(sevenMoorLoginRInfo == null){
								sevenMoorLoginRInfo = new SevenMoorLoginRInfo();
							}
							sevenMoorLoginRInfo.setUserAcc(userAcc);
							msetMap.put((RedisKeyConstant.YTX_7MOOR_LOGIN_INFO_R + requestData.getConnectionId())
									.getBytes(), JsonUtils.bean2json(sevenMoorLoginRInfo).getBytes());
							baseRedisDao.mset(msetMap);*/
							
							sBuilder = new StringBuilder();
							sBuilder.append(RedisKeyConstant.YTX_7MOOR_LOGIN_INFO).append(userAcc);
							String key =  sBuilder.toString();
							baseRedisDao.saveRedisValue(key, requestData.getConnectionId(), Constants.SEVEN_MOOR_LOGIN_EXPIRE);
							
							if(sevenMoorLoginRInfo == null){
								sevenMoorLoginRInfo = new SevenMoorLoginRInfo();
							}
							sevenMoorLoginRInfo.setUserAcc(userAcc);
							
							sBuilder = new StringBuilder();
							sBuilder.append(RedisKeyConstant.YTX_7MOOR_LOGIN_INFO_R).append(requestData.getConnectionId());
							key =  sBuilder.toString();
							baseRedisDao.saveRedisValue(key, JsonUtils.bean2json(sevenMoorLoginRInfo), Constants.SEVEN_MOOR_LOGIN_EXPIRE);
							
						} else {
							MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
							pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_LOGIN));
						}
						
					} else {
						logger.info("login to 7moor node server error");
						
						MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
						pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_LOGIN));
					}
				} 
				catch (CCPServiceException e) {
					logger.error("sdkLogin to 7moor error, CCPServiceException:{}", e);
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					try {
						pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_LOGIN));
					} 
					catch (NumberFormatException | CCPServiceException e1) {
						logger.error("sdkLogin catch CCPServiceException,push error msg to sdk exception{}", e1);
					}
				} 
				catch (Exception e) {
					logger.error("sdkLogin to 7moor error, exception:{}", e);
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					try {
						pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_LOGIN));
					} 
					catch (NumberFormatException | CCPServiceException e1) {
						logger.error("sdkLogin catch Exception,push error msg to sdk exception{}", e1);
					}
				} 
				finally {
					PrintUtil.printEndTag("login7moor");
					ThreadContext.removeStack();
				}
			}
		});
		
	}

	@Override
	public void sdkNewMsg(final MCMDataInner sendMsg,final Connector connector, final String userAcc,
			final int protoClientNo) throws CCPServiceException {
		logger.info("@7moor sdkNewMsg");
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_sendMsg");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("sdkNewMsg");
				
				try {
					// 根据userAcc获取用户登录数据
					String connectionId = baseRedisDao.getRedisValue(RedisKeyConstant.YTX_7MOOR_LOGIN_INFO + userAcc);
					if (StringUtils.isEmpty(connectionId)) {
						logger.error("send msg to 7moor node server error,get connectionId from redis error,connectionId is empty.");
						
						MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
						mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
						pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_LOGIN_INFO_EMPTY));
						return;
					}
					
					SdkNewMsgJsonData jsonData = new SdkNewMsgJsonData();
					jsonData.setCommand("Action");
					jsonData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_NEWMSG);
					jsonData.setConnectionId(connectionId);
					jsonData.setActionID(Constants.SEVEN_MOOR_ACTION_NAME_SDK_NEWMSG + System.currentTimeMillis());
					String userData = sendMsg.hasUserData() ? sendMsg.getUserData() : "";
					jsonData.setUserData(userData);
					
					for (MSGDataInner msgData : sendMsg.getMSGDataList()) {
						//文本消息处理
						if (MCMTypeInner.MCMType_txt_VALUE == msgData.getMsgType()) {
							jsonData.setContentType(Constants.SEVEN_MOOR_MSG_TYPE_TEXT);
							String content = StringUtil.replaceBlank(msgData.getMsgContent());
							content = content.replace("\"", "%22");
							content = content.replace("\\", "%5C");
							jsonData.setMessage(content);
						} else if (MCMTypeInner.MCMType_emotion_VALUE == msgData.getMsgType()) {
							//图片
							jsonData.setContentType(Constants.SEVEN_MOOR_MSG_TYPE_IMAGE);
							jsonData.setMessage(msgData.getMsgFileUrl());
						} else if (MCMTypeInner.MCMType_audio_VALUE == msgData.getMsgType()) {
							//语音
							jsonData.setContentType(Constants.SEVEN_MOOR_MSG_TYPE_VOICE);
							jsonData.setMessage(msgData.getMsgFileUrl());
							jsonData.setVoiceSecond(msgData.getVoiceSecond());
						}
							
						SdkNewMsgData data = new SdkNewMsgData();
						data.setData(JsonUtils.toJson(jsonData));
						
						String jsonMsg = JsonUtils.toJson(data);

						Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
						
						String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
						String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
						
						TransferData respData = (TransferData)JsonUtils.jsonToObj(content, TransferData.class);
						if (Constants.SUCC == Integer.parseInt(statusCode)) {
							if(respData.isSucceed()){
								logger.info("send msg to 7moor node server success");
								
								MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
								mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
								pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
								
							} else{
								logger.error("send msg to 7moor node server error,Message:{}",respData.getMessage());
								
								MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
								mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
								pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_SEND_MSG));
							}
						} else {
							logger.error("send msg to 7moor node server error,statusCode:{}",statusCode);
							
							MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
							mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
								
							pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_SEND_MSG));
						}
					}
				} 
				catch (Exception e) {
					logger.error("send7moorMsg error,exception:{}",e);
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
					try {
						pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_SEND_MSG));
					} catch (NumberFormatException | CCPServiceException e1) {
						logger.error("send7moorMsg catch Exception,push error msg to sdk exception{}", e1);
					}
				} 
				finally {
					PrintUtil.printEndTag("sdkNewMsg");
					ThreadContext.removeStack();
				}
			}
		});
		
	}
	
	@Override
	public void sdkGetInvestigate(TransferData transferData,int protoClientNo,String userAcc) {
		logger.info("@7moor sdkGetInvestigate");
		try {
			transferData.setConnectionId(transferData.getKey());
			transferData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_GET_INVESTIGATE);
			String jsonMsg = transferData.toSdkGetInvestigateJson();

			Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
			
			String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
			String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
			
			TransferData respData = (TransferData)JsonUtils.jsonToObj(content,TransferData.class);

			if (Constants.SUCC == Integer.parseInt(statusCode)) {
				if(respData.isSucceed()){
					//将满意度数据通知到SDK
					logger.info("sdkGetInvestigate to 7moor node server success");
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_InvestigateData_VALUE);
					mcmMessageInfo.setMsgJsonData(JsonUtils.bean2json(respData.getList()));
					pushService.doPushMsg(userAcc, mcmMessageInfo, -1, Constants.SUCC);
				}else{
					logger.error("sdkGetInvestigate to 7moor node server error,Message:{}",respData.getMessage());
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_InvestigateData_VALUE);
					pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo,
							Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_GET_INVESTIGATE));
				}
			} else {
				logger.error("sdkGetInvestigate to 7moor node server error,statusCode:{}",statusCode);
				MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
				mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_InvestigateData_VALUE);
				pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo,
						Integer.parseInt(Constants.ERROR_MCM_IM_7MOOR_GET_INVESTIGATE));
			}
			
		} catch (CCPServiceException e) {
			logger.error("sdkGetInvestigate to 7moor error:{}",e);
		}
	}

	@Override
	public void sdkBeginNewChatSession(TransferData transferData) {
		logger.info("@7moor sdkBeginNewChatSession");
		transferData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_BEGIN_NEW_CHAT_SESSION);
		String jsonMsg = transferData.tosdkBeginNewChatSessionJson();

		Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
		
		String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
		String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
		logger.info("sdkBeginNewChatSession to 7moor result：{},message:{}",statusCode,content);
	}

	@Override
	public void sdkGetMsg(final TransferData transferData) {
		logger.info("@7moor sdkGetMsg");
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_sdkGetMsg");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("7moor sdkGetMsg");
				try {
					
					//根据connectionId获取该用户数据
					String key = RedisKeyConstant.YTX_7MOOR_LOGIN_INFO_R + transferData.getConnectionId();
					String value = baseRedisDao.getRedisValue(key);
					logger.info("sdkGetMsg redis key:{},value:{}",key,value);
					if(StringUtils.isEmpty(value)){
						logger.error("sdkGetMsg to 7moor error,logininfo is empty,key:{}",key);
						return;
					}
					
					SevenMoorLoginRInfo sevenMoorLoginRInfo = (SevenMoorLoginRInfo)JsonUtils.jsonToObj(value, SevenMoorLoginRInfo.class);
					for(int i=0;i<sevenMoorLoginRInfo.getReceivedMsgIds().size();i++){
						logger.info("sdkGetMsg ReceivedMsgIds:{}",sevenMoorLoginRInfo.getReceivedMsgIds().get(i));
					}
					
					TransferData requestData = new TransferData();
					requestData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_GETMSG);
					requestData.setConnectionId(transferData.getConnectionId());
					requestData.setReceivedMsgIds(sevenMoorLoginRInfo.getReceivedMsgIdsString());
					logger.info("sdkGetMsg ReceivedMsgIds:{}",sevenMoorLoginRInfo.getReceivedMsgIdsString());
					
					String jsonMsg = requestData.toGetMsgJson();

					Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
					
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);

					if (Constants.SUCC == Integer.parseInt(statusCode)) {
						logger.info("sdkGetMsg to 7moor node server success");
						
						if (StringUtils.isNotEmpty(content)) {
							TransferData respData = (TransferData) JsonUtils.jsonToObj(content, TransferData.class);
							
							if(respData.isSucceed() && respData.getData() != null && respData.getData().size() > 0){
								sevenMoorLoginRInfo.setReceivedMsgIds(new ArrayList<String>());
								
								MessageInfo messageInfo = null;
								String userAcc = sevenMoorLoginRInfo.getUserAcc();
								
								logger.info("respData size: {}.", respData.getData().size());
								
								// 向SDK发送消息
								for(SevenMoorMsg tempMsg : respData.getData()){
									messageInfo = new MessageInfo();
									
									long version = versionDao.getMessageVersion(userAcc);
									logger.info("version: {}.", version);
									
									messageInfo.setMcmEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE);
									messageInfo.setVersion(version);
									messageInfo.setMsgId(tempMsg.get_id());
									messageInfo.setDateCreated(String.valueOf(System.currentTimeMillis()));
									String channelType = String.valueOf(MCMChannelTypeInner.MCType_im_VALUE);
									messageInfo.setExtOpts(EncryptUtil.base64Encoder("{\"channelType\":\"" + channelType + "\"}"));
									
									if(Constants.SEVEN_MOOR_MSG_TYPE_TEXT.equals(tempMsg.getContentType())){
										messageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
										messageInfo.setMsgContent(tempMsg.getContent());
										
									} else if(Constants.SEVEN_MOOR_MSG_TYPE_IMAGE.equals(tempMsg.getContentType())){
										messageInfo.setMsgType(MCMTypeInner.MCMType_emotion_VALUE);
										messageInfo.setFileUrl(tempMsg.getContent());
									}
									
									messageInfo.setMsgSender(Constants.OS_UNITY_ACCOUNT);
									
									// 向SDK发送消息
									boolean result = pushService.doPushMsgFor7Moor(userAcc, messageInfo);
									if(result){
										sevenMoorLoginRInfo.getReceivedMsgIds().add(tempMsg.get_id());
									}
								}
								
								baseRedisDao.saveRedisValue(key, JsonUtils.bean2json(sevenMoorLoginRInfo));
							}
							
						} else {
							logger.error("sdkGetMsg to 7moor node server error,content:{}",content);
						}
						
					} else {
						logger.error("sdkGetMsg to 7moor node server error,statusCode:{}",statusCode);
					}
					
				} 
				catch (CCPServiceException e) {
					logger.error("sdkGetMsg to 7moor error,exception:{}", e);
				}
				finally {
					PrintUtil.printEndTag("login7moor");
					ThreadContext.removeStack();
				}
			}
		});
		
	}
	
	private void agentEndAsk(TransferData transferData) throws CCPServiceException {
		String key = RedisKeyConstant.YTX_7MOOR_LOGIN_INFO_R+transferData.getConnectionId();
		String value = baseRedisDao.getRedisValue(key);
		
		logger.info("agentEndAsk redis key:{},value:{}",key,value);
		
		if(StringUtils.isEmpty(value)){
			logger.error("agentEndAsk to 7moor error,logininfo is empty,key:{}",key);
			return;
		}
		
		SevenMoorLoginRInfo sevenMoorLoginRInfo = (SevenMoorLoginRInfo)JsonUtils.jsonToObj(value, SevenMoorLoginRInfo.class);
		
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_EndAsk_VALUE);
		pushService.doPushMsg(sevenMoorLoginRInfo.getUserAcc(), mcmMessageInfo, -1, Constants.SUCC);
	}

	@Override
	public void submitInvestigate(final MCMDataInner sendMsg, final Connector connector, final String userAcc,final int protoClientNo) {
		logger.info("@submitInvestigate");
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_submitInvestigate");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("7moor submitInvestigate");
				try {
					
					//根据connectionId获取该用户数据
					String key = RedisKeyConstant.YTX_7MOOR_LOGIN_INFO+userAcc;
					String value = baseRedisDao.getRedisValue(key);
					logger.info("submitInvestigate redis key:{},value:{}",key,value);
					if(StringUtils.isEmpty(value)){
						logger.error("submitInvestigate to 7moor error,logininfo is empty,key:{}",key);
						return;
					}
					String connectionId = value;
					
					TransferData requestData = (TransferData)JsonUtils.jsonToObj(sendMsg.getMsgJsonData(), TransferData.class);
					requestData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_SDK_SUBMIT_INVESTIGATE);
					requestData.setConnectionId(connectionId);

					String jsonMsg = requestData.toSubmitInvestigateJson();

					Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
					
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					
					logger.info("submitInvestigate result:{}",content);
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
					pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
				} 
				catch (CCPServiceException e) {
					logger.error("submitInvestigate to 7moor error,exception:{}", e);
				} 
				finally {
					PrintUtil.printEndTag("submitInvestigate 7moor");
					ThreadContext.removeStack();
				}
			}
		});
		
	}

	@Override
	public void endAsk(final MCMDataInner sendMsg, final Connector connector, final String userAcc, final int protoClientNo) {
		//向七陌发送结束会话请求
		logger.info("@endAsk");
		final String sessionId = ThreadContext.peek();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_endAsk");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("7moor endAsk");
				try {
					
					//根据userAcc获取该用户数据
					String key = RedisKeyConstant.YTX_7MOOR_LOGIN_INFO+userAcc;
					String value = baseRedisDao.getRedisValue(key);
					logger.info("endAsk redis key:{},value:{}",key,value);
					if(StringUtils.isEmpty(value)){
						logger.error("endAsk to 7moor error,logininfo is empty,key:{}",key);
						return;
					}
					String connectionId = value;
					
					TransferData requestData = new TransferData();
					requestData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_CONNECTION_BREAK);
					requestData.setConnectionId(connectionId);
					requestData.setActionId("connectionBreak"+System.currentTimeMillis());

					String jsonMsg = requestData.toUserEndAskJson();

					Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
					
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					
					logger.info("sdkGetInvestigate result:{}",content);
					MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
					mcmMessageInfo.setMCMEvent(sendMsg.getMCMEvent());
					pushService.doPushMsg(userAcc, mcmMessageInfo, protoClientNo, Constants.SUCC);
					
				} 
				catch (CCPServiceException e) {
					logger.error("endAsk to 7moor error,exception:{}", e);
				}
				finally {
					PrintUtil.printEndTag("endAsk 7moor");
					ThreadContext.removeStack();
				}
			}
		});
		
	}
	
	@Override
	public void userDisconnect(final String connectionId) {
		//向七陌发送结束会话请求
		logger.info("@userDisconnect");
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("SeverMoorServiceThread_userDisconnect");
				ThreadContext.push(sessionId);
				PrintUtil.printStartTag("7moor userDisconnect");
				try {
					TransferData requestData = new TransferData();
					requestData.setAction(Constants.SEVEN_MOOR_ACTION_NAME_CONNECTION_BREAK);
					requestData.setConnectionId(connectionId);
					requestData.setActionId("connectionBreak"+System.currentTimeMillis());

					String jsonMsg = requestData.toUserEndAskJson();

					Map<String, String> httpResult = sendRequest(sevenMoorNodeServerUrl, jsonMsg);
					
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					logger.info("userDisconnect result:{}",content);
					
				} 
				catch (Exception e) {
					logger.error("userDisconnect to 7moor error,exception:{}", e);
				}
				finally {
					PrintUtil.printEndTag("userDisconnect 7moor");
					ThreadContext.removeStack();
				}
			}
		});
		
	}
	
	/**
	 * @Description: 发送http请求
	 * @param url
	 * @param messageBody
	 */
	private Map<String, String> sendRequest(String url, String messageBody) {
		HttpMethod httpMethod = HttpMethod.POST;
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			Map<String, String> httpResult = httpClient.sendPacket(url, httpMethod, header, messageBody);
			String statusCode = httpResult.get("statusCode");
			if(statusCode == null || !statusCode.equals(String.valueOf(Constants.SUCC))){
				for (int i = 1; i <= 3; i++) {
					logger.info("send request return statusCode[{}] error, retry count: {}.", statusCode, i);
					httpResult = httpClient.sendPacket(url, httpMethod, header,messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode != null && statusCode.equals(String.valueOf(Constants.SUCC))){
						break;
					}
				}
			}
			
			return httpResult;
		}catch (CCPServiceException e) {
			logger.error("sendRequest#error()", e);
			return null;
		}
	}
	
	/**
	 * set inject
	 */
	public void setSevenMoorNodeServerUrl(String sevenMoorNodeServerUrl) {
		this.sevenMoorNodeServerUrl = sevenMoorNodeServerUrl;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	public void setSevenmoorAccessId(String sevenmoorAccessId) {
		this.sevenmoorAccessId = sevenmoorAccessId;
	}

	public void setLocalServerUrl(String localServerUrl) {
		this.localServerUrl = localServerUrl;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}
}
