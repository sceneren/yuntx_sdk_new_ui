package com.yuntongxun.mcm.mcm.model;

import java.util.HashSet;
import java.util.Set;

import com.yuntongxun.mcm.core.AbstractDataModel;

/**
 * 用户坐席实时会话
 */
public class UserAndAgentDialog extends AbstractDataModel {

	private int CCSType;
	private String sid;
	private String osUnityAccount;
	private long dateCreated;
	private String skillGroupId;
	
	private String channel;
	private String historyAgentIds;	//历史座席id，记录所有转接过的座席id，每转接一次，增加一个座席id，用逗号分割，“80010,80011,80112”
	private Set<AgentInfo> agentInfoSet; //记录坐席相关信息
	private String asServiceMode;	//AS侧服务模式，0表示AS模式、1表示IM模式、2机器人模式。默认为IM模式自动排队
	private String asWelcome;	//AS侧欢迎语
	private String optResultCBUrl;	//AS侧操作回调地址
	private String appId;
	private String mcm_notify_url;
	private String customAppId;
	private Integer chanType;
	private int queueCount;
	private int isReserved; //是否预留 1.未预留 2.预留
	
	public UserAndAgentDialog(){
		this.agentInfoSet = new HashSet<AgentInfo>();
	}
	
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getOsUnityAccount() {
		return osUnityAccount;
	}

	public void setOsUnityAccount(String osUnityAccount) {
		this.osUnityAccount = osUnityAccount;
	}

	public int getCCSType() {
		return CCSType;
	}

	public void setCCSType(int cCSType) {
		CCSType = cCSType;
	}

	public String getSkillGroupId() {
		return skillGroupId;
	}

	public void setSkillGroupId(String skillGroupId) {
		this.skillGroupId = skillGroupId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getHistoryAgentIds() {
		return historyAgentIds;
	}

	public void setHistoryAgentIds(String historyAgentIds) {
		this.historyAgentIds = historyAgentIds;
	}

	public Set<AgentInfo> getAgentInfoSet() {
		return agentInfoSet;
	}

	public void setAgentInfoSet(Set<AgentInfo> agentInfoSet) {
		this.agentInfoSet = agentInfoSet;
	}

	public String getAsServiceMode() {
		return asServiceMode;
	}

	public void setAsServiceMode(String asServiceMode) {
		this.asServiceMode = asServiceMode;
	}

	public String getAsWelcome() {
		return asWelcome;
	}

	public void setAsWelcome(String asWelcome) {
		this.asWelcome = asWelcome;
	}

	public String getOptResultCBUrl() {
		return optResultCBUrl;
	}

	public void setOptResultCBUrl(String optResultCBUrl) {
		this.optResultCBUrl = optResultCBUrl;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMcm_notify_url() {
		return mcm_notify_url;
	}

	public void setMcm_notify_url(String mcm_notify_url) {
		this.mcm_notify_url = mcm_notify_url;
	}

	public String getCustomAppId() {
		return customAppId;
	}

	public void setCustomAppId(String customAppId) {
		this.customAppId = customAppId;
	}

	public Integer getChanType() {
		return chanType;
	}

	public void setChanType(Integer chanType) {
		this.chanType = chanType;
	}
	
	public int getQueueCount() {
		return queueCount;
	}

	public void setQueueCount(int queueCount) {
		this.queueCount = queueCount;
	}

	public int getIsReserved() {
		return isReserved;
	}

	public void setIsReserved(int isReserved) {
		this.isReserved = isReserved;
	}
	
}
