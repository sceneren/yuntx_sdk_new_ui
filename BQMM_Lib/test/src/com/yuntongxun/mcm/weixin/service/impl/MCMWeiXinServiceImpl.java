package com.yuntongxun.mcm.weixin.service.impl;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.ming.sample.json.JSONUtil;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.dao.AppRedisDao;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.fileserver.util.FileServerUtils;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.weixin.WeiXinConfigData;
import com.yuntongxun.mcm.weixin.WeiXinConfiguration;
import com.yuntongxun.mcm.weixin.WeiXinPushMsg;
import com.yuntongxun.mcm.weixin.WeiXinResponseData;
import com.yuntongxun.mcm.weixin.WeiXinSendMsg;
import com.yuntongxun.mcm.weixin.service.MCMWeiXinService;
import com.yuntongxun.mcm.weixin.util.WeiXinConstant;
import com.yuntongxun.mcm.weixin.util.WeiXinUtils;

/**
 * 负责推送消息的业务层
 * 
 * @author weily
 */
public class MCMWeiXinServiceImpl implements MCMWeiXinService {

	public static final Logger logger = LogManager.getLogger(MCMWeiXinServiceImpl.class);

	private String weixinVerifyToken;
	
	private HttpClient httpClient;
	
	private FileServerService fileServerService;
	
	private AppRedisDao appRedisDao;
	
	private AsService asService;

	@Override
	public String verifyMsg(String signature, String timestamp, String nonce,
			String echostr) throws CCPServiceException {
		String result = null;
		
		boolean isValidMsg = WeiXinUtils.isValidMsg(signature, timestamp, nonce, weixinVerifyToken);
		if(isValidMsg){
			result = echostr;
		}else{
			result = "invalid msg";
		}
		return result;
	}

	@Override
	public void pushMsg(WeiXinPushMsg pushMsg) throws CCPServiceException {
		//获取配置文件数据
		WeiXinConfiguration weixinConfig = WeiXinConfiguration.getInstance();
		WeiXinConfigData configData = weixinConfig.getPublicAccMap().get(pushMsg.getToUserName());
		if(configData==null){
			logger.info("weixin account ["+pushMsg.getToUserName()+"] is not config,msg can't process,return...");
			return;
		}
	    
	    McmWeiXinMsgInfo weixinMsg = new McmWeiXinMsgInfo();
		weixinMsg.setMCMEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
		weixinMsg.setOsUnityAccount(pushMsg.getToUserName());
		weixinMsg.setOpenID(pushMsg.getToUserName());
		weixinMsg.setUserID(pushMsg.getFromUserName());
		weixinMsg.setCreateTime(pushMsg.getCreateTime());
		weixinMsg.setMsgId(pushMsg.getMsgId());
		if(WeiXinMsgTypeEnum.TEXT.getValue().equals(pushMsg.getMsgType())){
			weixinMsg.setMsgType(String.valueOf(MCMTypeInner.MCMType_txt_VALUE));
			weixinMsg.setContent(pushMsg.getContent());
		}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(pushMsg.getMsgType())
				||WeiXinMsgTypeEnum.VOICE.getValue().equals(pushMsg.getMsgType())){
			String accessToken = getValidAccessToken(configData.getOpenID());
			//将多媒体文件从微信文件服务器上下载到本地，再上传到公司文件服务器，并获取url
			String filePath = getTempFile(accessToken, pushMsg.getMediaId());
			File file = new File(filePath);
			if(file==null||!file.exists()){
				logger.info("receive a weixin image msg,but image can't download from weixin server,msg data:"+JSONUtil.object2json(pushMsg));
				return;
			}
			String fileName = file.getName();
			
			String sig = null;
			try {
				sig = FileServerUtils.getFileServerSig();
			} catch (NoSuchAlgorithmException e) {
				throw new CCPServiceException(Constants.ERROR_CALCULATE_FILE_SERVER_SIG);
			}
			
			String fileUrl = fileServerService.uploadFile(sig, configData.getRonglianAppId(), pushMsg.getFromUserName(), fileName, filePath);
			
			if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(pushMsg.getMsgType())){
				weixinMsg.setMsgType(String.valueOf(MCMTypeInner.MCMType_emotion_VALUE));
			}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(pushMsg.getMsgType())){
				weixinMsg.setMsgType(String.valueOf(MCMTypeInner.MCMType_emotion_VALUE));
				//微信发送的语音包含有语音格式，如果调试时需要传格式，需要再加上，目前协议中没有
			}
			weixinMsg.setUrl(fileUrl);
			weixinMsg.setFileName(fileName);
		}

		
		//根据appId获取app数据，判断第三方url是否存在，若存在，向第三方推送微信消息，若不存在，查找座席
    		try {
    			AppAttrs appAttrs=appRedisDao.getAppAttrsByAppkey(configData.getRonglianAppId());
				if(appAttrs==null){
		    		logger.info(" proccess weixin from weixingw error,can't get app info");
					throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
		    	}else{
		    		String mcmNotifyUrl = appAttrs.getMcm_notify_url();
		    		if(StringUtils.isNotEmpty(mcmNotifyUrl)){
		    			logger.info("push weixin msg to AS,url:"+mcmNotifyUrl);
		    			/**存在第三方url，向第三方推送微信消息*/
		    			asService.pushWeiXinToAs(weixinMsg,appAttrs);
		    		}else{
		    			logger.info("push weixin msg to agent or call manager");
		    			/**不存在第三方url，寻找座席*/
		    			//eventProcessService.pushWeiXinToAgent(weixinMsg,appAttrs);
		    		}
		    		
		    	}
			} catch (Exception e) {
				logger.error(" get app info from cassandra error:"+e.getMessage());
				throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
			}
    		/*catch (CCPCassandraDaoException e) {
				logger.error(" get app info from cassandra error:"+e.getMessage());
				throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
			} catch (IOException e) {
				logger.error(" send weixin msg to agent error:"+e.getMessage());
				throw new CCPServiceException(Constants.ERROR_MCM_PROTOBUF_IOEXCEPTION);
			}*/ 
		
		
		
	}

	@Override
	public void sendMsg(McmWeiXinMsgInfo mcmWeixinMsg) throws CCPServiceException {
		/***先获取token*/
		String weixinAccount = mcmWeixinMsg.getOpenID();
		String accessToken = getValidAccessToken(weixinAccount);
		
		if(StringUtils.isEmpty(accessToken)){
			logger.error("can't send msg to weixin server,token is empty!");
		}else{
			WeiXinSendMsg sendMsgObj = new WeiXinSendMsg();
			sendMsgObj.setAccessToken(accessToken);
			sendMsgObj.setTouser(mcmWeixinMsg.getUserID());
			sendMsgObj.setMsgtype(mcmWeixinMsg.getMsgType());
			if(WeiXinMsgTypeEnum.TEXT.getValue().equals(mcmWeixinMsg.getMsgType())){
				sendMsgObj.setContent(mcmWeixinMsg.getContent());
			}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(mcmWeixinMsg.getMsgType())){
				if(StringUtils.isEmpty(mcmWeixinMsg.getUrl())){
					logger.error("send msg to weixin server error,type is image,but url is empty!");
					return;
				}
				//从文件服务器下载文件到本地目录
				String filePath = fileServerService.downloadFile(mcmWeixinMsg.getUrl());
				
				//将本地目录文件上传到微信服务器，获取media_id
				WeiXinResponseData response = uploadTempFile(accessToken, mcmWeixinMsg.getMsgType(), null, filePath);
				if(StringUtils.isNotEmpty(response.getMediaId())){
					sendMsgObj.setMediaId(response.getMediaId());
				}else{
					logger.error("send msg to weixin server error,upload file to weixin server failed,mediaId is empty!");
				}
			}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(mcmWeixinMsg.getMsgType())){
				if(StringUtils.isEmpty(mcmWeixinMsg.getUrl())){
					logger.error("send msg to weixin server error,type is voice,but url is empty!");
					return;
				}
				//从文件服务器下载文件到本地目录
				String filePath = fileServerService.downloadFile(mcmWeixinMsg.getUrl());
				
				//将本地目录文件上传到微信服务器，获取media_id
				WeiXinResponseData response = uploadTempFile(accessToken, mcmWeixinMsg.getMsgType(), null, filePath);
				if(StringUtils.isNotEmpty(response.getMediaId())){
					sendMsgObj.setMediaId(response.getMediaId());
				}else{
					logger.error("send msg to weixin server error,upload file to weixin server failed,mediaId is empty!");
				}
				
			}
			if(StringUtils.isNotEmpty(sendMsgObj.getMediaId())||StringUtils.isNotEmpty(sendMsgObj.getContent())){
				sendMsg(sendMsgObj);
			}else{
				logger.error("send msg to weixin server error,content and mediaId are empty!");
			}
		}
		
	}
	
	@Override
	public void sendMsg(WeiXinSendMsg sendMsg) throws CCPServiceException {
		//生成发送json格式包体文本
		String sendBody = null;
		if(WeiXinMsgTypeEnum.TEXT.getValue().equals(sendMsg.getMsgtype())){
			sendBody = sendMsg.toTextJson();
		}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(sendMsg.getMsgtype())){
			sendBody = sendMsg.toImageJson();
		}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(sendMsg.getMsgtype())){
			sendBody = sendMsg.toVoiceJson();
		} 
		
		//向微信服务器发送消息
		if(StringUtils.isNotEmpty(sendBody)){
			HttpMethod httpMethod = HttpMethod.POST;
			String url = WeiXinConstant.URL_SEND_MSG+"?access_token="+sendMsg.getAccessToken();
			logger.info("ready to send msg to weixin,\r\n msg body:"+sendMsg.toJson()+",url:"+url);
			Map<String,String> httpResult = httpClient.sendPacket(url, httpMethod, null, sendBody);
			String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
			String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				for (int i = 1; i <=3; i++) {
					logger.info("send msg to weixin failed,try times:"+i+",statusCode:"+statusCode+"\r\n msg body:"+sendMsg.toJson());
					httpResult=httpClient.sendPacket(url, httpMethod, null,sendBody);
					statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
						content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
						break;
					}
				}
			}
			
			if(Constants.SUCC == Integer.parseInt(statusCode)){
				logger.info("send msg to weixin success,statusCode:"+statusCode+",content:"+content);
			}
			
//			if(StringUtils.isNotEmpty(content)){
//				JSONObject jsonObj = null;
//				try {
//					jsonObj = new JSONObject(content);
//				} catch (ParseException e) {
//					logger.info("parse weixin access token failed,content is not json format,content:"+content);
//				}
//				if(jsonObj!=null){
//					responseData = new WeiXinResponseData();
//					responseData.setAccessToken(jsonObj.getString(WeiXinResponseData.ACCESS_TOKEN));
//					responseData.setExpiresIn(jsonObj.getString(WeiXinResponseData.EXPIRES_IN));
//				}
//			}
//			return responseData;
			
		}else{
			logger.info("send msg to weixin failed,msg body is empty! \r\n msg body:"+sendMsg.toJson());
		}
	}

	/**
	 * 获取token
	 */
	@Override
	public WeiXinResponseData getAccessToken(String appId, String appSecret) throws CCPServiceException {
		WeiXinResponseData responseData = null;
		HttpMethod httpMethod = HttpMethod.GET;
		String url = WeiXinConstant.URL_GET_ACCESS_TOKEN+"?grant_type=client_credential&appid="+appId+"&secret="+appSecret;
		logger.info("ready to get weixin access token,appId:"+appId+",appSecret:"+appSecret+",url:"+url);
		Map<String,String> httpResult = httpClient.sendPacket(url, httpMethod, null, null);
		String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
		String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
		if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
			for (int i = 1; i <=3; i++) {
				logger.info("get weixin access token failed,try times:"+i+",appId:"+appId+",appSecret:"+appSecret+",url:"+url+",statusCode:"+statusCode);
				httpResult=httpClient.sendPacket(url, httpMethod, null,null);
				statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
				if(statusCode!=null&&Integer.parseInt(statusCode)<500){
					content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					break;
				}
			}
		}
		
		if(Constants.SUCC == Integer.parseInt(statusCode)){
			if(StringUtils.isNotEmpty(content)){
				JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(content);
				} catch (ParseException e) {
					logger.info("parse weixin access token failed,content is not json format,content:"+content);
				}
				if(jsonObj!=null&&jsonObj.has(WeiXinResponseData.ACCESS_TOKEN)){
					responseData = new WeiXinResponseData();
					responseData.setAccessToken(jsonObj.getString(WeiXinResponseData.ACCESS_TOKEN));
					responseData.setExpiresIn(jsonObj.getString(WeiXinResponseData.EXPIRES_IN));
					logger.info("get weixin access token success,appId:"+appId+",appSecret:"+appSecret+",url:"+url+",statusCode:"+statusCode+",content:"+content);
				}else{
					logger.info("get weixin access token failed,response don't have the key["+content+WeiXinResponseData.MEDIA_ID+"]");
				}
			}
		}

		return responseData;
	}

	@Override
	public WeiXinResponseData uploadTempFile(String accessToken, String type,
			String fileName, String filePath) throws CCPServiceException {
		WeiXinResponseData responseData = null;
		HttpMethod httpMethod = HttpMethod.POST;
		String url = WeiXinConstant.URL_UPLOAD_TEMP_FILE+"?access_token="+accessToken+"&type="+type;
		logger.info("ready to upload temp file for weixin,access_token:"+accessToken+",type:"+type+",filePath:"+filePath+",url:"+url);
		Map<String,String> httpResult = httpClient.uploadFile(url, httpMethod, null, filePath);
		String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
		String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
		if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
			for (int i = 1; i <=3; i++) {
				logger.info("upload temp file for weixin failed,try times:"+i+",access_token:"+accessToken+",type:"+type+",filePath:"+filePath+",url:"+url+",statusCode:"+statusCode);
				httpResult = httpClient.uploadFile(url, httpMethod, null,filePath);
				statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
				if(statusCode!=null&&Integer.parseInt(statusCode)<500){
					content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					break;
				}
			}
		}
		
		if(Constants.SUCC == Integer.parseInt(statusCode)){
			if(StringUtils.isNotEmpty(content)){
				JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(content);
				} catch (ParseException e) {
					logger.info("parse weixin access token failed,content is not json format,content:"+content);
				}
				if(jsonObj!=null&&jsonObj.has(WeiXinResponseData.MEDIA_ID)){
					responseData = new WeiXinResponseData();
						responseData.setMediaId(jsonObj.getString(WeiXinResponseData.MEDIA_ID));
				}else{
						logger.info("upload temp file for weixin failed,response don't have the key["+content+WeiXinResponseData.MEDIA_ID+"]");
				}
			}
		}
		return responseData;
	}

	@Override
	public String getTempFile(String accessToken, String mediaId) throws CCPServiceException {
		String response = null;
		HttpMethod httpMethod = HttpMethod.GET;
		String url = WeiXinConstant.URL_GET_TEMP_FILE+"?access_token="+accessToken+"&media_id="+mediaId;
		logger.info("ready to download temp file from weixin,access_token:"+accessToken+",media_id:"+mediaId+",url:"+url);
		Map<String,String> httpResult =  httpClient.downloadFile(url, httpMethod);
		String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
		String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
		if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
			for (int i = 1; i <=3; i++) {
				logger.info("download temp file from weixin failed,try times:"+i+",access_token:"+accessToken+",content:"+content+",url:"+url+",statusCode:"+statusCode);
				httpResult = httpClient.downloadFile(url, httpMethod);
				statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
				if(statusCode!=null&&Integer.parseInt(statusCode)<500){
					content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
					break;
				}
			}
		}
		
		if(Constants.SUCC == Integer.parseInt(statusCode)){
			logger.info("download temp file from weixin success,access_token:"+accessToken+",content:"+content+",url:"+url+",statusCode:"+statusCode);
			if(StringUtils.isNotEmpty(content)){
				response = content;
			}else{
				logger.info("download temp file from weixin failed,content is empty,access_token:"+accessToken+",url:"+url+",statusCode:"+statusCode);
			}
		}
		
		return response;
	}
	
	@Override
	public String getValidAccessToken(String weixinAccount) throws CCPServiceException {
		//先判断内存中的token是否可用
		WeiXinConfiguration weiXinConfig = WeiXinConfiguration.getInstance();
		Map<String,WeiXinConfigData> weixinAccMap = weiXinConfig.getPublicAccMap();
		WeiXinConfigData configData = weixinAccMap.get(weixinAccount);
		String accessToken = configData.getAccessToken();
		if(StringUtils.isNotEmpty(accessToken)){
			//判断token是否过期
			String tokenExpirationTime = configData.getTokenExpirationTime();
			if(StringUtils.isNotEmpty(tokenExpirationTime)){
				Long expirationTime = Long.parseLong(tokenExpirationTime);
				Long nowTime = System.currentTimeMillis();
				
				//当过期时间大于当前时间，并且超过一个数值（防止出现判断的时候token未过期，但发送请求到微信服务器过期的情况）时，该token可用
				//微信token接口每天上限为2000次，token有效期2小时，这里我们设置1小时更换一次token
				if(expirationTime>nowTime&&expirationTime-nowTime>=3600*1000){
					return accessToken;
				}
			}
		}
		
		//若以上代码没有返回token，说明token需要重新获取，向微信服务器请求token
		WeiXinResponseData responseToken = getAccessToken(configData.getAppID(), configData.getAppSecret());
		if(responseToken==null){
			logger.error("get valid access token failed,accountId:"+weixinAccount+",appId:"+configData.getAppID()+",appSecret:"+configData.getAppSecret());
			return null;
		}else{
			accessToken = responseToken.getAccessToken();
		}
		
		//设置token 与 token过期时间
		configData.setAccessToken(accessToken);
		configData.setTokenExpirationTime(String.valueOf(System.currentTimeMillis()+Long.parseLong(responseToken.getExpiresIn())*1000));
		
		//返回token之前，更新内存，以及配置文件（xml）中的token以及token过期时间
		WeiXinConfiguration.updateAccountToCache(configData);
		WeiXinConfiguration.updateAccountToXml(configData);
		
		return configData.getAccessToken();
	}


	@Override
	public void uploadPermanentFile(String accessToken, String type,
			String fileName, String filePath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPermanentFile(String accessToken, String mediaId) {
		// TODO Auto-generated method stub
		
	}
	
	public String getWeixinVerifyToken() {
		return weixinVerifyToken;
	}

	public void setWeixinVerifyToken(String weixinVerifyToken) {
		this.weixinVerifyToken = weixinVerifyToken;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public FileServerService getFileServerService() {
		return fileServerService;
	}

	public void setFileServerService(FileServerService fileServerService) {
		this.fileServerService = fileServerService;
	}

	public AppRedisDao getAppRedisDao() {
		return appRedisDao;
	}

	public void setAppRedisDao(AppRedisDao appRedisDao) {
		this.appRedisDao = appRedisDao;
	}

	public AsService getAsService() {
		return asService;
	}

	public void setAsService(AsService asService) {
		this.asService = asService;
	}
	
}
