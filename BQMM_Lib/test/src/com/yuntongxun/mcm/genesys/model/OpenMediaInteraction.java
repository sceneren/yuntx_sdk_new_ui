package com.yuntongxun.mcm.genesys.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yuntongxun.mcm.genesys.enumerate.InteractionStatus;
import com.yuntongxun.mcm.genesys.util.CommonUtils;

public class OpenMediaInteraction {
	
	private String connId;
	private Integer eventId;
	private String eventName;
	private String mediaType;
	private String placeId;
	
	private InteractionStatus status;
	private Map<String, String> attachedData = new ConcurrentHashMap<String, String>();
	private String url;
	private Integer ticketId;
	private Integer visibility;
	
	private String textMessage;
	private String thisQueue;
	private String nickName;
	private Map<String, String> extension = new ConcurrentHashMap<String, String>();

	public String getConnId() {
		return connId;
	}

	public void setConnId(final String connId) {
		this.connId = connId;
	}

	public Integer getEventId() {
		return eventId;
	}

	public void setEventId(Integer eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public InteractionStatus getStatus() {
		return status;
	}

	public void setStatus(InteractionStatus status) {
		this.status = status;
	}

	public Map<String, String> getAttachedData() {
		return attachedData;
	}

	public void setAttachedData(final Map<String, String> attachedData) {
		this.attachedData = attachedData;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getTicketId() {
		return this.ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getVisibility() {
		return visibility;
	}

	public void setVisibility(Integer visibility) {
		this.visibility = visibility;
	}

	public String getTextMessage() {
		return this.textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String getThisQueue() {
		return thisQueue;
	}

	public void setThisQueue(String thisQueue) {
		this.thisQueue = thisQueue;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Map<String, String> getExtension() {
		return this.extension;
	}

	public void setExtension(Map<String, String> extension) {
		this.extension = extension;
	}

	public OpenMediaInteraction() {
	}

	public OpenMediaInteraction(final String connId, final String placeId,
			final String mediaType, final InteractionStatus status,
			final Map<String, String> attachedData) {
		if (CommonUtils.isEmpty(connId)) {
			throw new IllegalArgumentException("connId is required.");
		}
		if (CommonUtils.isEmpty(mediaType)) {
			throw new IllegalArgumentException("mediaType is required.");
		}

		this.connId = connId;
		this.placeId = placeId;
		this.mediaType = mediaType;
		this.status = status;
		this.attachedData = attachedData;
	}

	public OpenMediaInteraction(String connId, String placeId,
			String mediaType, final InteractionStatus status, String url,
			Integer ticketId, Map<String, String> attachedData, String nickName) {
		this(connId, placeId, mediaType, status, attachedData);
		this.ticketId = ticketId;
		this.url = url;
		this.nickName = nickName;
	}

	@Override
	public String toString() {
		return "OpenMediaInteraction [attachedData=" + attachedData
				+ ", connId=" + connId + ", eventId=" + eventId
				+ ", eventName=" + eventName + ", extension=" + extension
				+ ", mediaType=" + mediaType + ", nickName=" + nickName
				+ ", placeId=" + placeId + ", status=" + status
				+ ", textMessage=" + textMessage + ", thisQueue=" + thisQueue
				+ ", ticketId=" + ticketId + ", url=" + url + ", visibility="
				+ visibility + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof OpenMediaInteraction)) {
			return false;
		}

		OpenMediaInteraction eqObj = (OpenMediaInteraction) obj;

		if (!eqObj.getConnId().equals(this.getConnId())) {
			return false;
		}

		return true;
	}

}
