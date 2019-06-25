package com.yuntongxun.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.mcm.form.McmInfoForm;
import com.yuntongxun.mcm.mcm.model.ASDataInfo;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;

public interface AsService {

	/*public void doAskHttpClient(MCMDataInner sendMsg,Connector connector, AppAttrs appAttrs, int protoClientNo);*/
	
	/*public void doEndHttpClient(MCMDataInner sendMsg, Connector connector, AppAttrs appAttrs);*/
	
	/*public void doReceiveAsFile(MCMDataInner sendMsg, Connector connector);*/
	
	public void handleRevicedMessage(McmInfoForm mcm) throws CCPServiceException ;

	/**
	 * 向第三方推送邮件消息
	 * @param mailMsg
	 * @param appAttrs
	 */
	public void pushMailToAs(McmMailMsgInfo mailMsg, AppAttrs appAttrs);

	/**
	 * 向第三方推送微信消息
	 * @param weixinMsg
	 * @param appAttrs
	 */
	public void pushWeiXinToAs(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs);

	/**
	 * 用户开始咨询（20160224 weily）
	 * @param sendMsg
	 * @param connector
	 * @param appInfo
	 * @param protoClientNo
	 * @param chanType
	 * @throws CCPServiceException 
	 */
	public void imStartAsk(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) throws CCPServiceException;

	/**
	 * 用户结束咨询（20160224 weily）
	 * @param sendMsg
	 * @param connector
	 * @param appInfo
	 * @param protoClientNo
	 * @throws CCPServiceException 
	 */
	public void endAsk(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) throws CCPServiceException;
	
	/**
	 * 操作回调请求AS侧
	 * @param userAndAgentDialog
	 * @param rmServerTransferData 
	 * @return
	 */
	public ASDataInfo optCallbackRequestAS(UserAndAgentDialog userAndAgentDialog, RMServerTransferData rmServerTransferData);

	/**
	 * 向AS发送用户停止咨询请求
	 * @param userAndAgentDialog
	 * @param rmServerTransferData
	 */
	public boolean stopMsgRequestAS(UserAndAgentDialog userAndAgentDialog);

	/**
	 * 微信端发起的开始用户咨询
	 * @param weixinMsg
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	public void weixinStartAsk(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException;

	/**
	 * 开始咨询请求到as侧
	 * @param messageBody
	 * @param asUrl
	 * @throws
	 */
	public ASDataInfo startAskRequestAS(final String messageBody, String asUrl);
	
	/**
	 * 用户断开连接
	 * @param connector
	 * @param appAttrs
	 * @param userAndAgentDialog
	 * @throws CCPServiceException
	 */
	public void userDisconnect(Connector connector, String userAcc, AppAttrs appAttrs, 
			UserAndAgentDialog userAndAgentDialog) throws CCPServiceException;

}
