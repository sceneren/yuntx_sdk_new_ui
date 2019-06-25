package com.yuntongxun.mcm.enumerate;

/**
 * callManager 向 mcmgw 转发请求 时，参数中的channelType类型
 * @author weily
 *
 */
public enum WeiXinMsgTypeEnum {
	
	TEXT("text"),	
	IMAGE("image"),
	VOICE("voice");
	
	private String value;
	
	private WeiXinMsgTypeEnum(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}

}
