package com.yuntongxun.mcm.core.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yuntongxun.tools.balance.SimpleClient;
import org.yuntongxun.tools.balance.SimpleServer;

import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;

public class ModuleProducter {

	public static final Logger logger = LogManager.getLogger(ModuleProducter.class);
	
	private static ModuleProducter instance;

	private SimpleClient simpleClient;
	
	private SimpleServer simpleServer;

	public ModuleProducter() {
		instance = this;
	}

	public static ModuleProducter getInstance() {
		return instance;
	}

	/**
	 * @param destinationName
	 * @param msgLite
	 */
	public void sendBytesMessage(final String destinationName, final MsgLiteInner msgLite) {
		long startTime = System.currentTimeMillis();
		try {
			logger.info("send Message to [{}], data : [{}]", destinationName, MsgLiteFactory.printMsgLite(msgLite));
			simpleClient.sendBytesMessage(destinationName, msgLite);
		} catch (Exception e) {
			logger.error("ModuleProducter#sendBytesMessage: ", e);
		} finally{
			long cosTime = System.currentTimeMillis() - startTime;
			logger.info("send message to[{}] finshed, costTime: {}.", msgLite.getProtoType(), cosTime);
		}
	}

	public void replyBytesMessage(String channelId, MsgLiteInner msgLite) {
		long startTime = System.currentTimeMillis();
		try {
			logger.info("reply Message to channel [{}], data : [{}]", channelId, MsgLiteFactory.printMsgLite(msgLite));
			simpleServer.sendBytesMessage(channelId, msgLite);
		} catch (Exception e) {
			logger.error("ModuleProducter#replyBytesMessage: ", e);
		} finally{
			long cosTime = System.currentTimeMillis() - startTime;
			logger.info("reply message finshed, costTime: {}.", cosTime);
		}
	}
	
	/**
	 * @return the simpleClient
	 */
	public SimpleClient getSimpleClient() {
		return simpleClient;
	}

	public void setSimpleClient(SimpleClient simpleClient) {
		this.simpleClient = simpleClient;
	}

	public SimpleServer getSimpleServer() {
		return simpleServer;
	}

	public void setSimpleServer(SimpleServer simpleServer) {
		this.simpleServer = simpleServer;
	}

}
