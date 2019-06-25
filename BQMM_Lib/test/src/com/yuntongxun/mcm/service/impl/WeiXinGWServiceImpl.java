package com.yuntongxun.mcm.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.dao.AppRedisDao;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.service.IMUserService;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;

/**
 * 负责推送消息的业务层
 * 
 * @author weily
 */
public class WeiXinGWServiceImpl implements WeiXinGWService {

	public static final Logger logger = LogManager.getLogger(WeiXinGWServiceImpl.class);

	private ExecutorService executorService;
	
	private String weixingwServerUrl;

	private HttpClient httpClient;
	
	private int resendTimeNum;

	private AsService asService;
	
	private AppRedisDao appRedisDao;

	private IMUserService imUserService;

	public void init() {
		executorService = Executors.newFixedThreadPool(1);
	}
	
	@Override
	public void handleWeiXinMsg(McmWeiXinMsgInfo weixinMsg) throws CCPServiceException {
		if(MCMEventDefInner.UserEvt_SendWXMsg_VALUE == weixinMsg.getMCMEvent()){
			//根据appId获取app数据，判断第三方url是否存在，若存在，向第三方推送微信消息，若不存在，查找座席
			AppAttrs appAttrs = appRedisDao.getAppAttrsByAppkey(weixinMsg.getAppId());
	    	if(appAttrs != null){
	    		try {
		    		String mcmNotifyUrl = appAttrs.getMcm_notify_url();
		    		if(StringUtils.isNotEmpty(mcmNotifyUrl)){
		    			logger.info("push weixin to AS,url:"+mcmNotifyUrl);
		    			/**存在第三方url，向第三方推送微信消息*/
		    			asService.pushWeiXinToAs(weixinMsg,appAttrs);
		    		}else{
		    			logger.info("push weixin msg to agent");
		    			/**不存在第三方url，寻找座席*/
		    			imUserService.sendWeixinMSG(weixinMsg,appAttrs);
		    		}
			    		
				} catch (Exception e) {
					logger.error(" send weixin msg to agent error:{}",e);
					throw new CCPServiceException(Constants.ERROR_MCM_PROTOBUF_IOEXCEPTION);
				} 
	    	}else{
	    		logger.error(" send weixin msg to agent error,appId is empty.");
	    		throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
	    	}
		}else if(MCMEventDefInner.UserEvt_StartAsk_VALUE == weixinMsg.getMCMEvent()){
			//根据appId获取app数据，判断第三方url是否存在，若存在，向AS发送开启咨询请求
	    	AppAttrs appAttrs=appRedisDao.getAppAttrsByAppkey(weixinMsg.getAppId());
	    	
	    	if(appAttrs != null){
	    		try {
    				String mcmNotifyUrl = appAttrs.getMcm_notify_url();
    				if(StringUtils.isNotEmpty(mcmNotifyUrl)){
    					logger.info("push weixin to AS,url:"+mcmNotifyUrl);
    					/**存在第三方url，向第三方推送微信消息*/
    					asService.weixinStartAsk(weixinMsg,appAttrs);
    				}else{
    					logger.info("push weixin msg to agent");
    					/**不存在第三方url，寻找座席*/
    					imUserService.weixinStartAsk(weixinMsg,appAttrs);
    				}
	    				
	    		} catch (Exception e) {
					logger.error(" send weixin msg to agent error:{}",e);
	    			throw new CCPServiceException(Constants.ERROR_MCM_PROTOBUF_IOEXCEPTION);
	    		} 
	    	}else{
	    		logger.error("weixin start ask to agent error,appId is empty.");
	    		throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
	    	}
//	    	}
		}
	}

	@Override
	public void sendWeiXinMsg(final McmWeiXinMsgInfo requestData) {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				//Thread.currentThread().setName("WeiXinGWThread_sendWeiXinMsg");
				String response="";
				HttpMethod httpMethod=HttpMethod.POST;
				HashMap<String, String> header=new  HashMap<String, String>();
				header.put("Content-Type", "application/json");
				long currentTime = System.currentTimeMillis();
				try {
					ThreadContext.push(sessionId);
					PrintUtil.printStartTag("SendWeiXinMsg");
					
					String jsonMsg = requestData.toJsonForWeiXinSend();

					String url = weixingwServerUrl + Constants.WEIXINGW_SEND_URL_SUFFIX;
					Map<String,String> httpResult=httpClient.sendPacket(url, httpMethod, header,jsonMsg);
					String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
					String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
						
					if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
						for (int i = 1; i <=resendTimeNum; i++) {
							httpResult=httpClient.sendPacket(url, httpMethod, header,jsonMsg);
							statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
							content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
							if(statusCode!=null&&Integer.parseInt(statusCode)<500){
								break;
							}
						}
					}
					
//					if(StringUtils.isEmpty(content)){
//						
//					}else{
//						
//					}
					
				} catch (CCPServiceException e) {
					logger.error(e.getMessage());
				} finally{
					PrintUtil.printEndTag("SendWeiXinMsg");
					ThreadContext.removeStack();
				}
				
			}
		});
		
	}

	

	public String getWeixingwServerUrl() {
		return weixingwServerUrl;
	}

	public void setWeixingwServerUrl(String weixingwServerUrl) {
		this.weixingwServerUrl = weixingwServerUrl;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public int getResendTimeNum() {
		return resendTimeNum;
	}

	public void setResendTimeNum(int resendTimeNum) {
		this.resendTimeNum = resendTimeNum;
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

	public void setImUserService(IMUserService imUserService) {
		this.imUserService = imUserService;
	}
	
}
