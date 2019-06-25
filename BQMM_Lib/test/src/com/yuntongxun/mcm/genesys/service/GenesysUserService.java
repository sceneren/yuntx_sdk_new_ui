package com.yuntongxun.mcm.genesys.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.model.Connector;

public interface GenesysUserService {

	/**
	 * @Description: 用户开始咨询
	 * @param sendMsg
	 * @param connector
	 * @throws CCPServiceException
	 */
	void startAsk(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 用户停止咨询
	 * @param sendMsg
	 * @param connector
	 * @throws CCPServiceException
	 */
	void endAsk(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

	/**
	 * @Description: 用户发送消息
	 * @param sendMsg
	 * @param connector
	 * @throws CCPServiceException
	 */
	void sendMSG(MCMDataInner sendMsg, Connector connector, String userAcc) throws CCPServiceException;

}
