package com.yuntongxun.mcm.genesys.model;


public class MsgJsonDataModel {

	private int serviceCap; // 1语音、2多媒体消息、3全能力
	private String placeId; // 座席工位号
	
	/**
	 * sip server
	 */
	private String queue; // 技能组
	private String callType; //呼叫类型
	private String thisDN; // 坐席号码
	private String otherDN; //被叫号码
	private String passwd; // 密码
	private String workMode; // 工作模式

	public int getServiceCap() {
		return serviceCap;
	}

	public void setServiceCap(int serviceCap) {
		this.serviceCap = serviceCap;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getWorkMode() {
		return workMode;
	}

	public void setWorkMode(String workMode) {
		this.workMode = workMode;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getThisDN() {
		return thisDN;
	}

	public void setThisDN(String thisDN) {
		this.thisDN = thisDN;
	}

	public String getOtherDN() {
		return otherDN;
	}

	public void setOtherDN(String otherDN) {
		this.otherDN = otherDN;
	}

	@Override
	public String toString() {
		return "MsgJsonDataModel [serviceCap=" + serviceCap + ", placeId="
				+ placeId + ", thisDN=" + thisDN + ", otherDN=" + otherDN
				+ ", queue=" + queue + ", passwd=" + passwd + ", workMode="
				+ workMode + ", callType=" + callType + "]";
	}
	
}
