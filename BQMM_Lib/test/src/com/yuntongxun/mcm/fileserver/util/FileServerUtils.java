package com.yuntongxun.mcm.fileserver.util;

import java.security.NoSuchAlgorithmException;

import com.yuntongxun.mcm.weixin.util.MessageDigestUtil;

/**
 * 项目：ECMCMServer
 * 描述：
 * 创建人：weily
 * 创建时间：2015年8月6日 下午1:58:46 
 */
public class FileServerUtils {

	/**
	 * 计算文件服务器sig值
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getFileServerSig() throws NoSuchAlgorithmException{
    	String sig = null;
    	try {
	    	String userName = FileServerConstant.FILE_SERVER_USERNAME;
	    	String password = FileServerConstant.FILE_SERVER_PASSWORD;
			sig = MessageDigestUtil.md5(userName+password).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			throw e;
		}
    	return sig;
    }
}
