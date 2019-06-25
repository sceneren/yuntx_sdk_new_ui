package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.model.Connector;

public interface MCMService {

	/**
	 * @Description: 处理消息
	 * @param msgLite
	 * @param connector 
	 * @throws CCPServiceException
	 */
	void handleMcmReceivedMessage(MsgLiteInner msgLite, Connector connector) throws CCPServiceException;
	
	/**
	 * @Description: 处理会话断开连接消息
	 * @param msgLite
	 * @param connector 
	 * @throws CCPServiceException
	 */
	void handlerConnectorCloseReceivedMessage(MsgLiteInner msgLite, Connector connector) throws CCPServiceException;

}
