package com.yuntongxun.mcm.service.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.common.protobuf.ProtobufCodecManager;
import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.connection.ModuleProducter;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.AgentStateOptInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.core.protobuf.PushAction.MsgRealInner;
import com.yuntongxun.mcm.core.protobuf.PushAction.MsgRouteInner;
import com.yuntongxun.mcm.core.protobuf.PushAction.PushActionInner;
import com.yuntongxun.mcm.core.protobuf.PushMsgNotify.PushMsgNotifyInner;
import com.yuntongxun.mcm.dao.MessageRedisDao;
import com.yuntongxun.mcm.dao.UserRedisDao;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.mcm.dao.MCMDao;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.model.mail.McmMailAttchmentInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.sevenmoor.model.SevenMoorLoginRInfo;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.StringUtil;
import com.yuntongxun.mcm.util.cryptos.Cryptos;

/**
 * 负责推送消息的业务层
 * 
 * @author chao
 */
public class PushServiceImpl implements PushService {

	public static final Logger logger = LogManager.getLogger(PushServiceImpl.class);

	private ExecutorService executorService;
	
	private ModuleProducter moduleProducter;
	
	private String cryptosKey;
	
	private int pushMessageThreadNumber;
	
	private MCMDao mcmDao;

	private VersionDao versionDao;

	private UserRedisDao userRedisDao;
	
	private UserAgentDialogRedisDao userAgentDialogRedisDao;
	
	private AsService asService;
	
	private RMServerRequestService rmServerRequestService;
	
	private Integer defaultQueueType;
	
	private MessageRedisDao messageRedisDao;
	
	public void init() {
		executorService = Executors.newFixedThreadPool(pushMessageThreadNumber);
	}
	
	@Override
	public void doPushMsg(String userAcc, MCMMessageInfo mcmMessageInfo)
			throws CCPServiceException {
		doPushMsg(userAcc, mcmMessageInfo, -1, Constants.SUCC);
	}
	
	@Override
	public void doPushMsg(String userAcc, MCMMessageInfo mcmMessageInfo,
			UserAndAgentDialog userAndAgentDialog, String sendUserAcc) throws CCPServiceException {
		doPushMsg(userAcc, mcmMessageInfo, -1, Constants.SUCC, userAndAgentDialog, sendUserAcc);
	}
	
	@Override
	public void doPushMsg(final String userAcc, final MCMMessageInfo mcmMessageInfo, 
			final int protoClientNo, final int errorCode) throws CCPServiceException {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				List<UserLoginInfo> receiverList = null;
				
				try {
					ThreadContext.push(sessionId);
					PrintUtil.printStartTag("PushMsg");
					
					receiverList = userRedisDao.getUserLoginInfo(userAcc);
					
					long t2 = System.currentTimeMillis();
					logger.info("query user online info, cost time: {}", (t2 - t1));
					
					// 账号下有1个或者多个设备在线
					if (receiverList != null && !receiverList.isEmpty()) {
						
						logger.info("{}: have [{}] device online.", userAcc, receiverList.size());
						
						for (UserLoginInfo receiverLogin : receiverList) {
							logger.info("doPushMsg to userName: {}, deviceno: {}.",  receiverLogin.getUserName(), 
									receiverLogin.getDeviceno());
							
							try {
								doPush(mcmMessageInfo, receiverLogin.getConnectorId(), receiverLogin.getSessionId(), 
										userAcc, protoClientNo, errorCode);
							} catch (IOException e) {
								logger.error("doPushMsg error:", e);
								continue;
							}
						}
						
						long t3 = System.currentTimeMillis();
						logger.info("doPushMsg to all online device, total used time: {}.", (t3 - t1));
						
					}else{
						logger.info("{}: have [{}] device online.", userAcc, 0);
					}
					
				} catch (CCPRedisException e) {
					logger.error("doPushMsg#error()", e);
					
				} finally{
					PrintUtil.printEndTag("PushMsg");
					ThreadContext.removeStack();
				}
			}
			
		});
	}
	
	@Override
	public void doPushMsg(final String userAcc, final MCMMessageInfo mcmMessageInfo, 
			final int protoClientNo, final int errorCode, final UserAndAgentDialog userAndAgentDialog, 
			final String sendUserAcc) throws CCPServiceException {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				List<UserLoginInfo> receiverList = null;
				
				try {
					ThreadContext.push(sessionId);
					PrintUtil.printStartTag("PushMsg");
					
					receiverList = userRedisDao.getUserLoginInfo(userAcc);
					
					long t2 = System.currentTimeMillis();
					logger.info("query user online info, cost time: {}", (t2 - t1));
					
					// 账号下有1个或者多个设备在线
					if (receiverList != null && !receiverList.isEmpty()) {
						
						logger.info("{}: have [{}] device online.", userAcc, receiverList.size());
						
						for (UserLoginInfo receiverLogin : receiverList) {
							logger.info("doPushMsg to userName: {}, deviceno: {}.",  receiverLogin.getUserName(), 
									receiverLogin.getDeviceno());
							
							try {
								doPush(mcmMessageInfo, receiverLogin.getConnectorId(), receiverLogin.getSessionId(), 
										userAcc, protoClientNo, errorCode);
							} catch (IOException e) {
								logger.error("doPushMsg error:", e);
								continue;
							}
						}
						
						long t3 = System.currentTimeMillis();
						logger.info("doPushMsg to all online device, total used time: {}.", (t3 - t1));
						
					}else{
						logger.info("{}: have [{}] device online.", userAcc, 0);
						
						/*if(userAndAgentDialog != null){
							String notifyUrl = userAndAgentDialog.getMcm_notify_url();
							logger.info("notify url: {}.", notifyUrl);
							
							if(StringUtils.isNotBlank(notifyUrl)){
								//重新分配坐席
								logger.info("{}: start reload ask.", sendUserAcc);
								
								// 结束咨询
								boolean result = asService.stopMsgRequestAS(userAndAgentDialog);
								if(result){
									logger.info("{}: start modify dialog.", sendUserAcc);
									userAndAgentDialog.setAgentInfoSet(new HashSet<AgentInfo>());
									userAgentDialogRedisDao.saveDialog(sendUserAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
									
									//准备向AS侧发送开始咨询对象
									ASDataInfo requestData = new ASDataInfo();
									requestData.setAction(Constants.AS_ACTION_START_MESSAGE);
									requestData.setSid(userAndAgentDialog.getSid());
									requestData.setOsUnityAccount(userAndAgentDialog.getOsUnityAccount());
									requestData.setAppId(userAndAgentDialog.getAppId());
									requestData.setCustomAppID(userAndAgentDialog.getCustomAppId());
									requestData.setChanType(String.valueOf(userAndAgentDialog.getChanType()));
									requestData.setMsgid("");
									requestData.setCreateTime(String.valueOf(System.currentTimeMillis()));
									requestData.setUserAccount(sendUserAcc);
									
									//向AS侧发送 HTTP请求
									ASDataInfo responseData = asService.startAskRequestAS(requestData.toStartAskJson(), notifyUrl);
									if(responseData == null){
										logger.info("responseData is null.");
									}else{
										logger.info("AS response data:{}", JSONUtil.object2json(responseData));
									}
									
									// 申请获取坐席
									RMServerTransferData rmServerTransferData = new RMServerTransferData();
									rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
									rmServerTransferData.setAppid(userAndAgentDialog.getAppId());
									rmServerTransferData.setSid(userAndAgentDialog.getSid()); // 用户session ID
									
									// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
									if(responseData.isForce()){
										rmServerTransferData.setForce(true);
									}
									if(StringUtils.isNotEmpty(responseData.getAgentidfirst())){
										rmServerTransferData.setAgentid(responseData.getAgentidfirst());
									}
									if(StringUtils.isNotEmpty(responseData.getQueueType())){
										rmServerTransferData.setQueuetype(Integer.parseInt(responseData.getQueueType()));
									}
									rmServerTransferData.setUseraccount(sendUserAcc);
									rmServerTransferData.setKeytype(userAndAgentDialog.getChanType());
									rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
									
									SeqInfo seqInfo = new SeqInfo();
									seqInfo.setMcmEvent(MCMEventDefInner.UserEvt_StartAsk_VALUE);
									seqInfo.setAppId(userAndAgentDialog.getAppId());
									seqInfo.setProtoClientNo(protoClientNo);
									seqInfo.setAsFlag(Constants.AS_FLAG_YES);
									seqInfo.setUserAccount(sendUserAcc);
									if(StringUtils.isNotEmpty(responseData.getWelcome())){
										seqInfo.setAsWelcome(responseData.getWelcome());
									}
									rmServerTransferData.setSeq(seqInfo.toString());
									
									String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
									
									try {
										rmServerRequestService.doPushMessage(cmdMessage);
									} catch (CCPServiceException e) {
										logger.error("cmdAllocImAgent#error()", e);
									}
								}
							} else {
								//重新分配坐席
								logger.info("{}: start reload ask.", sendUserAcc);
								
								// 通知rm server结束服务，还有问题，queueType怎么传
								RMServerTransferData rmServerTransferData = new RMServerTransferData();
								rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
								rmServerTransferData.setAppid(userAndAgentDialog.getAppId());
								rmServerTransferData.setSid(userAndAgentDialog.getSid());
								
								Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
								for(AgentInfo agentInfo : agentInfoSet){
									if(!agentInfo.getAgentAccount().equals(userAcc)){
										continue;
									}
									rmServerTransferData.setAgentid(agentInfo.getAgentId());
									
									SeqInfo seqInfo = new SeqInfo();
									seqInfo.setMcmEvent(-1);
									seqInfo.setUserAccount(userAcc);
									seqInfo.setAgentId(agentInfo.getAgentId());
									seqInfo.setLogSessionId(ThreadContext.peek());
									rmServerTransferData.setSeq(seqInfo.toString());
									
									try {
										rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
									} catch (CCPServiceException e) {
										logger.error("serviceEnd#error()", e);
									}
								}
								
								// 重新分配坐席
								rmServerTransferData = new RMServerTransferData();
								rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
								rmServerTransferData.setAppid(userAndAgentDialog.getAppId());
								rmServerTransferData.setSid(userAndAgentDialog.getSid()); 
								
								logger.info("set default queue type: {}.", defaultQueueType);
								if(defaultQueueType == null){
									logger.warn("default queue type is null.");
								} else{
									rmServerTransferData.setQueuetype(defaultQueueType);	
								}
								
								// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
								// rmServerTransferData.setForce(force);
								rmServerTransferData.setUseraccount(sendUserAcc);
						
								rmServerTransferData.setKeytype(userAndAgentDialog.getChanType());
								
								// 自定义文本内容。对于微信用户此字段会有内容
								rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
								
								SeqInfo seqInfo = new SeqInfo();
								seqInfo.setMcmEvent(-1);
								seqInfo.setAppId(userAndAgentDialog.getAppId());
								seqInfo.setUserAccount(sendUserAcc);
								seqInfo.setLogSessionId(ThreadContext.peek());
								rmServerTransferData.setSeq(seqInfo.toString());
								
								String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
								try {
									rmServerRequestService.doPushMessage(cmdMessage);
								} catch (CCPServiceException e) {
									logger.error("allocImAgent#error()", e);
								}
								
							}
						} else {
							logger.info("userAndAgentDialog is null.");
						}*/
					}
					
				} catch (CCPRedisException e) {
					logger.error("doPushMsg#error()", e);
					
				} finally{
					PrintUtil.printEndTag("PushMsg");
					ThreadContext.removeStack();
				}
			}
			
		});
	}
	
	/**
	 * @Description: 删除会话中坐席相关信息
	 * @param agentId
	 * @param agentInfoList 
	 */
	private void removeAgentInfo(String userAcc, Set<AgentInfo> agentInfoSet){
		for (Iterator<AgentInfo> iter = agentInfoSet.iterator(); iter.hasNext();) {  
			AgentInfo agentInfo = iter.next();  
			if(userAcc.equals(agentInfo.getAgentAccount())){
				logger.info("start remove agentAccount: {}.", userAcc);
			    iter.remove();  
            }  
        }  
	}
	
	/**
	 * @Description: 推送消息
	 * @param messageInfo
	 * @param connectorId
	 * @param sessionId connector sessionId
	 * @param userAcc 用户编号
	 * @throws IOException
	 */
	private void doPush(final MCMMessageInfo messageInfo, final String connectorId, final String sessionId, 
			String userAcc, int protoClientNo, int errorCode) throws IOException {
		byte[] encodeByte = null;
		int protoType = Constants.PUSH_TYPE_NOTIFY;
		
		long t1 = System.currentTimeMillis();

//		if (messageInfo.getMsgLength() <= Constants.PUSH_SMALL_MSG_LENGTH) {
			if (true) {
			logger.info("message length [" + messageInfo.getMsgLength() + "] less than ["
					+ Constants.PUSH_SMALL_MSG_LENGTH + "], push message...");
			
			MCMDataInner.Builder builder=MCMDataInner.newBuilder();
			
			if (messageInfo.getVersion() != -1) {
				builder.setVersion(messageInfo.getVersion());
				//builder.setChanType(""+messageInfo.getChanType());
			}
			
			if(messageInfo.getMCMEvent() > 0){
				builder.setMCMEvent(messageInfo.getMCMEvent());
			}
			
			if (messageInfo.getMsgDateCreated() != null && messageInfo.getMsgDateCreated().length() != 0) {
				builder.setMsgDateCreated(messageInfo.getMsgDateCreated());
			}
			
			if (messageInfo.getUserAccount() != null && messageInfo.getUserAccount().length() != 0) {
				builder.setUserAccount(messageInfo.getUserAccount());
			}
			
			if (messageInfo.getAgentAccount() != null && messageInfo.getAgentAccount().length() != 0) {
				builder.setUserAccount(messageInfo.getAgentAccount());
			}
			
			if (messageInfo.getOsUnityAccount() != null && messageInfo.getOsUnityAccount().length() != 0) {
				builder.setOsUnityAccount(messageInfo.getOsUnityAccount());
			}
			
			if(messageInfo.getAppId()!=null && messageInfo.getAppId().length()>0){
				builder.setAppId(messageInfo.getAppId());
			}
			
			//genesys
			if(messageInfo.getNickName()!=null && messageInfo.getNickName().length() != 0){
				builder.setAppId(messageInfo.getNickName());
			}
			
			if(messageInfo.getMsgJsonData()!=null && messageInfo.getMsgJsonData().length() != 0){
				builder.setMsgJsonData(messageInfo.getMsgJsonData());
			}
			
			if(messageInfo.getCCSType() > 0){
				builder.setCCSType(messageInfo.getCCSType());
			}
			
			if(messageInfo.getCcpCustomData()!=null && messageInfo.getCcpCustomData().length() != 0){
				builder.setCcpCustomData(messageInfo.getCcpCustomData());
			}
			
			if(messageInfo.getAgentId()!=null && messageInfo.getAgentId().length() != 0){
				builder.setAgentId(messageInfo.getAgentId());
			}
			
			if(messageInfo.getOptRetDes() != null && messageInfo.getOptRetDes().length() != 0){
				AgentStateOptInner.Builder aso = AgentStateOptInner.newBuilder();
				aso.setOptResult(messageInfo.getOptResult());
				aso.setOptRetDes(messageInfo.getOptRetDes());	
				builder.setAgentStateOpt(aso);
			}
			
			builder.setChanType(String.valueOf(messageInfo.getChanType()));
			builder.setIdlecount(messageInfo.getIdlecount());
			
			if(messageInfo.getMsgType() != 0){
				MSGDataInner.Builder msagDataBuilder=MSGDataInner.newBuilder();
				
				msagDataBuilder.setMsgType(messageInfo.getMsgType());
				
				if(messageInfo.getMsgType()==MCMTypeInner.MCMType_txt_VALUE){
					if(messageInfo.getMsgContent() !=null && messageInfo.getMsgContent().length()>0){
						msagDataBuilder.setMsgContent(messageInfo.getMsgContent());
					}
				}
				
				if (messageInfo.getMsgType() != 0 && messageInfo.getMsgType() != MCMTypeInner.MCMType_txt_VALUE) {
					if (messageInfo.getMsgFileName() != null && messageInfo.getMsgFileName().length() != 0) {
						msagDataBuilder.setMsgFileName(messageInfo.getMsgFileName());
					}
					if(messageInfo.getMsgFileUrl()!=null && messageInfo.getMsgFileUrl().length()>0){
						msagDataBuilder.setMsgFileUrl(messageInfo.getMsgFileUrl());
					}
				}
				
				builder.addMSGData(msagDataBuilder.build());
			}
			
			encodeByte = ProtobufCodecManager.encoder(builder.build());
			
			protoType = Constants.PUSH_MCM_QUEUE;
		} /*else { // push notify when more than 320 bytes
			logger.info("message length [" + messageInfo.getMsgLength() + "] more than ["
					+ Constants.PUSH_SMALL_MSG_LENGTH + "], push notify...");
			PushMsgNotifyInner pushMsg = PushMsgNotifyInner.newBuilder().setVersion(messageInfo.getVersion()).build();
			encodeByte = ProtobufCodecManager.encoder(pushMsg);
			//超过320字节，入库
			try {
				if(StringUtils.isNotEmpty(messageInfo.getMsgContent())){
					MessageInfo dbMsgInfo = new MessageInfo();
					dbMsgInfo.setVersion(messageInfo.getVersion());
					dbMsgInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
					dbMsgInfo.setMsgId(messageInfo.getMsgId()); // messageId出的是版本号
					
					dbMsgInfo.setMsgSender(messageInfo.getOsUnityAccount());
					dbMsgInfo.setMsgReceiver(StringUtil.splitUserAcc(userAcc).get(
							"userAccount"));
					
					String msgContent = "";
					try {
						msgContent = Base64.encode(messageInfo.getMsgContent().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e2) {
						logger.info("return welcome word to user error:"+e2.getMessage());
					}
					dbMsgInfo.setMsgContent(msgContent);
					
					dbMsgInfo.setDateCreated(messageInfo.getMsgDateCreated());
					dbMsgInfo.setMcmEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE); // 多渠道消息事件
					mcmDao.saveInstantMessage(userAcc, dbMsgInfo);
				}
				
			} catch (CCPDaoException e) {
				logger.info("save message to cassandra exeception "+e);
			}
		}*/

		//消息加密 Encode
		byte[] qesEncodeByte = Cryptos.toQESEncode(cryptosKey, encodeByte);
		
		// PUSH到客户端
		MsgLiteInner pushResp = MsgLiteFactory.getMsgLite(protoType, qesEncodeByte, protoClientNo, 
				sessionId, null, errorCode, encodeByte.length, null);
		String dest = moduleProducter.getSimpleClient().getRoute(Constants.CONNECTOR_QUEUE, connectorId);
		moduleProducter.sendBytesMessage(dest, pushResp);
	}
	
	@Override
	public void doPushMsg(MsgLiteInner pushResp, String destQueueName) {
		moduleProducter.sendBytesMessage(destQueueName, pushResp);
		logger.info("push to a message to"+destQueueName+" success!");
	}

	@Override
	public void doPushMailMsg(final String userAcc, final McmMailMsgInfo mailMsgInfo) 
			throws CCPServiceException {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				long t1=System.currentTimeMillis();
				// find the receiver route
				List<UserLoginInfo> receiverList;
				try {
					receiverList = userRedisDao.getUserLoginInfo(userAcc);
					long t2=System.currentTimeMillis();
					logger.info("++++++++++ 31-1 query user time "+(t2-t1));
					if (receiverList != null && receiverList.size() > 0) {// 账号下有1个或者多个设备在线
						for (UserLoginInfo receiverLogin : receiverList) {
							logger.info("Receiver: [" + receiverLogin.getUserName() + " - " + receiverLogin.getDeviceno() + "]");
							try {
								doPushMailMsg(mailMsgInfo, receiverLogin.getConnectorId(), receiverLogin.getSessionId(),userAcc);
							} catch (IOException e) {
								logger.error("doPushMailMsg error:"+e.getMessage());
							}
						}
						long t3=System.currentTimeMillis();
						logger.info("++++++++++ 31-4 doPushMailMsg time "+(t3-t1));
					}
				} catch (CCPRedisException e) {
					logger.error("doPushMailMsg error:"+e.getMessage());
				}
				
			}
		});
	}

	/**
	 * @Description: 推送邮件消息
	 * @param mailMsgInfo
	 * @param connectorId
	 * @param sessionId
	 * @param userAcc
	 * @throws IOException
	 */
	private void doPushMailMsg(final McmMailMsgInfo mailMsgInfo, final String connectorId, 
			final String sessionId, String userAcc) throws IOException {
		byte[] encodeByte = null;
		byte[] qesEncodeByte = null;
		MsgLiteInner pushResp = null;
		int protoType = Constants.PUSH_TYPE_NOTIFY;
		
		long version = versionDao.getMessageVersion(userAcc);
		
		MCMDataInner.Builder mcmDataBuilder=MCMDataInner.newBuilder();
		mcmDataBuilder.setMCMEvent(MCMEventDefInner.UserEvt_SendMail_VALUE);
		if(StringUtils.isNotEmpty(mailMsgInfo.getUserAccount())){
			mcmDataBuilder.setUserAccount(mailMsgInfo.getUserAccount());
		}
		if(StringUtils.isNotEmpty(mailMsgInfo.getOsUnityAccount())){
			mcmDataBuilder.setOsUnityAccount(mailMsgInfo.getOsUnityAccount());
			
		}
		if(StringUtils.isNotEmpty(mailMsgInfo.getAppId())){
			mcmDataBuilder.setAppId(mailMsgInfo.getAppId());
			
		}
		mcmDataBuilder.setVersion(version);
		boolean isNotify = false;
		mcmDataBuilder.setChanType(String.valueOf(MCMChannelTypeInner.MCType_mail_VALUE));
		if(StringUtils.isNotEmpty(mailMsgInfo.getMsgContent())){
			MSGDataInner.Builder msgDataBuilder=MSGDataInner.newBuilder();
			if(mailMsgInfo.getMsgContent().length()>Constants.PUSH_SMALL_MSG_LENGTH){
				isNotify = true;
				logger.info("message length [" + mailMsgInfo.getMsgContent().length() + "] more than ["
						+ Constants.PUSH_SMALL_MSG_LENGTH + "], push notify...");
			}else{
				logger.info("message length [" + mailMsgInfo.getMsgContent().length() + "] less than ["
						+ Constants.PUSH_SMALL_MSG_LENGTH + "], push message...");
			}
			msgDataBuilder.setMsgContent(mailMsgInfo.getMsgContent());
			if(StringUtils.isNotEmpty(mailMsgInfo.getMsgType())){
				msgDataBuilder.setMsgType(Integer.parseInt(mailMsgInfo.getMsgType()));
			}
			mcmDataBuilder.addMSGData(msgDataBuilder.build());
		}
		
		if(mailMsgInfo.getAttachment()!=mailMsgInfo&&mailMsgInfo.getAttachment().size()>0){
			MSGDataInner.Builder msgDataBuilder = null;
			for(McmMailAttchmentInfo attchment:mailMsgInfo.getAttachment()){
				msgDataBuilder = MSGDataInner.newBuilder();
				if(StringUtils.isNotEmpty(attchment.getFileName())){
					msgDataBuilder.setMsgFileName(attchment.getFileName());
				}

				if(StringUtils.isNotEmpty(attchment.getUrl())){
					msgDataBuilder.setMsgFileUrl(attchment.getUrl());
				}
				mcmDataBuilder.addMSGData(msgDataBuilder.build());
			}
		}
		
		//判断消息通知类型
		//当消息长度小于320字节，推送消息类型为多渠道消息
		if(isNotify){
			//当消息长度大于320字节，推送消息类型为通知
			// push notify when more than 320 bytes
			PushMsgNotifyInner pushMsg = PushMsgNotifyInner.newBuilder().setVersion(version).build();
			encodeByte = ProtobufCodecManager.encoder(pushMsg);
			protoType = Constants.PUSH_TYPE_NOTIFY;
		}else{
			protoType = Constants.PUSH_MCM_QUEUE;
			//消息加密 Encode
			encodeByte = ProtobufCodecManager.encoder(mcmDataBuilder.build());
			protoType = Constants.PUSH_MCM_QUEUE;
		}
		
		//消息加密 Encode
		qesEncodeByte = Cryptos.toQESEncode(cryptosKey, encodeByte);
		// PUSH到客户端
		pushResp = MsgLiteFactory.getMsgLite(protoType, qesEncodeByte, -1, sessionId, null, Constants.SUCC, encodeByte.length, null);
		
		String dest = moduleProducter.getSimpleClient().getRoute(Constants.CONNECTOR_QUEUE, connectorId);
		moduleProducter.sendBytesMessage(dest, pushResp);
	}
	
	@Override
	public void replySuccToSDK(int protoClientNo,String connectorId){
		MsgLiteInner pushResp = MsgLiteFactory.getMsgLite(Constants.PUSH_MCM_QUEUE, null, protoClientNo, null, null, Constants.SUCC, -1, null);
		
		String dest = moduleProducter.getSimpleClient().getRoute(Constants.CONNECTOR_QUEUE, connectorId);
		moduleProducter.sendBytesMessage(dest, pushResp);
	}
	
	@Override
	public boolean doPushMsgFor7Moor(SevenMoorLoginRInfo sevenMoorLoginRInfo, MCMMessageInfo mcmMessageInfo,
			int protoClientNo, int errorCode) throws CCPServiceException {
		boolean result = false;
		List<UserLoginInfo> receiverList = null;
		try {
			PrintUtil.printStartTag("PushMsg");

			receiverList = userRedisDao.getUserLoginInfo(sevenMoorLoginRInfo.getUserAcc());

			// 账号下有1个或者多个设备在线
			if (receiverList != null && !receiverList.isEmpty()) {

				logger.info("{}: have [{}] device online.", sevenMoorLoginRInfo.getUserAcc(), receiverList.size());

				for (UserLoginInfo receiverLogin : receiverList) {
					logger.info("doPushMsg to userName: {}, deviceno: {}.", receiverLogin.getUserName(),
							receiverLogin.getDeviceno());
					try {
						doPush(mcmMessageInfo, receiverLogin.getConnectorId(), receiverLogin.getSessionId(),
								sevenMoorLoginRInfo.getUserAcc(), protoClientNo, errorCode);
					} catch (IOException e) {
						logger.error("doPushMsg error:", e);
						continue;
					}
				}
				result = true;
			} else {
				logger.info("{}: have [{}] device online.", sevenMoorLoginRInfo.getUserAcc(), 0);
				result = false;
			}
		} catch (CCPRedisException e) {
			logger.error("doPushMsg#error()", e);
			return result;
		} finally {
			PrintUtil.printEndTag("PushMsg");
		}
		return result;
	}
	
	@Override
	public boolean doPushMsgFor7Moor(String userAcc, MessageInfo messageInfo) throws CCPServiceException {
		PushActionInner.Builder builder = PushActionInner.newBuilder();
		
		String appId = StringUtil.getAppIdFormUserAcc(userAcc);
		String userName = StringUtil.getUserNameFormUserAcc(userAcc);
		
		messageInfo.setMsgReceiver(userName);
		builder.addMsgReal(getMsgRealInner(messageInfo));
		
		MsgRouteInner.Builder routeBuilder = MsgRouteInner.newBuilder();
		routeBuilder.setAppId(appId);
		routeBuilder.setUsername(messageInfo.getMsgSender());
		routeBuilder.setSessionId(ThreadContext.peek());
		builder.setMsgRoute(routeBuilder.build());
		
		try {
			MsgLiteInner messageLite = MsgLiteFactory.getMsgLite(Constants.PUSH_TYPE_MSG, ProtobufCodecManager.encoder(builder.build()));
			// push message
			String pushQueueName = moduleProducter.getSimpleClient().getRoute(Constants.PUSH_TYPE_MSG, userName);
			moduleProducter.sendBytesMessage(pushQueueName, messageLite);
			logger.info("message has already put into the PushServer Queue...");
			
			
			String msgContent = messageInfo.getMsgContent();
			if(StringUtils.isNotBlank(msgContent)){
				msgContent = EncryptUtil.base64Encoder(msgContent);	
				messageInfo.setMsgContent(msgContent);
			}
			messageRedisDao.saveMessage(userAcc, messageInfo);
		} catch (Exception e) {
			logger.error("doPushMsgFor7Moor#error()", e);
			return false;
		}
		return true;
	}
	
	private MsgRealInner getMsgRealInner(MessageInfo messageInfo) {
		MsgRealInner.Builder builder = MsgRealInner.newBuilder();
		
		if (messageInfo.getVersion() != -1) {
			builder.setVersion(messageInfo.getVersion());
			builder.setMsgId("" + messageInfo.getVersion());
		}
		
		//com.google.protobuf.ByteString msgContent = messageInfo.handleMsgContent();
		String msgContent = messageInfo.getMsgContent();
		if (msgContent != null && msgContent.length() != 0) {
			builder.setMsgContent(com.google.protobuf.ByteString.copyFrom(msgContent.getBytes()));
		}
		
		if (messageInfo.getDateCreated() != null && messageInfo.getDateCreated().length() != 0) {
			builder.setMsgDateCreated(messageInfo.getDateCreated());
		}
		
		if (messageInfo.getMsgDomain() != null && messageInfo.getMsgDomain().length() != 0) {
			builder.setMsgDomain(messageInfo.getMsgDomain());
		}
		
		if (messageInfo.getMsgReceiver() != null && messageInfo.getMsgReceiver().length() != 0) {
			builder.setMsgReceiver(messageInfo.getMsgReceiver());
		}
		
		if (messageInfo.getMsgSender() != null && messageInfo.getMsgSender().length() != 0) {
			builder.setMsgSender(messageInfo.getMsgSender());
		}
		
		if (messageInfo.getMsgCompressLen() > 0) {
			builder.setMsgCompressLen(messageInfo.getMsgCompressLen());
		}
		
		if (messageInfo.getMsgType() > 0) {
			builder.setMsgType(messageInfo.getMsgType());
		}
		
		int mcmEvent = messageInfo.getMcmEvent();
		if(mcmEvent > 0){
			builder.setMcmEvent(mcmEvent);	
		}
		
		if (messageInfo.getMsgType() != 0 && messageInfo.getMsgType() != MessageInfo.MSG_TEXT) {
			if (messageInfo.getMsgFileName() != null && messageInfo.getMsgFileName().length() != 0) {
				builder.setMsgFileName(messageInfo.getMsgFileName());
			}
			
			if (messageInfo.getFileUrl() != null && messageInfo.getFileUrl().length() != 0) {
				if (messageInfo.getMsgType() == MessageInfo.MSG_PIC) {
					builder.setMsgFileUrl(messageInfo.getFileUrl() + "_thum");
				} else {
					builder.setMsgFileUrl(messageInfo.getFileUrl());
				}
			}
			
			if (messageInfo.getMsgFileSize() != null && messageInfo.getMsgFileSize().length() != 0) {
				builder.setMsgFileSize(messageInfo.getMsgFileSize());
			}
			
		}
		
		builder.setMsgLength(messageInfo.getMsgLength());

		if (messageInfo.getExtOpts() != null && messageInfo.getExtOpts().length() != 0) {
			builder.setExtOpts(messageInfo.getExtOpts());
		}
		
		return builder.build();
	}
	
	/**
	 * set inject
	 */
	public void setCryptosKey(String cryptosKey) {
		this.cryptosKey = cryptosKey;
	}

	public void setPushMessageThreadNumber(int pushMessageThreadNumber) {
		this.pushMessageThreadNumber = pushMessageThreadNumber;
	}

	public void setMcmDao(MCMDao mcmDao) {
		this.mcmDao = mcmDao;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}
	
	public void setUserRedisDao(UserRedisDao userRedisDao) {
		this.userRedisDao = userRedisDao;
	}

	public void setUserAgentDialogRedisDao(UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}

	public void setAsService(AsService asService) {
		this.asService = asService;
	}

	public void setRmServerRequestService(RMServerRequestService rmServerRequestService) {
		this.rmServerRequestService = rmServerRequestService;
	}

	public void setDefaultQueueType(Integer defaultQueueType) {
		this.defaultQueueType = defaultQueueType;
	}

	public void setModuleProducter(ModuleProducter moduleProducter) {
		this.moduleProducter = moduleProducter;
	}

	public void setMessageRedisDao(MessageRedisDao messageRedisDao) {
		this.messageRedisDao = messageRedisDao;
	}

	@Override
	public void doPushMsg2(String channelId, MCMMessageInfo mcmMessageInfo,
			int protoClientNo, int errorCode) throws CCPServiceException {
		try {
			doPush2(mcmMessageInfo, null, protoClientNo, errorCode, channelId);
		} catch (Exception e) {
			logger.error("doPushMsg2 error:", e);
		}
	}
	
	private void doPush2(final MCMMessageInfo messageInfo, final String sessionId, int protoClientNo, 
			int errorCode, String channelId) throws IOException {
		byte[] encodeByte = null;

		MCMDataInner.Builder builder=MCMDataInner.newBuilder();
		
		if (messageInfo.getVersion() != -1) {
			builder.setVersion(messageInfo.getVersion());
			//builder.setChanType(""+messageInfo.getChanType());
		}
		
		if(messageInfo.getMCMEvent() > 0){
			builder.setMCMEvent(messageInfo.getMCMEvent());
		}
		
		if (messageInfo.getMsgDateCreated() != null && messageInfo.getMsgDateCreated().length() != 0) {
			builder.setMsgDateCreated(messageInfo.getMsgDateCreated());
		}
		
		if (messageInfo.getUserAccount() != null && messageInfo.getUserAccount().length() != 0) {
			builder.setUserAccount(messageInfo.getUserAccount());
		}
		
		if (messageInfo.getAgentAccount() != null && messageInfo.getAgentAccount().length() != 0) {
			builder.setUserAccount(messageInfo.getAgentAccount());
		}
		
		if (messageInfo.getOsUnityAccount() != null && messageInfo.getOsUnityAccount().length() != 0) {
			builder.setOsUnityAccount(messageInfo.getOsUnityAccount());
		}
		
		if(messageInfo.getAppId()!=null && messageInfo.getAppId().length()>0){
			builder.setAppId(messageInfo.getAppId());
		}
		
		//genesys
		if(messageInfo.getNickName()!=null && messageInfo.getNickName().length() != 0){
			builder.setAppId(messageInfo.getNickName());
		}
		
		if(messageInfo.getMsgJsonData()!=null && messageInfo.getMsgJsonData().length() != 0){
			builder.setMsgJsonData(messageInfo.getMsgJsonData());
		}
		
		if(messageInfo.getCCSType() > 0){
			builder.setCCSType(messageInfo.getCCSType());
		}
		
		if(messageInfo.getCcpCustomData()!=null && messageInfo.getCcpCustomData().length() != 0){
			builder.setCcpCustomData(messageInfo.getCcpCustomData());
		}
		
		if(messageInfo.getAgentId()!=null && messageInfo.getAgentId().length() != 0){
			builder.setAgentId(messageInfo.getAgentId());
		}
		
		if(messageInfo.getOptRetDes() != null && messageInfo.getOptRetDes().length() != 0){
			AgentStateOptInner.Builder aso = AgentStateOptInner.newBuilder();
			aso.setOptResult(messageInfo.getOptResult());
			aso.setOptRetDes(messageInfo.getOptRetDes());	
			builder.setAgentStateOpt(aso);
		}
		
		builder.setChanType(String.valueOf(messageInfo.getChanType()));
		builder.setIdlecount(messageInfo.getIdlecount());
		
		if(messageInfo.getMsgType() != 0){
			MSGDataInner.Builder msagDataBuilder=MSGDataInner.newBuilder();
			
			msagDataBuilder.setMsgType(messageInfo.getMsgType());
			
			if(messageInfo.getMsgType()==MCMTypeInner.MCMType_txt_VALUE){
				if(messageInfo.getMsgContent() !=null && messageInfo.getMsgContent().length()>0){
					msagDataBuilder.setMsgContent(messageInfo.getMsgContent());
				}
			}
			
			if (messageInfo.getMsgType() != 0 && messageInfo.getMsgType() != MCMTypeInner.MCMType_txt_VALUE) {
				if (messageInfo.getMsgFileName() != null && messageInfo.getMsgFileName().length() != 0) {
					msagDataBuilder.setMsgFileName(messageInfo.getMsgFileName());
				}
				if(messageInfo.getMsgFileUrl()!=null && messageInfo.getMsgFileUrl().length()>0){
					msagDataBuilder.setMsgFileUrl(messageInfo.getMsgFileUrl());
				}
			}
			
			builder.addMSGData(msagDataBuilder.build());
		}
		
		encodeByte = ProtobufCodecManager.encoder(builder.build());
			
		int protoType = Constants.PUSH_MCM_QUEUE;

		//消息加密 Encode
		byte[] qesEncodeByte = Cryptos.toQESEncode(cryptosKey, encodeByte);
		
		// PUSH到客户端
		MsgLiteInner pushResp = MsgLiteFactory.getMsgLite(protoType, qesEncodeByte, protoClientNo, 
				sessionId, null, errorCode, encodeByte.length, null);
		moduleProducter.replyBytesMessage(channelId, pushResp);
	}
}
