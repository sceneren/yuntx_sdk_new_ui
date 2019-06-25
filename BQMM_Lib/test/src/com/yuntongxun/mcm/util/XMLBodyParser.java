package com.yuntongxun.mcm.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;


public class XMLBodyParser {

	public static final Logger logger = LogManager.getLogger(XMLBodyParser.class);
	
	/**
	 *<OSSendMessage>
	 *  <msgSid>070706524191111111155004</msgSid>
	 *  <appId>20150314000000110000000000000010</appId>
	 *  <userAccount>18511250371</userAccount>
	 *  <osUnityAccount>KF4008818600668603</osUnityAccount>
	 *  <msgType>1</msgType>
	 *  <content>汉字OK？</content>
	 *  <createTime>14324564231</createTime>
	 *  <fileName>test.png</fileName>
	 *  <fileDownUrl>http://xxx.xxx:8080/xxxx</fileDownUrl>
	 *</OSSendMessage>
	 * @param body
	 * @return
	 */
	public static MCMMessageInfo parseMCMMessageInfoBody(String body){
		Document document = null;
		MCMMessageInfo mcmMessageInfo = null;
		try {
			document = DocumentHelper.parseText(body);
			Element root=document.getRootElement(); 
			if(Constants.MCM_MESSAGE_XML_RESPONSE_ROOT_ELEMENT_NAME.equals(root.getName())){
				mcmMessageInfo=new MCMMessageInfo();
				if(root.element("userAccount")!=null){
					mcmMessageInfo.setUserAccount(root.element("userAccount").getText());
				}
				if(root.element("osUnityAccount")!=null){
					mcmMessageInfo.setOsUnityAccount(root.element("osUnityAccount").getText());
				}
				if(root.element("appId")!=null){
					mcmMessageInfo.setAppId(root.element("appId").getText());
				}
				if(root.element("msgSid")!=null){
					mcmMessageInfo.setMsgId(root.element("msgSid").getText());
				}
				if(root.element("createTime")!=null){
					mcmMessageInfo.setMsgDateCreated(root.element("createTime").getText());
				}
				if(root.element("msgType")!=null){
					mcmMessageInfo.setMsgType(Integer.parseInt(root.element("msgType").getText()));
				}
				if(root.element("content")!=null){
					mcmMessageInfo.setMsgContent(root.element("content").getText());
				}
				if(root.element("fileDownUrl")!=null){
					mcmMessageInfo.setMsgFileUrl(root.element("fileDownUrl").getText());
				}
				if(root.element("fileName")!=null){
					mcmMessageInfo.setMsgFileName(root.element("fileName").getText());
				}
			}else{
				return null;
			}
		} catch (DocumentException e) {
			logger.error(">>>parse xml exception,xml body:"+body+",exception message:"+e.getMessage());
		}
		return mcmMessageInfo;
	}
	
}
