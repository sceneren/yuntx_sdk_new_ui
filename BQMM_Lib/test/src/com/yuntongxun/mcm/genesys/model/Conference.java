package com.yuntongxun.mcm.genesys.model;

import com.genesyslab.platform.voice.protocol.ConnectionId;

/**
 * 
* @Description: 记录设置的两步会议 
* @author wanglei 
* @date 2016-1-22 9:32:08 
*
 */
public class Conference {

	private String thisDN;
	private ConnectionId connId;
	private ConnectionId conferenceConnId;

	public String getThisDN() {
		return thisDN;
	}

	public void setThisDN(String thisDN) {
		this.thisDN = thisDN;
	}

	public ConnectionId getConnId() {
		return connId;
	}

	public void setConnId(ConnectionId connId) {
		this.connId = connId;
	}

	public ConnectionId getConferenceConnId() {
		return conferenceConnId;
	}

	public void setConferenceConnId(ConnectionId conferenceConnId) {
		this.conferenceConnId = conferenceConnId;
	}

}
