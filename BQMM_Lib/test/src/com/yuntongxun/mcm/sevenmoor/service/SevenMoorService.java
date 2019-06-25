package com.yuntongxun.mcm.sevenmoor.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.sevenmoor.model.TransferData;

public interface SevenMoorService {

	/**
	 * 模拟登陆七陌sdk
	 * 
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void sdkLogin(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo);

	/**
	 * 发送消息
	 * 
	 * @param sendMsg
	 * @param connector
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void sdkNewMsg(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo) throws CCPServiceException;

	/**
	 * 开启新会话
	 * 
	 * @param transferData
	 */
	void sdkBeginNewChatSession(TransferData transferData);

	/**
	 * 获取消息
	 * 
	 * @param transferData
	 */
	void sdkGetMsg(TransferData transferData);

	/**
	 * 处理七陌回调方法
	 * 
	 * @param transferData
	 * @throws CCPServiceException
	 */
	void processAction(TransferData transferData) throws CCPServiceException;

	/**
	 * 提交满意度
	 * 
	 * @param transferData
	 * @param protoClientNo
	 * @param userAcc
	 */
	void sdkGetInvestigate(TransferData transferData, int protoClientNo, String userAcc);

	/**
	 * 提交满意度
	 * 
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void submitInvestigate(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo);

	/**
	 * 结束咨询
	 * 
	 * @param sendMsg
	 * @param connector
	 * @param userAcc
	 * @param protoClientNo
	 */
	void endAsk(MCMDataInner sendMsg, Connector connector, String userAcc, int protoClientNo);

	/**
	 * 异常断线
	 * 
	 * @param connectionId
	 */
	void userDisconnect(String connectionId);

}
