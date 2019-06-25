package com.yuntongxun.mcm.weixin.util;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.weixin.WeiXinPushMsg;

public class WeiXinUtils {
	public static final Logger logger = LogManager.getLogger(WeiXinUtils.class);

	public static boolean isValidMsg(String signature,String timestamp,String nonce,String token){
    	if(StringUtils.isEmpty(signature)){
    		logger.info("signature is empty,can't verify message,return...");
    		return false;
        	
        }
        if(StringUtils.isEmpty(timestamp)){
        	logger.info("timestamp is empty,can't verify message,return...");
    		return false;
        	
        }
        if(StringUtils.isEmpty(nonce)){
        	logger.info("nonce is empty,can't verify message,return...");
    		return false;
        	
        }
        if(StringUtils.isEmpty(token)){
        	logger.info("token is empty,can't verify message,return...");
    		return false;
        	
        }
    	boolean result = false;
    	try{
    		List<String> strList = new ArrayList<String>();
    		strList.add(timestamp);
    		strList.add(nonce);
    		strList.add(token);
    		Collections.sort(strList);
    		StringBuffer strBuf = new StringBuffer();
    		for(String str:strList){
    			strBuf.append(str);
    		}
    		String str = strBuf.toString();
    		String sha1Str = MessageDigestUtil.sha1(str);
    		logger.info("result_sha1:"+sha1Str);
    		logger.info("signature:"+signature);
    		if(sha1Str.equals(signature)){
    			result = true;
    		}
    	}catch(NoSuchAlgorithmException e){
    		logger.info("sha1 msg error:"+e.getMessage());
    	}
    	return result;
    }
	
	/**
	 * 解析微信向公众号推送用户发送的消息，数据格式为xml，格式如下：
	 * 
	 * 文本消息
	 * <xml>
	 *	<ToUserName><![CDATA[toUser]]></ToUserName>
	 *	<FromUserName><![CDATA[fromUser]]></FromUserName> 
	 *	<CreateTime>1348831860</CreateTime>
	 *	<MsgType><![CDATA[text]]></MsgType>
	 *	<Content><![CDATA[this is a test]]></Content>
	 *	<MsgId>1234567890123456</MsgId>
	 * </xml>
	 * 
	 * 图片消息
	 * <xml>
	 * 	<ToUserName><![CDATA[toUser]]></ToUserName>
	 * 	<FromUserName><![CDATA[fromUser]]></FromUserName>
	 * 	<CreateTime>1348831860</CreateTime>
	 * 	<MsgType><![CDATA[image]]></MsgType>
	 * 	<PicUrl><![CDATA[this is a url]]></PicUrl>
	 * 	<MediaId><![CDATA[media_id]]></MediaId>
	 * 	<MsgId>1234567890123456</MsgId>
	 * </xml>
	 * 
	 * 语音消息
	 * <xml>
	 * 	<ToUserName><![CDATA[toUser]]></ToUserName>
	 * 	<FromUserName><![CDATA[fromUser]]></FromUserName>
	 * 	<CreateTime>1357290913</CreateTime>
	 * 	<MsgType><![CDATA[voice]]></MsgType>
	 * 	<MediaId><![CDATA[media_id]]></MediaId>
	 * 	<Format><![CDATA[Format]]></Format>
	 * 	<MsgId>1234567890123456</MsgId>
	 * </xml>
	 * 
	 * 
	 * @param xmlBody
	 * @return WeiXinPushMsg
	 * @throws Exception
	 */
	public static WeiXinPushMsg parsePushMsg(String xmlBody,WeiXinPushMsg pushMsg) throws Exception{
		Document document = DocumentHelper.parseText(xmlBody);
		Element root=document.getRootElement(); 
		if(root.element("MsgType")!=null){
			if(pushMsg == null){
				pushMsg = new WeiXinPushMsg();
			}
			String msgType = root.element("MsgType").getText();
			//消息类型
			pushMsg.setMsgType(msgType);
			
			//开发者微信号
			if(root.element("ToUserName")!=null&&StringUtils.isNotEmpty(root.element("ToUserName").getText())){
				pushMsg.setToUserName(root.element("ToUserName").getText());
			}else{
				logger.info("parse weixin push msg error,[ToUserName] can't read...");
				return null;
			}
			
			//发送方帐号（一个OpenID）
			if(root.element("FromUserName")!=null&&StringUtils.isNotEmpty(root.element("FromUserName").getText())){
				pushMsg.setFromUserName(root.element("FromUserName").getText());
			}else{
				logger.info("parse weixin push msg error,[FromUserName] can't read...");
				return null;
			}
			
			//消息创建时间 （整型）
			if(root.element("CreateTime")!=null&&StringUtils.isNotEmpty(root.element("CreateTime").getText())){
				pushMsg.setCreateTime(root.element("CreateTime").getText());
			}else{
				logger.info("parse weixin push msg error,[CreateTime] can't read...");
				return null;
			}
			
			//消息id，64位整型
			if(root.element("MsgId")!=null&&StringUtils.isNotEmpty(root.element("MsgId").getText())){
				pushMsg.setMsgId(root.element("MsgId").getText());
			}else{
				logger.info("parse weixin push msg error,[MsgId] can't read...");
				return null;
			}
			
			//处理不同消息类型的不同字段
			if(WeiXinMsgTypeEnum.TEXT.getValue().equals(msgType)){
				//文本消息内容
				if(root.element("Content")!=null&&StringUtils.isNotEmpty(root.element("Content").getText())){
					pushMsg.setContent(root.element("Content").getText());
				}else{
					logger.info("parse weixin push msg error,[Content] can't read...");
					return null;
				}
			}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(msgType)){
				//图片URL
				if(root.element("PicUrl")!=null&&StringUtils.isNotEmpty(root.element("PicUrl").getText())){
					pushMsg.setPicUrl(root.element("PicUrl").getText());
				}else{
					logger.info("parse weixin push msg error,[PicUrl] can't read...");
					return null;
				}
				//图片文件在微信文件服务器的id
				if(root.element("MediaId")!=null&&StringUtils.isNotEmpty(root.element("MediaId").getText())){
					pushMsg.setMediaId(root.element("MediaId").getText());
				}else{
					logger.info("parse weixin push msg error,[MediaId] can't read...");
					return null;
				}
			}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(msgType)){
				//语音文件格式
				if(root.element("Format")!=null&&StringUtils.isNotEmpty(root.element("Format").getText())){
					pushMsg.setFormat(root.element("Format").getText());
				}else{
					logger.info("parse weixin push msg error,[Format] can't read...");
					return null;
				}
				//语音文件在微信文件服务器的id
				if(root.element("MediaId")!=null&&StringUtils.isNotEmpty(root.element("MediaId").getText())){
					pushMsg.setMediaId(root.element("MediaId").getText());
				}else{
					logger.info("parse weixin push msg error,[MediaId] can't read...");
					return null;
				}
			}else{
				logger.info("parse weixin push msg error,[MsgType="+msgType+"] is not support...");
				return null;
			}
		}else{
			logger.info("parse weixin push msg error,[MsgType] can't read...");
			return null;
		}
		return pushMsg;
	}
	
//	public WeiXinPushMsg buildPushMsg(String jsonBody) throws ParseException{
//		JSONObject msgJsonObj = new JSONObject(jsonBody);
//		WeiXinPushMsg weiXinPushMsg = null;
//		if(msgJsonObj != null){
//			weiXinPushMsg = new WeiXinPushMsg();
//			weiXinPushMsg.setAccessToken(msgJsonObj.getString("access_token"));
//			String msgType = msgJsonObj.getString("msgtype");
//			if(WeiXinMsgTypeEnum.TEXT.getValue().equals(msgType)){
//				weiXinPushMsg.setContent(msgJsonObj.getString("content"));
//				weiXinPushMsg.setMsgtype(msgJsonObj.getString("msgtype"));
//				
//			}else if(WeiXinMsgTypeEnum.IMAGE.getValue().equals(msgType)){
//				
//			}else if(WeiXinMsgTypeEnum.VOICE.getValue().equals(msgType)){
//				
//			}
//			weiXinPushMsg.setMediaId(msgJsonObj.getString("media_id"));
//			weiXinPushMsg.setTouser(msgJsonObj.getString("touser"));
//			
//		}
//	}
	
	/**
	 * 获取文件服务器 文件地址的扩展名
	 * @param url
	 * @return
	 */
	public static String getExtentName(String url){
		String lastPart = url.substring(url.lastIndexOf("/")+1);
		
		if(lastPart.lastIndexOf(".")>=0){
			return lastPart.substring(lastPart.lastIndexOf("."));
		}else{
			return null;
		}
			
	}
}
