package com.yuntongxun.mcm.util;

import net.sf.json.JSONObject;

import com.yuntongxun.mcm.mcm.model.MCMMessageInfo;


public class JSONBodyParser {

	/**
	 * 构建从第三方返回的消息体
	 * @param mailMsg
	 * @param appAttrs
	 * @param msgId
	 * @return
	 * 
	 *{
	 *    "msgSid": "msgSid",
	 *    "appId": "appId",
	 *    "userAccount": "userAccount",
	 *    "osUnityAccount": "osUnityAccount",
	 *    "msgType": "1",
	 *    "content": "content",
	 *    "createTime": "14324564578",
	 *    "fileName": "fileName",
	 *    "fileDownUrl": "fileDownUrl"
	 *}     
	 * 
	 * 
	 */
	public static MCMMessageInfo parseMCMMessageInfoBody(String body) {
		JSONObject jsonObj = JSONObject.fromObject(body);
		if(jsonObj.get("userAccount")!=null&&jsonObj.get("osUnityAccount")!=null){
			MCMMessageInfo mcmMessageInfo = new MCMMessageInfo();
			mcmMessageInfo.setUserAccount(jsonObj.get("userAccount").toString());
			mcmMessageInfo.setOsUnityAccount(jsonObj.get("osUnityAccount").toString());

			if(jsonObj.get("appId")!=null){
				mcmMessageInfo.setAppId(jsonObj.get("appId").toString());
			}
			if(jsonObj.get("msgSid")!=null){
				mcmMessageInfo.setMsgId(jsonObj.get("msgSid").toString());
			}
			if(jsonObj.get("createTime")!=null){
				mcmMessageInfo.setMsgDateCreated(jsonObj.get("createTime").toString());
			}
			if(jsonObj.get("msgType")!=null){
				mcmMessageInfo.setMsgType(Integer.parseInt(jsonObj.get("msgType").toString()));
			}
			if(jsonObj.get("content")!=null){
				mcmMessageInfo.setMsgContent(jsonObj.get("content").toString());
			}
			if(jsonObj.get("fileName")!=null){
				mcmMessageInfo.setMsgFileName(jsonObj.get("fileName").toString());
				
			}
			if(jsonObj.get("fileDownUrl")!=null){
				mcmMessageInfo.setMsgFileUrl(jsonObj.get("fileDownUrl").toString());
			}
			return mcmMessageInfo;
		}else{
			return null;
		}
	}
}
