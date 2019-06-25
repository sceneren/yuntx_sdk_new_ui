package com.yuntongxun.mcm.weixin;

import net.sf.json.JSONObject;

/**
 * 项目：ECMCMServer
 * 描述：向微信发送消息类
 * 创建人：weily
 * 创建时间：2015年8月5日 上午10:05:33 
 */
public class WeiXinSendMsg {

	private String accessToken;
	private String touser;
	private String msgtype;
	private String content;
	private String mediaId;
	

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTouser() {
		return touser;
	}

	public void setTouser(String touser) {
		this.touser = touser;
	}

	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String toTextJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"touser\":\"");
		sb.append(touser==null?"":touser);
		sb.append("\",\"msgtype\":\"");
		sb.append(msgtype==null?"":msgtype);
		sb.append("\",\"text\":");
		sb.append("{");
		sb.append("\"content\":\"");
		sb.append(content==null?"":content);
		sb.append("\"}");
		sb.append("}");
		return sb.toString();
	}

	public String toImageJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"touser\":\"");
		sb.append(touser==null?"":touser);
		sb.append("\",\"msgtype\":\"");
		sb.append(msgtype==null?"":msgtype);
		sb.append("\",\"image\":");
		sb.append("{");
		sb.append("\"media_id\":\"");
		sb.append(mediaId==null?"":mediaId);
		sb.append("\"}");
		sb.append("}");
		return sb.toString();
	}

	public String toVoiceJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"touser\":\"");
		sb.append(touser==null?"":touser);
		sb.append("\",\"msgtype\":\"");
		sb.append(msgtype==null?"":msgtype);
		sb.append("\",\"voice\":");
		sb.append("{");
		sb.append("\"media_id\":\"");
		sb.append(mediaId==null?"":mediaId);
		sb.append("\"}");
		sb.append("}");
		return sb.toString();
	}
	
	public String toJson(){
		JSONObject jsonObj = JSONObject.fromObject(this);
		return jsonObj.toString();
	}
	
}
