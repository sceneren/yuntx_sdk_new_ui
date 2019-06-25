package com.yuntongxun.mcm.mcm.service.impl;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yuntongxun.tools.protocol.tcp.client.TCPClient;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.listener.RMServerHandlerListener;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;

public class RMServerRequestServiceImpl implements RMServerRequestService {

	public static final Logger logger = LogManager.getLogger(RMServerRequestServiceImpl.class);
	
	private String requestNo;
	
	private String[] host;
	  
	private int port;
	
	private TCPClient tcpClient;
	
	private ExecutorService executorService;
	
	private Integer isConnect;
	
	public void init(){
		if(isConnect == null){
			isConnect = 1;
		}
		logger.info("is connect rm server: {}.", isConnect);
		
		if(isConnect == 1){
			logger.info("start connect rm server, host: {}, port: {}, requestNo: {}.", 
					host, port, requestNo);
			
			executorService = Executors.newSingleThreadExecutor();
			
			try {
				tcpClient.setHost(host);
				tcpClient.setPort(port);
				tcpClient.connectRMServer(requestNo, new RMServerHandlerListener(tcpClient));
			} catch (Exception e) {
				logger.error("connect rm server error: ", e);
			}
		}
	}
	
	@Override
	public void doPushMessage(final String message) throws CCPServiceException {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				long startTime = System.currentTimeMillis();
				try {
					if(StringUtils.isNotBlank(sessionId)){
						ThreadContext.push(sessionId);	
					}
					PrintUtil.printStartTag("RmServer Req");
					
					logger.info("start send: {} to rm server.", message);
					
					if(StringUtils.isBlank(message)){
						logger.info("start send message to rm server fail, message is null.");
						
					}else{
						for(String sessionId : Constants.channelMap.keySet()){
							Channel channel = Constants.channelMap.get(sessionId);
							logger.info("sessionId: {}, channel: {}.", sessionId, channel);
							
							tcpClient.sendRMServer(channel, message.getBytes());
						}	
					}
					
				} catch (Exception e) {
					logger.error("RMServerRequest#run(), ", e);
				} finally{
					long endTime = System.currentTimeMillis();
					long cosTime = endTime - startTime;
					logger.info("send request to rm server finished, cost time: {} ms.",  cosTime);
					
					PrintUtil.printEndTag("RmServer Req");
					ThreadContext.removeStack();
				}
			}
			
		});
	}
	
	/**
	 * set inject
	 */
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}

	public void setHost(String[] host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTcpClient(TCPClient tcpClient) {
		this.tcpClient = tcpClient;
	}

	public void setIsConnect(Integer isConnect) {
		this.isConnect = isConnect;
	}
	
}


