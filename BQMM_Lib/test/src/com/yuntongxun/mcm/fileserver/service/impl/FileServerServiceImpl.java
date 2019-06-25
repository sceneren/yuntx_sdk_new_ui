package com.yuntongxun.mcm.fileserver.service.impl;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.fileserver.util.FileServerConstant;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.service.impl.AgentServiceImpl;
import com.yuntongxun.mcm.util.Constants;

/**
 * 项目：ECMCMServer
 * 描述：文件服务器业务实现类
 * 创建人：weily
 * 创建时间：2015年8月6日 上午11:17:05 
 */
public class FileServerServiceImpl implements FileServerService {
	
	public static final Logger logger = LogManager.getLogger(AgentServiceImpl.class);
	
	private String fileServerUrl;
	
	private HttpClient httpClient;	
	
	@Override
	public String uploadFile(String sig, String appId, String userName, String fileName, 
			String filePath) throws CCPServiceException {
		String resultFileUrl = null;
		
		StringBuilder urlStringBuilder = new StringBuilder();
		urlStringBuilder.append(fileServerUrl)
						.append(FileServerConstant.URI_VTM_FILE_UPLOAD_INTERFACE)
						.append("?")
						.append("appId=").append(appId)
						.append("&")
						.append("userName=").append(userName)
						.append("&")
						.append("fileName=").append(fileName)
						.append("&")
						.append("sig=").append(sig);
		String finalUrl = urlStringBuilder.toString();
		logger.info("uploadFile final url: {}.", finalUrl);
		
		HttpMethod postMethod = HttpMethod.POST;
		Map<String, String> httpResult = httpClient.uploadFileToFileServer(finalUrl, postMethod, null, filePath);
		
		String statusCode = httpResult.get("statusCode");
		logger.info("request file server return statusCode: {}.", statusCode);
		
		String content = httpResult.get("content");
		logger.info("request file server return content: {}.", content);
		
		if(String.valueOf(Constants.SUCC).equals(statusCode) && StringUtils.isNotEmpty(content)){
			try {
				JSONObject responseJsonObj = new JSONObject(content);
				resultFileUrl = responseJsonObj.getString("downloadUrl");
			} catch (ParseException e) {
				logger.error("upload file error, parse result to json object error: ", e);
				return null;
			}
		}
		
		return resultFileUrl;
	}
	
	@Override
	public String downloadFile(String url) throws CCPServiceException{
		HttpMethod httpMethod = HttpMethod.POST;
		logger.info("downloadFile final url: {}.", url);
		
		Map<String, String> httpResult = httpClient.downloadFile(url, httpMethod);
		if(httpResult != null){
			String statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
			logger.info("request file server return statusCode: {}.", statusCode);
			
			String content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
			logger.info("request file server return content: {}.", content);
			
			if(statusCode == null || (statusCode != null && Integer.parseInt(statusCode) >= 500)){
				for (int i = 1; i <= 3; i++) {
					logger.info("download file from file server failed, try times:" + i);
					
					httpResult=httpClient.downloadFile(url, httpMethod);
					if(httpResult != null){
						statusCode = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE);
						if(statusCode != null && Integer.parseInt(statusCode) < 500){
							content = httpResult.get(Constants.CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT);
							break;
						}	
					}
				}
			}else if(Constants.SUCC == Integer.parseInt(statusCode)){
				logger.info("download file from file server success,url:"+url);
			}
			return content;
		}else {
			logger.info("request file server return result is null.");
			return null;
		}
	}

	@Override
	public String uploadFile(String sig, String appId, String userName, String fileName,
			InputStream is) throws CCPServiceException {
		String resultFileUrl = null;
		
		StringBuilder urlStringBuilder = new StringBuilder();
		urlStringBuilder.append(fileServerUrl)
						.append(FileServerConstant.URI_VTM_FILE_UPLOAD_INTERFACE)
						.append("?")
						.append("appId=").append(appId)
						.append("&")
						.append("userName=").append(userName)
						.append("&")
						.append("fileName=").append(fileName)
						.append("&")
						.append("sig=").append(sig);
		String finalUrl = urlStringBuilder.toString();
		logger.info("uploadFile final url: {}.", finalUrl);
		
		HttpMethod postMethod = HttpMethod.POST;
		Map<String, String> httpResult = httpClient.uploadFileToFileServer(finalUrl,postMethod, null, is);
		
		String statusCode = httpResult.get("statusCode");
		logger.info("request file server return statusCode: {}.", statusCode);
		
		String content = httpResult.get("content");
		logger.info("request file server return content: {}.", content);
		
		if(String.valueOf(Constants.SUCC).equals(statusCode)&&StringUtils.isNotEmpty(content)){
			try {
				JSONObject responseJsonObj = new JSONObject(content);
				resultFileUrl = responseJsonObj.getString("downloadUrl");
			} catch (ParseException e) {
				logger.error("upload file error, parse result to json object error: ", e);
			}
		}
		
		return resultFileUrl;
	}
	
	/**
	 * set inject
	 */
	public void setFileServerUrl(String fileServerUrl) {
		this.fileServerUrl = fileServerUrl;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}
