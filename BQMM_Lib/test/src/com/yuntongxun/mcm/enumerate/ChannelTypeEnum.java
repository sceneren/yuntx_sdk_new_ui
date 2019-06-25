package com.yuntongxun.mcm.enumerate;

/**
 * callManager 向 mcmgw 转发请求 时，参数中的channelType类型
 * @author weily
 *
 */
public enum ChannelTypeEnum {
	
	IM(0),	
	WEIXIN(1),
	MAIL(2),
	SMS(3),
	FAX(4);
	
	private int value;
	
	private ChannelTypeEnum(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}

}
