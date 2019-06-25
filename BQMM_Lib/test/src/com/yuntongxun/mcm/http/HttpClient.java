package com.yuntongxun.mcm.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;

/**
 * 内部/外部模块之间通讯控制器
 * 
 * @author chao
 */
public interface HttpClient {


	/**
	 * 外部模块请求
	 * 
	 * @param url
	 * @param body
	 * @return
	 * @throws CCPServiceException
	 * @throws CCPDaoException
	 * @throws CCPException
	 */
	public Map<String, String> sendPacket(String url, HttpMethod method, HashMap<String, String> header, String body)
			throws CCPServiceException;

	/**
	 * 上传文件
	 * @param url
	 * @param method
	 * @param header
	 * @param filePath
	 * @return
	 * @throws CCPServiceException
	 */
	public Map<String, String> uploadFile(String url, HttpMethod method,
			HashMap<String, String> header, String filePath)
			throws CCPServiceException;

	/**
	 * 下载文件
	 * @param url
	 * @param method
	 * @return
	 * @throws CCPServiceException
	 */
	public Map<String,String> downloadFile(String url, HttpMethod method)
			throws CCPServiceException;

	/**
	 * 文件服务器上传文件方法（与微信不同）
	 * @param url
	 * @param method
	 * @param header
	 * @param filePath
	 * @return
	 * @throws CCPServiceException
	 */
	public Map<String, String> uploadFileToFileServer(String url, HttpMethod method,
			HashMap<String, String> header, String filePath)
			throws CCPServiceException;
	
	/**
	 * 根据流上传文件
	 * @param url
	 * @param method
	 * @param header
	 * @param is
	 * @return
	 * @throws CCPServiceException
	 */
	public Map<String, String> uploadFileToFileServer(String url, HttpMethod method,
			HashMap<String, String> header, InputStream is)
			throws CCPServiceException;
}
