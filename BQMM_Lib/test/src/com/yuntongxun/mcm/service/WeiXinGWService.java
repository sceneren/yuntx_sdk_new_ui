package com.yuntongxun.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;

public interface WeiXinGWService {

	/**
	 * @Description: 处理微信消息
	 * @param requestData 
	 * @throws CCPServiceException
	 */
	public void handleWeiXinMsg(McmWeiXinMsgInfo requestData) throws CCPServiceException;

	/**
	 * @Description: 发送微信消息
	 * @param requestData 
	 * @throws
	 */
	public void sendWeiXinMsg(McmWeiXinMsgInfo requestData);
	
}
