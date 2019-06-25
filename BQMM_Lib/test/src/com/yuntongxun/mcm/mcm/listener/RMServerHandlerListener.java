package com.yuntongxun.mcm.mcm.listener;

import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;
import org.yuntongxun.tools.protocol.tcp.client.TCPClient;
import org.yuntongxun.tools.protocol.tcp.client.TCPClientListener;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.model.RMServerAgent;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.service.ResponseService;
import com.yuntongxun.mcm.mcm.service.impl.ResponseServiceImpl;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.SpringUtil;
import com.yuntongxun.mcm.util.StringUtil;

public class RMServerHandlerListener implements TCPClientListener {

	public static final Logger logger = LogManager.getLogger(RMServerHandlerListener.class);

	private static boolean reconnectState = false;

	private static int reconnectCount = 1;
	
	private TCPClient tcpClient;

	private ResponseService responseService = (ResponseServiceImpl)SpringUtil.getBean("responseService");
	
	public RMServerHandlerListener(TCPClient tcpClient) {
		this.tcpClient = tcpClient;
	}

	@Override
	public void OnChannelReceived(String sessionId, Channel channel, Object obj)
			throws Exception {
		try {
			ThreadContext.push(sessionId);
			PrintUtil.printStartTag("onConnected");

			logger.info("OnChannelReceived: {}, obj: {}.", sessionId, obj);
			
			String result = null;
			if(obj != null){
				result = (String)obj;
			}else{
				logger.error("RM Server return string is empty,return...");
				return;
			}
			
			RMServerTransferData rmServerTransferData = null;
			try {
				@SuppressWarnings("rawtypes")
				Map<String, Class> classMap = new HashMap<String, Class>();
				classMap.put("idleagents", RMServerAgent.class);
				
				rmServerTransferData = (RMServerTransferData)JSONUtil.jsonToObj(result, RMServerTransferData.class, classMap);
			} catch (JSONException e) {
				rmServerTransferData = null;
				logger.error("OnChannelReceived obj parse error, ", e);
			}
			
			if(rmServerTransferData == null){
				logger.error("received get rmServerTransferData is null.");
			}else{
				responseService.handleRMResponse(rmServerTransferData);	
			}
			
		} catch (Exception e) {
			logger.error("OnChannelReceived#run()", e);
		} finally {
			PrintUtil.printEndTag("onConnected");
			ThreadContext.removeStack();
		}
	}

	@Override
	public void OnConnected(String sessionId, Channel channel) throws Exception {
		try {
			ThreadContext.push(sessionId);

			if (reconnectState) {
				reconnectState = false;
				reconnectCount = 1;
				logger.info("reconnect is succeed.");
			}

			// 连接建立成功
			logger.info("OnConnected sessionId:{}", sessionId);
			
			//保存sessionId、channel
			Constants.channelMap.put(sessionId, channel);
			
			//登录RM服务器
			loginRMServer(channel);
		} catch (Exception e) {
			logger.error("OnConnected#run()", e);
		} finally {
			ThreadContext.removeStack();
		}
	}
	
	@Override
	public void OnDisconnected(String sessionId, Channel channel)
			throws Exception {
		try {
			ThreadContext.push(sessionId);
			// 断开连接
			logger.info("OnDisconnected:{}", sessionId);

			reconnectState = true;
			// 重连三次不成功，就主动断开连接（websocket,connector都断开）
			channel.close();
			while (reconnectState) {
				logger.info("reconnect Count:{}", reconnectCount);
				
				try {
					tcpClient.reconnect(sessionId);
				} catch (Exception e) {
					logger.error("reconnect error: ", e);
					continue;
				} finally{
					try {
						reconnectCount++;
						Thread.sleep(1000 * 10);
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			logger.error("OnDisconnected#run()", e);
		} finally {
			ThreadContext.removeStack();
		}

	}

	@Override
	public void OnExceptionCaught(String sessionId, Channel channel,
			Throwable cause) throws Exception {
		try {
			ThreadContext.push(sessionId);
			// 底层报错
			logger.info("OnExceptionCaught:{}. Throwable:{}", sessionId, cause);

			reconnectState = true;
			channel.close();
			while (reconnectState) {
				logger.info("reconnect Count:{}", reconnectCount);
				
				try {
					tcpClient.reconnect(sessionId);
				} catch (Exception e) {
					logger.error("reconnect error: ", e);
					continue;
				} finally{
					try {
						reconnectCount++;
						Thread.sleep(1000 * 10);
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			logger.error("OnExceptionCaught#run()", e);
		} finally {
			ThreadContext.removeStack();
		}
	}
	
	private void loginRMServer(Channel channel) {
		String command = CommandEnum.CMD_MCCC_MODULE_LOGIN.getValue();
		String seq = EncryptUtil.base64Encoder(StringUtil.getUUID());
		String request = "{\"command\":\""+command+"\",\"cmserial\":"+Constants.M3C_SERIAL+",\"seq\":\""+seq+"\",\"autorestart\":\"false\"}";
		tcpClient.sendRMServer(channel, request.getBytes());
	}

	@Override
	public void OnChannelBothIdle(Channel channel, IdleStateEvent ise)
			throws Exception {
		logger.info("OnChannelBothIdle:{}. IdleStateEvent:{}", channel, ise);
	}
	
}
