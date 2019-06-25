package com.yuntongxun.mcm.mcm.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yuntongxun.mcm.cc.service.ICCService;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.genesys.service.GenesysUserService;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.service.IMUserService;
import com.yuntongxun.mcm.mcm.service.UserService;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.sevenmoor.service.SevenMoorService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

public class UserServiceImpl implements UserService{

	public static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
	
	private IMUserService imUserService;

	private GenesysUserService genesysUserService; 
	
	private ICCService ccService; 
	
	private SevenMoorService sevenMoorService;
	
	@Override
	public void startAsk(MCMDataInner sendMsg, Connector connector, int protoClientNo, 
			AppAttrs appAttrs) throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(StringUtils.isNotBlank(appAttrs.getMcm_notify_url())){
			logger.debug("mcmNotifyUrl: {}.", appAttrs.getMcm_notify_url());
			
			ccService.startMessage(sendMsg, connector, appAttrs, protoClientNo);
		} else{
			if(CCSType == Constants.CCS_TYPE_GENESYS){
				genesysUserService.startAsk(sendMsg, connector, userAcc);
			}else if(CCSType == Constants.CCS_TYPE_7MOOR){
				sevenMoorService.sdkLogin(sendMsg, connector, userAcc, protoClientNo);
			}else{
				imUserService.imStartAsk(sendMsg, connector, userAcc,protoClientNo);
			}
		}
	}

	@Override
	public void endAsk(MCMDataInner sendMsg, Connector connector, int protoClientNo, 
			AppAttrs appAttrs) throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(StringUtils.isNotBlank(appAttrs.getMcm_notify_url())){
			logger.debug("mcmNotifyUrl: {}.", appAttrs.getMcm_notify_url());
			
			ccService.startMessage(sendMsg, connector, appAttrs, protoClientNo);
		} else{
			if(CCSType == Constants.CCS_TYPE_GENESYS){
				genesysUserService.endAsk(sendMsg, connector, userAcc);
			}else if(CCSType == Constants.CCS_TYPE_7MOOR){
				sevenMoorService.endAsk(sendMsg, connector, userAcc,protoClientNo);
			}else{
				imUserService.endAsk(sendMsg, connector, userAcc,protoClientNo);
			}
		}
	}

	@Override
	public void sendMSG(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			genesysUserService.sendMSG(sendMsg, connector, userAcc);
		}else if(CCSType == Constants.CCS_TYPE_7MOOR){
			sevenMoorService.sdkNewMsg(sendMsg, connector, userAcc, protoClientNo);
		}else{
			imUserService.sendMSG(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void getAGList(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imUserService.getAGList(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void ircn(MCMDataInner sendMsg, Connector connector, int protoClientNo)
			throws CCPServiceException {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		}else{
			imUserService.ircn(sendMsg, connector, userAcc, protoClientNo);
		}
	}

	@Override
	public void userDisconnect(Connector connector, AppAttrs appAttrs, String userAcc, 
			UserAndAgentDialog userAndAgentDialog) throws CCPServiceException {
		int ccsType = userAndAgentDialog.getCCSType();
		
		logger.info("@userAcc: {}, CCSType: {}.", userAcc, ccsType);
		
		if(StringUtils.isNotBlank(appAttrs.getMcm_notify_url())){
			logger.debug("mcmNotifyUrl: {}.", appAttrs.getMcm_notify_url());
			
			ccService.userDisconnect(connector, userAcc, appAttrs, userAndAgentDialog);
		} else {
			if(ccsType == Constants.CCS_TYPE_GENESYS){
				
			} else{
				imUserService.userDisconnect(connector, userAcc, userAndAgentDialog);				
			}
		}
	}


	@Override
	public void submitInvestigate(MCMDataInner sendMsg, Connector connector, int protoClientNo) {
		String userAcc = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		int CCSType = sendMsg.getCCSType();
		
		logger.info("@submitInvestigate: {}, CCSType: {}.", userAcc, CCSType);
		
		if(CCSType == Constants.CCS_TYPE_GENESYS){
			
		} else if(CCSType == Constants.CCS_TYPE_7MOOR) {
			sevenMoorService.submitInvestigate(sendMsg, connector, userAcc, protoClientNo);				
		}
	}
	
	/**
	 * set inject
	 */
	public void setImUserService(IMUserService imUserService) {
		this.imUserService = imUserService;
	}

	public void setGenesysUserService(GenesysUserService genesysUserService) {
		this.genesysUserService = genesysUserService;
	}

	public void setCcService(ICCService ccService) {
		this.ccService = ccService;
	}

	public void setSevenMoorService(SevenMoorService sevenMoorService) {
		this.sevenMoorService = sevenMoorService;
	}
	
}
