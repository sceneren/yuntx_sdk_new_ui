package com.yuntongxun.mcm.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class RMServerErrorCode {

	public static Map<String, String> errorCodeMap(){
		Map<String, String> errorCodeMap = new HashMap<String, String>();
		/*errorCodeMap.put("104000", Constants.ERROR_MCM_IM_NO_OPNE_CALL_CENTER_BUSINESS); // 未开通呼叫中心业务
		errorCodeMap.put("104015", Constants.ERROR_MCM_IM_QUEUE_NOT_EXIST); // 队列不存在
		errorCodeMap.put("104024", Constants.ERROR_MCM_IM_AGENT_READY); // 座席已就绪
		errorCodeMap.put("104058", Constants.ERROR_MCM_IM_AGENT_LINE_STATUS_BUSY); // 座席线路忙状态 自动就绪未强制
		errorCodeMap.put("104008", Constants.ERROR_MCM_IM_AGENT_LOCKING); // 座席锁定中，自动就绪未强制
		
		errorCodeMap.put("104009", Constants.ERROR_MCM_IM_AGENT_IN_CALL); // 座席通话中，自动就绪未强制
		errorCodeMap.put("108026", Constants.ERROR_MCM_IM_AGENT_ALREADY_WORK); // 坐席已上班 
		errorCodeMap.put("108027", Constants.ERROR_MCM_IM_AGENT_NOT_FOUNT); // 座席未找到
		errorCodeMap.put("108028", Constants.ERROR_MCM_IM_AGENT_LOCKING_OR_IN_CALL); // 座席被锁定或通话中，不可以下班
		errorCodeMap.put("104022", Constants.ERROR_MCM_IM_AGENT_NUMBER_IS_NULL); // 座席号码为空
		
		errorCodeMap.put("104075", Constants.ERROR_MCM_IM_AGENT_NO_PREPARE_READY); //座席未准备就绪
		*/
		
		errorCodeMap.put("000000", "请求成功");  
		errorCodeMap.put("104000", "未开通呼叫中心业务");  
		errorCodeMap.put("104015", "队列不存在");
		errorCodeMap.put("104024", "座席已就绪");
		errorCodeMap.put("104058", "座席线路忙状态 自动就绪未强制");
		errorCodeMap.put("104008", "座席锁定中，自动就绪未强制");
		
		errorCodeMap.put("104009", "座席通话中，自动就绪未强制");
		errorCodeMap.put("108026", "坐席已上班"); 
		errorCodeMap.put("108027", "座席未找到");
		errorCodeMap.put("108028", "座席被锁定或通话中，不可以下班");
		errorCodeMap.put("104022", "座席号码为空"); 
		
		errorCodeMap.put("104075", "座席未准备就绪");
		
		return errorCodeMap;
	}

	public static String getErrorCodeByRMServerCodeNo(String statusCode){
		Map<String, String> map = errorCodeMap();
		
		String result = map.get(statusCode);
		if(StringUtils.isBlank(result)){
			result = statusCode;
		}
		
		return result;
	}
}
