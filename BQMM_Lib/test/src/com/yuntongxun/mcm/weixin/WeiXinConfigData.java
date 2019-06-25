package com.yuntongxun.mcm.weixin;

import net.sf.json.JSONObject;

public class WeiXinConfigData {

	private String openID;
	private String appID;
	private String appSecret;
	private String ronglianAppId;
	private String accessToken;
	private String tokenExpirationTime;

	public String getOpenID() {
		return openID;
	}

	public void setOpenID(String openID) {
		this.openID = openID;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getRonglianAppId() {
		return ronglianAppId;
	}

	public void setRonglianAppId(String ronglianAppId) {
		this.ronglianAppId = ronglianAppId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenExpirationTime() {
		return tokenExpirationTime;
	}

	public void setTokenExpirationTime(String tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	public String toJson(){
		JSONObject jsonObj = JSONObject.fromObject(this);
		return jsonObj.toString();
	}
}
