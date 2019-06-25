package com.yuntongxun.mcm.genesys.service;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;

import com.genesyslab.platform.applicationblocks.commons.Action;
import com.genesyslab.platform.applicationblocks.commons.broker.EventReceivingBrokerService;
import com.genesyslab.platform.applicationblocks.commons.broker.MessageFilter;
import com.genesyslab.platform.applicationblocks.commons.protocols.ProtocolManagementServiceImpl;
import com.genesyslab.platform.applicationblocks.commons.protocols.TServerConfiguration;
import com.genesyslab.platform.commons.GEnum;
import com.genesyslab.platform.commons.collections.KeyValueCollection;
import com.genesyslab.platform.commons.protocol.ChannelClosedEvent;
import com.genesyslab.platform.commons.protocol.ChannelErrorEvent;
import com.genesyslab.platform.commons.protocol.ChannelListener;
import com.genesyslab.platform.commons.protocol.ChannelState;
import com.genesyslab.platform.commons.protocol.Message;
import com.genesyslab.platform.commons.protocol.Protocol;
import com.genesyslab.platform.commons.threading.SingleThreadInvoker;
import com.genesyslab.platform.voice.protocol.ConnectionId;
import com.genesyslab.platform.voice.protocol.tserver.AddressType;
import com.genesyslab.platform.voice.protocol.tserver.AgentWorkMode;
import com.genesyslab.platform.voice.protocol.tserver.CallType;
import com.genesyslab.platform.voice.protocol.tserver.ControlMode;
import com.genesyslab.platform.voice.protocol.tserver.DNRole;
import com.genesyslab.platform.voice.protocol.tserver.MakeCallType;
import com.genesyslab.platform.voice.protocol.tserver.MonitorNextCallType;
import com.genesyslab.platform.voice.protocol.tserver.RegisterMode;
import com.genesyslab.platform.voice.protocol.tserver.events.EventAbandoned;
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentLogin;
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentLogout;
import com.genesyslab.platform.voice.protocol.tserver.events.EventAgentReady;
import com.genesyslab.platform.voice.protocol.tserver.events.EventAttachedDataChanged;
import com.genesyslab.platform.voice.protocol.tserver.events.EventDestinationBusy;
import com.genesyslab.platform.voice.protocol.tserver.events.EventDialing;
import com.genesyslab.platform.voice.protocol.tserver.events.EventError;
import com.genesyslab.platform.voice.protocol.tserver.events.EventEstablished;
import com.genesyslab.platform.voice.protocol.tserver.events.EventHeld;
import com.genesyslab.platform.voice.protocol.tserver.events.EventMuteOff;
import com.genesyslab.platform.voice.protocol.tserver.events.EventMuteOn;
import com.genesyslab.platform.voice.protocol.tserver.events.EventPartyAdded;
import com.genesyslab.platform.voice.protocol.tserver.events.EventPartyChanged;
import com.genesyslab.platform.voice.protocol.tserver.events.EventPartyDeleted;
import com.genesyslab.platform.voice.protocol.tserver.events.EventRegistered;
import com.genesyslab.platform.voice.protocol.tserver.events.EventReleased;
import com.genesyslab.platform.voice.protocol.tserver.events.EventRetrieved;
import com.genesyslab.platform.voice.protocol.tserver.events.EventRinging;
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentLogin;
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentLogout;
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentNotReady;
import com.genesyslab.platform.voice.protocol.tserver.requests.agent.RequestAgentReady;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestCancelMonitoring;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestMonitorNextCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestRegisterAddress;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestSetMuteOff;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestSetMuteOn;
import com.genesyslab.platform.voice.protocol.tserver.requests.dn.RequestUnregisterAddress;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestAnswerCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestCompleteConference;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestDeleteFromConference;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestHoldCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestInitiateConference;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestMakeCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestReleaseCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.party.RequestRetrieveCall;
import com.genesyslab.platform.voice.protocol.tserver.requests.userdata.RequestUpdateUserData;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.genesys.model.Conference;
import com.yuntongxun.mcm.genesys.model.SipLogin;
import com.yuntongxun.mcm.genesys.util.CommonUtils;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.util.Constants;

public class SipServerService implements Action<Message>, ChannelListener {
	
	private static final Logger logger = LogManager.getLogger(SipServerService.class);
	
	private PushService pushService;
	
	private String TSERVER_IDENTIFIER;
	
	private Protocol voiceProtocol;
	
	private ProtocolManagementServiceImpl protocolManagementService;
	
	private String clientName; 

	private SipLogin sipLogin;
	
	private Conference conference;
	
	/**
	 * @Description 初始化 sip Server
	 * @params
	 * @return
	 */
	public int init(SipLogin sipLogin, String sipServerIP, String sipServerPort){
		try {
			this.sipLogin = sipLogin;
			this.TSERVER_IDENTIFIER = "softphone-sipclient_" + sipLogin.getUserAcc();
			this.clientName = "sip_softphone_" + sipLogin.getUserAcc();
			
			TServerConfiguration conf = new TServerConfiguration(TSERVER_IDENTIFIER);
			conf.setClientName(clientName);
			conf.setUri(sipServerIP, Integer.parseInt(sipServerPort));
			
			protocolManagementService = new ProtocolManagementServiceImpl();
			protocolManagementService.register(conf);
			protocolManagementService.addChannelListener((ChannelListener) this);
			
			EventReceivingBrokerService eventBrokerService = new EventReceivingBrokerService(
					new SingleThreadInvoker("TServerEventReceivingBrokerService"));
			eventBrokerService.register(this, new MessageFilter(
					protocolManagementService.getProtocol(TSERVER_IDENTIFIER).getProtocolId()));

			voiceProtocol = protocolManagementService.getProtocol(TSERVER_IDENTIFIER);
			voiceProtocol.setReceiver(eventBrokerService);
		} catch(Exception e) {
			logger.error("SipServerService init error.", e);
			sendExceptionMsg(48, "login sip server init failed.");
			return 0;
			 
		}
		return 1;
	}

	public Protocol getProtocol() {
		return voiceProtocol;
	}
	
	public int connect(){
		logger.info("begin to connect to sip server.");
		
		try {
			voiceProtocol.open();
			
			logger.info("ixn server protocol state is " + voiceProtocol.getState().toString());
		} catch (Exception ex) {
			logger.error("an exception occurred when connect to sip server.", ex);

			sendExceptionMsg(48, "login sip server, connect server failed.");
			return 0;
		}
		
		logger.info("begin to get sip service.");
		return 1;
	}
	

	public void disconnect() {
		try {
			if ((voiceProtocol != null) && (voiceProtocol.getState().equals(ChannelState.Opened))) {
				voiceProtocol.close();
				protocolManagementService.unregister(TSERVER_IDENTIFIER);
			}
		} catch (Exception e) {
			logger.error("an exception occurred when disconnect from sip server", e.fillInStackTrace());
		}
	}
	

	@Override
	public void handle(Message tMessage) {
		logger.info("Message is : \n" + tMessage);
		
		try {
			int messageId = tMessage.messageId();
			String messageName = tMessage.messageName();
			logger.info(messageName);
			
			String ani = "";
			String otherDN = "";
			String thisDN = "";
			ConnectionId connId = (ConnectionId) tMessage.getMessageAttribute("ConnID");
			logger.info("connId is:" + connId);
			
			if (tMessage.getMessageAttribute("ThisDN") != null) {
				thisDN = (String) tMessage.getMessageAttribute("ThisDN");
				logger.info("thisDN：" + thisDN);
			}
			if (tMessage.getMessageAttribute("OtherDN") != null) {
				otherDN = (String) tMessage.getMessageAttribute("OtherDN");
				logger.info("otherDN：" + otherDN);
			}
			if (tMessage.getMessageAttribute("CallType") != null) {
				String callType = ((CallType) tMessage
						.getMessageAttribute("CallType")).name();
				logger.info("callType：" + callType);
			}
			if (tMessage.getMessageAttribute("ANI") != null) {
				  ani= (String) tMessage.getMessageAttribute("ANI");
				  logger.info("ani：" + ani);
			}
			if (tMessage.getMessageAttribute("DNIS") != null) {
				String dnis = (String) tMessage.getMessageAttribute("DNIS");
				logger.info("dnis：" + dnis);
			}
			if (tMessage.getMessageAttribute("UserData") != null) {
				logger.info("UserData：" + tMessage.getMessageAttribute("UserData"));
			}
			if (tMessage.getMessageAttribute("Extensions") != null) {
				logger.info("Extensions：" + tMessage.getMessageAttribute("Extensions"));
			}
			if (tMessage.getMessageAttribute("ThisDNRole") != null) {
				String thisDNRole = ((DNRole) tMessage
						.getMessageAttribute("ThisDNRole")).name();
				logger.info("thisDNRole：" + thisDNRole);
			}
			if (tMessage.getMessageAttribute("OtherDNRole") != null) {
				String otherDNRole = ((DNRole) tMessage
						.getMessageAttribute("OtherDNRole")).name();
				logger.info("otherDNRole：" + otherDNRole);
			}
			if (tMessage.getMessageAttribute("TransferConnID") != null) {
				ConnectionId transferConnId = (ConnectionId) tMessage
						.getMessageAttribute("TransferConnID");
				logger.info("transferConnId：" + transferConnId);
			}
			if (tMessage.getMessageAttribute("CallState") != null) {
				Integer callState = (Integer) tMessage
						.getMessageAttribute("CallState");
				logger.info("callState：" + callState);
			}
			
			MCMMessageInfo mcMessageInfo = null;
			
			switch (messageId) {
				case EventRinging.ID:
					logger.info("have ringing.");
					
					//有新用户咨询
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_NewUserCallin_VALUE);
					mcMessageInfo.setUserAccount(connId.toString());
					Map<String, String> map1 = new HashMap<String, String>();
					map1.put("otherDN", otherDN);
					mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map1));
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
					
					sipLogin.setConnId(connId);
					break;
				case EventDialing.ID:
					logger.info("have dialing.");
					
					sipLogin.setConnId(connId);
					break;
				case EventEstablished.ID:
					logger.info("have established.");
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(80);
					mcMessageInfo.setUserAccount(connId.toString());
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
					
					sipLogin.setConnId(connId);
					
					if(conference != null){
						logger.info("[" + sipLogin.getUserAcc() + "," + conference.getThisDN() + "]start complete conference.");
						completeConference(conference.getConferenceConnId(), conference.getThisDN(), conference.getConferenceConnId());
					}
					
					break;
				case EventHeld.ID:
					logger.info("have held.");
					break;
				case EventRetrieved.ID:
					logger.info("have retrieved.");
					logger.info("取回");
					break;
				case EventReleased.ID:
					logger.info("离开");
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_UserReleaseCall_VALUE);
					mcMessageInfo.setUserAccount(connId.toString());
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
					
					//sipLogin.setConnId(null);
					conference = null;
					break;
				case EventAbandoned.ID:
					logger.info("用户无法响应");
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_UserReleaseCall_VALUE);
					mcMessageInfo.setUserAccount(connId.toString());
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
					//sipLogin.setConnId(null);
					conference = null;
					break;
				case EventDestinationBusy.ID:
					logger.info("繁忙");
					break;
				case EventPartyChanged.ID:
					logger.info("changed party");
					break;
				case EventPartyAdded.ID:
					logger.info("party added.");
					break;
				case EventPartyDeleted.ID:
					logger.info("party deleted.");
					break;
				case EventAttachedDataChanged.ID:
					logger.info("attached data changed");
					break;
				case EventMuteOff.ID:
					logger.info("静音");
					break;
				case EventMuteOn.ID:
					logger.info("取消静音");
					break;
				case EventAgentLogin.ID:
					//签入
					logger.info("EventAgentLogin");
					logger.info("AgentEvt_KFOnWorkResp_VALUE: " + sipLogin.getUserAcc());
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(MCMEventDefInner.NotifyAgent_KFOnWorkResp_VALUE);
					Map<String, String> map = new HashMap<String, String>();
					map.put("optRetDes", "login success");
					map.put("optResult", "0");
					mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map));
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC); 
					break;
				case EventAgentReady.ID:
					//就绪
					logger.info("EventAgentReady");
					logger.info("AgentEvt_KFOnWorkResp_VALUE: " + sipLogin.getUserAcc());
					mcMessageInfo = new MCMMessageInfo();
					mcMessageInfo.setCCSType(1);
					mcMessageInfo.setMCMEvent(78);
					Map<String, String> map2 = new HashMap<String, String>();
					map2.put("optRetDes", "ready success");
					map2.put("optResult", "0");
					mcMessageInfo.setMsgJsonData(JSONUtil.map2json(map2));
					pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC); 
					break;
				case EventError.ID:
					logger.info("eventError");
					break;
			}
		} catch (Exception e) {
			logger.error("sipServerService handle error:", e);
		}
	}

	/**
	 * @Description 响铃
	 * @params
	 * @return
	 */
	protected void onEventRinging(final Message message) {
		try {
			EventRinging event = (EventRinging) message;
			ConnectionId connId = event.getConnID();

		} catch (Exception ex) {
			logger.info("onEventRinging to get sip service.");
		}
	}

	public void throwIfNotConnected() throws Exception {
		logger.info("voiceProtocol state: " + voiceProtocol.getState() +
					", protocolManagementService: " + protocolManagementService);
		if (protocolManagementService == null
				|| voiceProtocol.getState() != ChannelState.Opened) {
			throw new Exception("connection is not opened.");
		}
	}

	/**
	 * @Description 注册
	 * @params
	 * @return
	 */
	public int registerAddress(String thisDN) {
		try {
			RequestRegisterAddress request = RequestRegisterAddress.create();
			request.setThisDN(thisDN);
			request.setAddressType(AddressType.DN);
			request.setControlMode(ControlMode.RegisterDefault);
			request.setRegisterMode(RegisterMode.ModeShare);
			
			logger.info("Sending: \n" + request);
			voiceProtocol.send(request);
			
			Message response = voiceProtocol.request(request);
			logger.info(response);
			
		} catch (Exception e) {
			logger.error("registerAddress error:\n", e.fillInStackTrace());
		}
		return 1;
	}

	public int unregisterAddress(String thisDN) {
		try {
			RequestUnregisterAddress request = RequestUnregisterAddress.create();
			request.setThisDN(thisDN);
			
			logger.info("Sending: \n" + request);
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("unregisterAddress error:\n", e.fillInStackTrace());
		}
		return 1;
	}

	/**
	 * @Description 签入
	 * @params
	 * @return
	 */
	public int login(String thisDN, String queueId, String loginId, String password) {
		try {
			throwIfNotConnected();
			
			registerAddress(thisDN);
			
			RequestAgentLogin request = RequestAgentLogin.create();
			request.setThisDN(thisDN);
			request.setThisQueue(queueId);
			request.setAgentID(loginId);
			request.setPassword(password);
			request.setAgentWorkMode(AgentWorkMode.ManualIn);
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
			//Message msgResponse = voiceProtocol.request(request);
			//int messageId = msgResponse.messageId();
		} catch (Exception e) {
			logger.error("login error:\n", e.fillInStackTrace());
			sendExceptionMsg(48, "login sip server,send login request to server failed.");
			return 0;
		}

		return 1;
	}

	/**
	 * @Description 签出
	 * @params
	 * @return
	 */
	public int logout(String thisDN, String queueId){
		try {
			thisDN = sipLogin.getThisDN();
			
			throwIfNotConnected();
			
			//先取消注册
			unregisterAddress(thisDN);
			
			RequestAgentLogout request = RequestAgentLogout.create();
			request.setThisDN(thisDN);
			request.setThisQueue(queueId);
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("logout error:\n", e.fillInStackTrace());
		}
		return 1;
	}

	/**
	 * @Description 就绪
	 * @params
	 * @return
	 */
	public int ready(String mediaType, String dn, String queueId) {
		try {
			throwIfNotConnected();
			RequestAgentReady request = RequestAgentReady.create();
			dn = sipLogin.getThisDN();
			logger.info("ready dn is:" + dn);
			request.setThisDN(dn);
			request.setThisQueue(queueId);
			
			logger.info("Sending: \n" + request);
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("ready error:\n", e.fillInStackTrace());
			sendExceptionMsg(78, "ready sip server,send ready request failed.");
		}
		return 1;
	}

	/**
	 * @Description 未就绪
	 * @params
	 * @return
	 */
	public int notReady(String mediaType, String reasonCode, String workMode,
			String dn, String queueId) {
		if (workMode == null || workMode.equals("0"))
			return notReady(reasonCode, AgentWorkMode.Unknown, dn, queueId);
		else if (workMode.equals("3"))
			return notReady(reasonCode, AgentWorkMode.AfterCallWork, dn,
					queueId);
		return 1;
	}

	private int notReady(String reasonCode, AgentWorkMode workMode, String dn,
			String queueId) {
		try {
			throwIfNotConnected();
			RequestAgentNotReady request = RequestAgentNotReady.create();
			dn = sipLogin.getThisDN();
			request.setThisDN(dn);
			request.setThisQueue(queueId);
			request.setAgentWorkMode(workMode);
			if (reasonCode != null && !reasonCode.equals("")) {
				Map<String, String> reasonMap = new HashMap<String, String>();
				reasonMap.put("reasonCode", reasonCode);
				request.setReasons(CommonUtils.mapToKeyValueCollection(reasonMap));
			}
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("notReady error:\n", e.fillInStackTrace());
		}
		return 1;
	}

	/**
	 * @Description 呼叫
	 * @params
	 * @return
	 */
	public void makeCall(final String otherDN, String thisDN,
			Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestMakeCall request = RequestMakeCall.create();
			request.setThisDN(thisDN);
			request.setOtherDN(otherDN);
			request.setMakeCallType(MakeCallType.Regular);
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("makeCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 应答
	 * @params
	 * @return
	 */
	public void answerCall(final String connId, String thisDN,
			Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestAnswerCall request = RequestAnswerCall.create();
			request.setThisDN(thisDN);
			request.setConnID(new ConnectionId(connId));
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("answerCall error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 应答
	 * @param thisDN
	 * @param params
	 */
	public void answerCall2(String thisDN, Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestAnswerCall request = RequestAnswerCall.create();
			request.setThisDN(thisDN);
			request.setConnID(this.sipLogin.getConnId());
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("answerCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 结束
	 * @params
	 * @return
	 */
	public void releaseCall(final String connId, String thisDN,
			Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestReleaseCall request = RequestReleaseCall.create();
			request.setThisDN(thisDN);
			request.setConnID(new ConnectionId(connId));
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("releaseCall error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 结束
	 * @param thisDN
	 * @param params
	 */
	public void releaseCall2(String thisDN, Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			
			RequestReleaseCall request = RequestReleaseCall.create();
			request.setThisDN(thisDN);
			//request.setConnID(new ConnectionId(connId));
			request.setConnID(this.sipLogin.getConnId());
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("releaseCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 保持
	 * @params
	 * @return
	 */
	public void holdCall(final String connId, String thisDN,
			Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestHoldCall request = RequestHoldCall.create();
			request.setThisDN(thisDN);
			request.setConnID(new ConnectionId(connId));
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("holdCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 恢复
	 * @params
	 * @return
	 */
	public void retrieveCall(final String connId, String thisDN,
			Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestRetrieveCall request = RequestRetrieveCall.create();
			request.setThisDN(thisDN);
			request.setConnID(new ConnectionId(connId));
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("holdCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 修改随机数据
	 * @params
	 * @return
	 */
	public void updateAttachedData(String connId, String thisDN,
			Map<String, String> attachedData) {
		try {
			throwIfNotConnected();
			RequestUpdateUserData request = RequestUpdateUserData.create();
			request.setThisDN(thisDN);
			request.setConnID(new ConnectionId(connId));
			request.setUserData(CommonUtils.mapToKeyValueCollection(attachedData));
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("holdCall error:\n", e.fillInStackTrace());
		}
	}

	/**
	 * @Description 设置会议
	 * @params
	 * @return
	 */
	public void completeConferenceInit(String connId, String thisDN, String otherDN){
		try {
			throwIfNotConnected();
			
			thisDN = sipLogin.getThisDN();
			logger.info("start initiate conference, otherDN[" 
							+ otherDN+ "], connId[" 
							+ sipLogin.getConnId() + "], thisDN[" 
							+ thisDN + "]");
			
			RequestInitiateConference inic = RequestInitiateConference.create();
			inic.setOtherDN(otherDN);
			inic.setConnID(sipLogin.getConnId());
			inic.setThisDN(thisDN);
			
			logger.info("Sending: \n" + inic);
			Message msgResponse = voiceProtocol.request(inic);
			logger.info("completeConference InitiateConference msgResponse: [" + msgResponse + "]");
			
			EventDialing eventDialing = (EventDialing) msgResponse;
			if(eventDialing != null){
				conference = new Conference();
				conference.setConferenceConnId(eventDialing.getConnID());
				conference.setThisDN(thisDN);
				conference.setConnId(sipLogin.getConnId());
			}else{
				logger.info("completeConference InitiateConference get eventDialing is null.");
			}
			
		} catch (Exception e) {
			logger.error("completeConferenceInit error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 进入会议
	 * RequestSingleStepConference request = RequestSingleStepConference.create();
	 * request.setReferenceID(ReferenceIdUtils.getReferenceId());
	 * CommonUtils.setParam(RequestSingleStepConference.class, request, params);
	 * logger.debug("Sending: \n" + request)
	 * @param connId
	 * @param thisDN
	 * @param otherDN
	 */
	public void completeConference(ConnectionId connId, String thisDN, ConnectionId conferenceConnID){
		try {
			throwIfNotConnected();
			logger.info("completeConference thisDn[" 
					+ thisDN
					+ "], connId[" + connId
					+", conferenceConnID[" + conferenceConnID + "]");
			
			RequestCompleteConference request = RequestCompleteConference.create();
			request.setThisDN(thisDN);
			request.setConnID(connId);
			request.setConferenceConnID(conferenceConnID);
			logger.info("Sending: \n" + request);
			//voiceProtocol.send(request);
			
			Message msgResponse = voiceProtocol.request(request);
			logger.info("completeConference msgResponse: \n" + msgResponse);
		} catch (Exception e) {
			logger.error("completeConference error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 监听
	 * @param thisDN
	 * @param otherDN
	 * @param monitorType  0是one call  1是all call
	 * @param monitorScope  call or agen
	 * @param monitorMode   mute or coach
	 */
	public void monitorNextCall(String thisDN, String otherDN, int monitorType, 
			String monitorScope, String monitorMode) {
		try {
			throwIfNotConnected();
			RequestMonitorNextCall request = RequestMonitorNextCall.create(thisDN,
					otherDN, (MonitorNextCallType)GEnum.getValue(MonitorNextCallType.class, monitorType));
			KeyValueCollection extensions = new KeyValueCollection();
			extensions.addString("MonitorScope", monitorScope);
			extensions.addString("MonitorMode", monitorMode);
			request.setExtensions(extensions);
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("monitorNextCall error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 取消监听
	 * @param thisDN
	 * @param otherDN
	 */
	public void cancelMonitoring(String thisDN,String otherDN) {
		try {
			throwIfNotConnected();
			RequestCancelMonitoring request = RequestCancelMonitoring.create(
					thisDN, otherDN);
			
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("cancelMonitoring error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 离开通话
	 * @param thisDN
	 * @param connId
	 * @param otherDN
	 */
	public void deleteFromConference(String thisDN,String connId, String otherDN){
		try {
			throwIfNotConnected();
			RequestDeleteFromConference request = RequestDeleteFromConference
					.create(thisDN, new ConnectionId(connId), otherDN);
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("deleteFromConference error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 静音
	 * @param thisDN
	 * @param connId
	 * @param params
	 */
	public void muteOn(String thisDN,String connId, Map<String, Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestSetMuteOn request = RequestSetMuteOn.create(thisDN,
					new ConnectionId(connId));
			CommonUtils.setParam(RequestSetMuteOn.class, request, params);
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("muteOn error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 取消静音
	 * @param thisDN
	 * @param connId
	 * @param params
	 */
	public void muteOff(String thisDN, String connId, Map<String, 
			Map<String, String>> params) {
		try {
			throwIfNotConnected();
			RequestSetMuteOff request = RequestSetMuteOff.create(thisDN,
					new ConnectionId(connId));
			CommonUtils.setParam(RequestSetMuteOff.class, request, params);
			logger.info("Sending: \n" + request);
			
			voiceProtocol.send(request);
		} catch (Exception e) {
			logger.error("muteOff error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 强插坐席通话
	 * @param connId
	 * @param monitorScope(call, agent)
	 */
	public void bargeIn(String thisDN,String connId, String monitorScope){
		logger.info("request to bargein, connId = " + connId + ", monitorScope = " + monitorScope);
		try {
			Map<String, Map<String, String>> params = new HashMap<String, Map<String, String>>();
			Map<String, String> extensions = new HashMap<String, String>();
			extensions.put("MonitorMode", "connect");
			extensions.put("MonitorScope", monitorScope);
			params.put("Extensions", extensions);
			muteOff(thisDN,connId, params); 
		} catch (Exception e) {
			logger.error("bargeIn error:\n", e.fillInStackTrace());
		}
	}
	
	/**
	 * 强拆坐席通话
	 * @param connId
	 * @param otherDN
	 * @param hasBargeIn
	 */
	public void forceOut(String thisDN,String connId, String otherDN){
		try {
			logger.info("request to force out, connId = " + connId + ", otherDN = " + otherDN );
			deleteFromConference(thisDN,connId, otherDN); 
		} catch (Exception e) {
			logger.error("forceOut error:\n", e.fillInStackTrace());
		} 	
	}
	
	/**
	 * 强制签出坐席
	 * @param thisDN
	 */
	public void fourceLogout(String thisDN){
		RequestRegisterAddress requestRegisterAddress = RequestRegisterAddress.create();
    	requestRegisterAddress.setThisDN(thisDN);
    	requestRegisterAddress.setAddressType(AddressType.DN);
    	requestRegisterAddress.setControlMode(ControlMode.RegisterDefault);
    	requestRegisterAddress.setRegisterMode(RegisterMode.ModeShare);
        Message registerMessage;
		try {
			registerMessage = voiceProtocol.request(requestRegisterAddress);
			if(registerMessage instanceof EventRegistered) {
	        	RequestAgentLogout requestAgentLogout = RequestAgentLogout.create(thisDN);
	            Message logoutMessage = voiceProtocol.request(requestAgentLogout);
	            if(logoutMessage instanceof EventAgentLogout) {
	            	logger.info("extension {} logout success."+thisDN);
	            } else {
	            	logger.info("extension {} logout fail."+thisDN);
	            }
	        } else {
	        	
	        }
		} catch (Exception e) {
			logger.error("fourceLogout error:\n", e.fillInStackTrace());
		} 
	}

	/**
	 * 强制坐席示忙
	 * @param thisDN
	 */
	public void fourceReady(String thisDN){
		RequestRegisterAddress requestRegisterAddress = RequestRegisterAddress.create();
    	requestRegisterAddress.setThisDN(thisDN);
    	requestRegisterAddress.setAddressType(AddressType.DN);
    	requestRegisterAddress.setControlMode(ControlMode.RegisterDefault);
    	requestRegisterAddress.setRegisterMode(RegisterMode.ModeShare);
        Message registerMessage;
		try {
			registerMessage = voiceProtocol.request(requestRegisterAddress);
			if(registerMessage instanceof EventRegistered) {
				AgentWorkMode word = AgentWorkMode.Unknown;
	        	RequestAgentReady requestAgentLogout = RequestAgentReady.create();
	        	requestAgentLogout.setThisDN(thisDN);
	            Message logoutMessage = voiceProtocol.request(requestAgentLogout);
	            if(logoutMessage instanceof EventAgentLogout) {
	            	logger.info("extension {} logout success."+thisDN);
	            } else {
	            	logger.info("extension {} logout fail."+thisDN);
	            }
	        } else {
	        	
	        }
		} catch (Exception e) {
			logger.error("fourceReady error:\n", e.fillInStackTrace());
		} 
	}
	
	public void onChannelClosed(ChannelClosedEvent arg0) {
		logger.info("onChannelClosed message is:\n" + arg0);
	}

	public void onChannelError(ChannelErrorEvent arg0) {
		logger.info("onChannelError message is:\n" + arg0);
	}

	public void onChannelOpened(EventObject arg0) {
		logger.info("onChannelOpened message is:\n" + arg0);
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
			pushService.doPushMsg(sipLogin.getUserAcc(), mcMessageInfo, -1, Constants.SUCC);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}
	
}
