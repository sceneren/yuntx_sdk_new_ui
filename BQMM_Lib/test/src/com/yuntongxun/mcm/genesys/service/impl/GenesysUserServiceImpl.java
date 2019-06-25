package com.yuntongxun.mcm.genesys.service.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.genesys.model.ChatUserLogin;
import com.yuntongxun.mcm.genesys.service.ChatUserService;
import com.yuntongxun.mcm.genesys.service.GenesysUserService;
import com.yuntongxun.mcm.genesys.util.FTPUtil;
import com.yuntongxun.mcm.genesys.util.HttpUtil;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.util.SpringUtil;
import com.yuntongxun.mcm.util.StringUtil;

public class GenesysUserServiceImpl implements GenesysUserService{

	public static final Logger logger = LogManager.getLogger(GenesysUserServiceImpl.class);
	
	private String fileServerUrl;
	
	private String genesyFileServerUrl;
	
	private Map<String, ChatUserService> chatUserServiceMap = new ConcurrentHashMap<String, ChatUserService>();
	
	@Override
	public void startAsk(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		String nickName = sendMsg.getNickName();
		if(nickName == null || nickName.length() == 0){
			nickName = StringUtil.getUserNameFormUserAcc(userAcc);
		}
		
		String userAccount = sendMsg.getUserAccount();
		
		ChatUserService chatUserService = chatUserServiceMap.get(userAcc);
		if(chatUserService == null){
			logger.info("userAccount: {" + userAccount + "}");
			ChatUserLogin oul = new ChatUserLogin();
			oul.setUserAcc(userAcc);
			oul.setOsUnityAccount(userAccount);
			oul.setNickName(nickName);
			
			chatUserService = (ChatUserService)SpringUtil.getBean("chatUserService");
			
			chatUserService.init(oul, "", "Resources:default", 0);
			
			chatUserServiceMap.put(userAcc, chatUserService);
		}else{
			logger.info("[" + userAcc + "]get chatUserService is not null.");
		}
	}

	@Override
	public void endAsk(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		ChatUserService chatUserService = chatUserServiceMap.get(userAcc);
		if(chatUserService != null){
			chatUserService.disconnect();
			
			chatUserServiceMap.remove(userAcc);
		}else{
			logger.info("[" + userAcc + "]get chatUserService is null.");
		}
	}

	@Override
	public void sendMSG(MCMDataInner sendMsg, Connector connector, String userAcc)
			throws CCPServiceException {
		ChatUserService chatUserService = chatUserServiceMap.get(userAcc);
		if(chatUserService != null){
				List<MSGDataInner> msgDatalist = sendMsg.getMSGDataList(); 
				
				if(msgDatalist.size() > 0){
					for (MSGDataInner object : msgDatalist) {
						int msgType = object.getMsgType();
						
						Map<String, String> map = new HashMap<String, String>();
						
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
						
						chatUserService.sendChatMessage(message);
						
					}// for msgDatalist
					
				}// if msgDatalist
		}else{
			logger.info("[" + userAcc + "]get chatUserService is null.");
		}
	}
	
	public void destroy(){
		logger.info("start destroy chat server.");
		
		if(chatUserServiceMap != null){
			for(String key : chatUserServiceMap.keySet()){
				ChatUserService chatUserService = chatUserServiceMap.get(key);
				if(chatUserService != null){
					chatUserService.disconnect();
				}
			}
		}
		
		logger.info("destroy chat server end.");
	}
	/**
	 * set inject
	 */
	public void setFileServerUrl(String fileServerUrl) {
		this.fileServerUrl = fileServerUrl;
	}

	public void setGenesyFileServerUrl(String genesyFileServerUrl) {
		this.genesyFileServerUrl = genesyFileServerUrl;
	}
	
}
