package com.yuntongxun.mcm.util;


/**
 * 项目：ECAuthServer
 * 描述：
 * 创建人：weily
 * 创建时间：2015年10月14日 下午2:56:09 
 */
public class RedisKeyConstant {

	public static final String REDIS_SEPARATOR = "|";
	
	public static final String YTX_USER_LOGIN = "ytx001"; //ytx001|{useracc}|{deviceType}
	public static final String YTX_USER_DEVICE_STATE = "ytx003";	//ytx003|{useracc}
	public static final String YTX_APPS_UPDATE = "ytx006";	//ytx006|{appKey}				(set类型)
	public static final String YTX_APPS = "ytx009";	//ytx009|{appKey}
	public static final String YTX_APPLE_DEVICE_TOKEN = "ytx011";	//ytx011|{useracc}     (set类型)
	
	public static final String YTX_APPLE_DEVICE_TOKEN_R = "ytx016";	//ytx016|deviceToken        value:useracc,deviceno
	public static final String YTX_USER_REGISTER = "ytx017";	//ytx017|{useracc}
	public static final String YTX_USER_SAMPLE_AUTH = "ytx018";	//ytx021|authtoken
	public static final String YTX_TAKE_MESSAGE = "ytx019";	//ytx019|{useracc}
	public static final String YTX_DEVICE_LOGIN_STATE = "ytx031";	//ytx031|useracc|deviceType
	
	public static final String YTX_USER_MSG_MAX_VERSION = "IM";	//IM+ userAcc
	public static final String YTX_USER_AGENT_DIALOG = "ytx060" + REDIS_SEPARATOR; // 用户客服会话实时 ytx060|userAcc
	public static final String YTX_USER_AGENT_DIALOG_SID = "ytx061" + REDIS_SEPARATOR; // 用户会话实时数据反查 ytx061|sid
	public static final String YTX_AGENT_EVENT = "ytx062" + REDIS_SEPARATOR; // 坐席rm对话信息ytx062|agentId
	public static final String YTX_7MOOR_LOGIN_INFO = "ytx063" + REDIS_SEPARATOR; // 7moor登录，ytx063|useracc value:	
	public static final String YTX_7MOOR_LOGIN_INFO_R = "ytx064" + REDIS_SEPARATOR; // 7moor登录，ytx064|connectionId value:{"userAcc":"","receivedMsgIds": ["",""]}
}
