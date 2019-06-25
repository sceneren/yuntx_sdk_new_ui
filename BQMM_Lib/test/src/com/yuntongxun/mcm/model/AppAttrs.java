package com.yuntongxun.mcm.model;

public class AppAttrs {

	/**
	 * 群组范围 0:100 1: 200 2:500 3:1000 4:2000
	 */
	private int groupScope;

	/**
	 * 该应用下支持多少群组(缺省5位：大概是40000个（10000 - 49999），6位大概是900000个)
	 */
	private String groupSeed = "10000";

	/**
	 * 所有通知消息的显示号码
	 */
	private String serviceNo = "10089";
	
	private String appName;
	
	/**
	 * 应用的标识符，创建应用时后台自动生成(8位)
	 */
	private String appPrefix;
	private String appStatus;
	private String appToken;
	private int isAgent;
	private String mcm_notify_url;
	
	private String customer_appid;
	private String accountSid;
	private String blackWordMode;
	private String blackWordType;
	private String relatedApp;
	
	private String step_app_flag;
	private String dateCreated;
	private String dateUpdated;
	private String sipNextNumber; 
	private String multiDeviceNotify;
	
	private String multiDeviceMsgSyncSwitch;
	private String applyOfflineCall;
	private String maxGroupLimit;
	private String offlineCallHangupPolicy;
	private String offlineCallOrderPolicy;
	
	private String apns_im_sound;
	private String apns_call_sound;
	private String im_android_sign;
	private String im_ios_sign;
	
	public String getAccountSid() {
		return accountSid;
	}

	public void setAccountSid(String accountSid) {
		this.accountSid = accountSid;
	}

	public String getBlackWordMode() {
		return blackWordMode;
	}

	public void setBlackWordMode(String blackWordMode) {
		this.blackWordMode = blackWordMode;
	}

	public String getBlackWordType() {
		return blackWordType;
	}

	public void setBlackWordType(String blackWordType) {
		this.blackWordType = blackWordType;
	}

	public String getRelatedApp() {
		return relatedApp;
	}

	public void setRelatedApp(String relatedApp) {
		this.relatedApp = relatedApp;
	}

	public String getStep_app_flag() {
		return step_app_flag;
	}

	public void setStep_app_flag(String step_app_flag) {
		this.step_app_flag = step_app_flag;
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}

	public String getGroupSeed() {
		return groupSeed;
	}

	public void setGroupSeed(String groupSeed) {
		this.groupSeed = groupSeed;
	}

	public int getGroupScope() {
		return groupScope;
	}

	public void setGroupScope(int groupScope) {
		this.groupScope = groupScope;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppPrefix() {
		return appPrefix;
	}

	public void setAppPrefix(String appPrefix) {
		this.appPrefix = appPrefix;
	}

	public String getAppStatus() {
		return appStatus;
	}

	public void setAppStatus(String appStatus) {
		this.appStatus = appStatus;
	}

	public String getAppToken() {
		return appToken;
	}

	public void setAppToken(String appToken) {
		this.appToken = appToken;
	}

	public int getIsAgent() {
		return isAgent;
	}

	public void setIsAgent(int isAgent) {
		this.isAgent = isAgent;
	}

	public String getMcm_notify_url() {
		return mcm_notify_url;
	}

	public void setMcm_notify_url(String mcm_notify_url) {
		this.mcm_notify_url = mcm_notify_url;
	}

	public String getCustomer_appid() {
		return customer_appid;
	}

	public void setCustomer_appid(String customer_appid) {
		this.customer_appid = customer_appid;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getSipNextNumber() {
		return sipNextNumber;
	}

	public void setSipNextNumber(String sipNextNumber) {
		this.sipNextNumber = sipNextNumber;
	}
	
	public String getMultiDeviceNotify() {
		return multiDeviceNotify;
	}

	public void setMultiDeviceNotify(String multiDeviceNotify) {
		this.multiDeviceNotify = multiDeviceNotify;
	}

	public String getMultiDeviceMsgSyncSwitch() {
		return multiDeviceMsgSyncSwitch;
	}

	public void setMultiDeviceMsgSyncSwitch(String multiDeviceMsgSyncSwitch) {
		this.multiDeviceMsgSyncSwitch = multiDeviceMsgSyncSwitch;
	}

	public String getApplyOfflineCall() {
		return applyOfflineCall;
	}

	public void setApplyOfflineCall(String applyOfflineCall) {
		this.applyOfflineCall = applyOfflineCall;
	}

	public String getMaxGroupLimit() {
		return maxGroupLimit;
	}

	public void setMaxGroupLimit(String maxGroupLimit) {
		this.maxGroupLimit = maxGroupLimit;
	}

	public String getOfflineCallHangupPolicy() {
		return offlineCallHangupPolicy;
	}

	public void setOfflineCallHangupPolicy(String offlineCallHangupPolicy) {
		this.offlineCallHangupPolicy = offlineCallHangupPolicy;
	}

	public String getOfflineCallOrderPolicy() {
		return offlineCallOrderPolicy;
	}

	public void setOfflineCallOrderPolicy(String offlineCallOrderPolicy) {
		this.offlineCallOrderPolicy = offlineCallOrderPolicy;
	}

	public String getApns_im_sound() {
		return apns_im_sound;
	}

	public void setApns_im_sound(String apns_im_sound) {
		this.apns_im_sound = apns_im_sound;
	}

	public String getApns_call_sound() {
		return apns_call_sound;
	}

	public void setApns_call_sound(String apns_call_sound) {
		this.apns_call_sound = apns_call_sound;
	}

	public String getIm_android_sign() {
		return im_android_sign;
	}

	public void setIm_android_sign(String im_android_sign) {
		this.im_android_sign = im_android_sign;
	}

	public String getIm_ios_sign() {
		return im_ios_sign;
	}

	public void setIm_ios_sign(String im_ios_sign) {
		this.im_ios_sign = im_ios_sign;
	}

	@Override
	public String toString() {
		return "AppAttrs [groupScope=" + groupScope + ", groupSeed="
				+ groupSeed + ", serviceNo=" + serviceNo + ", appName="
				+ appName + ", appPrefix=" + appPrefix + ", appStatus="
				+ appStatus + ", appToken=" + appToken + ", isAgent=" + isAgent
				+ ", mcm_notify_url=" + mcm_notify_url + ", customer_appid="
				+ customer_appid + ", accountSid=" + accountSid
				+ ", blackWordMode=" + blackWordMode + ", blackWordType="
				+ blackWordType + ", relatedApp=" + relatedApp
				+ ", step_app_flag=" + step_app_flag + ", dateCreated="
				+ dateCreated + ", dateUpdated=" + dateUpdated
				+ ", sipNextNumber=" + sipNextNumber + "]";
	}
	
}
