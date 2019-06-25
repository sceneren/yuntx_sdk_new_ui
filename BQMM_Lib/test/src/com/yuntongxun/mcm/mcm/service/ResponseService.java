package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;

public interface ResponseService {

	/**
	 * @Description: 接收rm server的响应消息
	 * @param rmServerTransferData 
	 * @throws CCPServiceException
	 */
	void handleRMResponse(RMServerTransferData rmServerTransferData) throws CCPServiceException;
	
}
