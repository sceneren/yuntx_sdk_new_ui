package com.yuntongxun.mcm.util;
import io.netty.channel.Channel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * 定义系统常量
 *
 */
public interface Constants {

	public static final int SUCC = 200;

	public static final String RESPONSE_OK="000000";		//RESR返回值  代表成功

	/**
	 * The KEY of Local XML Config 
	 */
	//public static final String KEY_CONFIG_IMSERVER_ID = "imserver.id";
	//public static final String KEY_CONFIG_IMSERVER_ADDR = "imserver.addr";
	public static final String KEY_CONFIG_MCM_SERVER_ID = "mcmserver.id";
	/**
	 * The KEY of Properties
	 */
	public static final String KEY_CONFIG_PROPERTIES = "config.properties";
	public static final String KEY_BROKER_PROPERTIES = "broker.properties";
	public static final String KEY_CASSANDRA_PROPERTIES = "cassandra.properties";
	
	public static final String SCRIPT_DIR_NAME = "scriptDirName";
	public static final String LOCAL_DIR_NAME = "localDirName";
	public static final String ERROR_FILE_NAME = "ErrorCode";
	public static final String UNCHECKRIGHT_NAME = "UnCheckRight";
	public static final String MDN_BLACK_LIST_NAME = "NumberBlackList";

	public static final String SCRIPT_DIR_MATCH = File.separator + "script" + File.separator;
	
	/**
	 * 推送包体的阀值(byte)
	 */
	public static final int PUSH_SMALL_MSG_LENGTH = 320;
	
	/**
	 * MCM消息类型
	 * **/
	public static final int PUSH_MCM_QUEUE = 126;

	/**
	 * 推送消息方式 8: 通知 9: 消息  
	 */
	public static final int PUSH_TYPE_NOTIFY = 18;
	
	public static final int PUSH_TYPE_MSG = 19;
	
	/**
	 * messageID生成规则
	 * */
	public static final int SESSION_ID_LENGTH=8;
	public static final int MESSAGE_ID_RANDOM_LENGTH=3;
	
	public static final int MESSAGE_START_ASK_TYPE=0;
	public static final int MESSAGE_END_ASK_TYPE=100;
	public static final int MESSAGE_AS_SEND_TYPE=53;
	
	public static final int MCM_USERACCOUNTLENGTH = 64;
	public static final int MCM_OSUNITYACCOUNTLENGTH = 64;
	
	/**
	 * view.xml中定义的视图名称
	 */
	public static final String JSON_VIEW_NAME = "jsonView";
	/**
	 * view.xml中定义的视图名称
	 */
	public static final String XML_VIEW_NAME = "cloudcom";
	/**
	 * XML根元素
	 */
	public static final String ROOT_ELEMENT = "Response";
	
	public static final int MSG_TYPE_IM = 1;	//普通消息类型(包含群组消息)
	public static final int MSG_TYPE_GROUP = 2;	//群组管理类消息类型
	public static final int MSG_TYPE_MONITOR = 3;	//监听类消息（管理者监听座席与用户会话）
	public static final String RESTSN = "restserial";
	
	/**
	 * 结果集为空
	 */
	public static final int DB_EMPTY = -999;
	public static final int DB_COUNT_EMPTY = 0;
	
	public static final int ISMCM = 1;
	
	/****
	 * 多渠道错误码
	 * ***/
	public static final String ERROR_MCM_MCMDAtA_EMPTY = "540000";
	public static final String ERROR_MCM_OSUNITYACCOUNT_EMPTY = "540001";
	public static final String ERROR_MCM_MSGDAtA_EMPTY = "540002";
	public static final String ERROR_MCM_UNSUPPORT_AGENT = "540003";
	public static final String ERROR_MCM_SERVER_ISSUE = "540004";
	
	public static final String ERROR_MCM_RESPONSE_APPID_EMPTY = "540005"; // appId为空
	public static final String ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY = "540006"; // userAccount为空
	public static final String ERROR_MCM_RESPONSE_USERACCOUNT_OUT_BAND = "540007"; // userAccount长度超出限制
	public static final String ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY = "540008"; // osUnityAccount为空
	public static final String ERROR_MCM_RESPONSE_OSUNITYACCOUNT_OUT_BAND = "540009"; // osUnityAccount长度超出限制
	
	public static final String ERROR_MCM_RESPONSE_MSGDATA_SIZE_ZERO = "540010"; //msgDate 不存在或size为0
	public static final String ERROR_MCM_CALL_MANAGER_AGENTINFO_EMPTY = "540011";
	public static final String ERROR_MCM_RESPONSE_APPINFO_EMPTY = "540012";
	public static final String ERROR_MCM_PROTOBUF_IOEXCEPTION = "540013";
	public static final String ERROR_MCM_RECEIVE_MAIL_PARSE_ERROR = "540014";
	
	public static final String ERROR_MCM_MAILGW_CONFIG_ERROR = "540015";
	public static final String ERROR_MCM_MAILGW_SEND_ERROR = "540016";
	public static final String ERROR_MCM_MAILGW_URL_DECODE_ERROR = "540017";
	public static final String ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR = "540018";
	public static final String ERROR_CALCULATE_FILE_SERVER_SIG = "540019";   //计算文件服务器sig值错误
	
	public static final String ERROR_MCM_MAIL_COFIG_PARAM_ERROR = "540020";
	public static final String ERROR_MCM_GENESYS_AGENTID_EMPTY_ERROR = "540021"; // agentId为空
	public static final String ERROR_MCM_GENESYS_PALCEID_EMPTY_ERROR = "540022"; // placeID为空
	public static final String ERROR_MCM_GENESYS_DN_EMPTY_ERROR = "540023"; // Dn为空
	public static final String ERROR_MCM_GENESYS_MSGJSONDATA_PARSER_ERROR = "540024"; // 解析msgJsonData异常
	
	public static final String ERROR_MCM_GENESYS_INTERACTIONID_EMPTY_ERROR = "540025"; // interactionId为空
	public static final String ERROR_MCM_GENESYS_CALLED_EMPTY_ERROR = "540026"; //called为空
	public static final String ERROR_MCM_GENESYS_CONNECT_INTERACTION_SERVER_FAIL = "540027"; //连接interaction server fail
	public static final String ERROR_MCM_UNKNOWN_EVENT = "540028"; //未知的mcm event
	public static final String ERROR_MCM_IM_AGENTINFO_EMPTY_ERROR = "540029"; //获取的agentInfo信息为空
	
	public static final String ERROR_MCM_IM_USER_DIALOG_NOT_EXIST = "540030"; //用户会话状态为空
	public static final String ERROR_MCM_IM_AGENT_DIALOG_NOT_EXIST = "540031"; //座席会话状态为空
	public static final String ERROR_MCM_IM_AGENT_NOT_ONLINE = "540032"; //座席会话状态为空
	public static final String ERROR_MCM_IM_TRANSFER_QUEUE_QUEUETYPE_EMPTY = "540033"; //转接参数为空（agentId 和 queueType 同时为空）
	public static final String ERROR_MCM_IM_FORCE_END_SERVICE_RESP_SID_EMPTY = "540034"; // 管理者强制座席与用户会话响应错误，sid为空
	
	public static final String ERROR_MCM_IM_FORCE_END_SERVICE_RESP_M3CSEVENT_EMPTY = "540035"; // 管理者强制座席与用户会话响应错误，m3csEvent为空
	public static final String ERROR_MCM_IM_FORCE_END_SERVICE_SUPER_AGENTID_EMPTY = "540036"; // 管理者强制座席与用户会话响应错误，superAgentId为空
	public static final String ERROR_MCM_IM_MONITOR_AGENT_ID_NOT_EXIST = "540037"; //用户会话状态为空
	public static final String ERROR_MCM_IM_AGENT_TRANS_AGENT_ID_EMPTY = "540038"; //转接坐席Id为空
	public static final String ERROR_MCM_IM_AGENT_SUPER_AGENT_ID_EMPTY = "540039"; //转接坐席Id为空
	
	public static final String ERROR_MCM_IM_AGENT_CCP_CUSTOM_DATA_EMPTY = "540040"; //ccpCustomData为空
	public static final String ERROR_MCM_IM_AGENT_RESERVED_KEY_EMPTY = "540041"; //ReservedKey为空
	public static final String ERROR_MCM_IM_AGENT_SER_WITH_THE_USER_CHAN_TYPE_EMPTY = "540042"; //chanType为空
	
	public static final String ERROR_MCM_IM_APP_ATTRS_EMPTY = "540043"; //app_Attrs为空
	public static final String ERROR_MCM_IM_ISAGENT_EMPTY_OR_ZERO = "540044"; //未开通坐席业务
	public static final String ERROR_MCM_IM_7MOOR_LOGIN = "540045"; //7moor登录错误
	public static final String ERROR_MCM_IM_7MOOR_LOGIN_INFO_EMPTY = "540046"; //7moor登录信息为空
	public static final String ERROR_MCM_IM_7MOOR_SEND_MSG = "540047"; //7moor发送消息错误
	public static final String ERROR_MCM_IM_7MOOR_GET_INVESTIGATE =  "540048"; //7moor获取用户满意度
	
	public static final String ERROR_MCM_IM_CCS_BODY_EMPTY =  "540049"; //请求body为空
	
	public static final String TEMP_ROUTE_KEY_PREFIX = "k030"; //redis中，临时路由数据key的前缀

	public static final String HTTP_PROTOCOL_PREFIX = "http://";	
	public static final String URI_CALL_MANAGER = "/mcgwmsgnotify";	
	public static final String URI_RM_SERVER_GET_CM_ROUTE = "/GetCmRoute";	
	
	public static final String CONNECTOR_QUERE_NAME_PREFIX = "YTX_CONNECTOR_QUEUE_";

	public static final String MAILGW_INTERFACE_SEND_MAIL = "sendMail";
	public static final String MAILGW_INTERFACE_ADD_UPDATE_MAIL = "addOrUpdateMail";
	public static final String MAILGW_INTERFACE_DELETE_MAIL = "deleteMail";

	public static final String CONNECTOR_ID_CONSTANTS_MAIL = "00000"; //邮件网关发送的邮件消息，connectorId为常量
	public static final String CONNECTOR_ID_CONSTANTS_WEIXIN = "11111"; //微信网关发送的微信消息，connectorId为常量

	public static final String DATA_FORMAT_JSON="json";
	public static final String DATA_FORMAT_XML="xml";
	public static final String DATA_FORMAT_NONE="none";
	public static final String MCM_MESSAGE_XML_REQUEST_ROOT_ELEMENT_NAME="MCM";
	public static final String MCM_MESSAGE_XML_RESPONSE_ROOT_ELEMENT_NAME="OSSendMessage";

	public static final String MAILGW_CONFIG_TYPE_ADD_OR_UPDATE="1";
	public static final String MAILGW_CONFIG_TYPE_DELETE="2";
	public static final String MAILGW_CONFIG_ACTION_NAME_ADDUPDATE="addOrUpdateMail";
	public static final String MAILGW_CONFIG_ACTION_NAME_DELETE="deleteMail";
	public static final String MAILGW_CONFIG_ACTION_NAME_SEND="sendMail";

	public static final String MAILGW_CONFIG_URL_SUFFIX="/config";
	public static final String MAILGW_SEND_URL_SUFFIX="/send";
	public static final String WEIXINGW_SEND_URL_SUFFIX="/send";
	public static final String CUSTOM_MAP_KEY_HTTP_RESPONSE_STATUSCODE="statusCode";
	public static final String CUSTOM_MAP_KEY_HTTP_RESPONSE_CONTENT="content";
	public static final String CUSTOM_MAP_KEY_HTTP_RESPONSE_DATAFORMAT="dataFormat";
	
	//redis 存储微信配置公众号数据的key编码
	public static final String WEIXIN_CONFIG_REDIS_KEY = "k028";	
	
	//redis 存储邮箱配置公众号数据的key编码
	public static final String MAIL_CONFIG_REDIS_KEY = "k029";
	
	//未开通座席默认返回消息内容
	public static final String MSG_CONTENT_NOT_SUPPORT_CUSTOMER_SERVICE = "在线客服暂未开放，即将上线，敬请关注。";
	
	/**
	 * 终端类型 1: Android Phone 2: iPhone  10: iPad  11: Android Pad  20: PC  (Just Allowed phone 2 PC(Pad) login) 21: HTML5 WEBSOCKET
	 */
	public static final int DEVICE_ANDROIDPHONE = 1;
	public static final int DEVICE_IPHONE = 2;
	public static final int DEVICE_IPAD = 10;
	public static final int DEVICE_ANDROIDPAD = 11;
	public static final int DEVICE_PC = 20;
	public static final int DEVICE_HTML5WEBSOCKET = 21;
	
	/**
	 * 用户注册成功后，异步发出个登录成功消息开关
	 */
	public static final String  REGISTER_MESSAGE_SWITCH_ON = "1";	//开
	public static final String  REGISTER_MESSAGE_SWITCH_OFF = "2";	//关	
	public static final int PROTOTYPE_REPLY_LOGIN_AND_LOGOUT = 76; //proto type 76
	
	/**
	 * genesys config
	 */
	public static final String CHAT_TYPE = "chat";
	
	public static final String VIDYO_TYPE = "vidyo";
	
	public static final String CHAT_VIDYO_TYPE = "chat,vidyo";
	
	public static final String VOICE_TYPE = "voice";
	
	public static final int M3C_SERIAL = 54001;
	
	public static Map<String, Channel> channelMap = new HashMap<String, Channel>();
	
	public static final int SDK_RETURN_RESULT_SUCCESS = 200;

	public static final long DIALOG_VALID_TIME = 60 * 60 * 6;

	public static final String OS_UNITY_ACCOUNT = "KF10089";
	
	public static final String OS_SYSTEM_ACCOUNT = "system";

	public static final String OSSETSERVICEMODE_AS = "0";
	public static final String OSSETSERVICEMODE_IM = "1";
	public static final String OSSETSERVICEMODE_ROBOT = "2";
	

	public static final String AS_ACTION_START_MESSAGE = "startmessage";
	public static final String AS_ACTION_STOP_MESSAGE = "stopmessage";
	public static final String AS_ACTION_OSSETSERVICEMODE = "OSSetServiceMode";
	public static final String AS_ACTION_CBNOTIFY_SETMODE = "cbnotify_SetMode";

	public static  final String AS_FLAG_YES = "1";
	public static  final String AS_FLAG_NO = "2";
	
	public static final int NORMAL_AGENT_TYPE = 0; // 普通坐席
	public static final int MONITOR_AGENT_TYPE = 1; // 坐席监控者
	
	public static final int IS_NOT_RESERVED = 0; // 未预留
	public static final int IS_RESERVED = 1; // 预留
	
	public static final String MESSAGE_WRITE_SEQUENCE = "1"; //默认写cassandra

	public static final int MESSAGE_TTL_SECONDS = 604800; //消息redis有效时间

	public static final int CCS_TYPE_CCP = 0; 
	public static final int CCS_TYPE_GENESYS = 1; 
	public static final int CCS_TYPE_CISCO = 2; 
	public static final int CCS_TYPE_AVAYA = 3; 
	public static final int CCS_TYPE_7MOOR = 4; 
	
	public static final int SEVEN_MOOR_LOGIN_EXPIRE = 60 * 60 * 24;

	public static final String SEVEN_MOOR_ACTION_NAME_SDK_NEWMSG = "sdkNewMsg";
	public static final String SEVEN_MOOR_ACTION_NAME_SDK_GETMSG = "sdkGetMsg";
	public static final String SEVEN_MOOR_ACTION_NAME_SDK_LOGIN = "sdkLogin";
	public static final String SEVEN_MOOR_ACTION_NAME_SDK_GET_INVESTIGATE = "sdkGetInvestigate";
	public static final String SEVEN_MOOR_ACTION_NAME_SDK_BEGIN_NEW_CHAT_SESSION = "sdkBeginNewChatSession";
	public static final String SEVEN_MOOR_ACTION_NAME_NEW_MESSAGE = "NewMessage";
	public static final String SEVEN_MOOR_ACTION_NAME_SDK_SUBMIT_INVESTIGATE = "sdkSubmitInvestigate";
	public static final String SEVEN_MOOR_ACTION_NAME_NEW_SYS_MESSAGE = "NewSysMessage";	
	public static final String SEVEN_MOOR_ACTION_NAME_CONNECTION_BREAK = "connectionBreak";
	
	
	public static final String SEVEN_MOOR_MSG_TYPE_TEXT = "text";
	public static final String SEVEN_MOOR_MSG_TYPE_IMAGE = "image";
	public static final String SEVEN_MOOR_MSG_TYPE_VOICE = "voice";
	
	public static final String SEVEN_MOOR_SYS_MSG_TYPE_FINISH = "finish";
	
	public static final String CONNECTOR_QUEUE = "YTX_CONNECTOR_QUEUE";
	
	public static final String SIPGW_QUEUE = "YTX_SIPGW_QUEUE";
	
	public static final String KEY_PREFIX_PREFIX = "ytx042"; //保存消息key值
	
	public static final String KEY_SEPARATOR = "|";
	
	/**
	 * cc
	 */
	public static final String CC_RESPONSE_CODE = "code";
	public static final String CC_RESPONSE_DATA = "data";
}
