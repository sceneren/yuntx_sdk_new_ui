package com.yuntongxun.mcm.weixin;
/**
 * 项目：ECMCMServer
 * 描述：http请求微信接口的响应数据
 * 创建人：weily
 * 创建时间：2015年8月6日 下午4:38:59 
 */
public class WeiXinResponseData {

	public final static String ACCESS_TOKEN = "access_token";
	public final static String EXPIRES_IN = "expires_in";

	public final static String TYPE = "type";
	public final static String MEDIA_ID = "media_id";
	public final static String CREATED_AT = "created_at";
	
	//获取到的凭证
	private String accessToken;
	
	//凭证有效时间，单位：秒
	private String expiresIn;
	
	

	//媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb，主要用于视频与音乐格式的缩略图）
	private String type;
	
	//媒体文件上传后，获取时的唯一标识
	private String mediaId;
	
	//媒体文件上传时间戳
	private String createdAt;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	
}
