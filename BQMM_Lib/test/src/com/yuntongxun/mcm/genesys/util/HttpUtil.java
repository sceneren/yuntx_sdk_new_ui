package com.yuntongxun.mcm.genesys.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yuntongxun.mcm.util.StringUtil;
import com.yuntongxun.mcm.weixin.util.WeiXinUtils;

public class HttpUtil {
	
	private static final Logger logger = LogManager.getLogger(HttpUtil.class);
	
	public static InputStream downloadFilePost(String url) {		
		DefaultHttpClient httpClient = null;
		HttpPost httpPost = null;
		try {
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
	        cm.setMaxTotal(32);
			httpClient = new DefaultHttpClient(cm);
			httpPost = new HttpPost(url);
			
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpPost.getParams(), 50000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpPost.getParams(), 50000);

			long httpStart = System.currentTimeMillis();
			logger.info("Http Post Send Start.");
			HttpResponse response = httpClient.execute(httpPost);
			
			long httpEnd = System.currentTimeMillis();
			logger.info("Http Post Send Over. cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				logger.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					InputStream in = response.getEntity().getContent();
					return in;
				} else {
					return null;
				}
			}
			httpPost.abort();

		}catch (IOException ioe) {
		    if(httpPost.isAborted()){
			   httpPost.abort();
			}
		    logger.error("HttpUtil error.", ioe.fillInStackTrace());
		}
		
		return null;
	}
	
	public static Map<String, InputStream> downloadFileGet(String url) {
		DefaultHttpClient httpClient = null;
		HttpGet httpGet = null;
		
		Map<String, InputStream> map = null;
		
		try {
			PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
	        cm.setMaxTotal(32);
			httpClient = new DefaultHttpClient(cm);

			httpGet = new HttpGet(url);
			// 设置请求超时时间
			HttpConnectionParams.setConnectionTimeout(httpGet.getParams(), 50 * 1000);
			// 设置等待数据超时时间
			HttpConnectionParams.setSoTimeout(httpGet.getParams(), 50 * 1000);
			
			logger.info("download file from url:" + url);

			long httpStart = System.currentTimeMillis();
			logger.info("Http Get Send Start...");
			HttpResponse response = httpClient.execute(httpGet);
			
			long httpEnd = System.currentTimeMillis();
			logger.info("Http Get Send Over...cost: "+(httpEnd-httpStart)+"ms");

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				logger.info("Http Post status code: " + statusCode + "\r\n");
				if (statusCode == HttpStatus.SC_OK) {
					InputStream in = response.getEntity().getContent();
					
					String extendFileName = WeiXinUtils.getExtentName(url);
					String newFileName = StringUtil.getUUID()+extendFileName;
					map = new HashMap<String, InputStream>();
					map.put(newFileName, in);
					
					return map;
				} else {
					return null;
				}
			}
			httpGet.abort();
		} catch (IOException ioe) {
		    if(httpGet.isAborted()){
		    	httpGet.abort();
			}
		    logger.error("HttpUtil error.", ioe.fillInStackTrace());
		    return null;
		}
		
		return null;
	}
}
