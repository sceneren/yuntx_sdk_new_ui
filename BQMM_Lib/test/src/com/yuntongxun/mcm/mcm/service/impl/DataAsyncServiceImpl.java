package com.yuntongxun.mcm.mcm.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.mcm.dao.M3CSMessageHistoryDao;
import com.yuntongxun.mcm.mcm.model.M3CSMessageHistory;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.service.DataAsyncService;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.StringUtil;

public class DataAsyncServiceImpl implements DataAsyncService{

	public static final Logger logger = LogManager.getLogger(DataAsyncServiceImpl.class);
	
	private ExecutorService executorService;
	
	private M3CSMessageHistoryDao m3csMessageHistoryDao;
	
	public void init() {
		executorService = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void saveM3CSMessageHistory(MCMDataInner sendMsg, Connector connector, String receiverUserAcc, 
			String agentId, String resultCode) throws CCPServiceException {
		save(sendMsg, null, -1, connector, null, "", "", receiverUserAcc, agentId, resultCode);
	}
	
	@Override
	public void saveM3CSMessageHistory(MCMDataInner sendMsg, MSGDataInner msgData, long version, 
			Connector connector, UserAndAgentDialog userAndAgentDialog, String msgId, 
			String monitorAgentId, String receiverUserAcc, String agentId) {
		save(sendMsg, msgData, version, connector, userAndAgentDialog, msgId, monitorAgentId, 
				receiverUserAcc, agentId, "");
	}
	
	/**
	 * @Description: 记录坐席坐席及对话的相关信息
	 * @param sendMsg
	 * @param msgData
	 * @param version
	 * @param connector
	 * @param userAndAgentDialog
	 * @param msgId
	 * @param monitorAgentId
	 * @param receiverUserAcc
	 * @param agentId
	 * @param resultCode
	 */
	private void save(final MCMDataInner sendMsg, final MSGDataInner msgData, 
			final long version, final Connector connector, final UserAndAgentDialog userAndAgentDialog, 
			final String msgId, final String monitorAgentId, final String receiverUserAcc, 
			final String agentId, final String resultCode){
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				try {
					ThreadContext.push(sessionId);
					PrintUtil.printStartTag("M3CSMessageInsert");
					
					M3CSMessageHistory m3csMessageHistory = buildM3CSMessageHistory(sendMsg, msgData, version,
							connector, userAndAgentDialog, msgId, monitorAgentId, 
							receiverUserAcc, agentId, resultCode);
					m3csMessageHistoryDao.insert(m3csMessageHistory);
				} catch (CCPDaoException e) {
					logger.error("save m3cs message history error, ", e);
					
				}finally{
					logger.info("save m3cs message history finish, cost time: {}", System.currentTimeMillis() - startTime);
					
					PrintUtil.printEndTag("M3CSMessageInsert");
					ThreadContext.removeStack();
				}
			}
			
		});
	}
	
	/**
	 * @Description: 封装持久化消息对象
	 * @param sendMsg
	 * @param msgData
	 * @param version
	 * @param connector
	 * @param userAndAgentDialog
	 * @param type 1:坐席 2:用户
	 * @param msgId
	 */
	private M3CSMessageHistory buildM3CSMessageHistory(MCMDataInner sendMsg, MSGDataInner msgData, Long version, 
			Connector connector, UserAndAgentDialog userAndAgentDialog, String msgId, 
			String monitorAgentId, String receiverUserAcc, String agentId, 
			String resultCode){
		M3CSMessageHistory m3csMessageHistory = new M3CSMessageHistory();

		if(userAndAgentDialog != null){
			String receiverAppId = StringUtil.getAppIdFormUserAcc(receiverUserAcc);
			String receiverUserName = StringUtil.getUserNameFormUserAcc(receiverUserAcc);
			
			m3csMessageHistory.setSid(userAndAgentDialog.getSid());
			m3csMessageHistory.setOsUnityAccount(userAndAgentDialog.getOsUnityAccount());
			m3csMessageHistory.setSkillGroupId(userAndAgentDialog.getSkillGroupId());
			
			m3csMessageHistory.setAppIdReceiver(receiverAppId);
			m3csMessageHistory.setMsgReceiver(receiverUserName);
		}
		
		if(msgData != null){
			m3csMessageHistory.setMsgLen("");
			m3csMessageHistory.setMsgType(msgData.getMsgType());
			m3csMessageHistory.setMsgContent(msgData.getMsgContent());
			m3csMessageHistory.setMsgFileUrl(msgData.getMsgFileUrl());
			m3csMessageHistory.setMsgFileName(msgData.getMsgFileName());
		}
		
		if(StringUtils.isNotBlank(resultCode)){
			m3csMessageHistory.setResultCode(resultCode);
		}
		
		m3csMessageHistory.setCCSType(sendMsg.getCCSType());
		m3csMessageHistory.setEventType(sendMsg.getMCMEvent());
		m3csMessageHistory.setChannel(String.valueOf(Constants.M3C_SERIAL));
		m3csMessageHistory.setAppIdSender(connector.getAppId());
		m3csMessageHistory.setMsgSender(connector.getUserName());
		
		m3csMessageHistory.setDeviceNo(connector.getDeviceNo());
		m3csMessageHistory.setDeviceType(connector.getDeviceType());
		m3csMessageHistory.setVersion(version);
		m3csMessageHistory.setGroupId("");
		m3csMessageHistory.setMsgId(msgId);
		
		m3csMessageHistory.setMsgFileSize("");
		m3csMessageHistory.setMsgDomain("");
		m3csMessageHistory.setMsgCompressLen(0);
		m3csMessageHistory.setMcmEvent(1);
		m3csMessageHistory.setStatus(1);
		
		m3csMessageHistory.setType(Constants.MSG_TYPE_IM);
		m3csMessageHistory.setMsgDomainFlag(0);
		m3csMessageHistory.setAgentId(agentId);
		
		if(StringUtils.isNotEmpty(monitorAgentId)){
			m3csMessageHistory.setMonitorAgentId(monitorAgentId);
			m3csMessageHistory.setType(Constants.MSG_TYPE_MONITOR);
		}
		
		return m3csMessageHistory;
	}
	
	/**
	 * set inject
	 */
	public void setM3csMessageHistoryDao(M3CSMessageHistoryDao m3csMessageHistoryDao) {
		this.m3csMessageHistoryDao = m3csMessageHistoryDao;
	}
	
}
