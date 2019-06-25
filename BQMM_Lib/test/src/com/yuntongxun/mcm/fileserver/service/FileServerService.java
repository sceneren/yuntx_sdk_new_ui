package com.yuntongxun.mcm.fileserver.service;

import java.io.InputStream;

import com.yuntongxun.mcm.core.exception.CCPServiceException;

/**
 * 项目：ECMCMServer
 * 描述：文件服务器业务接口
 * 创建人：weily
 * 创建时间：2015年8月6日 上午11:02:38 
 */
public interface FileServerService {

	/**
	 * 上传文件
	 * @param fileName
	 * @param filePath
	 * @throws CCPServiceException 
	 */
	public String uploadFile(String sig, String appId, String userName, String fileName, 
			String filePath) throws CCPServiceException;
	
	/**
	 * 上传文件
	 * @param sig
	 * @param appId
	 * @param userName
	 * @param fileName
	 * @param is
	 * @return
	 * @throws CCPServiceException
	 */
	public String uploadFile(String sig, String appId, String userName, String fileName, 
			InputStream is) throws CCPServiceException;
	
	/**
	 * 下载文件
	 * @param url
	 * @return
	 * @throws CCPServiceException
	 */
	public String downloadFile(String url) throws CCPServiceException;
	
}
