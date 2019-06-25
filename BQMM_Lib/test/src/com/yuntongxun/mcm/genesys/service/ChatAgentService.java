package com.yuntongxun.mcm.genesys.service;

import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;

import com.genesyslab.platform.applicationblocks.commons.Predicate;
import com.genesyslab.platform.applicationblocks.commons.broker.EventReceivingBrokerService;
import com.genesyslab.platform.applicationblocks.commons.broker.MessageFilter;
import com.genesyslab.platform.applicationblocks.commons.broker.Subscriber;
import com.genesyslab.platform.applicationblocks.commons.protocols.BasicChatConfiguration;
import com.genesyslab.platform.applicationblocks.commons.protocols.ProtocolManagementServiceImpl;
import com.genesyslab.platform.commons.GEnum;
import com.genesyslab.platform.commons.collections.KeyValueCollection;
import com.genesyslab.platform.commons.protocol.ChannelClosedEvent;
import com.genesyslab.platform.commons.protocol.ChannelErrorEvent;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.genesyslab.platform.commons.protocol.ChannelState;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.Protocol;
import com.genesyslab.platform.commons.threading.SingleThreadInvoker;
import com.genesyslab.platform.webmedia.protocol.BasicChatProtocol;
import com.genesyslab.platform.webmedia.protocol.basicchat.Action;
import com.genesyslab.platform.webmedia.protocol.basicchat.BasicChatEventList;
import com.genesyslab.platform.webmedia.protocol.basicchat.MessageInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.MessageText;
import com.genesyslab.platform.webmedia.protocol.basicchat.NewPartyInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.NoticeInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.NoticeText;
import com.genesyslab.platform.webmedia.protocol.basicchat.PartyLeftInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.UserType;
import com.genesyslab.platform.webmedia.protocol.basicchat.Visibility;
import com.genesyslab.platform.webmedia.protocol.basicchat.events.EventError;
import com.genesyslab.platform.webmedia.protocol.basicchat.events.EventSessionInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestJoin;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestMessage;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestNotify;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestReleaseParty;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.fileserver.util.FileServerUtils;
import com.yuntongxun.mcm.genesys.model.ChatSessionMessage;
import com.yuntongxun.mcm.genesys.model.ChatUserInfo;
import com.yuntongxun.mcm.genesys.model.MessageModel;
import com.yuntongxun.mcm.genesys.util.CommonUtils;
import com.yuntongxun.mcm.genesys.util.HttpUtil;
import com.yuntongxun.mcm.genesys.util.ReferenceIdUtils;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.Constants;

public class ChatAgentService implements Subscriber<Message>, ChannelListener{
	
	private Logger logger = LogManager.getLogger(ChatAgentService.class);
	
	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	private ProtocolManagementServiceImpl protocolManagementService;
	
	private EventReceivingBrokerService eventBrokerService;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	private String CHATSERVER_IDENTIFIER;
	
	private Protocol chatProtocol;
	
	private Map<String, ChatUserInfo> sessionUsers = new ConcurrentHashMap<String, ChatUserInfo>();
	
	private String userAcc;
	
	private PushService pushService;
	
	private FileServerService fileServerService;
	
	private VersionDao versionDao;
	
	/**
	 * 初始化chat server
	 * @param url
	 * @param nickName
	 * @param visibility
	 * @param userAcc
	 */
	public ChatAgentService(String url, String nickName, int visibility, String userAcc, 
			PushService pushService, FileServerService fileServerService, VersionDao versionDao) {
		try {
			this.userAcc = userAcc;
			this.pushService = pushService;
			this.fileServerService = fileServerService;
			this.versionDao = versionDao;
			this.CHATSERVER_IDENTIFIER =  "softphone-csclient_" + userAcc;
			
			BasicChatConfiguration chatConfiguration = new BasicChatConfiguration(CHATSERVER_IDENTIFIER);
			
			logger.info("chat server url : tcp://" + url + ", nickname = " + nickName + ", visibility = " + visibility);
			
			chatConfiguration.setUri(new URI("tcp://" + url));
			
			if(visibility == Visibility.All.asInteger()){
				chatConfiguration.setUserType(UserType.Agent);
			}else{
				chatConfiguration.setUserType(UserType.Supervisor);
			}
			
			chatConfiguration.setUserNickname(nickName);
			
			String addpTrace = "";
			//String localTimeout = "600";
			//String remoteTimeout = "300";
			String localTimeout = "60";
			String remoteTimeout = "30";
			
			chatConfiguration.setUseAddp(true);
			chatConfiguration.setAddpTrace(addpTrace);
			chatConfiguration.setAddpClientTimeout(Integer.parseInt(localTimeout));
			chatConfiguration.setAddpServerTimeout(Integer.parseInt(remoteTimeout));
			
			protocolManagementService = new ProtocolManagementServiceImpl();
			protocolManagementService.register(chatConfiguration);
			protocolManagementService.addChannelListener((ChannelListener)this);
			
			eventBrokerService = new EventReceivingBrokerService(
					new SingleThreadInvoker("ChatServerEventReceivingBrokerService"));
			eventBrokerService.register(this);
			
			chatProtocol = protocolManagementService.getProtocol(CHATSERVER_IDENTIFIER);
			chatProtocol.setReceiver(eventBrokerService);
		} catch (Exception e) {
			logger.error("an exception occurred when create chat service, because of ", e.fillInStackTrace());
		}
	}
	
	protected void throwIfNotConnected() throws Exception {
		if (protocolManagementService == null || chatProtocol == null
				|| chatProtocol.getState() != ChannelState.Opened) {
			throw new Exception("connection is not opened.");
		}
	}
	
	/**
	 * @Description 打开链接
	 * @params
	 * @return
	 */
	public void connect() {
		try {
			if(chatProtocol != null && chatProtocol.getState() == ChannelState.Closed) {
				logger.info("begin to connect chat server...");
				
				chatProtocol.open();
				logger.info("chat server protocol state is "+chatProtocol.getState().toString());
			}
		} catch (Exception e) {
			logger.error("an exception occurred when connect to chat server, because of ", e.fillInStackTrace());
		}
	}
	
	/**
	 * @Description 关闭连接
	 * @params
	 * @return
	 */
	public void disconnect(){
		if (chatProtocol != null && chatProtocol.getState() == ChannelState.Opened) {
			try {
				logger.info("disconnect to chat server.");
				
				chatProtocol.close();
				eventBrokerService.unregister(this);
				protocolManagementService.unregister(CHATSERVER_IDENTIFIER);

			} catch (Exception e) {
				logger.error("an exception occurred when disconnect from chat server, because of ", e.fillInStackTrace());
			}
		}
	}
	
	/**
	 * @Description 加入聊天室
	 * @params
	 * @return
	 */
	public int join(final String connId, final String message, final int visibility) {
		logger.info("begin to join party to chat server, connId is " + connId);
		
		try {
			checkConnection();
			
			RequestJoin request = RequestJoin.create(connId, (Visibility) GEnum.getValue(Visibility.class, visibility), 
					MessageText.create(message));
			request.setReferenceId(ReferenceIdUtils.getReferenceId());
			logger.info(request.toString());
			chatProtocol.send(request);
			
			return 1;
		} catch (Exception e) {
			logger.error("an exception occurred when join party to chat server, because of"+e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * @Description 离开
	 * @params
	 * @return
	 */
	public int release(final String connId, final int action, final String message) {
		logger.info("begin to release party from chat server, connId is " + connId);
		
		try {
			RequestReleaseParty request = RequestReleaseParty.create(connId);
			request.setReferenceId(ReferenceIdUtils.getReferenceId());
			request.setAfterAction((Action) GEnum.getValue(Action.class, action));
			request.setMessageText(MessageText.create(message));
			ChatUserInfo sessions = sessionUsers.get(connId);
			
			if(sessions != null) {
				logger.info(request.toString());
				if (chatProtocol != null && chatProtocol.getState() == ChannelState.Opened) {
					chatProtocol.send(request);
				} else {
					logger.info("chat server channel has been closed.");
				}
				
				sessionUsers.remove(connId);
			} else {
				logger.info("the interaction has not been accepted, need not be released.");
			}
			
			return 1;
		} catch (Exception e) {
			logger.error("an exception occurred when stop chat, "+e.getMessage(), e);
			return 0;
		}
	}
	
	/**
	 * @Description 坐席发送普通消息
	 * @params
	 * @return
	 */
	public int sendMessage(final String connId, final String message, final int visibility) {
		logger.info("send chat message to client, connId = " + connId + ", message = " + message);
		
		try {
			RequestMessage request = RequestMessage.create();
			request.setSessionId(connId);
			request.setVisibility((Visibility)GEnum.getValue(Visibility.class, visibility));
			request.setMessageText(MessageText.create(message));
			request.setReferenceId(ReferenceIdUtils.getReferenceId());
			
			logger.info("send request to chat server:\n" + request.toString() + "\n************************************");
			
			if (chatProtocol != null && chatProtocol.getState() == ChannelState.Opened) {
				chatProtocol.send(request);
				return 1;
			} else {
				logger.info("chat server channel has been closed.");
				return 0;
			}
		} catch (Exception e) {
			logger.error("an exception occurred when send chat message, "+e.getMessage(), e);
			return 0;
		}
	}
	
	/**
	 * @Description 坐席发送通知消息
	 * @params
	 * @return
	 */
	public int sendNotify(final String connId, final String message) {
		try {
			logger.info("send notify client, connId = "+connId+", message = "+message);
			
			RequestNotify request = RequestNotify.create();
			request.setSessionId(connId);
			request.setVisibility(Visibility.All);
			request.setNoticeText(NoticeText.create(message));
			request.setReferenceId(ReferenceIdUtils.getReferenceId());
			
			logger.info(request.toString());
			if (chatProtocol != null && chatProtocol.getState() == ChannelState.Opened) {
				chatProtocol.send(request);
				return 1;
			} else {
				logger.info("chat server channel has been closed.");
				return 0;
			}
		} catch (Exception e) {
			logger.error("an exception occurred when send chat notify, because of "+e.getMessage(), e);
			return 0;
		}
	}
	
	public Predicate<Message> getFilter() {
		return new MessageFilter(this.chatProtocol.getProtocolId());
	}

	@Override
	public void handle(Message message) {
		logger.info("receive chat server message:\n" + message);
		
		if (message != null) {
			try {
				switch (message.messageId()) {
				case EventSessionInfo.ID:
					EventSessionInfo sessionInfo = (EventSessionInfo) message;

					BasicChatEventList eventList = sessionInfo.getChatTranscript().getChatEventList();
					
					String connId = sessionInfo.getChatTranscript().getSessionId();
					String startAt = sessionInfo.getChatTranscript().getStartAt();
					String date = startAt.substring(0, 10);
					String time = startAt.substring(11,19);
					Date startTime;
					
					try {
						startTime = dateTimeFormat.parse(date+" "+time);
						String temp = CommonUtils.getUTC8DateTime(startTime.getTime());
						startTime = dateTimeFormat.parse(temp);
					} catch (ParseException e) {
						startTime = new Date();
					}
				
					
					int size = eventList.size();
					logger.info("event list size is "+size);
					
					int textType = 0;//0是普通  1是转接
					for (int i = 0; i < size; i++) {
						NewPartyInfo newPartyInfo = eventList.getAsNewPartyInfo(i);
						if(newPartyInfo==null){
							continue;
						}else{
							MessageText messageText = newPartyInfo.getMessageText();
							if(messageText!=null && messageText.getText()!=null && messageText.getText().trim().equalsIgnoreCase("transferAgent")){
								textType  = 1;
								break;
							}
						}
						
					}
					
					for (int i = 0; i < size; i++) {
						int state = 0;

						NewPartyInfo newPartyInfo = eventList.getAsNewPartyInfo(i);
						PartyLeftInfo partyLeftInfo = eventList.getAsPartyLeftInfo(i);
						MessageInfo messageInfo = eventList.getAsMessageInfo(i);
						NoticeInfo noticeInfo = eventList.getAsNoticeInfo(i); //附件

						if (newPartyInfo != null) {
							state = 1;
						} else if (partyLeftInfo != null) {
							state = 2;
						} else if (messageInfo != null) {
							state = 3;
						} else if (noticeInfo != null) {
							state = 4;
						} 

						ChatSessionMessage session = new ChatSessionMessage();
						session.setReferenceId(sessionInfo.getReferenceId());
						session.setConnId(sessionInfo.getChatTranscript().getSessionId());
						session.setMediaType("chat");
						
						switch (state) {
						case 1:
							try {
								ChatUserInfo userInfo = new ChatUserInfo();
								UserType userType = newPartyInfo.getUserInfo().getUserType();
								//userInfo.setUserType(newPartyInfo.getUserInfo().getUserType().name());
								KeyValueCollection userData = newPartyInfo.getUserData();
								if(userData!=null){
									if(userData.getString("screenname")!=null){
										userInfo.setNickName(userData.getString("screenname"));
										session.setNickName(userData.getString("screenname"));
									}else{
										userInfo.setNickName(newPartyInfo.getUserInfo().getUserNickname());
										session.setNickName(newPartyInfo.getUserInfo().getUserNickname());
									}
								}else{
									userInfo.setNickName(newPartyInfo.getUserInfo().getUserNickname());
									session.setNickName(newPartyInfo.getUserInfo().getUserNickname());
								}
								
								if (userType == UserType.Agent) {
									userInfo.setUserType(1);
								}else if(userType == UserType.Client){
									userInfo.setUserType(0);
								}
								sessionUsers.put(newPartyInfo.getUserId(), userInfo);
								session.setUserType(newPartyInfo.getUserInfo().getUserType().name());
								session.setTextMessage(newPartyInfo.getMessageText().getText());
								session.setVisibility(newPartyInfo.getVisibility().asInteger());
								Integer timeShift = newPartyInfo.getTimeShift();
								long messageTime = startTime.getTime()+timeShift*1000;
								session.setTime(timeFormat.format(messageTime));
								publishPartyJoinEvent(session);
							}catch(Exception e){
								logger.error(e.getMessage(), e);
							}
							break;
						case 2:
							try {
								String userId = partyLeftInfo.getUserId();
								ChatUserInfo userInfo = sessionUsers.get(userId);
								session.setNickName(userInfo.getNickName());
								if (partyLeftInfo.getMessageText() != null) {
									session.setTextMessage(partyLeftInfo.getMessageText().getText());
								}
								if (partyLeftInfo.getReason() != null) {
									session.setReason(partyLeftInfo.getReason().getText());
								}
								session.setVisibility(partyLeftInfo.getVisibility().asInteger());
								Integer timeShift = partyLeftInfo.getTimeShift();
								long messageTime = startTime.getTime() + timeShift * 1000;
								session.setTime(timeFormat.format(messageTime));
								publishPartyLeftEvent(session);
								sessionUsers.remove(partyLeftInfo.getUserId());
								if(userInfo.getUserType() == 0){
									logger.info("have party left, start push message to userAcc: {" + userAcc + "}");
									//用户结束咨询
									MCMMessageInfo mcMessageInfo = new MCMMessageInfo();
									mcMessageInfo.setMsgContent(partyLeftInfo.getMessageText().getText());
									mcMessageInfo.setUserAccount(sessionInfo.getChatTranscript().getSessionId());
									mcMessageInfo.setCCSType(1);
									mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_UserEndAsk_VALUE);
									pushService.doPushMsg(userAcc, mcMessageInfo, -1, Constants.SUCC);
								}else{
									logger.info("user type:" + userInfo.getUserType());
								}
							}catch(Exception e){
								logger.error(e.getMessage(), e);
							}
							break;
						case 3:
							try {
								String userId = messageInfo.getUserId();
								ChatUserInfo userInfo = sessionUsers.get(userId);
								session.setNickName(userInfo.getNickName());
								session.setTextMessage(messageInfo.getMessageText().getText());
								session.setVisibility(messageInfo.getVisibility().asInteger());
								Integer timeShift = messageInfo.getTimeShift();
								long messageTime = startTime.getTime() + timeShift*1000;
								session.setTime(timeFormat.format(messageTime));
								if(textType==1){
									session.setOperation("transferAgent");
								}else{
									session.setOperation("normal");
								}
								publishSessonInfoEvent(session);
								
								if(userInfo.getUserType() == 0 && !userInfo.getNickName().equals("system")){
									logger.info("start push message [" + session.getTextMessage() + "] to userAcc: {" + userAcc + "}");
									
									String msg = session.getTextMessage();
									MessageModel mm = null;
									try {
										mm = (MessageModel)JSONUtil.jsonToObj(msg, MessageModel.class);
									} catch (Exception e) {
										mm = null;
									}
									if(mm == null){
										mm = new MessageModel();
										mm.setMediaType("1");
										mm.setMsg(msg);
									}
									
									MCMMessageInfo mcMessageInfo = null;
									if("1".equals(mm.getMediaType())){ //文本
										//接收消息
										mcMessageInfo = new MCMMessageInfo();
										mcMessageInfo.setMsgContent(mm.getMsg());
										mcMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
										mcMessageInfo.setUserAccount(sessionInfo.getChatTranscript().getSessionId());
										mcMessageInfo.setCCSType(1);
										mcMessageInfo.setMCMEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
										long version = versionDao.getMessageVersion(userAcc);
										mcMessageInfo.setVersion(version);
										pushService.doPushMsg(userAcc, mcMessageInfo, -1, Constants.SUCC);
									}else{ //附件
										String fileUrl = mm.getMsg();
										Map<String, InputStream> map = HttpUtil.downloadFileGet(fileUrl);
										
										if(map == null){
											logger.info("start genesys file server download file fail.");
										}else{
											for(String fileName : map.keySet()){
												InputStream is = map.get(fileName);
												logger.info("upload[" + fileUrl + "]to ytx file server");
												
												String sig = null;
												try {
													sig = FileServerUtils.getFileServerSig();
												} catch (NoSuchAlgorithmException e) {
													logger.info("get file server sig fail.");
												}
												
												String[] arrays = userAcc.split("#");
												String appId = "";
												String userName = "";
												if(arrays.length == 2){
													appId = arrays[0];
													userName = arrays[1];
												}
												
												String ytxFileUrl = fileServerService.uploadFile(sig, appId, userName, fileName, is);
												logger.info("return ytx fileUrl[" + ytxFileUrl + "].");
												
												if(ytxFileUrl != null && ytxFileUrl.length() != 0){
													mcMessageInfo = new MCMMessageInfo();
													mcMessageInfo.setMsgFileName(fileName);
													mcMessageInfo.setMsgFileUrl(ytxFileUrl);
													if("2".equals(mm.getMediaType())){
														mcMessageInfo.setMsgType(MCMTypeInner.MCMType_emotion_VALUE);
													}else if("4".equals(mm.getMediaType())){
														mcMessageInfo.setMsgType(MCMTypeInner.MCMType_audio_VALUE);
													}else if("3".equals(mm.getMediaType())){
														mcMessageInfo.setMsgType(MCMTypeInner.MCMType_video_VALUE);
													}
													
													mcMessageInfo.setMCMEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
													mcMessageInfo.setUserAccount(sessionInfo.getChatTranscript().getSessionId());
													
													pushService.doPushMsg(userAcc, mcMessageInfo, -1, Constants.SUCC);	
												}
											}
										}
									}
								}else{
									logger.info("user type:" + userInfo.getUserType());
								}
							} catch(Exception e){
								logger.error(e.getMessage(), e);
							}
							break;
						case 4:
							try {
								String userId = messageInfo.getUserId();
								ChatUserInfo userInfo = sessionUsers.get(userId);
								session.setNickName(session.getNickName());
								
								session.setTextMessage(noticeInfo.getNoticeText().getText());
								session.setVisibility(noticeInfo.getVisibility().asInteger());
								Integer timeShift = messageInfo.getTimeShift();
								long messageTime = startTime.getTime() + timeShift*1000;
								session.setTime(timeFormat.format(messageTime));
								if(textType==1){
									session.setOperation("transferAgent");
								}else{
									session.setOperation("normal");
								}
								publishSessonInfoEvent(session);
								
								if(userInfo.getUserType() == 0){
									logger.info("start push notice message to userAcc: {" + userAcc + "}");
								}else{
									logger.info("user type:" + userInfo.getUserType());
								}
							} catch(Exception e){
								logger.error(e.getMessage(), e);
							}
							break;
						default :
							break;
						}
						
						logger.info("session info : "+session);
					}
					break;
				case EventError.ID:
					EventError eventError = (EventError) message;
					logger.info("eventError" + eventError.getErrorId());
					break;
				default :
					break;
				}
			} catch(Exception e){
				logger.error("an exception occurred when handle interaction event, "+e.getMessage(), e);
			}
		}
	}
		
	private void publishPartyJoinEvent(final ChatSessionMessage sessionMessage) {
		logger.info(sessionMessage.getNickName()+" join the chat."+sessionMessage.getConnId());
	}
	
	private void publishPartyLeftEvent(final ChatSessionMessage sessionMessage) {
		logger.info(sessionMessage.getNickName()+" left the chat."+sessionMessage.getConnId());
	}

	private void publishSessonInfoEvent(final ChatSessionMessage sessionMessage) {
		logger.info(sessionMessage.getNickName()+">>:"+sessionMessage.getTextMessage()+sessionMessage.getConnId());
	}

	public Protocol getProtocol() {
		return chatProtocol;
	}

	/**
	 * 检查连接
	 * @throws Exception
	 */
	private void checkConnection() throws Exception {
		if(chatProtocol != null) {
			if(chatProtocol.getState() == ChannelState.Closed || chatProtocol.getState() == ChannelState.Closing) {
				this.connect();
			}
			
			long beginTime = System.currentTimeMillis();
			while(true){
				if(chatProtocol.getState() == ChannelState.Opened)
					break;
				try{
					Thread.sleep(2000);
				}catch(Exception e){
					logger.error("checkConnection", e);
				}
				
				long endTime = System.currentTimeMillis();
				
				if(endTime - beginTime > 10000){
					throw new Exception("chat server can not be connected.");
				}
			}
		}
	}
	
	@Override
	public void onChannelClosed(ChannelClosedEvent cce) {
		BasicChatProtocol protocol = (BasicChatProtocol)cce.getSource();
		logger.info("chat server channel closed, the channel protocol is "+protocol.getEndpoint().getUri().toString());
		if(cce.getCause()!=null) {
			logger.info("cause is " + cce.getCause().getMessage());
			if(cce.getCause().getMessage().indexOf("connection is unresponsive")>=0) {
				publishForcedConnectionStatusChanged("Disconnected");
			}
		} else {
			publishConnectionStatusChanged("Disconnected");
		}
	}

	@Override
	public void onChannelError(ChannelErrorEvent cee) {
		logger.info("chat server channel error, "+cee.toString());
	}

	@Override
	public void onChannelOpened(EventObject eo) {
		BasicChatProtocol protocol = (BasicChatProtocol)eo.getSource();
		logger.info("chat server channel opened, the channel protocol is "+protocol.getEndpoint().getUri().toString());
	}
	
	protected void publishConnectionStatusChanged(final String status) {
		executorService.submit(new Runnable() {
			public void run() {
				/*for (MultimediaServiceListener listener : listeners) {
					try {
						listener.onMultimediaConnectionStatusChanged(Constants.CHANNEL_CHATSERVER, status);
					} catch (Exception ex) {
						logger.error("an exception occurred when publish ConnectionStatusChangedEvent, "+ex.getMessage());
					}
				}*/
			}
		});
	}
	
	protected void publishForcedConnectionStatusChanged(final String status) {
		executorService.submit(new Runnable() {
			public void run() {
				/*for (MultimediaServiceListener listener : listeners) {
					try {
						listener.onMultimediaForcedConnectionStatusChanged(Constants.CHANNEL_CHATSERVER, status);
					} catch (Exception ex) {
						logger.error("an exception occurred when publish ForcedConnectionStatusChangedEvent, "+ex.getMessage());
					}
				}*/
			}
		});
	}
}
