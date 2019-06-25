package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;

public interface RMServerRequestService {

	/**
	 * @Description: 推送消息到rm server
	 * @param message 
	 * @throws CCPServiceException
	 */
	void doPushMessage(String message) throws CCPServiceException;

}
