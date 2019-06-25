/**
 * 
 */
package com.yuntongxun.mcm.service;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;

/**
 * @author weily
 */
public interface MailGWService {

	/**
	 * 处理邮件消息
	 * @param sendMsg
	 * @param connector
	 * @param appAttrs
	 * @throws CCPServiceException 
	 */
	public void handleMailMsg(McmMailMsgInfo requestData) throws CCPServiceException;

	/**
	 * 发送邮件消息
	 * @param sendMsg
	 * @param connector
	 * @param appAttrs
	 */
	public void sendMail(McmMailMsgInfo requestData);
	
	/**
	 * 处理邮件配置请求
	 * @param requestData
	 * @return 
	 * @throws CCPServiceException 
	 */
	public String handleMailConfig(McmMailMsgInfo requestData) throws CCPServiceException;
}
