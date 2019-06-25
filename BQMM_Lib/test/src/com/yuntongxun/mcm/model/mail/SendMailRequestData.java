package com.yuntongxun.mcm.model.mail;

import java.util.List;

public class SendMailRequestData {

	private String action;
	private String actionId;
	private String mailId;
	private String pwd;
	private String to;
	private String cc;
	private String title;
	private String content;
	private List<McmMailAttchmentInfo> attachment;
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<McmMailAttchmentInfo> getAttachment() {
		return attachment;
	}
	public void setAttachment(List<McmMailAttchmentInfo> attachment) {
		this.attachment = attachment;
	}
	
	
}
