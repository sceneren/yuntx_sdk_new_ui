package com.yuntongxun.mcm.mcm.controller;

import java.io.IOException;

import org.ming.sample.json.JSONUtil;

import com.yuntongxun.mcm.core.AbstractController;
import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.connection.ModuleProducter;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MsgLite;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.mcm.service.MCMService;
import com.yuntongxun.mcm.model.Connector;

public class MCMController extends AbstractController {

	public static final int EVENT_MCM_MESSAGE = 126;
	
	public static final int EVENT_CONNECTOR_CLOSE_MESSAGE = 76;

	private MCMService mcmService;

	private ModuleProducter moduleProducter;
	
	public MCMController() {
	}

	public void init() {
		register(this, new int[] {EVENT_MCM_MESSAGE, EVENT_CONNECTOR_CLOSE_MESSAGE});
	}

	@Override
	public void postRequest(String channelId, MsgLiteInner msgLite) throws Exception {
		Connector connector = getConnector(msgLite.getProtoBackExp());
		if(connector == null){
			logger.warn("get connector is null");
			return;
		}
		connector.setChannelId(channelId);
		
		try {
			if (msgLite.getProtoType() == EVENT_MCM_MESSAGE) {
				mcmService.handleMcmReceivedMessage(msgLite, connector);
				
			} else if (msgLite.getProtoType() == EVENT_CONNECTOR_CLOSE_MESSAGE) {
				mcmService.handlerConnectorCloseReceivedMessage(msgLite, connector);
				
			} else {
				logger.warn("Not Support: protoType [{}]... \r\n\r\n",msgLite.getProtoType());
			}
			
		} catch (CCPServiceException se) {
			logger.error("postRequest#CCPServiceException()", se);
			handleException(se, msgLite.getProtoType(), msgLite.getProtoClientNo(), msgLite.getProtoSource(), 
					connector.getSessionId(), channelId);
			return;
		}
	}

	/**
	 * @Description: 获取connector相关信息
	 * @param protoBackExp
	 */
	private Connector getConnector(String protoBackExp){
		try {
			Connector connector = (Connector) JSONUtil.jsonToObj(protoBackExp, Connector.class);
			return connector;
		} catch (Exception e) {
			logger.error("getConnector#error()", e);
			return null;
		}
	}
	
	/**
	 * @param se
	 * @param protoType
	 * @param protoClientNo
	 * @param protoSource
	 * @throws IOException
	 */
	private void handleException(CCPServiceException se, int protoType, int protoClientNo, 
			String protoSource, String sessionId, String channelId) throws IOException {
		String errorCode = se.getErrorCode();
		MsgLite.MsgLiteInner msgLite = MsgLiteFactory.getMsgLite(protoType, protoClientNo, sessionId, 
				Integer.parseInt(errorCode));
		logger.info("channelId : {}", channelId);
		moduleProducter.replyBytesMessage(channelId, msgLite);
	}

	@Override
	protected String getName() {
		return MCMController.class.getName();
	}

	public void setMcmService(MCMService mcmService) {
		this.mcmService = mcmService;
	}

	public void setModuleProducter(ModuleProducter moduleProducter) {
		this.moduleProducter = moduleProducter;
	}
	
}
