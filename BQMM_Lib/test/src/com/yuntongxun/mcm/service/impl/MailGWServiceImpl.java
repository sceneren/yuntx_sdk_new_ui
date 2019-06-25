/**
 * 
 */
package com.yuntongxun.mcm.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.dao.AppRedisDao;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.MailGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

/**
 * 负责推送消息的业务层
 * 
 * @author weily
 */
public class MailGWServiceImpl implements MailGWService {

	public static final Logger logger = LogManager.getLogger(MailGWServiceImpl.class);

	private ExecutorService executorService;
	
	private String mailgwServerUrl;

	private HttpClient httpClient;
	
	private int resendTimeNum;

	private AsService asService;
	
	private AppRedisDao appRedisDao;

	public void init() {
		executorService = Executors.newFixedThreadPool(1);
	}
	
	@Override
	public void handleMailMsg(McmMailMsgInfo mailMsg) throws CCPServiceException {
		
		if(MCMEventDefInner.UserEvt_SendMail_VALUE == mailMsg.getMCMEvent()){
	    		try {
	    			//根据appId获取app数据，判断第三方url是否存在，若存在，向第三方推送邮件消息，若不存在，查找座席
	    			AppAttrs appAttrs = appRedisDao.getAppAttrsByAppkey(mailMsg.getAppId());
					if(appAttrs==null){
			    		logger.info(" proccess mail from mailgw error,can't get app info");
						throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_APPINFO_EMPTY);
			    	}else{
			    		String mcmNotifyUrl = appAttrs.getMcm_notify_url();
			    		if(StringUtils.isNotEmpty(mcmNotifyUrl)){
			    			logger.info("push mail to AS,url:"+mcmNotifyUrl);
			    			/**存在第三方url，向第三方推送邮件消息*/
			    			asService.pushMailToAs(mailMsg,appAttrs);
			    		}else{
			    			logger.info("push mail to agent or call manager");
			    			/**不存在第三方url，寻找座席*/
			    			// eventProcessService.pushMailToAgent(mailMsg,appAttrs);
			    		}
			    		
			    	}
				} catch (Exception e) {
					logger.error(" send mail to agent error:"+e.getMessage());
					throw new CCPServiceException(Constants.ERROR_MCM_PROTOBUF_IOEXCEPTION);
				}
	    		
		}else if(MCMEventDefInner.UserEvt_SendMail_VALUE == mailMsg.getMCMEvent()){
			
		}
		     
	}

	@Override
	public void sendMail(final McmMailMsgInfo requestData) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("MailServiceThread_sendMail");
				String response="";
				HttpMethod httpMethod=HttpMethod.POST;
				HashMap<String, String> header=new  HashMap<String, String>();
				header.put("Content-Type", "application/json");
				long currentTime = System.currentTimeMillis();
				try {
					String jsonMsg = requestData.toJsonForMailSend();

					String url = mailgwServerUrl + Constants.MAILGW_SEND_URL_SUFFIX;
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
				} catch (UnsupportedEncodingException e) {
					logger.error("url encdoe error:"+e.getMessage());
				}
				
			}
		});
		
	}

	/**新增活更新邮箱配置
	 * 
	 * @param requestData
	 * @return
	 * @throws CCPServiceException
	 */
	private String addOrUpdateMailConfig(McmMailMsgInfo requestData) throws CCPServiceException {
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		long currentTime = System.currentTimeMillis();
		String result = Constants.ERROR_MCM_MAILGW_SEND_ERROR;
		String url = mailgwServerUrl + Constants.MAILGW_CONFIG_URL_SUFFIX;
		requestData.setAction(Constants.MAILGW_CONFIG_ACTION_NAME_ADDUPDATE);
		requestData.setActionId(StringUtil.getUUID());
		requestData.setMailId(requestData.getUserAccount());
		String jsonMsg = requestData.toJsonForMailAddOrUpdate();
		Map<String,String> httpResult=httpClient.sendPacket(url, httpMethod, header,jsonMsg);
		String statusCode = httpResult.get("statusCode");
		String content = httpResult.get("content");
		
		if(String.valueOf(Constants.SUCC).equals(statusCode)){
			result = Constants.RESPONSE_OK;
		}else{
			result = Constants.ERROR_MCM_MAILGW_CONFIG_ERROR;
		}
			
//			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
//				for (int i = 1; i <=resendTimeNum; i++) {
//					httpResult=httpClient.sendPacket(mailgwServerUrl, httpMethod, header,jsonMsg);
//					statusCode = httpResult.get("statusCode");
//					content = httpResult.get("content");
//					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
//						break;
//					}
//				}
//			}
//			
//			if(StringUtils.isEmpty(content)){
//				
//			}else{
//				
//			}
			
			
		return result;
		
	}

	private String deleteMailConfig(McmMailMsgInfo requestData) throws CCPServiceException {
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		long currentTime = System.currentTimeMillis();
		String result = Constants.ERROR_MCM_MAILGW_SEND_ERROR;
		String url = mailgwServerUrl + Constants.MAILGW_CONFIG_URL_SUFFIX;
		requestData.setAction(Constants.MAILGW_CONFIG_ACTION_NAME_DELETE);
		requestData.setActionId(StringUtil.getUUID());
		requestData.setMailId(requestData.getUserAccount());
		String jsonMsg = requestData.toJsonForMailDelete();
		Map<String,String> httpResult=httpClient.sendPacket(url, httpMethod, header,jsonMsg);
		String statusCode = httpResult.get("statusCode");
		String content = httpResult.get("content");
		
		if(String.valueOf(Constants.SUCC).equals(statusCode)){
			result = Constants.RESPONSE_OK;
		}else{
			result = Constants.ERROR_MCM_MAILGW_CONFIG_ERROR;
		}
			
//			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
//				for (int i = 1; i <=resendTimeNum; i++) {
//					httpResult=httpClient.sendPacket(mailgwServerUrl, httpMethod, header,jsonMsg);
//					statusCode = httpResult.get("statusCode");
//					content = httpResult.get("content");
//					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
//						break;
//					}
//				}
//			}
//			
//			if(StringUtils.isEmpty(content)){
//				
//			}else{
//				
//			}
			
			
		return result;
		
	}


	@Override
	public String handleMailConfig(McmMailMsgInfo requestData) throws CCPServiceException {
		String result = null;
		if(Constants.MAILGW_CONFIG_TYPE_ADD_OR_UPDATE.equals(requestData.getConfigType())){
			//新增或更新邮箱
			result = addOrUpdateMailConfig(requestData);
			
		}else if(Constants.MAILGW_CONFIG_TYPE_DELETE.equals(requestData.getConfigType())){
			//删除邮箱
			result = deleteMailConfig(requestData);
		}
		return result;
	}
	
	public String getMailgwServerUrl() {
		return mailgwServerUrl;
	}

	public void setMailgwServerUrl(String mailgwServerUrl) {
		this.mailgwServerUrl = mailgwServerUrl;
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
	
}
