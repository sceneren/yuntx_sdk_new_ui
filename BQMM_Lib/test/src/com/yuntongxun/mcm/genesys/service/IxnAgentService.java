 package com.yuntongxun.mcm.genesys.service;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;

import com.genesyslab.platform.applicationblocks.commons.Action;
import com.genesyslab.platform.applicationblocks.commons.broker.EventReceivingBrokerService;
import com.genesyslab.platform.applicationblocks.commons.broker.MessageFilter;
import com.genesyslab.platform.applicationblocks.commons.protocols.InteractionServerConfiguration;
import com.genesyslab.platform.applicationblocks.commons.protocols.ProtocolManagementServiceImpl;
import com.genesyslab.platform.commons.collections.KeyValueCollection;
import com.genesyslab.platform.commons.collections.KeyValuePair;
import com.genesyslab.platform.commons.protocol.ChannelClosedEvent;
import com.genesyslab.platform.commons.protocol.ChannelErrorEvent;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.genesyslab.platform.commons.protocol.ChannelState;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.Protocol;
import com.genesyslab.platform.commons.threading.SingleThreadInvoker;
import com.genesyslab.platform.openmedia.protocol.InteractionServerProtocol;
import com.genesyslab.platform.openmedia.protocol.interactionserver.InteractionClient;
import com.genesyslab.platform.openmedia.protocol.interactionserver.InteractionOperation;
import com.genesyslab.platform.openmedia.protocol.interactionserver.InteractionProperties;
import com.genesyslab.platform.openmedia.protocol.interactionserver.PartyInfo;
import com.genesyslab.platform.openmedia.protocol.interactionserver.ReasonInfo;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventAck;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventCurrentAgentStatus;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventError;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventForcedAgentStateChange;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventInvite;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventPartyAdded;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventPartyRemoved;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventRejected;
import com.genesyslab.platform.openmedia.protocol.interactionserver.events.EventRevoked;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.agentmanagement.RequestAgentLogin;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.agentmanagement.RequestAgentLogout;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.agentmanagement.RequestCancelNotReadyForMedia;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.agentmanagement.RequestNotReadyForMedia;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactiondelivery.RequestAccept;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactiondelivery.RequestReject;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactionmanagement.RequestChangeProperties;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactionmanagement.RequestConference;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactionmanagement.RequestPlaceInQueue;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactionmanagement.RequestStopProcessing;
import com.genesyslab.platform.openmedia.protocol.interactionserver.requests.interactionmanagement.RequestTransfer;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.genesys.enumerate.AgentStatus;
import com.yuntongxun.mcm.genesys.enumerate.InteractionStatus;
import com.yuntongxun.mcm.genesys.model.IxnLogin;
import com.yuntongxun.mcm.genesys.model.OpenMediaInteraction;
import com.yuntongxun.mcm.genesys.util.CommonUtils;
import com.yuntongxun.mcm.genesys.util.ReferenceIdUtils;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.Constants;

public class IxnAgentService implements ChannelListener{
	
	private static final Logger logger = LogManager.getLogger(IxnAgentService.class);
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	private EventReceivingBrokerService eventBrokerService;
	
	private InteractionServerEventHandler eventHandler = new InteractionServerEventHandler();
	
	private final Map<String, OpenMediaInteraction> interactions = new ConcurrentHashMap<String, OpenMediaInteraction>();
	
	private Protocol ixnProtocol;
	
	private Map<String, ChatAgentService> chatServices = new ConcurrentHashMap<String, ChatAgentService>();
	
	private final Map<String, AgentStatus> agentStatus = new HashMap<String, AgentStatus>();
	
	private final Map<String, Integer> agentCapacity = new HashMap<String, Integer>();
	
	private ProtocolManagementServiceImpl protocolManagementService;
	
	private String IXNSERVER_IDENTIFIER;
	
	private String clientName;
	
	private KeyValueCollection mediaList;
	
	private List<String> chatMediaList = new ArrayList<String>();
	
	private boolean isLoggedIn = false;
	
	private boolean isForceLoggedOut = false;
	
	private boolean isInitialized = true;
	
	private boolean isPushLogin = false;
	
	private boolean isReady;
	
	private boolean isPushReady = false;
	
	private boolean isNotReady;
	
	private boolean isPushNotReady = false;
	
	/**
	 * 保存坐席登录后的相关信息
	 */
	private IxnLogin ixnLogin;
	
	private PushService pushService;
	private String tenantId;
	private FileServerService fileServerService;
	
	private VersionDao versionDao;
	
	/**
	 * @Description 初始化 Ixn Server
	 * @params
	 * @return
	 */
	public int init(IxnLogin ixnLogin, String serverIP, String serverPort, String tenantId) {
		int isResult = 1;
		try {
			this.ixnLogin = ixnLogin;
			//this.pushService = pushService;
			this.tenantId = tenantId;
			//this.fileServerService = fileServerService;
			this.mediaList = new KeyValueCollection();
			//this.versionDao = versionDao;
			
			this.IXNSERVER_IDENTIFIER = "softphone-ixnclient_" + ixnLogin.getUserAcc();
			this.clientName = "softphone_" + ixnLogin.getUserAcc();
					
			if(ixnLogin.getMedia() != null && ixnLogin.getMedia().length() != 0){
				for(String mediaType : ixnLogin.getMedia().split(",")){
					this.mediaList.addInt(mediaType, 11);	
				}
			}
			
			if (protocolManagementService != null) {
				throw new Exception("ixnServiceImpl has been instantiated.");
			}
			
			InteractionServerConfiguration ixnServerConfiguration = new InteractionServerConfiguration(IXNSERVER_IDENTIFIER);
			ixnServerConfiguration.setClientName(clientName);
			ixnServerConfiguration.setClientType(InteractionClient.AgentApplication);
			
			String chatTypeList = ixnLogin.getChatType();
			if(chatTypeList != null && chatTypeList.length() != 0) {
				for(String media : chatTypeList.split(",")){
					this.chatMediaList.add(media);
				}
			}
				
			ixnServerConfiguration.setUri(new URI("tcp://" + serverIP + ":" + serverPort));
					
			protocolManagementService = new ProtocolManagementServiceImpl();
			protocolManagementService.register(ixnServerConfiguration);
			protocolManagementService.addChannelListener((ChannelListener)this);
			
			eventBrokerService = new EventReceivingBrokerService(new SingleThreadInvoker("IxnServerEventReceivingBrokerService"));
			eventBrokerService.register((Action<Message>) eventHandler, 
					new MessageFilter(protocolManagementService.getProtocol(IXNSERVER_IDENTIFIER).getProtocolId()));
			
			ixnProtocol = protocolManagementService.getProtocol(IXNSERVER_IDENTIFIER);
			ixnProtocol.setReceiver(eventBrokerService);
			
		} catch(Exception e) {
			logger.error("IxnAgentService init error.", e);
			sendExceptionMsg(48, "login inx server init failed.");
			return 0;
		}
		
		return isResult;
	}
	
	public Protocol getProtocol() {
		return ixnProtocol;
	}
	
	protected void throwIfNotConnected() throws Exception {
		if (protocolManagementService == null || ixnProtocol == null
				|| ixnProtocol.getState() != ChannelState.Opened) {
			throw new Exception("connection is not opened.");
		}
	}
	
	/**
	 * @Description 连接IxnServer
	 * @params
	 * @return
	 */
	public int connect(){
		logger.info("begin to connect to ixn server.");
		
		try {
			ixnProtocol.open();
			
			logger.info("ixn server protocol state is " + ixnProtocol.getState().toString());
		} catch (Exception ex) {
			logger.error("an exception occurred when connect to interaction server.", ex);
			sendExceptionMsg(48, "login inx server connect server failed.");
			return 0;
		}
		
		logger.info("begin to get ixn service.");
		return 1;
	}
	
	/**
	 * @Description 断开IxnServer
	 * @params
	 * @return
	 */
	public void disconnect() {
		try {
			if (chatServices != null && !chatServices.isEmpty()) {
				Iterator<Entry<String, ChatAgentService>> iter = chatServices.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, ChatAgentService> entry = iter.next();
					try {
						entry.getValue().disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				chatServices.clear();
			}
		
			if ((ixnProtocol != null) && (ixnProtocol.getState() == ChannelState.Opened)) {
				ixnProtocol.close();
				eventBrokerService.unregister(eventHandler);
				protocolManagementService.unregister(IXNSERVER_IDENTIFIER);
			}
		
		} catch (Exception e) {
			logger.error("an exception occurred when disconnect from interaction server", e.fillInStackTrace());
		}
	}
	
	/**
	 * @Description 坐席登录
	 * @params
	 * @return
	 */
	public int login(Map<String, Map<String, String>> params) {
		try {
			String placeId = ixnLogin.getPlaceId();
			String agentId = ixnLogin.getAgentId();
			Integer tempTenantId = Integer.valueOf(tenantId);
			
			RequestAgentLogin message = RequestAgentLogin.create(tempTenantId, placeId, null);
			message.setReferenceId(ReferenceIdUtils.getReferenceId());
			message.setAgentId(agentId);
			
			//mediaList.addInt("chat", 0);
			message.setMediaList(mediaList);
			
			CommonUtils.setParam(RequestAgentLogin.class, message, params);
			
			logRequest(message.toString());
			Message msgResponse = ixnProtocol.request(message);
			
			if(checkReturnMessage(msgResponse, null, "login")) {
				isLoggedIn = true;
				isForceLoggedOut = false;
				return 1;
			}
		} catch (Exception e) {
			logger.error("an exception occurred when agent login, because of", e.fillInStackTrace());

			sendExceptionMsg(48, "send login inx server request to server failed.");
		}
		
		return 0;
	}
	
	/**
	 * @Description 座席签出
	 * @params
	 * @return
	 */
	public int logout(Map<String, Map<String, String>> params) {
		try {
			if ((ixnProtocol != null)
					&& (ixnProtocol.getState() == ChannelState.Opened)) {
				if(isLoggedIn) {
					
					logger.info("chatServices is :"+ chatServices);
					
					if (chatServices!=null && !chatServices.isEmpty()) {
						Iterator<Entry<String, ChatAgentService>> iter = chatServices.entrySet().iterator();
						while (iter.hasNext()) {
							Entry<String, ChatAgentService> entry = iter.next();
							ChatAgentService  cs = entry.getValue();
							if(cs!=null){
								logger.info("disconnect id is :"+ entry.getKey());
								try {
									cs.disconnect();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						chatServices.clear();
					}
					
					RequestAgentLogout message = RequestAgentLogout.create(null);
					message.setReferenceId(ReferenceIdUtils.getReferenceId());
					CommonUtils.setParam(RequestAgentLogout.class, message, params);
					
					logRequest(message.toString());
					
					Message msgResponse = ixnProtocol.request(message);
					if(checkReturnMessage(msgResponse,null,"logout")) {
						isLoggedIn = false;
						return 1;
					}
				} else {
					logger.info("agent has not been logged in or has been logged out.");
				}
			} else {
				logger.info("ixn server channel has disconnected.");
			}
		} catch (Exception e) {
			logger.error("an exception occurred when agent logout, because of ", e.fillInStackTrace());
		}
		
		return 0;
	}

	/**
	 * @Description 坐席就绪
	 * @params
	 * @return
	 */
	public int ready(String mediaType, Map<String, Map<String, String>> params) {
		try {
			if (agentStatus != null && !agentStatus.isEmpty()) {
				Iterator<Entry<String, AgentStatus>> iter = agentStatus.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, AgentStatus> entry = iter.next();
					if(mediaType!=null && mediaType.equalsIgnoreCase(entry.getKey())){
						if(entry.getValue().equals(AgentStatus.Ready)){
							continue;
						}
						RequestCancelNotReadyForMedia message = RequestCancelNotReadyForMedia.create();
						message.setMediaTypeName(entry.getKey());
						message.setReferenceId(ReferenceIdUtils.getReferenceId());
						CommonUtils.setParam(RequestCancelNotReadyForMedia.class, message, params);
						logRequest(message.toString());
						Message msgResponse = ixnProtocol.request(message);
						if(checkReturnMessage(msgResponse,null,"ready"))
							return 1;
					}
				}
			}
		} catch (Exception e) {
			logger.error("an exception occurred when agent ready, because of ", e.fillInStackTrace());
		}
		
		return 0;
	}
	
	/**
	 * @Description 坐席未就绪
	 * @params
	 * @return
	 */
	public int notReady(String mediaType, String reasonCode, String workMode11, Map<String, Map<String, String>> params) {
		try {
			RequestNotReadyForMedia message = RequestNotReadyForMedia.create(mediaType, null);
			message.setReferenceId(ReferenceIdUtils.getReferenceId());
			if(reasonCode!=null && !reasonCode.equals("")){
				ReasonInfo ri = ReasonInfo.create(reasonCode, "reasonCode");
				ri.setReason(Integer.valueOf(reasonCode));
				message.setReason(ri);
			}
			CommonUtils.setParam(RequestNotReadyForMedia.class, message, params);
			logRequest(message.toString());
			Message msgResponse = ixnProtocol.request(message);
			if(checkReturnMessage(msgResponse,null,"notReady"))
				return 1;
		} catch (Exception e) {
			logger.error("an exception occurred when agent not ready, because of ", e.fillInStackTrace());
			
		}
		
		return 0;
	}
	
	/**
	 * @Description 加入聊天室
	 * @params
	 * @return
	 */
	public int joinChatService(String interactionId){
		int result = 0;
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if (ixn != null) {
			try {
				logger.info("begin to joinChatService, interactionId is "+interactionId);

				if(isChatType(ixn.getMediaType())){
					String message = "new";
					Map<String, String> extension = ixn.getExtension();
					if(extension!=null && extension.get("operation")!=null) {
						message = extension.get("operation");
					}
					result = getChatService(interactionId).join(ixn.getConnId(), message, ixn.getVisibility());
					
					if(result == 0){
						if(isChatType(ixn.getMediaType()))
							logger.info("join a chat session failure.");
						else if(ixn.getMediaType().equalsIgnoreCase("email"))
							logger.info("accept email failure.");
					}
				}
			} catch (Exception e) {
				logger.info("an exception occurred when joinChatService, because of "+e.getMessage());
			}
		}
		return result;
		
	}
	
	/**
	 * @Description 接起会话, 分两步，先加入聊天室，再接起
	 * @params
	 * @return
	 */
	public int accept(String interactionId) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if (ixn != null) {
			try {
				logger.info("begin to accept interaction, interactionId is "+interactionId);
				
				RequestAccept request = RequestAccept.create(ixn.getTicketId(),	ixn.getConnId());
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				logRequest(request.toString());
				Message msgResponse = ixnProtocol.request(request);
				if(checkReturnMessage(msgResponse,ixn,"accept")) {
					if(ixn.getExtension()==null || ixn.getExtension().isEmpty()) {
						
					}
					logger.info("interaction accept success.");
					return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when accept interaction, because of "+e.getMessage());
			}
		}
		logger.info("interaction accept fail.");
		return 0;
	}
	
	/**
	 * @Description 挂断会话
	 * @params
	 * @return
	 */
	public int stop(String interactionId, Map<String, Map<String, String>> params) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if (ixn != null) {
			try {
				logger.info("ixn server begin to stop interaction, interactionId is "+interactionId);
				RequestStopProcessing request = RequestStopProcessing.create();
				request.setInteractionId(ixn.getConnId());
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				CommonUtils.setParam(RequestStopProcessing.class, request, params);
				logRequest(request.toString());
				Message msgResponse = ixnProtocol.request(request);
				
				if(checkReturnMessage(msgResponse,ixn,"stop")){
					interactions.remove(ixn);
					
					if(isChatType(ixn.getMediaType())) {
						try {
							getChatService(interactionId).release(ixn.getConnId(), 0, "normal");
							getChatService(interactionId).disconnect();
						} catch(Exception e) {
							logger.info("disconnect with chat server failed.");
						}
						chatServices.remove(interactionId);
					}
					try {
						
					} catch(Exception e) {
						logger.info("update interaction failed.");
					}
					logger.info("interaction stop success.");
					return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when stop interaction, because of "+e.getMessage());
			}
		}
		logger.info("interaction stop fail.");
		return 0;
	}
	
	/**
	 * @Description 坐席离开聊天室
	 * @params
	 * @return
	 */
	public int releaseChatService(String interactionId){
		int result = 0;
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if (ixn != null) {
			try {
				logger.info("begin to releaseChatService, interactionId is "+interactionId);

				if(isChatType(ixn.getMediaType())){
					/*String message = "new";
					Map<String, String> extension = ixn.getExtension();
					if(extension!=null && extension.get("operation")!=null) {
						message = extension.get("operation");
					}*/
					result = getChatService(interactionId).release(ixn.getConnId(), 0, "normal");
					getChatService(interactionId).disconnect();
					
					if(result == 0){
						if(isChatType(ixn.getMediaType()))
							logger.info("release a chat session failure.");
						else if(ixn.getMediaType().equalsIgnoreCase("email"))
							logger.info("release email failure.");
					}
				}
			} catch (Exception e) {
				logger.info("an exception occurred when release interaction, because of "+e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * 创建ChatService会话实例
	 * @param ixn
	 * @throws Exception
	 */
	private void putMultimedia(OpenMediaInteraction ixn)
			throws Exception {
		String mediaType = ixn.getMediaType();
		if(isChatType(mediaType)) {
			ChatAgentService chatService = getChatService(ixn.getConnId());
			
			if(chatService == null) {
				logger.info("create chat service instance, interactionId = "+ ixn.getConnId());
				chatService = new ChatAgentService(ixn.getUrl(), ixnLogin.getNickName(), ixn.getVisibility(), 
						ixnLogin.getUserAcc(), pushService, fileServerService, versionDao);
				
				chatServices.put(ixn.getConnId(), chatService);
				
				//for (MultimediaServiceListener listener : multimediaListeners) {
					//chatService.addListener(listener);
				//}
				
				chatService.connect();
			}
		}
	}
	
	/**
	 * @Description 得到chatServer 
	 * @params
	 * @return
	 */
	private ChatAgentService getChatService(String interactionId) {
		if (!chatServices.isEmpty()) {
			Iterator<Entry<String, ChatAgentService>> iter = chatServices.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ChatAgentService> entry = iter.next();
				if (interactionId.equals(entry.getKey())) {
					return entry.getValue();
				}
			}
		}
		return null;
	}
	
	/**
	 * @Description 发送文本消息
	 * @params
	 * @return
	 */
	public int sendChatMessage(String interactionId, String message, int visibility) {
		try {
			OpenMediaInteraction ixn = interactions.get(interactionId);
			if (ixn != null) {
				return getChatService(interactionId).sendMessage(interactionId, message, visibility);
			} else {
				logger.info("interaction is null, send message failed.");
			}
		} catch (Exception e) {
			logger.error("sendChatMessage failed.", e.fillInStackTrace());
		}
		return 0;
	}
	
	/**
	 * @Description 发送通知消息
	 * @params
	 * @return
	 */
	public int sendNotify(String interactionId, String message) {
		try {
			OpenMediaInteraction ixn = interactions.get(interactionId);
			if (ixn != null) {
				return getChatService(interactionId).sendNotify(interactionId, message);
			} else {
				logger.info("interaction is null, send notify failed.");
			}
		} catch (Exception e) {
			logger.error("sendNotify failed.", e.fillInStackTrace());
		}
		
		return 0;
	}

	/**
	 * @Description 拒绝
	 * @params
	 * @return
	 */
	public int reject(String interactionId, Map<String, Map<String, String>> params) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if (ixn != null) {
			try {
				logger.info("begin to reject interaction, interactionId is "+interactionId);
				RequestReject request = RequestReject.create(ixn.getTicketId(),	ixn.getConnId(), null);
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				CommonUtils.setParam(RequestReject.class, request, params);
				logRequest(request.toString());
				Message msgResponse = ixnProtocol.request(request);
				if(checkReturnMessage(msgResponse,ixn,"reject")) {
					interactions.remove(ixn);
					if(isChatType(ixn.getMediaType())) {
						getChatService(interactionId).disconnect();
						chatServices.remove(interactionId);
					}
					logger.info("interaction reject success.");
					return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when reject interaction, because of "+e.getMessage());
			}
		}
		logger.info("interaction reject fail.");
		return 0;
	}
	
	/**
	 * @Description 转接坐席
	 * @params
	 * @return
	 */
	public int transferAgent(String interactionId, String agentId, Map<String, Map<String, String>> params) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(params!=null && params.get("UserData")!=null) {
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					Map<String, String> userData = params.get("UserData");
					for(String key : userData.keySet()) {
						kvList.addString(key, userData.get(key));
					}
					req.setAddedProperties(kvList);
					logRequest(req.toString());
					ixnProtocol.request(req);
				}
				RequestTransfer request = RequestTransfer.create(interactionId);
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				request.setAgentId(agentId);
				KeyValueCollection kvc = new KeyValueCollection();
				kvc.addString("operation", "transferAgent");
				request.setExtension(kvc);
				logRequest(request.toString());
				ixnProtocol.send(request);
				return 1;
			} catch (Exception e) {
				logger.error("an exception occurred when transfer interaction to agent, because of "+ e.getMessage());
			}
		}
		return 0;
	}

	/**
	 * @Description 转接队列
	 * @params
	 * @return
	 */
	public int transferQueue(String interactionId, String queueId, Map<String, Map<String, String>> params) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(params!=null && params.get("UserData")!=null) {
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setReferenceId(ReferenceIdUtils.getReferenceId());
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					Map<String, String> userData = params.get("UserData");
					for(String key : userData.keySet()) {
						kvList.addString(key, userData.get(key));
					}
					req.setAddedProperties(kvList);
					logRequest(req.toString());
					ixnProtocol.request(req);
				}
				RequestPlaceInQueue request = RequestPlaceInQueue.create();
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				request.setInteractionId(ixn.getConnId());
				request.setQueue(queueId);
				KeyValueCollection kvc = new KeyValueCollection();
				kvc.addString("operation", "transferQueue");
				request.setExtension(kvc);
				logRequest(request.toString());
				Message msgResponse = ixnProtocol.request(request);
				if(checkReturnMessage(msgResponse,ixn,"transferQueue")){
					if(isChatType(ixn.getMediaType())) {
						getChatService(interactionId).release(ixn.getConnId(), 0, "transferQueue");
						getChatService(interactionId).disconnect();
						chatServices.remove(interactionId);
					}
					
					return 1;
				}
			} catch (Exception e) {
				logger.error("an exception occurred when transfer interaction to queue, because of "+ e.getMessage());
			}
		}
		return 0;
	}
	
	/**
	 * @Description 会议坐席
	 * @params
	 * @return
	 */
	public int conferenceAgent(String interactionId, String agentId, Map<String, Map<String, String>> params){
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(params!=null && params.get("UserData")!=null) {
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setReferenceId(ReferenceIdUtils.getReferenceId());
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					Map<String, String> userData = params.get("UserData");
					for(String key : userData.keySet()) {
						kvList.addString(key, userData.get(key));
					}
					req.setAddedProperties(kvList);
					logRequest(req.toString());
					ixnProtocol.request(req);
				}
				RequestConference request = RequestConference.create(interactionId);
				request.setReferenceId(ReferenceIdUtils.getReferenceId());
				request.setAgentId(agentId);
				KeyValueCollection kvc = new KeyValueCollection();
				kvc.addString("operation", "conference");
				request.setExtension(kvc);
				logRequest(request.toString());
				ixnProtocol.send(request);
				return 1;
			} catch (Exception e) {
				logger.error("an exception occurred when conference agent, because of "+ e.getMessage());
			}
		}
		return 0;
	}
	
	/**
	 * @Description 添加随路数据
	 * @params
	 * @return
	 */
	public int addAttachedData(String interactionId, Map<String, String> userData) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(userData!=null && userData.size()>0){
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setReferenceId(ReferenceIdUtils.getReferenceId());
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					for(Entry<String, String> entry : userData.entrySet()){
						kvList.addString(entry.getKey(), entry.getValue());
					}
					req.setAddedProperties(kvList);
					logRequest(req.toString());
					Message msgResponse = ixnProtocol.request(req);
					if(checkReturnMessage(msgResponse,ixn,"addAttachedData"))
						return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when add attached data, because of "+ e.getMessage());
			}
		}
		return 0;
	}
	
	/**
	 * @Description 更新随路数据
	 * @params
	 * @return
	 */
	public int updateAttachedData(String interactionId, Map<String, String> userData) {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(userData!=null && userData.size()>0){
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setReferenceId(ReferenceIdUtils.getReferenceId());
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					for(Entry<String, String> entry : userData.entrySet()){
						kvList.addString(entry.getKey(), entry.getValue());
					}
					req.setChangedProperties(kvList);
					logRequest(req.toString());
					Message msgResponse = ixnProtocol.request(req);
					if(checkReturnMessage(msgResponse,ixn,"updateAttachedData"))
						return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when update attached data, because of "+ e.getMessage());
			}
		}
		return 0;
	}
	
	/**
	 * @Description 删除随路数据
	 * @params
	 * @return
	 */
	public int deleteAttachedData(String interactionId, Map<String, String> userData) throws Exception {
		OpenMediaInteraction ixn = interactions.get(interactionId);
		if(ixn != null) {
			try {
				throwIfNotConnected();
				if(userData!=null && userData.size()>0){
					RequestChangeProperties req = RequestChangeProperties.create();
					req.setReferenceId(ReferenceIdUtils.getReferenceId());
					req.setInteractionId(ixn.getConnId());
					KeyValueCollection kvList = new KeyValueCollection();
					for(Entry<String, String> entry : userData.entrySet()){
						kvList.addString(entry.getKey(), entry.getValue());
					}
					req.setDeletedProperties(kvList);
					logRequest(req.toString());
					Message msgResponse = ixnProtocol.request(req);
					if(checkReturnMessage(msgResponse,ixn,"deleteAttachedData"))
						return 1;
				}
			} catch (Exception e) {
				logger.info("an exception occurred when delete attached data, because of "+ e.getMessage());
			}
		}
		return 0;
	}
	
	private boolean isChatType(final String mediaType) {
		if(chatMediaList.contains(mediaType)) {
			return true;
		}
		return false;
	}
	
	private void logRequest(final String log) {
		logger.info("send request to interaction server:\n" + log + "\n************************************");
	}
	
	protected void publishInteractionRemoved(final OpenMediaInteraction ixn) {
		executorService.submit(new Runnable() {
			public void run() {
				/*for (MultimediaServiceListener listener : multimediaListeners) {
					try {
						listener.onInteractionRemoved(Constants.MEDIATYPE_MULTIMEDIA, ixn);
					} catch (Exception ex) {
						logMessage("an exception occurred when publish InteractionRemovedEvent, "+ex.getMessage());
					}
				}*/
			}
		});
	}
	
	/**
	 * @Description 强制更新状态
	 * @params
	 * @return
	 */
	protected void publishForcedAgentStatusChanged(final String mediaType, final String status) {
		executorService.submit(new Runnable() {
			public void run() {
				/*for (MultimediaServiceListener listener : multimediaListeners) {
					try {
						listener.onMultimediaForcedAgentStatusChanged(mediaType, status);
					} catch (Exception ex) {
						logMessage("an exception occurred when publish ForcedAgentStatusChangedEvent, "+ex.getMessage());
					}
				}*/
			}
		});
	}
	
	protected void publishForcedConnectionStatusChanged(final String status) {
		logger.info(status);
	}
	
	protected void publishInteractionUpdated(final OpenMediaInteraction ixn) {
		logger.info(ixn.toString());
	}
	
	/**
	 * @Description 显示接收的相应日志
	 * @params
	 * @return
	 */
	public boolean checkReturnMessage(Message message, OpenMediaInteraction ixn, 
			String functionName) {
		logger.info("receive response message:\n"+message);
		
		if (message == null) {
			return false;
		}
		
		boolean success = false;
		try {
			switch (message.messageId()) {
			case EventAck.ID:
				success = true;
				EventAck eventAck = (EventAck) message;
				if(ixn != null){
					OpenMediaInteraction newIxn = new OpenMediaInteraction();
					newIxn.setAttachedData(ixn.getAttachedData());
					newIxn.setConnId(ixn.getConnId());
					newIxn.setEventId(ixn.getEventId());
					newIxn.setMediaType(ixn.getMediaType());
					if(functionName!=null && functionName.equalsIgnoreCase("accept")){//接起
						newIxn.setStatus(InteractionStatus.Established);
						newIxn.setEventName("EventEstablished");
						newIxn.setExtension(ixn.getExtension());
						logger.info(newIxn.toString());
					}else if(functionName!=null && functionName.equalsIgnoreCase("stop")){//挂断
						newIxn.setStatus(InteractionStatus.Idle);
						newIxn.setEventName("EventReleased");
						newIxn.setExtension(ixn.getExtension());
						publishInteractionRemoved(newIxn);
					}else if(functionName!=null && functionName.equalsIgnoreCase("leaveInteraction")){//离开会议室
						newIxn.setStatus(InteractionStatus.Idle);
						newIxn.setEventName("EventPartyRemoved");
						newIxn.setExtension(ixn.getExtension());
						publishInteractionRemoved(newIxn);
					}else if(functionName!=null && functionName.equalsIgnoreCase("login")){//登录成功
						logger.info("login success.");
					}
				}else{
					if(functionName!=null && functionName.equalsIgnoreCase("logout")){//签出
						Map<String, Object> agentInfo = new HashMap<String, Object>();
						Iterator<String> it = agentStatus.keySet().iterator();
						while (it.hasNext()) {
							String mediaType = it.next();
							if(!mediaType.equals("voice")) {
								agentInfo.put("mediaType", mediaType);
								agentInfo.put("status", "LoggedOut");
								logger.info(agentInfo.toString());
							}
						}
					}
				}
				break;
			case EventError.ID:
				EventError eventError = (EventError) message;
				if(eventError.getErrorCode()==43) { //Unknown interaction identifier specified
					logger.info("the interaction has been stopped by URS.");
					success = true;
				} else {
					logger.info(eventError.toString());
				}
				break;
			default:
				break;
		}
		} catch (Exception e) {
			logger.error("checkReturnMessage error.", e.fillInStackTrace());
		}
		
		return success;
	}
	
	class InteractionServerEventHandler implements Action<Message> {
		
		public void handle(final Message message) {
			logger.info(" \n" + message);
			
			OpenMediaInteraction ixn = new OpenMediaInteraction();
			String mediaType = "";
			String interactionId = "";
			if (message != null) {
				try {
					switch (message.messageId()) {
						case EventInvite.ID:
							logger.info("go-inviting");
							
							try {
								EventInvite eventInvite = (EventInvite) message;
								InteractionProperties ixnProps = eventInvite.getInteraction();
								KeyValueCollection userData = ixnProps.getInteractionUserData();
								Map<String, String> attachedData = CommonUtils.convertKvcToMap(userData);
								mediaType = ixnProps.getInteractionMediatype();
								interactionId = ixnProps.getInteractionId();
								
								ixn.setConnId(interactionId);
								ixn.setEventId(message.messageId());
								ixn.setEventName(message.messageName());
								ixn.setTicketId(eventInvite.getTicketId());
								ixn.setStatus(InteractionStatus.Ringing);
								ixn.setMediaType(mediaType);
								ixn.setThisQueue(ixnProps.getInteractionQueue());
								ixn.setPlaceId(ixnProps.getInteractionPlaceId());
								ixn.setAttachedData(attachedData);
								
								if(eventInvite.getExtension() != null) {
									Map<String, String> extension = CommonUtils.convertKvcToMap(eventInvite.getExtension());
									ixn.setExtension(extension);
								}
								
								if(isChatType(mediaType) && "vidyo".equals(mediaType)){
									
									//有新用户咨询
									MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
									mcMessageInfo.setUserAccount(interactionId);
									String firstName = attachedData.get("FirstName");
									String lastName = attachedData.get("LastName");
									String roomid = attachedData.get("roomid");
									String roomurl = attachedData.get("roomurl");
									String submediatype = attachedData.get("submediatype");
									mcMessageInfo.setNickName(firstName + lastName);
									mcMessageInfo.setCCSType(1);
									mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_NewUserAsk_VALUE);
									
									Map<String, String> map = new HashMap<String, String>();
									map.put("roomid", roomid);
									map.put("roomurl", URLEncoder.encode(roomurl, "utf-8"));
									map.put("submediatype", submediatype);
									map.put("mediaType", mediaType);
									mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
									logger.info("new video accept,push to client" + JSONUtil.map2json(map));
									
									pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
								}
								
								if(isChatType(mediaType) && "chat".equals(mediaType)) {
									String firstName = attachedData.get("FirstName");
									String lastName = attachedData.get("LastName");
									
									String chatServerHost = userData.getString("ChatServerHost");
									String chatServerPort = userData.getString("ChatServerPort");
									
									logger.info("invite chatServerHost:" + chatServerHost + ",chatServerPort:" + chatServerPort);
									
									ixn.setUrl(chatServerHost + ":" + chatServerPort);
									ixn.setNickName(firstName+lastName);
									if(eventInvite.getVisibilityMode()!=null) {
										int visibility = eventInvite.getVisibilityMode().asInteger();
										ixn.setVisibility(visibility);
									}else{
										ixn.setVisibility(0);
									}
								}
								
								interactions.put(interactionId, ixn);
								
								if(isChatType(mediaType) && "chat".equals(mediaType)){
									//创建ChatService会话实例
									putMultimedia(ixn);
									
									//有新用户咨询
									logger.info("have new user.");
									MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
									mcMessageInfo.setUserAccount(interactionId);
									String firstName = attachedData.get("FirstName");
									String lastName = attachedData.get("LastName");
									mcMessageInfo.setNickName(firstName + lastName);
									mcMessageInfo.setCCSType(1);
									mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_NewUserAsk_VALUE);
									Map<String, String> map = new HashMap<String, String>();
									map.put("mediaType", mediaType);
									mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
									
									pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
								}
								
								logger.info(ixn.toString());
							} catch (Exception e) {
								logger.info(e.getMessage());
							}
							
							publishAgentStatusChanged();
							
							break;
						case EventRevoked.ID:
							logger.info("go in revoked.");
							
							EventRevoked eventRevoked = (EventRevoked) message;
			    			interactionId = eventRevoked.getInteraction().getInteractionId();
			    			mediaType = eventRevoked.getInteraction().getInteractionMediatype();
			    			interactions.remove(interactionId);
			    			if(isChatType(mediaType)) {
			    				ChatAgentService chatService = getChatService(interactionId);
			    				if(chatService!=null) {
			    					try {
				    					chatService.release(interactionId, 0, "normal");
				    					chatService.disconnect();
										chatServices.remove(interactionId);
										
										//用户结束咨询
										/*MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
										mcMessageInfo.setUserAccount(interactionId);
										mcMessageInfo.setCCSType(1);
										mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_UserEndAsk_VALUE);
										pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo);*/
			    					}catch(Exception e){
			    						logger.info("an exception occurred when handle interaction event, "+e.getMessage());
			    					}
			    				}
							}
			    			Map<String, String> rovokedExtension = new HashMap<String, String>();
			    			if(eventRevoked.getReason()!=null && eventRevoked.getReason().getReasonSystemName().equalsIgnoreCase("Expired")){//坐席超时未接起
			    				rovokedExtension.put("revokedReason", "Expired");
			    			}else{
			    				rovokedExtension.put("revokedReason", "Stop");
			    			}
			    			ixn.setExtension(rovokedExtension);
			    			ixn.setConnId(interactionId);
			    			ixn.setEventId(message.messageId());
			    			ixn.setEventName(message.messageName());
			    			ixn.setMediaType(mediaType);
			    			publishInteractionRemoved(ixn);
							break;
						case EventRejected.ID:
							logger.info("go in rejected.");
							
							break;
						case EventAck.ID:
							logger.info("go in eventAck.");
							
							EventAck eventAck = (EventAck) message;
							break;
						case EventCurrentAgentStatus.ID:
							logger.info("go in EventCurrentAgentStatus.");
							
							EventCurrentAgentStatus eventCurrentAgentStatus = (EventCurrentAgentStatus) message;
							
							KeyValueCollection mediaStateList = eventCurrentAgentStatus.getMediaStateList();
							
							if(mediaStateList != null) {
								
								if(isPushLogin){
									agentStatus.clear();
									Enumeration<KeyValuePair> stateIterator = mediaStateList.getEnumeration();
									while (stateIterator.hasMoreElements()) {
										KeyValuePair pair = stateIterator.nextElement();
										agentStatus.put(pair.getStringKey(), pair.getIntValue() == 0 ? AgentStatus.NotReady : AgentStatus.Ready);
									}
									
									if(isPushReady){
										logger.info("isPushReady: " + isPushReady);
									}else{
										logger.info("ready success: " + ixnLogin.getUserAcc());
										
										MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
										mcMessageInfo.setCCSType(1);
										mcMessageInfo.setMCMEvent(78);
										Map<String, String> map = new HashMap<String, String>();
										map.put("optRetDes", "ready success");
										map.put("optResult", "0");
										mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
										
										pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC); 
										
										isPushReady = true;
										//isPushNotReady = false;
										//setNotReady(false);
									}
									
									/*if(isNotReady()){
										if(isPushNotReady){
											logger.info("isPushNotReady: " + isPushReady);
										}else{
											logger.info("not ready success: " + ixnLogin.getUserAcc());
											
											MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
											mcMessageInfo.setCCSType(1);
											mcMessageInfo.setMCMEvent(78);
											Map<String, String> map = new HashMap<String, String>();
											map.put("optRetDes", "not ready success");
											map.put("optResult", "0");
											mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
											
											pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo); 
											
											isPushNotReady = true;
											isPushReady = false;
											
											setReady(false);
										}
									}*/
									
								}else{
									agentStatus.clear();
									Enumeration<KeyValuePair> stateIterator = mediaStateList.getEnumeration();
									while (stateIterator.hasMoreElements()) {
										KeyValuePair pair = stateIterator.nextElement();
										agentStatus.put(pair.getStringKey(), pair.getIntValue() == 0 ? AgentStatus.NotReady : AgentStatus.Ready);
									}//while 
									
									logger.info("login in success: " + ixnLogin.getUserAcc());
									
									MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
									mcMessageInfo.setCCSType(1);
									mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_KFOnWorkResp_VALUE);
									Map<String, String> map = new HashMap<String, String>();
									map.put("optRetDes", "login in success");
									map.put("optResult", "0");
									mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
									
									pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC); 
									isPushLogin = true;
								}
							}
							
							agentCapacity.clear();
							KeyValueCollection mediaCapacityList = eventCurrentAgentStatus.getMediaCapacityList();
							if(mediaCapacityList!=null) {
								Enumeration<KeyValuePair> capacityIterator = mediaCapacityList.getEnumeration();
								while (capacityIterator.hasMoreElements()) {
									KeyValuePair pair = capacityIterator.nextElement();
									agentCapacity.put(pair.getStringKey(), pair.getIntValue());
								}
							}
							
							publishAgentStatusChanged();
							
							break;
						case EventForcedAgentStateChange.ID:
							logger.info("go in EventForcedAgentStateChange.");
							
							EventForcedAgentStateChange eventForcedAgentStateChange = (EventForcedAgentStateChange) message;
			    			mediaType = eventForcedAgentStateChange.getMediaTypeName();
			    			KeyValueCollection mediaList = eventForcedAgentStateChange.getMediaList();
			    			Enumeration<KeyValuePair> kvIter = mediaList.getEnumeration();
							while (kvIter.hasMoreElements()) {
								KeyValuePair pair = kvIter.nextElement();
								if(pair.getStringKey().equals(mediaType)) {
									AgentStatus status = pair.getIntValue() == 0 ? AgentStatus.NotReady : AgentStatus.Ready;
									agentStatus.put(pair.getStringKey(), status);
									publishForcedAgentStatusChanged(mediaType, status.name());
								}
							}
							break;
						case EventPartyAdded.ID:
							logger.info("go in EventPartyAdded.");
							
							EventPartyAdded eventPartyAdded = (EventPartyAdded)message;
							interactionId = eventPartyAdded.getInteraction().getInteractionId();
							OpenMediaInteraction interaction = interactions.get(interactionId);
							if(interaction != null) {
								InteractionOperation operation = eventPartyAdded.getOperation();
				    			if(operation != null){
				    				if(operation == InteractionOperation.Intrude) {
				    					logger.info("supervisor intrude the interaction.");
				    					break;
				    				}
				    				mediaType = eventPartyAdded.getInteraction().getInteractionMediatype();
				    				if(operation == InteractionOperation.Transfer) {
					    				if(isChatType(mediaType)) {
					    					ChatAgentService chatService = getChatService(interactionId);
					    					if(chatService!=null) {
					    						try {
						    						chatService.release(interactionId, 0, "transferAgent");
						    						chatService.disconnect();
					    						} catch(Exception e) {
					    							logger.info("disconnect with chat server failed.");
					    						}
					    						chatServices.remove(interaction.getConnId());
					    					}
					    				}
					    				
						    			ixn.setConnId(interactionId);
						    			ixn.setEventId(message.messageId());
						    			ixn.setEventName(message.messageName());
						    			ixn.setMediaType(mediaType);
						    			Map<String, String> extension = new HashMap<String, String>();
						    			extension.put("operation", "transfer");
						    			ixn.setExtension(extension);
						    			publishInteractionRemoved(ixn);
				    				} else if(operation == InteractionOperation.Conference) {
				    					
						    			ixn.setConnId(interactionId);
						    			ixn.setEventId(message.messageId());
						    			ixn.setEventName(message.messageName());
						    			ixn.setMediaType(mediaType);
						    			Map<String, String> extension = new HashMap<String, String>();
						    			extension.put("operation", "conference");
						    			ixn.setExtension(extension);
						    			publishInteractionUpdated(ixn);
				    				}
				    			}
							}
							
			    			break;
						case EventPartyRemoved.ID:
							logger.info("go in EventPartyRemoved.");
							
							EventPartyRemoved eventPartyRemoved = (EventPartyRemoved)message;
							interactionId = eventPartyRemoved.getInteraction().getInteractionId();
							OpenMediaInteraction openMediaInteraction = interactions.get(interactionId);
							if(openMediaInteraction != null) {
								PartyInfo partyInfo = eventPartyRemoved.getParty();
								ixn.setConnId(interactionId);
				    			ixn.setEventId(message.messageId());
				    			ixn.setEventName(message.messageName());
				    			ixn.setMediaType(eventPartyRemoved.getInteraction().getInteractionMediatype());
				    			if(openMediaInteraction.getExtension()!=null && openMediaInteraction.getExtension().size()>0){
				    				ixn.setExtension(openMediaInteraction.getExtension());
				    				ixn.getExtension().put("agentId", partyInfo.getAgentId());
				    			}else{
				    				Map<String, String> extension = new HashMap<String, String>();
				    				extension.put("agentId", partyInfo.getAgentId());
				    				ixn.setExtension(extension);
				    			}
				    			publishInteractionUpdated(ixn);
							}
							break;
						case EventError.ID:
							logger.info("go in eventError.");
							EventError eventError = (EventError) message;
							//publishError(new ErrorMessage(ErrorType.ixn.getValue(), eventError.getErrorDescription(), eventError.getErrorCode()));
							break;
						default :
							break;
						}
					}catch(Exception e){
						logger.info("an exception occurred when handle interaction event, "+e.getMessage());
					}
			}
		}
	}

	protected void publishAgentStatusChanged() {
		executorService.submit(new Runnable() {
			public void run() {
				final Map<String, Object> agentInfo = new HashMap<String, Object>();
				Iterator<String> it = agentStatus.keySet().iterator();
				while (it.hasNext()) {
					String mediaType = it.next();
					if(!mediaType.equals("voice")) {
						AgentStatus status = agentStatus.get(mediaType);
						agentInfo.put("mediaType", mediaType);
						agentInfo.put("status", status.name());
						Integer capacity = agentCapacity.get(mediaType);
						agentInfo.put("capacity", capacity == null ? 0 : capacity);
						logger.info(agentInfo.toString());
						/*for (MultimediaServiceListener listener : multimediaListeners) {
							try {
								listener.onMultimediaAgentStatusChanged(agentInfo);
							} catch (Exception ex) {
								logMessage("an exception occurred when publish AgentStatusChangedEvent, " + ex.getMessage());
							}
						}*/
					}
				}
			}
		});
	}
	
	@Override
	public void onChannelClosed(ChannelClosedEvent cce) {
		InteractionServerProtocol protocol = (InteractionServerProtocol)cce.getSource();
		logger.info("ixn server channel closed, the channel protocol is " + protocol.getEndpoint().getUri().toString());
		
		if(cce.getCause()!=null) {
			String causeMessage = cce.getCause().getMessage();
			logger.info("cause is " + causeMessage);
			if(causeMessage.indexOf("Connection is closed by remote peer")>=0 || causeMessage.indexOf("您的主机中的软件中止了一个已建立的连接")>=0) {
				isForceLoggedOut = true;
				publishForcedConnectionStatusChanged("Disconnected");
			}else{
				logger.info("Disconnected");
			}
		}else{
			logger.info("Disconnected");
		}
	}

	@Override
	public void onChannelError(ChannelErrorEvent cee) {
		logger.info("ixn server channel error, "+cee.toString());
	}

	@Override
	public void onChannelOpened(EventObject eo) {
		InteractionServerProtocol protocol = (InteractionServerProtocol)eo.getSource();
		
		logger.info("ixn server channel opened, the channel protocol is " + protocol.getEndpoint().getUri().toString());
		
		logger.info("Connected");
		if(!isInitialized) {
			if(isLoggedIn && !isForceLoggedOut) {
				try {
					logger.info("begin to relogin agent.");
					this.login(null);
				} catch(Exception e) {
					logger.info(e.getMessage());
				}
			}
		} else {
			isInitialized = true;
		}
	}

	public IxnLogin getIxnLogin() {
		return ixnLogin;
	}

	public void setIxnLogin(IxnLogin ixnLogin) {
		this.ixnLogin = ixnLogin;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public boolean isNotReady() {
		return isNotReady;
	}

	public void setNotReady(boolean isNotReady) {
		this.isNotReady = isNotReady;
	}

	public void sendExceptionMsg(int event,String des){
		try{
			MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
			mcMessageInfo.setCCSType(1);
			mcMessageInfo.setMCMEvent(event);
			Map<String, String> map = new HashMap<String, String>();
			map.put("optResult", "1");
			map.put("optRetDes", des);
			mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
			pushService.doPushMsg(ixnLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setFileServerService(FileServerService fileServerService) {
		this.fileServerService = fileServerService;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}
	
}
