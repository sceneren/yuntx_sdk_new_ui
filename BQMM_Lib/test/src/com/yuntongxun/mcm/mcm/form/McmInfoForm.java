package com.yuntongxun.mcm.mcm.form;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


public class McmInfoForm {
	private static final Logger logger = LogManager.getLogger(McmInfoForm.class);
	private  String appId;
	private  String msgSid;
	private  String userAccount;
	private  String osUnityAccount;
	private  int msgType;
	private  String content;
	private  String channelType;
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getMsgSid() {
		return msgSid;
	}
	public void setMsgSid(String msgSid) {
		this.msgSid = msgSid;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getOsUnityAccount() {
		return osUnityAccount;
	}
	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getChannelType() {
		return channelType;
	}
	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}
	public static McmInfoForm prase(final String body)
	{
//		logger.info("++++++++++ praseToMcmInfoForm  begin "+System.currentTimeMillis());
//		logger.info("+++++++++++++");
//		logger.info("xml body:"+body);
//		logger.info("+++++++++++++");
		Document document;
		try {
			McmInfoForm mcmInfo=new McmInfoForm();
			document = DocumentHelper.parseText(body);
			Element root=document.getRootElement(); 
			if(root.element("msgSid")!=null){
				mcmInfo.setMsgSid(root.element("msgSid").getText());
			}else{
				logger.info("msgSid is null");
			}
			if(root.element("appId")!=null){
				mcmInfo.setAppId(root.element("appId").getText());
			}else{
				logger.info("appId is null");
			}
			
			if(root.element("userAccount")!=null){
				mcmInfo.setUserAccount(root.element("userAccount").getText());
			}else{
				logger.info("userAccount is null");
			}
			
			if(root.element("osUnityAccount")!=null){
				mcmInfo.setOsUnityAccount(root.element("osUnityAccount").getText());
			}else{
				logger.info("osUnityAccount is null");
			}
			
			if(root.element("msgType")!=null){
				mcmInfo.setMsgType(Integer.parseInt(root.element("msgType").getText()));
			}else{
				logger.info("msgType is null");
			}

			if(root.element("content")!=null){
				mcmInfo.setContent(root.element("content").getText());
			}else{
				logger.info("content is null");
			}
			if(root.element("channelType")!=null){
				mcmInfo.setChannelType(root.element("channelType").getText());
			}
			
			return mcmInfo;
		} catch (DocumentException e) {
			e.printStackTrace();
			logger.info("mcmInfo prase error");
		}
		return null	;
	}
	
//	<OSSendMessage>
//	<appId>sdfkdsfl</appId>
//	<sid>123434345094483</sid>
//	<userAccount><![CDATA[userAcc]]></userAccount>
//	<osUnityAccount><![CDATA[userAcc]]></osUnityAccount>
//	<MsgData>
//	<Data>
//	<type></type>
//	<content><![CDATA[msgcontent]]></content>
//	<Data>
//	</MsgData>
//	</OSSendMessage>
}
