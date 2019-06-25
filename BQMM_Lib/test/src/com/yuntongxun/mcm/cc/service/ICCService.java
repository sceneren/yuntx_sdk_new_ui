package com.yuntongxun.mcm.cc.service;

import com.yuntongxun.mcm.cc.form.WakeupUserForm;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;

public interface ICCService {

	/**
	 * @Description: 开始咨询
	 * @param sendMsg
	 * @param connector
	 * @param appInfo
	 * @param protoClientNo
	 * @throws CCPServiceException
	 */
	void startMessage(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 微信开始咨询
	 * @param weixinMsg
	 * @param appAttrs 
	 * @throws CCPServiceException
	 */
	void wechatStartMessage(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException;
	
	/**
	 * @Description: 结束咨询
	 * @param sendMsg
	 * @param connector
	 * @param appInfo
	 * @param protoClientNo 
	 * @throws CCPServiceException
	 */
	void stopmessage(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) 
			throws CCPServiceException;
	
	/**
	 * @Description: 用户断线通知
	 * @param connector
	 * @param userAccount
	 * @param appAttrs
	 * @param userAndAgentDialog 
	 * @throws CCPServiceException
	 */
	void userDisconnect(Connector connector, String userAccount, AppAttrs appAttrs, UserAndAgentDialog userAndAgentDialog) 
			throws CCPServiceException;
	
	/**
	 * @Description: 接收唤醒用户请求
	 * @param wakeupUserForm 
	 * @throws CCPServiceException
	 */
	void wakeUpUser(WakeupUserForm wakeupUserForm) throws CCPServiceException;
}
