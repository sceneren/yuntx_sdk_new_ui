package com.yuntongxun.mcm.weixin.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.weixin.WeiXinPushMsg;
import com.yuntongxun.mcm.weixin.WeiXinResponseData;
import com.yuntongxun.mcm.weixin.WeiXinSendMsg;

/**
 * @author weily
 */
public interface MCMWeiXinService {

	/**
	 * 
	 * 校验消息真实性接口
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @param echostr
	 * @throws CCPServiceException
	 */
	public String verifyMsg(String signature,String timestamp,String nonce,String echostr) throws CCPServiceException;

	/**
	 * 推送微信消息
	 * @param pushMsg
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	public void pushMsg(WeiXinPushMsg pushMsg) throws CCPServiceException;
	
	/**
	 * 发送微信消息
	 * @param sendMsg
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	public void sendMsg(WeiXinSendMsg sendMsg) throws CCPServiceException;
	
	/**
	 * 获取token
	 * @param appId
	 * @param appSecret
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	public WeiXinResponseData getAccessToken(String appId,String appSecret) throws CCPServiceException;
	
	/**
	 * 新增临时素材
	 * @param access_token
	 * @param type
	 * @param fileName
	 * @param filePath
	 */
	public WeiXinResponseData uploadTempFile(String accessToken,String type,String fileName,String filePath) throws CCPServiceException;
	
	/**
	 * 获取临时素材
	 * @param accessToken
	 * @param mediaId
	 * @return filePath 文件路径
	 */
	public String getTempFile(String accessToken,String mediaId) throws CCPServiceException;
	
	/**
	 * 新增永久素材
	 * @param access_token
	 * @param type
	 * @param fileName
	 * @param filePath
	 */
	public void uploadPermanentFile(String accessToken,String type,String fileName,String filePath) throws CCPServiceException;
	
	/**
	 * 获取永久素材
	 * @param accessToken
	 * @param mediaId
	 */
	public void getPermanentFile(String accessToken,String mediaId) throws CCPServiceException;

	/**
	 * 多渠道调用该接口发送微信消息，需要对业务数据进行相应处理后，向微信服务器发送消息
	 * @param sendMsg
	 * @param weixinMsg
	 * @throws CCPServiceException 
	 */
	public void sendMsg(McmWeiXinMsgInfo weixinMsg) throws CCPServiceException;
	
	/**
	 * 获取合法可用的accessToken
	 * @param weixinAccount
	 * @return
	 * @throws CCPServiceException 
	 */
	public String getValidAccessToken(String weixinAccount) throws CCPServiceException;
	
}
