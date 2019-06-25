package com.yuntongxun.mcm.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.Connector;

public interface DataAsyncService {

	/**
	 * @Description: 记录客服坐席交互历史消息
	 * @param sendMsg
	 * @param msgData
	 * @param version
	 * @param connector
	 * @param userAndAgentDialog 
	 * @param msgId
	 * @param monitorAgentId
	 * @param receiverUserAcc
	 * @param agentId
	 * @throws CCPServiceException
	 */
	void saveM3CSMessageHistory(MCMDataInner sendMsg, MSGDataInner msgData, long version, 
			Connector connector, UserAndAgentDialog userAndAgentDialog, String msgId, 
			String monitorAgentId, String receiverUserAcc, String agentId) throws CCPServiceException;

	/**
	 * @Description: 记录坐席操作结果
	 * @param sendMsg
	 * @param connector
	 * @param receiverUserAcc
	 * @param agentId
	 * @param resultCode
	 * @throws CCPServiceException
	 */
	void saveM3CSMessageHistory(MCMDataInner sendMsg, Connector connector, String receiverUserAcc, 
			String agentId, String resultCode) throws CCPServiceException;
}
