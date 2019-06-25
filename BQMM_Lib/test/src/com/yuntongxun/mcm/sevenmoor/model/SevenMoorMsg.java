package com.yuntongxun.mcm.sevenmoor.model;

public class SevenMoorMsg {

	private String _id;
	private String sid;
	private long when;
	private String dateTime;
	private String content;
	private String contentType;
	private String showHtml;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public long getWhen() {
		return when;
	}

	public void setWhen(long when) {
		this.when = when;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getShowHtml() {
		return showHtml;
	}

	public void setShowHtml(String showHtml) {
		this.showHtml = showHtml;
	}

}
