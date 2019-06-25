package com.yuntongxun.mcm.cc.enumerate;

public enum ActionEnum {

	CMD_USER_START_MESSAGE("startmessage"), // 开始咨询

	CMD_USER_STOP_MESSAGE("stopmessage"); // 结束咨询

	private String value;

	private ActionEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
