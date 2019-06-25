package com.yuntongxun.mcm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;
import org.ming.sample.util.ProtocolUtil;
import org.springframework.http.HttpMethod;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMChannelTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMDataInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMEventDefInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MCMTypeInner;
import com.yuntongxun.mcm.core.protobuf.MCMEventData.MSGDataInner;
import com.yuntongxun.mcm.dao.VersionDao;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.http.HttpClient;
import com.yuntongxun.mcm.mcm.dao.MCMDao;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.form.McmInfoForm;
import com.yuntongxun.mcm.mcm.model.ASDataInfo;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.SeqInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.mcm.service.IMUserService;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.model.Connector;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.service.MailGWService;
import com.yuntongxun.mcm.service.PushService;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.JSONBodyBuilder;
import com.yuntongxun.mcm.util.JSONBodyParser;
import com.yuntongxun.mcm.util.ScriptManager;
import com.yuntongxun.mcm.util.StringUtil;
import com.yuntongxun.mcm.util.XMLBodyBuilder;
import com.yuntongxun.mcm.util.XMLBodyParser;



public class AsServiceImpl implements AsService{
	
  private static final Logger logger = LogManager.getLogger(AsServiceImpl.class);
  
  private HttpClient dispatcher;
  private PushService pushService;
  private VersionDao versionDao;
  private BaseRedisDao baseRedisDao;
  private MCMDao mcmDao;
  
  private String moduleCode;
  
  private int resendTimeNum;
  private int processMessageThreadNumber;
  private ExecutorService executorService;
  private String asPushMailFormat;
  private String asPushWeiXinFormat;
  
  private MailGWService mailGWService;
  private WeiXinGWService weiXinGWService;
  private IMUserService imUserService;
  private UserAgentDialogRedisDao userAgentDialogRedisDao;
  private RMServerRequestService rmServerRequestService;

	public void init() {
		executorService = Executors.newFixedThreadPool(processMessageThreadNumber);
	}
	
	/***
	 * 1:将消息保存到数据库中
	 * 2:推送消息
	 * ****/
	@Override
	public void handleRevicedMessage(McmInfoForm mcm) throws CCPServiceException {
			//若渠道来源为空，默认为IM消息
			if(StringUtils.isEmpty(mcm.getChannelType())){
				mcm.setChannelType(String.valueOf(MCMChannelTypeInner.MCType_im_VALUE));
			}
			
			//针对不同的渠道来源，做不同的逻辑处理
			if(String.valueOf(MCMChannelTypeInner.MCType_im_VALUE).equals(mcm.getChannelType())){
				/** 渠道来源为IM时，将消息推送至相应的MQ*/
				String appId = mcm.getAppId();
				String userAcc = StringUtil.getUserAcc(appId, mcm.getUserAccount());
				String osUnityAccount = mcm.getOsUnityAccount();
				if(mcm.getContent().length()>0){
					//生成消息版本号
					Long version=versionDao.getMessageVersion(userAcc);
					
					List<MessageInfo> messageInfoList=new ArrayList<MessageInfo>();
					int i=0;
					MessageInfo messageInfo=new MessageInfo();
					MCMMessageInfo mcmessageInfo = new MCMMessageInfo();
					long current=System.currentTimeMillis();
					messageInfo.setDateCreated(""+current);
					messageInfo.setMcmEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE); //多渠道消息事件
					messageInfoList.add(messageInfo);
					mcmessageInfo.setVersion(version);
					mcmessageInfo.setMCMEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE);
					mcmessageInfo.setMsgId(messageInfo.getMsgId()); 
					mcmessageInfo.setChanType(Integer.parseInt(mcm.getChannelType()));
					mcmessageInfo.setAppId(appId);
					mcmessageInfo.setMsgDateCreated("" +current );
					mcmessageInfo.setMsgContent(mcm.getContent());
					mcmessageInfo.setMsgType(mcm.getMsgType());
					mcmessageInfo.setUserAccount(mcm.getUserAccount());
					mcmessageInfo.setOsUnityAccount(osUnityAccount);
					//消息推送
					try {
						pushService.doPushMsg(userAcc, mcmessageInfo);
						
					} catch (Exception e) {
						logger.info("pushMsg im exception , the receiver is :"+userAcc +" push messageInfo is "+JSONUtil.objToMap(mcmessageInfo));
					}
				}
			}else if(String.valueOf(MCMChannelTypeInner.MCType_mail_VALUE).equals(mcm.getChannelType())){
				/** 渠道来源为邮件时，将消息发送至邮件网关*/
				McmMailMsgInfo mailData = new McmMailMsgInfo();
				mailData.setAction(Constants.MAILGW_CONFIG_ACTION_NAME_SEND);
				mailData.setActionId(StringUtil.getUUID());
				mailData.setMailId(mcm.getOsUnityAccount());
				mailData.setTo(mcm.getUserAccount());
				mailData.setContent(mcm.getContent());
				mailGWService.sendMail(mailData);
			}else if(String.valueOf(MCMChannelTypeInner.MCType_wx_VALUE).equals(mcm.getChannelType())){
				/** 渠道来源为微信时，将消息发送至微信网关*/
				McmWeiXinMsgInfo weixinData = new McmWeiXinMsgInfo();
				weixinData.setOpenID(mcm.getOsUnityAccount());
				weixinData.setUserID(mcm.getUserAccount());
				weixinData.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
				weixinData.setContent(mcm.getContent());
				weiXinGWService.sendWeiXinMsg(weixinData);
			}
	}

	
	/*****
	 * 请求AsHttpClient  开始事件    
	 * *****/
	/*@Override
	public void doAskHttpClient(MCMDataInner sendMsg,Connector connector,AppAttrs appAttrs, int protoClientNo) {
		String msgId = StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId());
		//向AS发送开始咨询通知
		doHttpClient(sendMsg,praseToASMessageBody(sendMsg,connector,appAttrs,msgId),appAttrs.getMcm_notify_url(),connector,Constants.MESSAGE_START_ASK_TYPE,msgId);
	}*/

	//used
	/**
	 * MCM向AS发送 开始咨询、结束咨询 http请求
	 * @param sendMsg
	 * @param messageBody
	 * @param startAskUrl
	 * @param connector	
	 */
	/*private void doHttpClient(final MCMDataInner sendMsg,final String messageBody, String startAskUrl,final Connector connector,int msgType,String msgId) {
			HttpMethod httpMethod=HttpMethod.POST;
			HashMap<String, String> header=new  HashMap<String, String>();
			header.put("Content-Type", "application/xml");
			try {
				logger.info("ready to send http request,event:"+sendMsg.getMCMEvent()+",url:"+startAskUrl+",requestBody"+messageBody);
				Map<String,String> httpResult=dispatcher.sendPacket(startAskUrl, httpMethod, header,messageBody);
				String statusCode = httpResult.get("statusCode");
				String content = httpResult.get("content");
				String dataFormat = httpResult.get("dataFormat");
//				content = "{\"msgSid\": \"msgSid\",\"appId\": \"20150314000000110000000000000010\",\"userAccount\": \"userAccount\",\"osUnityAccount\": \"KF4008818600668603\",\"msgType\": \"1\",\"content\": \"欢迎您\",\"createTime\": \"14324564578\",\"fileName\": \"fileName\",\"fileDownUrl\": \"fileDownUrl\"}                               ";
//				content = "<OSSendMessage><msgSid>070706524191111111155004</msgSid><appId>20150314000000110000000000000010</appId><userAccount>18511250371</userAccount><osUnityAccount>KF4008818600668603</osUnityAccount><msgType>1</msgType><content>汉字OK？</content><createTime>14324564231</createTime><fileName>test.png</fileName><fileDownUrl>http://xxx.xxx:8080/xxxx</fileDownUrl></OSSendMessage>";
//				dataFormat = "xml";
				if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
					for (int i = 1; i <=resendTimeNum; i++) {
						httpResult=dispatcher.sendPacket(url, httpMethod, header,messageBody);
						statusCode = httpResult.get("statusCode");
						if(statusCode!=null&&Integer.parseInt(statusCode)<500){
							content = httpResult.get("content");
							dataFormat = httpResult.get("dataFormat");
							break;
						}
					}
				}
				
				if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
					logger.info("start or end ask to AS error,statusCode:"+statusCode);
				}else{
					logger.info("start or end ask to AS success");
					if(StringUtils.isNotEmpty(content)){
						MCMMessageInfo mcmMessageInfo = null;
						if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
							mcmMessageInfo = JSONBodyParser.parseMCMMessageInfoBody(content);
						}else if(Constants.DATA_FORMAT_XML.equals(dataFormat)){
							mcmMessageInfo = XMLBodyParser.parseMCMMessageInfoBody(content);
						}
						//返回包体有消息内容时，封装相应结果 插入 库 并推送到MQ
						if(mcmMessageInfo!=null&&StringUtils.isNotEmpty(mcmMessageInfo.getMsgContent())){
							logger.info("response data have an " + dataFormat + " message body,ready to push msg to MQ,event:"+sendMsg.getMCMEvent()+",content:"+mcmMessageInfo.getMsgContent()+",connectorId:"+connector.getConnectorId());
							doAsyInsertAndPush(mcmMessageInfo,connector);
						}
					}
				}
			}catch (CCPServiceException e) {
				logger.error("mcm send start or end ask to AS agent error:"+e.getMessage());
			}
	}*/
	
	/**
	 * 根据响应包体 向 MQ 推送消息，并入库
	 * @param responseData.getMcmMessageInfo().getMsgContent()
	 * @param connector
	 * @param sendMsg
	 * @param mcm
	 */
	private void doAsyInsertAndPush(final MCMMessageInfo mCMMessageInfo,final Connector connector){
				
		// 开始咨询或者结束咨询推送手机端消息

		String userAcc = StringUtil.getUserAcc(connector.getAppId(),connector.getUserName());

		Long current = System.currentTimeMillis();
		long version = (Long) versionDao.getMessageVersion(userAcc);
		String msgId = StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId());
//			String msgId = "" + generateMessageMsgId(current);

//			MessageInfo messageInfo = new MessageInfo();
//			messageInfo.setVersion(version);
//			messageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
//			messageInfo.setMsgId(msgId); // messageId出的是版本号
//
//			messageInfo.setMsgSender(sendMsg.getOsUnityAccount());
//			messageInfo.setMsgReceiver(StringUtil.splitUserAcc(userAcc).get(
//					"userAccount"));
//
//			String msgContent = "";
//			try {
//				msgContent = Base64.encode(response.getBytes("UTF-8"));
//			} catch (UnsupportedEncodingException e2) {
//				logger.info("return welcome word to user error:"+e2.getMessage());
//			}
//			messageInfo.setMsgContent(msgContent);
//
//			messageInfo.setDateCreated("" + current);
//			messageInfo.setMcmEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE); // 多渠道消息事件

		mCMMessageInfo.setVersion(version);
		mCMMessageInfo.setMsgId(msgId);
		mCMMessageInfo.setAppId(connector.getAppId());
		mCMMessageInfo.setMCMEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE);
//			mCMMessageInfo.setMsgType(MCMTypeInner.MCMType_txt_VALUE);
//			mCMMessageInfo.setOsUnityAccount(sendMsg.getOsUnityAccount());
//			mCMMessageInfo.setUserAccount(connector.getUserName());
		mCMMessageInfo.setMsgDateCreated("" + System.currentTimeMillis());
		try {
			// push
			pushService.doPushMsg(userAcc,mCMMessageInfo);
		} catch (Exception e) {
			logger.info("return welcome word,push to user mq error:"+e.getMessage());
		} 
		/*catch (IOException e) {
			logger.info("return welcome word,push to user mq error:"+e.getMessage());
		} catch (CCPCassandraDaoException e) {
			logger.info("return welcome word,push to user mq error:"+e.getMessage());
		}*/
		/**weily-update*/
//			try {
//				mcmDao.saveInstantMessage(userAcc, messageInfo);
//			} catch (CCPCassandraDaoException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			
	}

	/**
	 * MCM网关向AS发送开始咨询和结束咨询消息时，需要将数据转化成xml格式
	 * @param sendMsg
	 * @param connector
	 * @param appAttrs
	 * @return
	 */
	public String praseToASMessageBody(MCMDataInner sendMsg,Connector connector,AppAttrs appAttrs,String msgId){
		Long current=System.currentTimeMillis();
		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+connector.getUserName()+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+sendMsg.getOsUnityAccount()+"]]></osUnityAccount>");
		builder.append("<appId>"+connector.getAppId()+"</appId>");
		builder.append("<customAppId><![CDATA["+(appAttrs.getCustomer_appid()==null?"":appAttrs.getCustomer_appid())+"]]></customAppId>");
		builder.append("<msgSid>"+msgId+"</msgSid>");
		builder.append("<createTime>"+current+"</createTime>");
		//开始咨询  结束咨询
		if(sendMsg.getMCMEvent()== MCMEventDefInner.UserEvt_StartAsk_VALUE ){
//			builder.append("<msgType>"+Constants.MESSAGE_START_ASK_TYPE+"</msgType>");
			builder.append("<action>startmessage</action>");
		}
		else if(sendMsg.getMCMEvent()== MCMEventDefInner.UserEvt_EndAsk_VALUE){
//			builder.append("<msgType>"+Constants.MESSAGE_END_ASK_TYPE+"</msgType>");
			builder.append("<action>stotmessage</action>");
		}
		else{
			
		}
		builder.append("</MCM>");
		return builder.toString();
		
	}
	
	/**
	 * 
	 * 生成msssageId
	 * //13位日期时间串+3位随机数+8位模块编号+8位随机sessionId（已过时）
	 * 10位时间串+4位循环递增数+8位模块编号+8位conntorId
	 * *****/
	private  String generateMessageMsgId(Long current) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		//13位时间串
		sb.append(current);
		//3位随机数
		sb.append(StringUtil.generateString(Constants.MESSAGE_ID_RANDOM_LENGTH));
		//8位模块编号
		sb.append(moduleCode);
		//8位随机sessionID
		sb.append(StringUtil.generateRandomNum(Constants.SESSION_ID_LENGTH));
		return sb.toString();
	}
	
	/**
	 * 路由关系
	 * key :length位sessionId
	 * value:发送者AppId+接受者账号+customApp_id
	 * 
	 * 等接受到AS相应以后把他从缓存里面给删除了
	 * **/
//	private String initSoutingRelationship(String  appId,String receiver,String customAppId) {
////		private String initSoutingRelationship(String  appId,MCMDataInner mcmDataInner) {
//		
////		logger.info("++++++++++ initSoutingRelationship begin "+System.currentTimeMillis());
//		String ehcache_key=StringUtil.generateRandomNum(Constants.SESSION_ID_LENGTH);
//		String ehcache_value=mcmCache.getData(ehcache_key);
//		if(ehcache_value == null) 
//		{
//			ehcache_value=""+appId+"@"+receiver+"@"+customAppId;
//		}
//		mcmCache.addData(ehcache_key, ehcache_value);
////		logger.info("++++++++++ initSoutingRelationship end "+System.currentTimeMillis());
//		return ehcache_key;
//	}


	public String getModuleCode() {
		return moduleCode;
	}


	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	/**
	 * MCM向AS发送结束咨询
	 * @param sendMsg
	 * @param connector
	 * @param appAttrs
	 * @throws CCPCassandraDaoException
	 */
	/*public void doEndHttpClient(MCMDataInner sendMsg, Connector connector,AppAttrs appAttrs) {
		url = appAttrs.getMcm_notify_url();
		String msgId = StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId());;
		//向AS发送结束咨询通知
		doHttpClient(sendMsg,praseToASMessageBody(sendMsg,connector,appAttrs,msgId),appAttrs.getMcm_notify_url(),connector,Constants.MESSAGE_END_ASK_TYPE,msgId);
	}*/

	
	/**
	 * MCM向AS发送消息
	 * @param receiver
	 * @param sendMsg
	 * @param connector
	 * @param appAttrs 
	 * @throws CCPCassandraDaoException
	 */
	/*public void generateMessageVersionAndDoHttpClient(final String receiver, final MCMDataInner sendMsg,final Connector connector, AppAttrs appAttrs)
	throws CCPCassandraDaoException {
			String appId="";
			String userAcc="";
			String sender="";
			//connector 为空 appId 、sender 需要从sendMsg中获取 
			// 不为空 appId 、sender 从 connector 中获取 
			if(connector == null){
				appId=connector.getAppId();
				userAcc=StringUtil.getUserAcc(appId,receiver);
				sender=connector.getUserName();
			}else{
				appId=connector.getAppId();
				userAcc=StringUtil.getUserAcc(connector.getAppId(), receiver);
				sender=connector.getUserName();
			}
			long t1 = System.currentTimeMillis();
			String customAppId= baseRedisDao.getRedisValue(appId);
			if(StringUtils.isEmpty(customAppId)){
				customAppId = appAttrs.getCustomer_appid();
			}
			long t2 = System.currentTimeMillis();
		    logger.info("++++weily++++redis-根据appId获取customAppId-cost time:"+(t2-t1)+" ms");
			List<MSGDataInner> msgDatalist=sendMsg.getMSGDataList(); 
			if(msgDatalist.size()>0){
				List<MessageInfo> mesageInfoList=new ArrayList<MessageInfo>();
				List<Response<Long>> versionList=versionDao.getBatchMessageVersionByOneUserAcc(msgDatalist.size(),userAcc);
				int i=0;
				String channelType = sendMsg.getChanType();
				if(StringUtils.isEmpty(channelType)){
					channelType = String.valueOf(ChannelTypeEnum.IM.getValue());
				}
				if(ChannelTypeEnum.IM.getValue() == Integer.parseInt(channelType)){
					//此处包括手机端发送的文本消息和文件消息
					for (MSGDataInner object : msgDatalist) {
						int mCMEvent=sendMsg.getMCMEvent();
						Long current=System.currentTimeMillis();
						MessageInfo messageInfo=new MessageInfo();
						messageInfo.setVersion((Long)versionList.get(i++).get());
						
						messageInfo.setMsgType(object.getMsgType());
						messageInfo.setMsgId(StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId())); //messageId出的是版本号
						messageInfo.setMsgSender(sender);
						messageInfo.setMsgReceiver(receiver);
						if(MCMTypeInner.MCMType_audio_VALUE == object.getMsgType() 
								|| MCMTypeInner.MCMType_video_VALUE==object.getMsgType() 
								|| MCMTypeInner.MCMType_emotion_VALUE==object.getMsgType()
								|| MCMTypeInner.MCMType_file_VALUE== object.getMsgType()){
							messageInfo.setMsgFileName(object.getMsgFileName());
							messageInfo.setFileUrl(object.getMsgFileUrl());
						}else{
							messageInfo.setMsgContent(object.getMsgContent());
						}
						messageInfo.setDateCreated(""+current);
						messageInfo.setMcmEvent(mCMEvent); //多渠道消息事件
						mesageInfoList.add(messageInfo);
						//入库
						//用户向As发送消息,调用httpClient
						doSendToASMessageHttpClient(userAcc, connector,praseSendToASMsgBody(sender,receiver,mCMEvent,messageInfo,connector,customAppId,appId,sendMsg.getChanType()));
					}
				}else if(ChannelTypeEnum.MAIL.getValue() == Integer.parseInt(channelType)){
					for (MSGDataInner object : msgDatalist) {
						int mCMEvent=sendMsg.getMCMEvent();
						Long current=System.currentTimeMillis();
						MessageInfo messageInfo=new MessageInfo();
						
						messageInfo.setVersion((Long)versionList.get(i++).get());
						
						messageInfo.setMsgType(object.getMsgType());
						messageInfo.setMsgId(StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId())); //messageId出的是版本号
						messageInfo.setMsgSender(sender);
						messageInfo.setMsgReceiver(receiver);
						if(MCMTypeInner.MCMType_audio_VALUE == object.getMsgType() 
								|| MCMTypeInner.MCMType_video_VALUE==object.getMsgType() 
								|| MCMTypeInner.MCMType_emotion_VALUE==object.getMsgType()
								|| MCMTypeInner.MCMType_file_VALUE== object.getMsgType()){
							messageInfo.setMsgFileName(object.getMsgFileName());
							messageInfo.setFileUrl(object.getMsgFileUrl());
						}else{
							messageInfo.setMsgContent(object.getMsgContent());
						}
						messageInfo.setDateCreated(""+current);
						messageInfo.setMcmEvent(mCMEvent); //多渠道消息事件
						mesageInfoList.add(messageInfo);
					}
						//入库
						//用户向As发送消息,调用httpClient
						String messageBody = praseSendToASMailMsgBody(sender,receiver,sendMsg.getMCMEvent(),null,connector,customAppId,appId,msgDatalist,sendMsg.getChanType());
						doSendToASMessageHttpClient(userAcc, null,messageBody);
				}
				
				//消息批量入库
//				try {
//					mcmDao.saveBatchInstantMessage(userAcc, mesageInfoList);
//				} catch (CCPCassandraDaoException e) {
//					logger.info("batch save message to cassandra exception:"+e.getMessage());
//				}
			}
			
		
	}*/

	/**
	 * MCM网关向AS发送消息时，需要将数据转化为XML格式
	 * @param userAccount
	 * @param receiver
	 * @param mCMEvent
	 * @param messageInfo
	 * @param connector
	 * @param customAppId
	 * @param appId
	 * @return
	 */
	private String praseSendToASMsgBody(String userAccount,String receiver,int mCMEvent,MessageInfo messageInfo,Connector connector,String customAppId ,String appId,String channelType) {
		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+userAccount+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+receiver+"]]></osUnityAccount>");
		builder.append("<appId>"+appId+"</appId>");
		builder.append("<customAppId><![CDATA["+customAppId+"]]></customAppId>");
		builder.append("<msgSid>"+messageInfo.getMsgId()+"</msgSid>");
		builder.append("<createTime>"+messageInfo.getDateCreated()+"</createTime>");
		builder.append("<channelType>"+channelType+"</channelType>");
		if(MCMEventDefInner.UserEvt_SendMSG_VALUE==mCMEvent){
			builder.append("<msgType>"+messageInfo.getMsgType()+"</msgType>");
			builder.append("<content><![CDATA["+messageInfo.getMsgContent()+"]]></content>");
			//多媒体消息   2：语音消息 3：视频消息  4：图片  6：文件
			if(messageInfo.getMsgType()==MCMTypeInner.MCMType_audio_VALUE 
					|| messageInfo.getMsgType()==MCMTypeInner.MCMType_video_VALUE
					|| messageInfo.getMsgType()==MCMTypeInner.MCMType_emotion_VALUE 
					|| messageInfo.getMsgType()==MCMTypeInner.MCMType_file_VALUE){
				builder.append("<fileDownUrl><![CDATA["+messageInfo.getFileUrl()+"]]></fileDownUrl>");
				builder.append("<fileName><![CDATA["+messageInfo.getMsgFileName()+"]]></fileName>");
			}
		}
		else{
			
		}
		builder.append("</MCM>");
		return builder.toString();
	}
	
	/**
	 * MCM网关向AS发送消息时，需要将数据转化为XML格式
	 * @param userAccount
	 * @param receiver
	 * @param mCMEvent
	 * @param messageInfo
	 * @param connector
	 * @param customAppId
	 * @param appId
	 * @param channelType 
	 * @return
	 */
	private String praseSendToASMailMsgBody(String userAccount,String receiver,int mCMEvent,MessageInfo messageInfo,Connector connector,String customAppId ,String appId,List<MSGDataInner> msgDatalist, String channelType) {
		StringBuilder builder = new StringBuilder();
		builder.append("<MCM>");
		builder.append("<userAccount><![CDATA["+userAccount+"]]></userAccount>");
		builder.append("<osUnityAccount><![CDATA["+receiver+"]]></osUnityAccount>");
		builder.append("<appId>"+appId+"</appId>");
		builder.append("<customAppId><![CDATA["+customAppId+"]]></customAppId>");
		builder.append("<msgSid>"+StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId())+"</msgSid>");
		builder.append("<createTime>"+System.currentTimeMillis()+"</createTime>");
		builder.append("<channelType>"+channelType+"</channelType>");
		if(MCMEventDefInner.UserEvt_SendMSG_VALUE==mCMEvent){
			builder.append("<mutiMsg>");
			for(MSGDataInner msgDataInner:msgDatalist){
				builder.append("<msg>");
				builder.append("<msgType>"+msgDataInner.getMsgType()+"</msgType>");
				builder.append("<content><![CDATA["+msgDataInner.getMsgContent()+"]]></content>");
				//多媒体消息   2：语音消息 3：视频消息  4：图片  6：文件
				if(msgDataInner.getMsgType()==MCMTypeInner.MCMType_audio_VALUE 
						|| msgDataInner.getMsgType()==MCMTypeInner.MCMType_video_VALUE
						|| msgDataInner.getMsgType()==MCMTypeInner.MCMType_emotion_VALUE 
						|| msgDataInner.getMsgType()==MCMTypeInner.MCMType_file_VALUE){
					builder.append("<fileDownUrl><![CDATA["+msgDataInner.getMsgFileUrl()+"]]></fileDownUrl>");
					builder.append("<fileName><![CDATA["+msgDataInner.getMsgFileName()+"]]></fileName>");
				}
				builder.append("</msg>");
			}
			builder.append("</mutiMsg>");
		}
		else{
			
		}
		builder.append("</MCM>");
		return builder.toString();
	}

	/**
	 * 向AS发送消息内容，http post请求
	 * @param userAcc
	 * @param connector
	 * @param messageBody
	 * @param url2
	 */
	/*private void doSendToASMessageHttpClient(final String userAcc,final Connector connector,final String messageBody) {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				HttpMethod httpMethod=HttpMethod.POST;
				HashMap<String, String> header=new  HashMap<String, String>();
				header.put("Content-Type", "application/xml");
				try {
					ThreadContext.push(sessionId);
					logger.info("-------------------------------[doSendToASMessageHttpClient start]-------------------------------");
					
					logger.info("ready to send http request,send msg to AS agent,url:"+url+",requestBody"+messageBody);
					Map<String,String> httpResult=dispatcher.sendPacket(url, httpMethod, header,messageBody);
					String statusCode = httpResult.get("statusCode");
					String content = httpResult.get("content");
					String dataFormat = httpResult.get("dataFormat");
//					content = "{\"msgSid\": \"msgSid\",\"appId\": \"20150314000000110000000000000010\",\"userAccount\": \"userAccount\",\"osUnityAccount\": \"KF4008818600668603\",\"msgType\": \"1\",\"content\": \"欢迎您\",\"createTime\": \"14324564578\",\"fileName\": \"fileName\",\"fileDownUrl\": \"fileDownUrl\"}                               ";
//					content = "<OSSendMessage><msgSid>070706524191111111155004</msgSid><appId>20150314000000110000000000000010</appId><userAccount>18511250371</userAccount><osUnityAccount>KF4008818600668603</osUnityAccount><msgType>1</msgType><content>汉字OK？</content><createTime>14324564231</createTime><fileName>test.png</fileName><fileDownUrl>http://xxx.xxx:8080/xxxx</fileDownUrl></OSSendMessage>";
//					dataFormat = "xml";
					if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
						for (int i = 1; i <=resendTimeNum; i++) {
							httpResult=dispatcher.sendPacket(url, httpMethod, header,messageBody);
							statusCode = httpResult.get("statusCode");
							if(statusCode!=null&&Integer.parseInt(statusCode)<500){
								content = httpResult.get("content");
								dataFormat = httpResult.get("dataFormat");
								break;
							}
						}
					}
					
					if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
						logger.info("send msg to AS error,statusCode:"+statusCode);
					}else{
						logger.info("send msg to AS success");
						if(StringUtils.isNotEmpty(content)){
							MCMMessageInfo mcmMessageInfo = null;
							if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
								mcmMessageInfo = JSONBodyParser.parseMCMMessageInfoBody(content);
							}else if(Constants.DATA_FORMAT_XML.equals(dataFormat)){
								mcmMessageInfo = XMLBodyParser.parseMCMMessageInfoBody(content);
							}
							//返回包体有消息内容时，封装相应结果 插入 库 并推送到MQ
							if(mcmMessageInfo!=null&&StringUtils.isNotEmpty(mcmMessageInfo.getMsgContent())){
								logger.info("response data have an " + dataFormat + " message body,ready to push msg to MQ,event:"+MCMEventDefInner.UserEvt_SendMSG_VALUE+",content:"+mcmMessageInfo.getMsgContent()+",connectorId:"+connector.getConnectorId());
								doAsyInsertAndPush(mcmMessageInfo,connector);
							}
							
					}}
				} catch (CCPServiceException e) {
					logger.error("send msg[http request] to AS error,exception:"+e.getMessage());
				}finally{
					logger.info("-------------------------------[doSendToASMessageHttpClient end]-------------------------------\r\n\r\n");
					ThreadContext.removeStack();
				}
			}
		});
		
		
	}*/
	//used
	/**
	 * 接收as侧发送的回复客户的消息，做如下两部操作：
	 * 1、将消息入库；
	 * 2、推送给客户（放入MQ）
	 * @param receiver
	 * @param sendMsg
	 * @param connector 
	 * @throws CCPCassandraDaoException
	 */
	/*public void generateMessageVersionAndPush(final String receiver, final MCMDataInner sendMsg, Connector connector) throws CCPCassandraDaoException {
				
				String appId=connector.getAppId();
				String userAcc=StringUtil.getUserAcc(appId,receiver);
				String sender=sendMsg.getOsUnityAccount();
				
				List<MSGDataInner> msgDatalist=sendMsg.getMSGDataList(); 
				if(msgDatalist.size()>0){
					List<MessageInfo> messageInfoList=new ArrayList<MessageInfo>();
					List<Response<Long>> versionList=versionDao.getBatchMessageVersionByOneUserAcc(msgDatalist.size(),userAcc);
					int i=0;
					for (MSGDataInner object : msgDatalist) {
						Long current=System.currentTimeMillis();
						MessageInfo messageInfo=new MessageInfo();
						messageInfo.setVersion((Long)versionList.get(i++).get());
						messageInfo.setMsgType(object.getMsgType());
//						messageInfo.setMsgId("" + generateMessageMsgId(current)); //messageId出的是版本号
						messageInfo.setMsgSender(sender);
						messageInfo.setMsgReceiver(receiver);
						
						if(object.getMsgType()==MCMTypeInner.MCMType_audio_VALUE 
								|| object.getMsgType()==MCMTypeInner.MCMType_video_VALUE 
								|| object.getMsgType()==MCMTypeInner.MCMType_emotion_VALUE 
								|| object.getMsgType()==MCMTypeInner.MCMType_file_VALUE	){
							messageInfo.setMsgFileName(object.getMsgFileName());
							messageInfo.setFileUrl(object.getMsgFileUrl());
						}else{
							messageInfo.setMsgContent(object.getMsgContent());
						}
						messageInfo.setDateCreated(""+current);
						messageInfo.setMcmEvent(sendMsg.getMCMEvent()); //多渠道消息事件
						
						
						messageInfoList.add(messageInfo);
						
						//组装推送对象
						MCMMessageInfo mcmessageInfo = new MCMMessageInfo(object);
						
						mcmessageInfo.setVersion(messageInfo.getVersion());
						//AS消息回复事件
						mcmessageInfo.setMCMEvent(MCMEventDefInner.AgentEvt_SendMCM_VALUE);
						mcmessageInfo.setMsgId(messageInfo.getMsgId()); //messageId出的是版本号
//						System.out.println(messageInfo.getMsgId());
						mcmessageInfo.setChanType(channelType);
						mcmessageInfo.setIsMCM(Constants.ISMCM);
						mcmessageInfo.setAppId(appId);
						mcmessageInfo.setUserAccount(receiver);
						mcmessageInfo.setOsUnityAccount(sender);
						mcmessageInfo.setMsgDateCreated("" +current );
						if(object.getMsgContent()!=null && object.getMsgContent().length()>0){
							mcmessageInfo.setMsgContent(object.getMsgContent());
						}
						
						mcmessageInfo.setMsgType(object.getMsgType());
						//文件类型的消息 //语音消息  //视频消息  //文件消息
						if(MCMTypeInner.MCMType_file_VALUE ==object.getMsgType()
							|| MCMTypeInner.MCMType_emotion_VALUE==object.getMsgType()
							|| MCMTypeInner.MCMType_audio_VALUE==object.getMsgType()
							|| MCMTypeInner.MCMType_video_VALUE==object.getMsgType()){
							mcmessageInfo.setMsgFileUrl(object.getMsgFileUrl());
							mcmessageInfo.setMsgFileName(object.getMsgFileName());
						}
						//消息推送
						try {
							pushService.doPushMsg(userAcc, mcmessageInfo);
						} catch (Exception e) {
							logger.info("return welcome word,push to user mq error:"+e.getMessage());
						}  
						catch (IOException e) {
							logger.info("push msg exception, ${ send : "+userAcc+" ,  receiver:"+userAcc+" , appId :"+appId
									+" messageInfo :"+JSONUtil.objToMap(messageInfo) +" }");
						} catch (CCPCassandraDaoException e) {
							logger.info("push msg exception, ${ send : "+userAcc+" ,  receiver:"+userAcc+" , appId :"+appId
									+" messageInfo :"+JSONUtil.objToMap(messageInfo) +" }");
						}
					}
					
					//messageInfoList 批量入库
//					try {
//						mcmDao.saveBatchInstantMessage(userAcc, messageInfoList);
//					} catch (CCPCassandraDaoException e1) {
//						logger.info("batch insert message exception,${ send : "+userAcc+" ,  receiver:"+userAcc+" , appId :"+appId
//								+"messageInfo :"+JSONUtil.objToMap(messageInfoList) +" }");
//					}
				}
				
				
	}*/
	

	

	//used
	/*@Override
	public void doReceiveAsFile(MCMDataInner sendMsg,Connector connector) {
		try {
			generateMessageVersionAndPush(connector.getUserName(),sendMsg,connector);
		} catch (CCPCassandraDaoException e) {
			logger.error(e.getMessage());
		}
		
	}*/
	
	/**
	 * 向第三方推送邮件消息
	 * @param mailMsg
	 * @param appAttrs
	 */
	@Override
	public void pushMailToAs(McmMailMsgInfo mailMsg,final AppAttrs appAttrs) {
		//生成消息id
		String msgId = StringUtil.generateMessageMsgId(moduleCode,Constants.CONNECTOR_ID_CONSTANTS_MAIL);
		String messageBody = null; 
		//构建推送数据对象
		if(Constants.DATA_FORMAT_JSON.equals(asPushMailFormat)){
			messageBody = JSONBodyBuilder.buildMailJSONBody(mailMsg,appAttrs,msgId);
		}else if(Constants.DATA_FORMAT_XML.equals(asPushMailFormat)){
			messageBody = XMLBodyBuilder.buildMailXMLBody(mailMsg,appAttrs,msgId);
		}
		final String finalMessageBody = messageBody;
		//推送到第三方
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("ASServiceThread_pushMailToAs");
				String response="";
				HttpMethod httpMethod=HttpMethod.POST;
				HashMap<String, String> header=new  HashMap<String, String>();
				if(Constants.DATA_FORMAT_JSON.equals(asPushMailFormat)){
					header.put("Content-Type", "application/json");
				}else if(Constants.DATA_FORMAT_XML.equals(asPushMailFormat)){
					header.put("Content-Type", "application/xml");
				}
				long currentTime = System.currentTimeMillis();
				try {
					Map<String,String> httpResult=dispatcher.sendPacket(appAttrs.getMcm_notify_url(), httpMethod, header,finalMessageBody);
					String statusCode = httpResult.get("statusCode");
					String content = httpResult.get("content");
					String dataFormat = httpResult.get("dataFormat");
//					content = "{\"msgSid\": \"msgSid\",\"appId\": \"20150314000000110000000000000010\",\"userAccount\": \"userAccount\",\"osUnityAccount\": \"KF4008818600668603\",\"msgType\": \"1\",\"content\": \"欢迎您\",\"createTime\": \"14324564578\",\"fileName\": \"fileName\",\"fileDownUrl\": \"fileDownUrl\"}                               ";
//					content = "<OSSendMessage><msgSid>070706524191111111155004</msgSid><appId>20150314000000110000000000000010</appId><userAccount>18511250371</userAccount><osUnityAccount>KF4008818600668603</osUnityAccount><msgType>1</msgType><content>汉字OK？</content><createTime>14324564231</createTime><fileName>test.png</fileName><fileDownUrl>http://xxx.xxx:8080/xxxx</fileDownUrl></OSSendMessage>";
//					dataFormat = "xml";
					if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
						for (int i = 1; i <=resendTimeNum; i++) {
							httpResult=dispatcher.sendPacket(appAttrs.getMcm_notify_url(), httpMethod, header,finalMessageBody);
							statusCode = httpResult.get("statusCode");
							if(statusCode!=null&&Integer.parseInt(statusCode)<500){
								content = httpResult.get("content");
								dataFormat = httpResult.get("dataFormat");
								break;
							}
						}
					}
					
					if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
						logger.info("send msg to AS error,statusCode:"+statusCode);
					}else{
						logger.info("send msg to AS success,type:mail");
						if(StringUtils.isNotEmpty(content)){
							MCMMessageInfo mcmMessageInfo = null;
							if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
								mcmMessageInfo = JSONBodyParser.parseMCMMessageInfoBody(content);
							}else if(Constants.DATA_FORMAT_XML.equals(dataFormat)){
								mcmMessageInfo = XMLBodyParser.parseMCMMessageInfoBody(content);
							}
							//返回包体有消息内容时，封装相应结果 插入 库 并推送到MQ
							if(mcmMessageInfo!=null&&StringUtils.isNotEmpty(mcmMessageInfo.getMsgContent())){
								logger.info("push mail to as:response data have an " + dataFormat + " message body,ready to reply mail to user,event:"+MCMEventDefInner.AgentEvt_SendMCM+",content:"+mcmMessageInfo.getMsgContent());
								McmMailMsgInfo mailData = new McmMailMsgInfo();
								mailData.setAction(Constants.MAILGW_CONFIG_ACTION_NAME_SEND);
								mailData.setActionId(StringUtil.getUUID());
								mailData.setMailId(mcmMessageInfo.getOsUnityAccount());
								mailData.setTo(mcmMessageInfo.getUserAccount());
								mailData.setContent(content);
								mailGWService.sendMail(mailData);
							}
							
					}}
				} catch (CCPServiceException e) {
					logger.error(e.getMessage());
				}
			}
		});
		
	}
	
	/**
	 * 推送微信消息到第三方（AS）
	 */
	@Override
	public void pushWeiXinToAs(final McmWeiXinMsgInfo weixinMsg, final AppAttrs appAttrs) {
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog == null){
			logger.error("push weixin to as error,dialog is not exist ,need to start ask.");
			return;
//			McmWeiXinMsgInfo weixinData = new McmWeiXinMsgInfo();
//			weixinData.setOpenID(weixinMsg.getOpenID());
//			weixinData.setUserID(weixinMsg.getUserID());
//			weixinData.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
//			weixinData.setContent("若需要客服服务，请点击在线客服.");
//			weiXinGWService.sendWeiXinMsg(weixinData);
		}
		if(Constants.OSSETSERVICEMODE_AS.equals(userAndAgentDialog.getAsServiceMode())){
			//生成消息id
			String msgId = StringUtil.generateMessageMsgId(moduleCode,Constants.CONNECTOR_ID_CONSTANTS_WEIXIN);
			String messageBody = null; 
			//构建推送数据对象
			if(Constants.DATA_FORMAT_JSON.equals(asPushWeiXinFormat)){
				messageBody = JSONBodyBuilder.buildWeiXinJSONBody(weixinMsg,appAttrs,msgId);
			}else if(Constants.DATA_FORMAT_XML.equals(asPushWeiXinFormat)){
				messageBody = XMLBodyBuilder.buildWeiXinXMLBody(weixinMsg,appAttrs,msgId);
			}
			final String finalMessageBody = messageBody;
			final String sessionId = ThreadContext.peek();
			//推送到第三方
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					//Thread.currentThread().setName("ASServiceThread_pushWeiXinToAs");
					String response="";
					HttpMethod httpMethod=HttpMethod.POST;
					HashMap<String, String> header=new  HashMap<String, String>();
					if(Constants.DATA_FORMAT_JSON.equals(asPushMailFormat)){
						header.put("Content-Type", "application/json");
					}else if(Constants.DATA_FORMAT_XML.equals(asPushMailFormat)){
						header.put("Content-Type", "application/xml");
					}
					long currentTime = System.currentTimeMillis();
					try {
						ThreadContext.push(sessionId);
						
						Map<String,String> httpResult=dispatcher.sendPacket(appAttrs.getMcm_notify_url(), httpMethod, header,finalMessageBody);
						String statusCode = httpResult.get("statusCode");
						String content = httpResult.get("content");
						
						String dataFormat = httpResult.get("dataFormat");
//								content = "{\"msgSid\": \"msgSid\",\"appId\": \"20150314000000110000000000000010\",\"userAccount\": \"userAccount\",\"osUnityAccount\": \"KF4008818600668603\",\"msgType\": \"1\",\"content\": \"欢迎您\",\"createTime\": \"14324564578\",\"fileName\": \"fileName\",\"fileDownUrl\": \"fileDownUrl\"}                               ";
//								content = "<OSSendMessage><msgSid>070706524191111111155004</msgSid><appId>20150314000000110000000000000010</appId><userAccount>18511250371</userAccount><osUnityAccount>KF4008818600668603</osUnityAccount><msgType>1</msgType><content>汉字OK？</content><createTime>14324564231</createTime><fileName>test.png</fileName><fileDownUrl>http://xxx.xxx:8080/xxxx</fileDownUrl></OSSendMessage>";
//								dataFormat = "xml";
						if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
							for (int i = 1; i <=resendTimeNum; i++) {
								httpResult=dispatcher.sendPacket(appAttrs.getMcm_notify_url(), httpMethod, header,finalMessageBody);
								statusCode = httpResult.get("statusCode");
								if(statusCode!=null&&Integer.parseInt(statusCode)<500){
									content = httpResult.get("content");
									dataFormat = httpResult.get("dataFormat");
									break;
								}
							}
						}
						
						if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
							logger.info("send msg to AS error,statusCode:"+statusCode);
						}else{
							logger.info("send msg to AS success,type:weixin");
							if(StringUtils.isNotEmpty(content)){
								MCMMessageInfo mcmMessageInfo = null;
								if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
									mcmMessageInfo = JSONBodyParser.parseMCMMessageInfoBody(content);
								}else if(Constants.DATA_FORMAT_XML.equals(dataFormat)){
									mcmMessageInfo = XMLBodyParser.parseMCMMessageInfoBody(content);
								}
								//返回包体有消息内容时，封装相应结果 插入 库 并推送到MQ
								if(mcmMessageInfo!=null&&StringUtils.isNotEmpty(mcmMessageInfo.getMsgContent())){
									logger.info("push weixin to as:response data have an " + dataFormat + " message body,ready to reply mail to user,event:"+MCMEventDefInner.AgentEvt_SendMCM+",content:"+mcmMessageInfo.getMsgContent());
									McmWeiXinMsgInfo weixinData = new McmWeiXinMsgInfo();
									weixinData.setOpenID(mcmMessageInfo.getUserAccount());
									weixinData.setUserID(mcmMessageInfo.getOsUnityAccount());
									weixinData.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
									weixinData.setContent(mcmMessageInfo.getMsgContent());
									weiXinGWService.sendWeiXinMsg(weixinData);
								}
								
						}}
					} catch (CCPServiceException e) {
						logger.error(e.getMessage());
					} finally{
						ThreadContext.removeStack();
					}
				}
			});
		}else if(Constants.OSSETSERVICEMODE_IM.equals(userAndAgentDialog.getAsServiceMode())){
			try{
				imUserService.sendWeixinMSG(weixinMsg, appAttrs);
			}catch(Exception e){
				logger.error("weixin msg send to as error,mode:{},exception:{}",userAndAgentDialog.getAsServiceMode(),e);
			}
		}
	}
	
	public HttpClient getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(HttpClient dispatcher) {
		this.dispatcher = dispatcher;
	}

	public PushService getPushService() {
		return pushService;
	}

	public void setPushService(PushService pushService) {
		this.pushService = pushService;
	}

	public BaseRedisDao getBaseRedisDao() {
		return baseRedisDao;
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	public VersionDao getVersionDao() {
		return versionDao;
	}

	public void setVersionDao(VersionDao versionDao) {
		this.versionDao = versionDao;
	}

	public MCMDao getMcmDao() {
		return mcmDao;
	}

	public void setMcmDao(MCMDao mcmDao) {
		this.mcmDao = mcmDao;
	}

	public int getResendTimeNum() {
		return resendTimeNum;
	}

	public void setResendTimeNum(int resendTimeNum) {
		this.resendTimeNum = resendTimeNum;
	}
	public int getProcessMessageThreadNumber() {
		return processMessageThreadNumber;
	}

	public void setProcessMessageThreadNumber(int processMessageThreadNumber) {
		this.processMessageThreadNumber = processMessageThreadNumber;
	}

	public String getAsPushMailFormat() {
		return asPushMailFormat;
	}

	public void setAsPushMailFormat(String asPushMailFormat) {
		this.asPushMailFormat = asPushMailFormat;
	}

	public String getAsPushWeiXinFormat() {
		return asPushWeiXinFormat;
	}

	public void setAsPushWeiXinFormat(String asPushWeiXinFormat) {
		this.asPushWeiXinFormat = asPushWeiXinFormat;
	}

	public MailGWService getMailGWService() {
		return mailGWService;
	}

	public void setMailGWService(MailGWService mailGWService) {
		this.mailGWService = mailGWService;
	}

	public WeiXinGWService getWeiXinGWService() {
		return weiXinGWService;
	}

	public void setWeiXinGWService(WeiXinGWService weiXinGWService) {
		this.weiXinGWService = weiXinGWService;
	}

	public IMUserService getImUserService() {
		return imUserService;
	}

	public void setImUserService(IMUserService imUserService) {
		this.imUserService = imUserService;
	}
	
	public void setUserAgentDialogRedisDao(UserAgentDialogRedisDao userAgentDialogRedisDao) {
		this.userAgentDialogRedisDao = userAgentDialogRedisDao;
	}
	
	public void setRmServerRequestService(RMServerRequestService rmServerRequestService) {
		this.rmServerRequestService = rmServerRequestService;
	}

	@Override
	public void imStartAsk(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) throws CCPServiceException {
		String msgId = StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId());
		String userAccount = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		
		if(!sendMsg.hasOsUnityAccount()){
			logger.info("osUnityAccount is empty.");
			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
		}

		String osUnityAccount = sendMsg.getOsUnityAccount(); 
		logger.info("@osUnityAccount: {}.", osUnityAccount);
		
		//向用户发送开始咨询响应
//		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
//		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
//		mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
//		mcmMessageInfo.setOptRetDes("start ask success.");
//		pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() > 0){
			logger.info("user already ask, have agentInfo.");
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
			mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
			mcmMessageInfo.setOptRetDes("start ask success.");
			pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
			
			return;
			
		} else if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() == 0){
			logger.info("user already ask, not exist agentInfo.");
			
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
			mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
			mcmMessageInfo.setOptRetDes("start ask success.");
			pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
			
			if(userAndAgentDialog.getQueueCount() > 0){
				logger.info("already queue.");
				
				mcmMessageInfo = new MCMMessageInfo();
				mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
				mcmMessageInfo.setMsgContent("正在分配客服...");
				pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
			}
			return;
			
		}else{
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, connector.getConnectorId());
			
			String chanType = String.valueOf(MCMChannelTypeInner.MCType_im_VALUE);
			
			if(sendMsg.hasChanType()){
				chanType = sendMsg.getChanType();
			}
			
			String msgJsonData = "";
			if(sendMsg.hasMsgJsonData()){
				msgJsonData = sendMsg.getMsgJsonData();
			}
			logger.info("as start ask msgJsonData: {}.", msgJsonData);
			
			String companyId = "";
			try {
				if(StringUtils.isNotBlank(msgJsonData)){
					JSONObject jsonObject = JSONObject.fromObject(msgJsonData);
					
					Object ctObj = jsonObject.get("companyId");
					if(ctObj != null){
						companyId = String.valueOf(ctObj);
					}
				}
			} catch (Exception e) {
				logger.error("parse msgJsonData error: " + msgJsonData);
			}
			logger.info("companyId: {}.", companyId);
			
			//准备向AS侧发送开始咨询对象
			ASDataInfo requestData = new ASDataInfo();
			requestData.setAction(Constants.AS_ACTION_START_MESSAGE);
			requestData.setUserAccount(userAccount);
			requestData.setSid(sid);
			requestData.setOsUnityAccount(osUnityAccount);
			requestData.setAppId(connector.getAppId());
			requestData.setCustomAppID(appInfo.getCustomer_appid());
			requestData.setChanType(chanType);
			requestData.setMsgid(msgId);
			requestData.setCompanyId(companyId);
			requestData.setCreateTime(String.valueOf(System.currentTimeMillis()));
			
			//向AS侧发送 HTTP请求
			ASDataInfo responseData = startAskRequestAS(requestData.toStartAskJson(), appInfo.getMcm_notify_url());
			
			if(responseData==null){
				logger.info("send commond to AS, but response is empty, return.");
				return;
			}else{
				logger.info("send commond to AS, response data: {}", JSONUtil.object2json(responseData));
			}
			
			// 用户开始咨询响应
			//sendNotifyMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, userAcc);
			
			// 申请获取坐席
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setSid(sid); // 用户session ID
			
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
			rmServerTransferData.setUseraccount(userAccount);
			//		if(!sendMsg.hasChanType()){
			//			// 用户来源或关键字类型，0 IM帐号、1 微信帐号、2邮件帐号、3短信发送号、4传真发送号、5自定义内容。
			//			chanType = sendMsg.getChanType();
			//			int ct = 0;
			//			if(StringUtils.isNotBlank(chanType) && StringUtils.isNumeric(chanType)){
			//				ct = Integer.parseInt(chanType);
			//			}
			//		}
			rmServerTransferData.setKeytype(Integer.parseInt(chanType));
			
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setMcmEvent(sendMsg.getMCMEvent());
			seqInfo.setAppId(connector.getAppId());
			seqInfo.setProtoClientNo(protoClientNo);
			seqInfo.setAsFlag(Constants.AS_FLAG_YES);
			seqInfo.setUserAccount(userAccount);
			seqInfo.setMsgJsonData(msgJsonData);
			
			if(StringUtils.isNotEmpty(responseData.getWelcome())){
				seqInfo.setAsWelcome(responseData.getWelcome());
			}
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
			
			// 生成会话记录
			saveDialog(sendMsg.getCCSType(), osUnityAccount, sid, userAccount,responseData,appInfo,Integer.parseInt(chanType),connector.getAppId());
			
			rmServerRequestService.doPushMessage(cmdMessage);
		}
		
		
	}
	
	@Override
	public void weixinStartAsk(McmWeiXinMsgInfo weixinMsg, AppAttrs appAttrs) throws CCPServiceException {
		String msgId = StringUtil.generateMessageMsgId(moduleCode,weixinMsg.getOpenID());
		String userAccount = StringUtil.getUserAcc(weixinMsg.getAppId(), weixinMsg.getUserID());
//		
//		if(StringUtils.is){
//			logger.info("osUnityAccount is empty.");
//			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
//		}
//
//		String osUnityAccount = sendMsg.getOsUnityAccount(); 
//		logger.info("@osUnityAccount: {}.", osUnityAccount);
		
		//向用户发送开始咨询响应
//		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
//		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_StartAskResp_VALUE);
//		mcmMessageInfo.setOptResult(Constants.SDK_RETURN_RESULT_SUCCESS);
//		mcmMessageInfo.setOptRetDes("start ask success.");
//		pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
		
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() > 0){
			//回复微信消息
			McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
			weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
			weiXinMsgInfo.setUserID(weixinMsg.getUserID());
			weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
			weiXinMsgInfo.setContent("会话已建立...");
			weiXinGWService.sendWeiXinMsg(weiXinMsgInfo);
			return;
			
		} else if(userAndAgentDialog != null && userAndAgentDialog.getAgentInfoSet().size() == 0){
			logger.info("user already ask, not exist agentInfo.");
			
			if(userAndAgentDialog.getQueueCount() > 0){
				logger.info("already queue.");
				McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
				weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
				weiXinMsgInfo.setUserID(weixinMsg.getUserID());
				weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
				weiXinMsgInfo.setContent("正在分配客服...");
				weiXinGWService.sendWeiXinMsg(weiXinMsgInfo);
			}
			return;
			
		}else{
			String sid = StringUtil.generateSid(Constants.M3C_SERIAL, weixinMsg.getOpenID());
			
			//准备向AS侧发送开始咨询对象
			ASDataInfo requestData = new ASDataInfo();
			requestData.setAction(Constants.AS_ACTION_START_MESSAGE);
			requestData.setSid(sid);
			requestData.setOsUnityAccount(weixinMsg.getOpenID());
			requestData.setAppId(weixinMsg.getAppId());
			requestData.setCustomAppID(appAttrs.getCustomer_appid());
			requestData.setChanType(String.valueOf(MCMChannelTypeInner.MCType_wx_VALUE));
			requestData.setMsgid(msgId);
			requestData.setCreateTime(String.valueOf(System.currentTimeMillis()));
			requestData.setUserAccount(userAccount);
			
			//向AS侧发送 HTTP请求
			ASDataInfo responseData = startAskRequestAS(requestData.toStartAskJson(), appAttrs.getMcm_notify_url());
			
			if(responseData==null){
				logger.error("weixin startask error,request AS server,return null.");
				//回复微信消息
				McmWeiXinMsgInfo weiXinMsgInfo = new McmWeiXinMsgInfo();
				weiXinMsgInfo.setOpenID(userAndAgentDialog.getOsUnityAccount());
				weiXinMsgInfo.setUserID(weixinMsg.getUserID());
				weiXinMsgInfo.setMsgType(WeiXinMsgTypeEnum.TEXT.getValue());
				weiXinMsgInfo.setContent("座席暂时无法提供服务...");
				weiXinGWService.sendWeiXinMsg(weiXinMsgInfo);
				return;
			}
			
			// 用户开始咨询响应
			//sendNotifyMessage(MCMEventDefInner.NotifyUser_StartAskResp_VALUE, userAcc);
			
			// 申请获取坐席
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_ALLOC_IM_AGENT.getValue());
			rmServerTransferData.setAppid(weixinMsg.getAppId());
			rmServerTransferData.setSid(sid); // 用户session ID
			
			// 强制服务。默认值为false。在座席要求为指定用户服务时，只要座席状态值在[11,16] 范围内，就可以为用户服务，反回成功.
			if(responseData.isForce()){
				rmServerTransferData.setForce(true);
			}
			if(StringUtils.isNotEmpty(responseData.getAgentidfirst())){
				rmServerTransferData.setAgentid(responseData.getAgentidfirst());
			}
			if(StringUtils.isNotEmpty(responseData.getQueueType())){
				rmServerTransferData.setQueuetype(Integer.parseInt(responseData.getQueueType()));
			}else{
				rmServerTransferData.setQueuetype(-1);
			}
			rmServerTransferData.setUseraccount(userAccount);
			rmServerTransferData.setKeytype(MCMChannelTypeInner.MCType_wx_VALUE);
			
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setMcmEvent(weixinMsg.getMCMEvent());
			seqInfo.setAppId(weixinMsg.getAppId());
			seqInfo.setAsFlag(Constants.AS_FLAG_YES);
			seqInfo.setUserAccount(userAccount);
			if(StringUtils.isNotEmpty(responseData.getWelcome())){
				seqInfo.setAsWelcome(responseData.getWelcome());
			}
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdAllocImAgent();
			
			// 生成会话记录
			saveDialog(0, weixinMsg.getOpenID(), sid, userAccount,responseData,appAttrs,MCMChannelTypeInner.MCType_wx_VALUE,weixinMsg.getAppId());
			
			rmServerRequestService.doPushMessage(cmdMessage);
		}
		
	}

	@Override
	public void endAsk(MCMDataInner sendMsg, Connector connector, AppAttrs appInfo, int protoClientNo) throws CCPServiceException {
		String msgId = StringUtil.generateMessageMsgId(moduleCode,connector.getConnectorId());
		String userAccount = StringUtil.getUserAcc(connector.getAppId(), connector.getUserName());
		
//		if(!sendMsg.hasOsUnityAccount()){
//			logger.info("osUnityAccount is empty.");
//			throw new CCPServiceException(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
//		}

//		String osUnityAccount = sendMsg.getOsUnityAccount(); 0
//		logger.info("@osUnityAccount: {}.", osUnityAccount);
		UserAndAgentDialog userAndAgentDialog = userAgentDialogRedisDao.getDialog(userAccount);
		
		// 通知用户会话结束
		MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
		mcmMessageInfo.setMCMEvent(MCMEventDefInner.NotifyUser_EndAskResp_VALUE);
		pushService.doPushMsg(userAccount, mcmMessageInfo, protoClientNo, Constants.SUCC);
					
		if(userAndAgentDialog == null){
			logger.info("endAsk fail, get user and agent dialog is null.");
		}else{
			Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
			if(agentInfoSet.isEmpty()){
				logger.info("agentInfoList is empty, start exit queue.");
				
				// 退出排队
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_EXIT_CCS_QUEUE.getValue());
				rmServerTransferData.setAppid(connector.getAppId());
				rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
				rmServerTransferData.setCallid(userAndAgentDialog.getSid());
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setLogSessionId(ThreadContext.peek());
				rmServerTransferData.setSeq(seqInfo.toString());
				
				String cmdMessage = rmServerTransferData.toJsonForCmdExitCCSQueue();
				rmServerRequestService.doPushMessage(cmdMessage);
				
			} else {
				logger.info("agentInfoList is not empty, start end server.");
				
				// 通知rm server结束服务
				RMServerTransferData rmServerTransferData = new RMServerTransferData();
				rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
				rmServerTransferData.setAppid(connector.getAppId());
				rmServerTransferData.setSid(userAndAgentDialog.getSid());
				
				/*Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
				if(agentInfoSet.isEmpty()){
					userAgentDialogRedisDao.deleteDialog(userAccount);
					userAgentDialogRedisDao.deleteDialogSid(userAndAgentDialog.getSid());
				}*/
				for(AgentInfo agentInfo : agentInfoSet){
					rmServerTransferData.setAgentid(String.valueOf(agentInfo.getAgentId()));
					
					SeqInfo seqInfo = new SeqInfo();
					seqInfo.setMcmEvent(sendMsg.getMCMEvent());
					seqInfo.setUserAccount(userAccount);
					seqInfo.setAgentId(agentInfo.getAgentId());
					seqInfo.setAsFlag(Constants.AS_FLAG_YES);
					rmServerTransferData.setSeq(seqInfo.toString());
					
					rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
				}
			}
		}
	}

	/**
	 * @Description: 生成实时会话记录
	 * @param ccsType
	 * @param osUnityAccount
	 * @param connectorId
	 * @param sid
	 * @param userAcc
	 * @param asDataInfo 
	 * @param appInfo 
	 * @param chanType 
	 */
	private void saveDialog(int ccsType, String osUnityAccount, String sid, 
			String userAcc, ASDataInfo asDataInfo, AppAttrs appInfo, Integer chanType,String appId){
		UserAndAgentDialog userAndAgentDialog = new UserAndAgentDialog();
		userAndAgentDialog.setCCSType(ccsType);
		userAndAgentDialog.setChannel(String.valueOf(Constants.M3C_SERIAL));
		userAndAgentDialog.setOsUnityAccount(osUnityAccount);
		userAndAgentDialog.setSid(sid);
		userAndAgentDialog.setDateCreated(System.currentTimeMillis());
		userAndAgentDialog.setAsServiceMode(asDataInfo.getMode());
		userAndAgentDialog.setAsWelcome(asDataInfo.getWelcome());
		userAndAgentDialog.setOptResultCBUrl(asDataInfo.getOptResultCBUrl());
		userAndAgentDialog.setMcm_notify_url(appInfo.getMcm_notify_url());
		userAndAgentDialog.setCustomAppId(appInfo.getCustomer_appid());
		userAndAgentDialog.setChanType(chanType);
		userAndAgentDialog.setAppId(appId);
		
		userAgentDialogRedisDao.saveDialog(userAcc, userAndAgentDialog, Constants.DIALOG_VALID_TIME);
		userAgentDialogRedisDao.saveDialogSid(sid, userAcc, Constants.DIALOG_VALID_TIME);
	}
	
	/**
	 * MCM向AS发送 开始咨询 http请求(new 20160224 weily)
	 * @param sendMsg
	 * @param messageBody
	 * @param startAskUrl
	 * @param connector	
	 */
	public ASDataInfo startAskRequestAS(final String messageBody, String asUrl) {
		ASDataInfo asDataInfo = null;
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			logger.info("ready to send http request,url:"+asUrl+",requestBody"+messageBody);
			Map<String,String> httpResult=dispatcher.sendPacket(asUrl, httpMethod, header,messageBody);
			String statusCode = httpResult.get("statusCode");
			String content = httpResult.get("content");
			String dataFormat = httpResult.get("dataFormat");
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				for (int i = 1; i <=resendTimeNum; i++) {
					httpResult=dispatcher.sendPacket(asUrl, httpMethod, header,messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
						content = httpResult.get("content");
						dataFormat = httpResult.get("dataFormat");
						break;
					}
				}
			}
			
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				logger.info("start ask to AS error,statusCode:"+statusCode);
			}else{
				logger.info("start ask to AS success");
				if(StringUtils.isNotEmpty(content)){
//					if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
						asDataInfo = new ASDataInfo();
						asDataInfo = (ASDataInfo)JSONUtil.jsonToObj(content, ASDataInfo.class);
//					}
					if(asDataInfo!=null){
						//AS响应的命令是“设置客服消息模式”
						if(Constants.AS_ACTION_OSSETSERVICEMODE.equals(asDataInfo.getAction())){
							//如果回调url地址不为空，向AS发送请求
							if(StringUtils.isNotEmpty(asDataInfo.getOptResultCBUrl())){
								
							}
							//如果欢迎语不为空，向用户发送欢迎语
							if(StringUtils.isNotEmpty(asDataInfo.getWelcome())){
								
							}
						}
					}
				}
			}
		}catch (Exception e) {
			logger.error("startAskRequestAS#error()", e);
		}
		return asDataInfo;
	}
	
	/**
	 * MCM向AS发送 开始咨询 http请求(new 20160224 weily)
	 * @param sendMsg
	 * @param messageBody
	 * @param startAskUrl
	 * @param connector	
	 */
	@Override
	public ASDataInfo optCallbackRequestAS(UserAndAgentDialog userAndAgentDialog, RMServerTransferData rmServerTransferData) {
		ASDataInfo asDataInfo = new ASDataInfo();
		asDataInfo.setAction(Constants.AS_ACTION_CBNOTIFY_SETMODE);
		asDataInfo.setSid(userAndAgentDialog.getSid());
		asDataInfo.setAppId(userAndAgentDialog.getAppId());
		asDataInfo.setResult(String.valueOf(0));
		asDataInfo.setServerAgentId(rmServerTransferData.getAgentid());
		if(rmServerTransferData.getQueuecount()>0){
			asDataInfo.setQueueCount(String.valueOf(rmServerTransferData.getQueuecount()));
		}
		String messageBody = asDataInfo.toCallbackSetModeJson();
		
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			logger.info("optCallbackRequestAS,ready to send http request,"+",url:"+userAndAgentDialog.getOptResultCBUrl()+",requestBody"+messageBody);
			Map<String,String> httpResult=dispatcher.sendPacket(userAndAgentDialog.getOptResultCBUrl(), httpMethod, header,messageBody);
			String statusCode = httpResult.get("statusCode");
			String content = httpResult.get("content");
			String dataFormat = httpResult.get("dataFormat");
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				for (int i = 1; i <=resendTimeNum; i++) {
					httpResult=dispatcher.sendPacket(userAndAgentDialog.getOptResultCBUrl(), httpMethod, header,messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
						content = httpResult.get("content");
						dataFormat = httpResult.get("dataFormat");
						break;
					}
				}
			}
			
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				logger.info("optCallbackRequestAS to AS error,statusCode:"+statusCode);
			}else{
				logger.info("optCallbackRequestAS to AS success");
//				if(StringUtils.isNotEmpty(content)){
//					if(Constants.DATA_FORMAT_JSON.equals(dataFormat)){
//						asDataInfo = new ASDataInfo();
//						asDataInfo = (ASDataInfo)JSONUtil.jsonToObj(content, ASDataInfo.class);
//					}
//					if(asDataInfo!=null){
//						//AS响应的命令是“设置客服消息模式”
//						if(Constants.AS_ACTION_OSSETSERVICEMODE.equals(asDataInfo.getAction())){
//							//如果回调url地址不为空，向AS发送请求
//							if(StringUtils.isNotEmpty(asDataInfo.getOptResultCBUrl())){
//								
//							}
//							//如果欢迎语不为空，向用户发送欢迎语
//							if(StringUtils.isNotEmpty(asDataInfo.getWelcome())){
//								
//							}
//						}
//					}
//				}
			}
		}catch (Exception e) {
			logger.error("optCallbackRequestAS to AS error:"+e.getMessage());
		}
		return asDataInfo;
	}

	@Override
	public boolean stopMsgRequestAS(UserAndAgentDialog userAndAgentDialog) {
		if(StringUtils.isEmpty(userAndAgentDialog.getMcm_notify_url())){
			logger.info("stopMsgRequestAS error,mcm_notify_url is empty.");
			return false;
		}
		
		ASDataInfo asDataInfo = new ASDataInfo();
		asDataInfo.setAction(Constants.AS_ACTION_STOP_MESSAGE);
		asDataInfo.setSid(userAndAgentDialog.getSid());
		asDataInfo.setAppId(userAndAgentDialog.getAppId());
		asDataInfo.setCreateTime(String.valueOf(System.currentTimeMillis()));
		asDataInfo.setOsUnityAccount(userAndAgentDialog.getOsUnityAccount());
		asDataInfo.setCustomAppID(userAndAgentDialog.getCustomAppId());
		asDataInfo.setChanType(String.valueOf(userAndAgentDialog.getChanType()));
		
		if(userAndAgentDialog.getAgentInfoSet().isEmpty()){
			logger.info("stopMsgRequestAS error,agentInfoList is empty.");
			return false;
		}
		
		List<String> agentIds = new ArrayList<String>();
		for(AgentInfo agentInfo:userAndAgentDialog.getAgentInfoSet()){
			agentIds.add(agentInfo.getAgentId());
		}
		asDataInfo.setAgentIds(agentIds);
		
		String messageBody = asDataInfo.toEndAskJson();
		
		HttpMethod httpMethod=HttpMethod.POST;
		HashMap<String, String> header=new  HashMap<String, String>();
		header.put("Content-Type", "application/json");
		try {
			logger.info("stopMsgRequestAS,ready to send http request,"+",url:"+userAndAgentDialog.getMcm_notify_url()+",requestBody"+messageBody);
			Map<String,String> httpResult=dispatcher.sendPacket(userAndAgentDialog.getMcm_notify_url(), httpMethod, header,messageBody);
			String statusCode = httpResult.get("statusCode");
			String content = httpResult.get("content");
			String dataFormat = httpResult.get("dataFormat");
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				for (int i = 1; i <=resendTimeNum; i++) {
					httpResult=dispatcher.sendPacket(userAndAgentDialog.getMcm_notify_url(), httpMethod, header,messageBody);
					statusCode = httpResult.get("statusCode");
					if(statusCode!=null&&Integer.parseInt(statusCode)<500){
						content = httpResult.get("content");
						dataFormat = httpResult.get("dataFormat");
						break;
					}
				}
			}
			
			if(statusCode==null||(statusCode!=null&&Integer.parseInt(statusCode)>=500)){
				logger.info("stopMsgRequestAS to AS error,statusCode:"+statusCode);
				return false;
			}else{
				logger.info("stopMsgRequestAS to AS success");
				return true;
			}
		}catch (Exception e) {
			logger.error("stopMsgRequestAS#error()", e);
			return false;
		}
	}

	@Override
	public void userDisconnect(Connector connector, String userAccount, AppAttrs appAttrs, 
			UserAndAgentDialog userAndAgentDialog) throws CCPServiceException {
		logger.info("as userDisconnect userAccount: {}.", userAccount);
		
		Set<AgentInfo> agentInfoSet = userAndAgentDialog.getAgentInfoSet();
		if(agentInfoSet.isEmpty()){
			logger.info("agentInfoList is empty, start exit queue.");
			
			// 退出排队
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_EXIT_CCS_QUEUE.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setCmserial(Constants.M3C_SERIAL); 
			rmServerTransferData.setCallid(userAndAgentDialog.getSid());
			
			SeqInfo seqInfo = new SeqInfo();
			seqInfo.setLogSessionId(ThreadContext.peek());
			seqInfo.setMcmEvent(MCMEventDefInner.ConnectorNotify_UserDisconnect_VALUE);
			rmServerTransferData.setSeq(seqInfo.toString());
			
			String cmdMessage = rmServerTransferData.toJsonForCmdExitCCSQueue();
			rmServerRequestService.doPushMessage(cmdMessage);
			
		} else {
			logger.info("agentInfoList is not empty, start end server.");
			
			// 通知rm server结束服务
			RMServerTransferData rmServerTransferData = new RMServerTransferData();
			rmServerTransferData.setCommand(CommandEnum.CMD_IM_AGENT_SERVICE_END.getValue());
			rmServerTransferData.setAppid(connector.getAppId());
			rmServerTransferData.setSid(userAndAgentDialog.getSid());
			
			for(AgentInfo agentInfo : agentInfoSet){
				rmServerTransferData.setAgentid(String.valueOf(agentInfo.getAgentId()));
				
				SeqInfo seqInfo = new SeqInfo();
				seqInfo.setMcmEvent(MCMEventDefInner.ConnectorNotify_UserDisconnect_VALUE);
				seqInfo.setUserAccount(userAccount);
				seqInfo.setAgentId(agentInfo.getAgentId());
				seqInfo.setAsFlag(Constants.AS_FLAG_YES);
				rmServerTransferData.setSeq(seqInfo.toString());
				
				rmServerRequestService.doPushMessage(rmServerTransferData.toJsonForCmdImAgentServiceEnd());
			}
		}
	}
}