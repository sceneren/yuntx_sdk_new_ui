package com.yuntongxun.mcm.genesys.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;

import com.yuntongxun.common.protobuf.ProtobufCodecManager;
import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.connection.ModuleProducter;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.core.protobuf.MsgLite;
import com.yuntongxun.mcm.core.protobuf.RegData.RegDataInner;
import com.yuntongxun.mcm.genesys.model.IxnLogin;
import com.yuntongxun.mcm.genesys.model.MsgJsonDataModel;
import com.yuntongxun.mcm.genesys.model.SipLogin;
import com.yuntongxun.mcm.genesys.service.GenesysAgentService;
import com.yuntongxun.mcm.genesys.service.IxnAgentService;
import com.yuntongxun.mcm.genesys.service.SipServerService;
import com.yuntongxun.mcm.genesys.util.FTPUtil;
import com.yuntongxun.mcm.genesys.util.HttpUtil;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.SpringUtil;

public class GenesysAgentServiceImpl implements GenesysAgentService{

	public static final Logger logger = LogManager.getLogger(GenesysAgentServiceImpl.class);
	
	private String ixnServerIP;
	
	private String ixnServerPort;
	
	private String tenantId;
	
	private String sipServerIP;

	private String sipServerPort;
	
	private int registerMessageSwitch;
	
	private ModuleProducter moduleProducter;
	
	private PushService pushService;
	
	private String fileServerUrl;
	
	private String genesyFileServerUrl;
	
	private Map<String, IxnAgentService> ixnAgentServiceMap = new ConcurrentHashMap<String, IxnAgentService>();
	
	private Map<String, SipServerService> sipServerServiceMap = new ConcurrentHashMap<String, SipServerService>();
	
	@Override
	public void onWork(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isEmpty(agentId)){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String nickName = sendMsg.getNickName();
		if(nickName == null || nickName.length() == 0){
			nickName = String.valueOf(agentId);
		}
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		int serviceCap = mjdm.getServiceCap();
		
		String chanType = "";
		if(serviceCap == 1){
			chanType = Constants.VOICE_TYPE;
		}else if(serviceCap == 2){
			chanType = Constants.CHAT_TYPE;
		}else if(serviceCap == 3){
			chanType = Constants.VIDYO_TYPE;
		}
		
		//连接ixnServer
		if(serviceCap == 2 || serviceCap == 3){
			String placeId = mjdm.getPlaceId();
			if(placeId == null || placeId.length() == 0){
				throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_PALCEID_EMPTY_ERROR);
			}
			
			IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
			
			if(ixnAgentService == null){
				IxnLogin ixnLogin = new IxnLogin();
    			ixnLogin.setAgentId(String.valueOf(agentId));
    			ixnLogin.setPlaceId(placeId);
    			ixnLogin.setChatType(chanType);
    			ixnLogin.setMedia(chanType);
    			ixnLogin.setUserAcc(userAcc);
    			ixnLogin.setNickName(nickName);
    			
    			logger.info("[" + userAcc+ "]start init and login ixn server, login params: " + ixnLogin.toString());
    			
				ixnAgentService = (IxnAgentService)SpringUtil.getBean("ixnAgentService");
				
				//初始化
				int flag = ixnAgentService.init(ixnLogin, ixnServerIP, ixnServerPort, tenantId);
				if(flag == 1){
					 //打开连接
    				 flag = ixnAgentService.connect();
    				 if(flag == 1){
    					//签入
		    			flag = ixnAgentService.login(null);
		    			if(flag == 1){
		    				ixnAgentServiceMap.put(userAcc, ixnAgentService);	
		    			}
    				 }else{
    					 throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_CONNECT_INTERACTION_SERVER_FAIL);
    				 }
				}
    			
			}else{
				sendNoticeMessage(48, "already login ixn server.", userAcc);
			}
		}
		
		//连接 TServer
		if(serviceCap == 1){
			String thisDN = mjdm.getThisDN();
			if(thisDN == null || thisDN.length() == 0){
				throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_DN_EMPTY_ERROR);
			}
			
			replyLoginAndlogoutMsg(userAcc, "", 3600, protoClientNo, connector);
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			
			String queue = mjdm.getQueue();
			String passWd = mjdm.getPasswd();
			String workMode = mjdm.getWorkMode();
			
			SipServerService sipServerService = sipServerServiceMap.get(userAcc);
			
			if(sipServerService == null){
				SipLogin sipLogin = new SipLogin();
				sipLogin.setAgentId(String.valueOf(agentId));
				sipLogin.setPasswd(passWd);
				sipLogin.setThisDN(thisDN);
				sipLogin.setQueue(queue);
				sipLogin.setUserAcc(userAcc);
				sipLogin.setWorkMode(workMode);
				
				logger.info("[" + userAcc + "]start init and login TServer, login params: " + sipLogin.toString());
				
				sipServerService =  (SipServerService)SpringUtil.getBean("sipServerService");
				
				//初始化
				int flag = sipServerService.init(sipLogin, sipServerIP, sipServerPort);
				if(flag == 1){
					//打开连接
    				flag = sipServerService.connect();
    				if(flag == 1){
    					//签入
    					flag = sipServerService.login(thisDN, queue, String.valueOf(agentId), passWd);
		    			if(flag == 1){
		    				sipServerServiceMap.put(userAcc, sipServerService);
		    			}
    				}
				}
			}else{
				sendNoticeMessage(48, "already login TServer.", userAcc);
			}
		}//if serviceCap
		
	}

	@Override
	public void offWork(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String queue = mjdm.getQueue();
		
		//ixn server
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		if(ixnAgentService != null){
			logger.info("[" + userAcc + "]logout ixn server, logout params: agentId[" + agentId + "], queue[" + queue + "]");
			
			//签出
			ixnAgentService.logout(null);
			
			//断开链接
			ixnAgentService.disconnect();
			
			//从缓存中去掉
			ixnAgentServiceMap.remove(userAcc);
		}
		
		//TServer
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			replyLoginAndlogoutMsg(userAcc, "", 0, protoClientNo, connector);
			
			logger.info("[" + userAcc + "]logout TServer, logout params: agentId[" + agentId + "], queue[" + queue + "]");
			
			//签出
			sipServerService.logout("", queue);
			
			//断开链接
			sipServerService.disconnect();
			
			//从缓存中去掉
			sipServerServiceMap.remove(userAcc);
		}
		
	}

	@Override
	public void ready(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		if(StringUtils.isEmpty(agentId)){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR);
		}
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String placeId = mjdm.getPlaceId();
		String queue = mjdm.getQueue();
		
		//ixn server
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		if(ixnAgentService != null){
			ixnAgentService.setReady(true);
			logger.info("[" + userAcc + "]ready ixn server, params: agentId[" + agentId + "], placeId[" + placeId + "].");
			ixnAgentService.ready(ixnAgentService.getIxnLogin().getChatType(), null);
		}else{
			logger.info("ixn server ready fail, because [" + userAcc + "] get ixnAgentService is null.");
		}
		
		//TServer
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc + "]ready TServer, params: agentId[" + agentId + "], placeId[" + placeId + "], queue[" + queue + "].");
			sipServerService.ready("voice", "", queue);
		}else{
			logger.info("TServer ready fail, because[" + userAcc + "] get sipServerService is null.");
		}
		
	}

	@Override
	public void notReady(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String placeId = mjdm.getPlaceId();
		String queue = mjdm.getQueue();
		
		//ixn server
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		if(ixnAgentService != null){
			ixnAgentService.setNotReady(true);
			logger.info("[" + userAcc + "]notReady ixn server, params: agentId[" + agentId + "], placeId[" + placeId + "].");
			ixnAgentService.notReady(ixnAgentService.getIxnLogin().getChatType(), "", "", null);
		}else{
			logger.info("ixn server notReady fail, because[" + userAcc + "] get ixnAgentService is null.");
		}
		
		//TServer
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc+ "]notReady TServer: agentId[" + agentId + "], placeId[" + placeId + "], queue[" + queue + "]");
			sipServerService.notReady("voice", "", "", "", queue);
		}else{
			logger.info("TServer notReady fail, because[" + userAcc + "] get sipServerService is null.");
		}
				
	}

	@Override
	public void startSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		
		String interactionId = sendMsg.getUserAccount();
		if(interactionId == null || interactionId.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_INTERACTIONID_EMPTY_ERROR);
		}
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String placeId = mjdm.getPlaceId();
		
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		if(ixnAgentService != null){
			if(ixnAgentService.getIxnLogin().getChatType().contains("chat")){
				logger.info("[" + userAcc + "]joinChatService, params: agentId[" + agentId + "], placeId[" 
						+ placeId + "], interactionId[" + interactionId + "]");
				//chat得先加入聊天室，视频不用加入聊天室，直接接起
				ixnAgentService.joinChatService(interactionId);
			}
			
			logger.info("[" + userAcc + "]accept, params: agentId[" + agentId + "], placeId[" 
					+ placeId + "], interactionId[" + interactionId + "]");
			
			ixnAgentService.accept(interactionId);
		}else{
			logger.info("accept fail, because[" + userAcc + "] get ixnAgentService is null.");
		}
	}

	@Override
	public void stopSerWithUser(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		
		String interactionId = sendMsg.getUserAccount();
		if(interactionId == null || interactionId.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_INTERACTIONID_EMPTY_ERROR);
		}
		
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String placeId = mjdm.getPlaceId();
		
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		if(ixnAgentService != null){
			logger.info("[" + userAcc + "]stop, params: agentId[" + agentId + "], placeId[" 
							+ placeId + "], interactionId[" + interactionId + "].");
			
			ixnAgentService.stop(interactionId, null);
		}else{
			logger.info("stop fail, because[" + userAcc + "] get ixnAgentService is null.");
		}
		
	}

	@Override
	public void sendMCM(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String agentId = sendMsg.getAgentId();
		String interactionId = sendMsg.getUserAccount();
		if(interactionId == null || interactionId.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_INTERACTIONID_EMPTY_ERROR);
		}
		 
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String placeId = mjdm.getPlaceId();
		
		logger.info("[" + userAcc + "]sendChatMessage: agentId[" + agentId + "], interationId[" 
				+ interactionId + "], placeId[" + placeId + "].");
		
		IxnAgentService ixnAgentService = ixnAgentServiceMap.get(userAcc);
		
		if(ixnAgentService != null){
			List<MSGDataInner> msgDatalist = sendMsg.getMSGDataList(); 
			
			if(msgDatalist.size() > 0){
				for (MSGDataInner object : msgDatalist) {
					int msgType = object.getMsgType();
					
					Map<String, String> map = new HashMap<String, String>();
					map.put("agentId", String.valueOf(agentId));
					
					if(MCMTypeInner.MCMType_file_VALUE == msgType //文件类型的消息
							|| MCMTypeInner.MCMType_emotion_VALUE == msgType //文件消息
							|| MCMTypeInner.MCMType_audio_VALUE == msgType  //语音消息 
							|| MCMTypeInner.MCMType_video_VALUE == msgType //视频消息
							|| msgType == 7){ //pc传文件，7为不加密
						String fileUrl = object.getMsgFileUrl();
						
						//从ytx文件服务器下载附件消息
						fileUrl = fileServerUrl + fileUrl;
	    				Map<String, InputStream> tempMap = HttpUtil.downloadFileGet(fileUrl);
	    				
	    				if(tempMap == null){
	    					//下载文件失败
	    					logger.info("from ytx fileserver get file[" + fileUrl + "] is null."); 
	    					
	    				}else{
	    					String returnUrl = "";
	    					for(String key : tempMap.keySet()){
	    						//上传到对应的genesys 文件服务器
	    						boolean isFlag = FTPUtil.getInstance().upload(key, tempMap.get(key));
			    				if(isFlag){
			    					returnUrl = genesyFileServerUrl + key;
			    					logger.info("upload[" + fileUrl + "] to genesys file server success, return url[" + returnUrl + "]");							    					
			    				}else{
			    					logger.info("upload[" + fileUrl + "] to genesys file server fail.");
			    				}
	    					}
	    					
		    				//"{mediaType:1,msg:"您好,eli,恒丰微信客服70002号竭诚为您服务。",agentid:"70002"}"  
		    				//mediaType  1是文本  2是图片  3是视频 4是语音
		    				if(MCMTypeInner.MCMType_audio_VALUE == msgType){
		    					map.put("mediaType", "4");
		    				}else if(MCMTypeInner.MCMType_emotion_VALUE == msgType){
		    					map.put("mediaType", "2");
		    				}else if(MCMTypeInner.MCMType_video_VALUE == msgType){
		    					map.put("mediaType", "3");
		    				}else{
		    					map.put("mediaType", "6");
		    				}
		    				
		    				map.put("msg", returnUrl);
	    				}// if tempMap
					}else{
						String message = object.getMsgContent();
						map.put("mediaType", "1");
						map.put("msg", message);
					}// if msgType
					
					String message = JSONUtil.map2json(map);
					ixnAgentService.sendChatMessage(interactionId, message, 0);
					
					logger.info("[" + userAcc + "]send message[" + message + "] to[" 
									+ interactionId + "].");
					
				}// for msgDatalist
			}// if msgDatalist
		}else{
			logger.info("send message fail, because [" + userAcc + "] get ixnAgentService is null.");
		}
	}

	@Override
	public void sendNotify(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		
	}

	@Override
	public void makeCall(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		String thisDN = mjdm.getThisDN();
		String otherDN = mjdm.getOtherDN();
		
		if(thisDN == null || thisDN.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_DN_EMPTY_ERROR);
		}
		
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc + "] makeCall, thisDN[" + thisDN + "], called[" + otherDN + "].");
			sipServerService.makeCall(otherDN, thisDN, null);
		}else{
			logger.info("makeCall fail, because[" + userAcc + "] get sipServerService is null.");
		}
		
	}

	@Override
	public void answerCall(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		
		String thisDN = mjdm.getThisDN();
		if(thisDN == null || thisDN.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_DN_EMPTY_ERROR);
		}
		
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc + "] answerCall, thisDN[" + thisDN + "].");
			sipServerService.answerCall2(thisDN, null);
		}else{
			logger.info("answerCall fail, because agent[" + userAcc + "] get sipServerService is null.");	
		}
	}

	@Override
	public void releaseCall(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		
		String thisDN = mjdm.getThisDN();
		if(thisDN == null || thisDN.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_DN_EMPTY_ERROR);
		}
		
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc + "] releaseCall, thisDN[" + thisDN + "].");
			sipServerService.releaseCall2(thisDN, null);
		}else{
			logger.info("releaseCall fail, because [" + userAcc + "] get sipServerService is null.");	
		}
		
	}

	@Override
	public void startConf(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String msgJsonData = sendMsg.getMsgJsonData();
		MsgJsonDataModel mjdm = getMsgJsonDataModelByJson(msgJsonData);
		
		String otherDN = mjdm.getOtherDN();
		if(otherDN == null || otherDN.length() == 0){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_DN_EMPTY_ERROR);
		}
		
		SipServerService sipServerService = sipServerServiceMap.get(userAcc);
		if(sipServerService != null){
			logger.info("[" + userAcc + "] completeConference, otherDN[" + otherDN + "].");
			sipServerService.completeConferenceInit("", "", otherDN);
		}else{
			logger.info("singleStepConference fail, because [" + userAcc + "] get sipServerService is null.");	
		}
	}

	/**
	* @Description: Genesys msgData 
	* @throws
	 */
	private MsgJsonDataModel getMsgJsonDataModelByJson(String msgJsonData) 
			throws CCPServiceException{
		if(msgJsonData == null || msgJsonData.length() == 0){
			return new MsgJsonDataModel();
		}
		MsgJsonDataModel mjdm = null;
		try {
			mjdm = (MsgJsonDataModel) JSONUtil.jsonToObj(msgJsonData, MsgJsonDataModel.class);
		} catch (Exception e) {
			logger.error("getMsgJsonDataModelByJson error", e);
		}
		if(mjdm == null){
			throw new CCPServiceException(Constants.ERROR_MCM_GENESYS_MSGJSONDATA_PARSER_ERROR);
		}
		logger.info(mjdm.toString());
		
		return mjdm;
	}
	
	/**
	 * 发送sip网关注册消息
	 * @params expires 3600签入 0签出
	 * @return
	 */
	private void replyLoginAndlogoutMsg(String userAcc, String passwd, int expires, 
			int protoClientNo, final Connector connector) {
		logger.info("[" + userAcc + "]start reply login or logout SIPGW, expires[" + expires + "].");
		
		//用户注册成功后，回复登录成功消息
		if(Constants.REGISTER_MESSAGE_SWITCH_ON.equals(registerMessageSwitch)){
			//先获取username
			//String userName = userAcc.split("#")[1];
			String userName = userAcc;
			RegDataInner.Builder builder = RegDataInner.newBuilder();
			if(StringUtils.isNotEmpty(userName)){
				builder.setUser(userName);
			}else{
				builder.setUser("");
			}
			if(StringUtils.isNotEmpty(passwd)){
				builder.setPassword(passwd);
			}else{
				builder.setPassword("");
			}
			builder.setExpires(expires);
			
			byte[] encodeData;
			try {
				encodeData = ProtobufCodecManager.encoder(builder.build());
				MsgLite.MsgLiteInner msgLiteResp = MsgLiteFactory.buildMsgLite(Constants.PROTOTYPE_REPLY_LOGIN_AND_LOGOUT, 
						encodeData, protoClientNo, connector.getSessionId(), Constants.SUCC);
				//String dest = "YTX_SIPGW_QUEUE";
				//moduleProducter.sendBytesMessage(dest, msgLiteResp);
				
				String dest = moduleProducter.getSimpleClient().getRoute(Constants.SIPGW_QUEUE, connector.getConnectorId());
				moduleProducter.sendBytesMessage(dest, msgLiteResp);
			} catch (IOException e) {
				logger.error("replyLoginAndlogoutMsg error", e);
			}
			
			logger.info("reply-message has already send to the [YTX_SIPGW_QUEUE], " +
					"userAcc:{" + userAcc + "},password:{" + passwd + "},expires:{" + expires + "}");
		}
	}
	
	/**
	 * @deprecat
	 * @param event
	 * @param des
	 * @param userAcc
	 */
	private void sendNoticeMessage(int event, String des, String userAcc){
		try{
			MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
			
			mcMessageInfo.setCCSType(1);
			mcMessageInfo.setMCMEvent(event);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("optResult", "1");
			map.put("optRetDes", des);
			
			mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
			pushService.doPushMsg(userAcc, mcMessageInfo);
		}catch(Exception e){
			logger.info("sendErrorMessage error" , e);
		}
	}
	
	public void destroy(){
		logger.info("start destroy ixn server And Tserver.");
		
		if(ixnAgentServiceMap != null){
			for(String key : ixnAgentServiceMap.keySet()){
				IxnAgentService IxnAgentService = ixnAgentServiceMap.get(key);
				if(IxnAgentService != null){
					IxnAgentService.logout(null);
					IxnAgentService.disconnect();
				}
			}
		}
		if(sipServerServiceMap != null){
			for(String key : sipServerServiceMap.keySet()){
				SipServerService sipServerService = sipServerServiceMap.get(key);
				if(sipServerService != null){
					sipServerService.disconnect();
				}
			}
		}
		
		logger.info("destroy ixn server And Tserver end.");
	}

	/**
	 * set inject
	 */
	public void setIxnServerIP(String ixnServerIP) {
		this.ixnServerIP = ixnServerIP;
	}

	public void setIxnServerPort(String ixnServerPort) {
		this.ixnServerPort = ixnServerPort;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public void setSipServerIP(String sipServerIP) {
		this.sipServerIP = sipServerIP;
	}

	public void setSipServerPort(String sipServerPort) {
		this.sipServerPort = sipServerPort;
	}

	public void setRegisterMessageSwitch(int registerMessageSwitch) {
		this.registerMessageSwitch = registerMessageSwitch;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setFileServerUrl(String fileServerUrl) {
		this.fileServerUrl = fileServerUrl;
	}

	public void setGenesyFileServerUrl(String genesyFileServerUrl) {
		this.genesyFileServerUrl = genesyFileServerUrl;
	}

	public void setModuleProducter(ModuleProducter moduleProducter) {
		this.moduleProducter = moduleProducter;
	}
	
}
