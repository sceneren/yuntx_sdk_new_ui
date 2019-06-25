package com.yuntongxun.mcm.genesys.service;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;

import com.genesyslab.platform.applicationblocks.commons.Action;
import com.genesyslab.platform.applicationblocks.commons.broker.EventReceivingBrokerService;
import com.genesyslab.platform.applicationblocks.commons.broker.MessageFilter;
import com.genesyslab.platform.applicationblocks.commons.protocols.ProtocolManagementServiceImpl;
import com.genesyslab.platform.commons.collections.KeyValueCollection;
import com.genesyslab.platform.commons.protocol.ChannelClosedEvent;
import com.genesyslab.platform.commons.protocol.ChannelErrorEvent;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.genesyslab.platform.commons.protocol.ChannelState;
import com.genesyslab.platform.commons.protocol.Endpoint;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.ProtocolException;
import com.genesyslab.platform.commons.threading.SingleThreadInvoker;
import com.genesyslab.platform.webmedia.protocol.BasicChatProtocol;
import com.genesyslab.platform.webmedia.protocol.basicchat.BasicChatEventList;
import com.genesyslab.platform.webmedia.protocol.basicchat.MessageInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.MessageText;
import com.genesyslab.platform.webmedia.protocol.basicchat.NewPartyInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.NoticeInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.NoticeText;
import com.genesyslab.platform.webmedia.protocol.basicchat.PartyLeftInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.UserInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.UserType;
import com.genesyslab.platform.webmedia.protocol.basicchat.Visibility;
import com.genesyslab.platform.webmedia.protocol.basicchat.events.EventSessionInfo;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestJoin;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestMessage;
import com.genesyslab.platform.webmedia.protocol.basicchat.requests.RequestNotify;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.genesys.model.ChatUserInfo;
import com.yuntongxun.mcm.genesys.model.ChatUserLogin;
import com.yuntongxun.mcm.genesys.model.MessageModel;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.Constants;

public class ChatUserService implements ChannelListener {

	private static final Logger logger = LogManager
			.getLogger(ChatUserService.class);

	private String CHATSERVER_IDENTIFIER;

	private ProtocolManagementServiceImpl protocolManagementService;

	private EventReceivingBrokerService eventReceivingBrokerService;

	private BasicChatProtocol protocol;

	private String url = "tcp://192.168.123.196:4800";

	// private static final String MEDIA_TYPE = "chat";

	private Map<String, ChatUserInfo> sessionUsers = new ConcurrentHashMap<String, ChatUserInfo>();

	private int visibility;

	private String queue;

	String connId = "";

	private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	private VersionDao versionDao;

	private PushService pushService;

	private FileServerService fileServerService;

	private ChatUserLogin oul; 
	
	public void init(ChatUserLogin oul, String url, String queue,
			int visibility) {
		this.visibility = visibility;
		this.queue = queue;
		this.oul = oul;
		//this.versionDao = versionDao;
		//this.pushService = pushService;
		//this.fileServerService = fileServerService;
		this.CHATSERVER_IDENTIFIER = "ChatClient_" + oul.getUserAcc();
		this.initializePSDKApplicationBlocks(visibility);
	}

	class ChatServerEventsHandler implements Action<Message> {

		@Override
		public void handle(Message message) {
			logger.info("chatUserService message: " + message);

			if (message instanceof EventSessionInfo) {
				EventSessionInfo sessionInfo = (EventSessionInfo) message;

				BasicChatEventList eventList = sessionInfo.getChatTranscript()
						.getChatEventList();

				connId = sessionInfo.getChatTranscript().getSessionId();
				String startAt = sessionInfo.getChatTranscript().getStartAt();
				String date = startAt.substring(0, 10);
				String time = startAt.substring(11, 19);
				Date startTime;
				try {
					startTime = dateTimeFormat.parse(date + " " + time);
				} catch (ParseException e) {
					startTime = new Date();
				}

				try {
					int size = eventList.size();
					for (int i = 0; i < size; i++) {
						if (eventList.get(i) instanceof MessageInfo) {
							MessageInfo msgInfo = eventList.getAsMessageInfo(i);
							MessageText msgText = msgInfo.getMessageText();
							String s = msgText.getText();
							ChatUserInfo usre = sessionUsers.get(msgInfo.getUserId());
							
							logger.info(usre.getNickName());
							
							if (usre != null) {
								if (usre.getUserType() == 0) {
									logger.info(usre.getUserType());
								}else if (usre.getUserType() == 1 && !usre.getNickName().equals("system")) {
									logger.info("start push message [" + s + "] to user, userAcc: {" + oul.getUserAcc() + "}");

									String msg = s;
									MessageModel mm = null;
									try {
										mm = (MessageModel) JSONUtil.jsonToObj(msg, MessageModel.class);
									} catch (Exception e) {
										mm = null;
									}
									if (mm == null) {
										mm = new MessageModel();
										mm.setMediaType("1");
										mm.setMsg(msg);
									}

									MCMMessageInfo mcMessageInfo = null;
									String content = "";
									if ("1".equals(mm.getMediaType())) { // 文本
										// 接收消息
										content = mm.getMsg();
									} else { // 附件
										content = "附件";
									}
									
									mcMessageInfo = new MCMMessageInfo();
									mcMessageInfo.setMsgContent(content);
									mcMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
									mcMessageInfo.setUserAccount("Genesys");
									mcMessageInfo.setCCSType(1);
									mcMessageInfo.setMCMEvent(MCMEventDefInner.UserEvt_SendMSG_VALUE);
									long version = versionDao.getMessageVersion(oul.getUserAcc());
									mcMessageInfo.setVersion(version);
									
									pushService.doPushMsg(oul.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
									
								} else {
									//logger.info(usre.getNickName() + ">>:" + s);
								}
							}

						} else if (eventList.get(i) instanceof NoticeInfo) {
							NoticeInfo noticeInfo = eventList.getAsNoticeInfo(i);
							NoticeText msgText = noticeInfo.getNoticeText();
							String noticeString = msgText.getText();
							logger.info("noticeInfo: " + noticeString);

						} else if (eventList.get(i) instanceof NewPartyInfo) {
							NewPartyInfo partyInfo = eventList.getAsNewPartyInfo(i);
							//Integer eventId = partyInfo.getEventId();
							UserInfo userInfo = partyInfo.getUserInfo();
							UserType userType = userInfo.getUserType();
							String customerId = userInfo.getUserNickname();
							String chat_userId = partyInfo.getUserId();
							MessageText messageText = partyInfo.getMessageText();
							ChatUserInfo user = new ChatUserInfo();
							user.setNickName(customerId);
							if (userType == UserType.Agent) {
								user.setUserType(1);
								logger.info(userInfo.getUserNickname() + " agent join the chat.");
							} else if (userType == UserType.Client) {
								user.setUserType(0);
								logger.info(userInfo.getUserNickname() + " client join the chat.");
							}

							sessionUsers.put(chat_userId, user);

						} else if (eventList.get(i) instanceof PartyLeftInfo) {
							PartyLeftInfo partyLeftInfo = eventList
									.getAsPartyLeftInfo(i);
							logger.info("chat user service party left info");

							ChatUserInfo usre = sessionUsers.get(partyLeftInfo.getUserId());
							if (usre != null) {
								logger.info(usre.getNickName() + " left the chat.");
							}

							sessionUsers.remove(partyLeftInfo.getUserId());
						}
					}
				} catch (Exception e) {
					logger.info("chat user service error", e.fillInStackTrace());
				}
			}
		}

	}

	public int accept() throws Exception {
		logger.info("chatUserService accept.");

		try {
			if (protocol.getState() == ChannelState.Closed) {
				this.connect();
			}
			RequestJoin reqJoin = RequestJoin.create(Visibility.All, queue,
					"hello", MessageText.create("welcome"));
			try {
				logger.info(reqJoin.toString());
				protocol.request(reqJoin);
			} catch (ProtocolException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("chatUserService accept error", e.fillInStackTrace());
			return 0;
		}

		return 1;
	}

	public int sendChatMessage(String message) {
		try {
			RequestMessage request = RequestMessage.create();
			request.setSessionId(connId);
			request.setVisibility(Visibility.All);
			request.setMessageText(MessageText.create(message));

			// KeyValueCollection userdata = new KeyValueCollection();
			// userdata.addString("aa", "22");
			// userdata.addString("bbb", "33");
			// protocol.setUserData(userdata);

			protocol.send(request);
		} catch (Exception e) {
			logger.error("chatUserServie send chat message error: ",
					e.fillInStackTrace());
			return 0;
		}

		return 1;
	}

	public int notify(String textMessage) {
		try {
			RequestNotify request = RequestNotify.create();
			request.setSessionId(connId);
			request.setVisibility(Visibility.All);
			NoticeText text = NoticeText.create(textMessage);
			// text.setNoticeType(NoticeType.TypingStarted);
			// text.setText(textMessage);
			request.setNoticeText(text);
			protocol.send(request);
		} catch (Exception e) {
			logger.error("chatUserServie send notify message error: ",
					e.fillInStackTrace());
			return 0;
		}
		return 1;
	}

	private void initializePSDKApplicationBlocks(int visibility) {
		try {
			protocol = new BasicChatProtocol(new Endpoint(new URI(url)));

			KeyValueCollection userdata = new KeyValueCollection();
			userdata.addString("FirstName", oul.getNickName());
			userdata.addString("LastName", oul.getNickName());
			userdata.addString("userAcc", oul.getUserAcc());

			protocol.setUserData(userdata);
			protocol.setUserNickname(oul.getNickName());
			protocol.setUserType(UserType.Client);

			protocol.addChannelListener((ChannelListener) this);

			eventReceivingBrokerService = new EventReceivingBrokerService(
					new SingleThreadInvoker("EventReceivingBrokerService-1"));

			eventReceivingBrokerService.register(new ChatServerEventsHandler(),
					new MessageFilter(protocol.getProtocolId()));
			protocol.setReceiver(eventReceivingBrokerService);

			connect();

			Thread.sleep(500);

			accept();
		} catch (Exception e) {
			logger.error("chatUserServie initializePSDKApplicationBlocks error: ",
					e.fillInStackTrace());
		}

	}

	public void finalizePSDKApplicationBlocks() {
		if ((protocol != null) && (protocol.getState() == ChannelState.Opened)) {
			logger.info("begin to finalizePSDKApplicationBlocks...");

			try {
				protocol.close();
				// protocolManagementService.unregister(CHATSERVER_IDENTIFIER);
			} catch (Exception e) {
				logger.error(
						"an exception occurred when finalizePSDKApplicationBlocks to chat server, because of ",
						e.fillInStackTrace());
			} finally {
				if (protocol != null) {
					protocol = null;
				}
			}
		}
		eventReceivingBrokerService.releaseReceivers();
		if (eventReceivingBrokerService != null) {
			eventReceivingBrokerService = null;
		}
		System.gc();
	}

	public void disconnect() {
		if ((protocol != null) && (protocol.getState() == ChannelState.Opened)) {
			logger.info("begin to disconnect chat server...");
			try {
				protocol.close();
				protocolManagementService.unregister(CHATSERVER_IDENTIFIER);

			} catch (Exception e) {
				logger.error(
						"an exception occurred when disconnect to chat server, because of ",
						e.fillInStackTrace());
			}
		}
	}

	public void connect() {
		try {
			if (protocol != null && protocol.getState() == ChannelState.Closed) {
				logger.info("begin to connect chat server...");

				protocol.open();
				logger.info("chat server protocol state is " + protocol.getState().toString());
			}
		} catch (Exception e) {
			logger.error(
					"an exception occurred when connect to chat server, because of ",
					e.fillInStackTrace());
		}
	}

	@Override
	public void onChannelClosed(ChannelClosedEvent cce) {
		logger.info("chat server channel closed, " + cce.toString());
	}

	@Override
	public void onChannelError(ChannelErrorEvent cee) {
		logger.info("chat server channel error, " + cee.toString());
	}

	@Override
	public void onChannelOpened(EventObject eo) {
		logger.info("chat server channel opened.");
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public void setFileServerService(FileServerService fileServerService) {
		this.fileServerService = fileServerService;
	}

}
