package com.yuntongxun.mcm.mcm.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.genesys.service.GenesysAgentService;
import com.yuntongxun.mcm.mcm.service.AgentService;
import com.yuntongxun.mcm.mcm.service.IMAgentService;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

public class AgentServiceImpl implements AgentService{

	public static final Logger logger = LogManager.getLogger(AgentServiceImpl.class);

	private GenesysAgentService genesysAgentService;
	
	private IMAgentService imAgentService;
	
	@Override
	public void onWork(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@onWork, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.onWork(sendMsg, connector, userAcc, protoClientNo);
		}else{
			imAgentService.onWork(sendMsg, connector, userAcc, protoClientNo);
		}
	}
	
	@Override
	public void offWork(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@offWork, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.offWork(sendMsg, connector, userAcc, protoClientNo);
		}else{
			imAgentService.offWork(sendMsg, connector, userAcc, protoClientNo);
		}
	}
	
	@Override
	public void ready(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@ready, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.ready(sendMsg, connector, userAcc);
		}else{
			imAgentService.ready(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void notReady(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@notReady, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.notReady(sendMsg, connector, userAcc);
		}else{
			imAgentService.notReady(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void startSerWithUser(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@startSerWithUser, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.startSerWithUser(sendMsg, connector, userAcc);
		}else{
			imAgentService.startSerWithUser(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void stopSerWithUser(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@stopSerWithUser, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.stopSerWithUser(sendMsg, connector, userAcc);
		}else{
			imAgentService.stopSerWithUser(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void sendMCM(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@sendMCM, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.sendMCM(sendMsg, connector, userAcc);
		}else{
			imAgentService.sendMCM(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void sendNotify(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@sendNotify, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.sendNotify(sendMsg, connector, userAcc);
		}else{
			
		}
	}

	@Override
	public void makeCall(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@makeCall, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.makeCall(sendMsg, connector, userAcc);
		}else{
			
		}
	}

	@Override
	public void answerCall(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@answerCall, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.answerCall(sendMsg, connector, userAcc);
		}else{
			
		}
	}

	@Override
	public void releaseCall(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@releaseCall, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.releaseCall(sendMsg, connector, userAcc);
		}else{
			
		}
	}

	@Override
	public void startConf(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@startConf, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysAgentService.startConf(sendMsg, connector, userAcc);
		}else{
			imAgentService.startConf(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void joinConf(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@joinConf, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
		}else{
			imAgentService.joinConf(sendMsg, connector, userAcc, protoClientNo);
		}
	}
	
	@Override
	public void exitConf(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@exitConf, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
		}else{
			imAgentService.exitConf(sendMsg, connector, userAcc, protoClientNo);
		}
		
	}
	
	@Override
	public void stateOpt(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@stateOpt, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.stateOpt(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void transKF(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@transKF, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.transKF(sendMsg, connector, userAcc, protoClientNo);
	}

	@Override
	public void enterCallService(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
	}

	@Override
	public void rejectUser(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@rejectUser, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.rejectUser(sendMsg, connector, userAcc, protoClientNo);
	}

	@Override
	public void transferQueue(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@transferQueue, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.transferQueue(sendMsg, connector, userAcc, protoClientNo);
	}

	@Override
	public void forceJoinConf(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@forceJoinConf, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.forceJoinConf(sendMsg, connector, userAcc, protoClientNo);
	}

	@Override
	public void forceTransfer(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@forceTransfer, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.forceTransfer(sendMsg, connector, userAcc, protoClientNo);
	}
	
	@Override
	public void forceEndService(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@forceEndService, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		imAgentService.forceEndService(sendMsg, connector, userAcc, protoClientNo);
	}

	@Override
	public void startSessionTimer(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@startSessionTimer, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.startSessionTimer(sendMsg, connector, userAcc, protoClientNo);
		}
	}
	
	@Override
	public void monitorAgent(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@monitorAgent, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.monitorAgent(sendMsg, connector, userAcc, protoClientNo);
		}
		
	}
	
	@Override
	public void cancelMonitorAgent(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@cancelMonitorAgent, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.cancelMonitorAgent(sendMsg, connector, userAcc, protoClientNo);
		}
		
	}
	
	@Override
	public void queryQueueInfo(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@queryQueueInfo, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.queryQueueInfo(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void queryAgentInfo(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@queryAgentInfo, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.queryAgentInfo(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void serWithTheUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@serWithTheUser, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.serWithTheUser(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void reservedForUser(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@reservedForUser, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.reservedForUser(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void cancelReserved(MCMDataInner sendMsg, Connector connector, int protoClientNo) 
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@cancelReserved, userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imAgentService.cancelReserved(sendMsg, connector, userAcc, protoClientNo);
		}
	}
	
	/**
	 * set inject
	 */
	public void setGenesysAgentService(GenesysAgentService genesysAgentService) {
		this.genesysAgentService = genesysAgentService;
	}

	public void setImAgentService(IMAgentService imAgentService) {
		this.imAgentService = imAgentService;
	}
	
}
