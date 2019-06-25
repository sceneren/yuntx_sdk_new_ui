package com.yuntongxun.mcm.cc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.cc.form.WakeupUserForm;
import com.yuntongxun.mcm.cc.service.ICCService;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;
import com.yuntongxun.mcm.util.StringUtil;

@Controller
@RequestMapping("/mcm/ccs/")
public class CCSController {

	public static final Logger logger = LogManager.getLogger(CCSController.class);
	
	@Autowired
	private ICCService ccService;
	
	/**
	 * @Description: 唤醒用户
	 * @param body
	 * @param request
	 * @param response
	 * @throws CCPServiceException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "wakeupuser")
	@ResponseBody
	public String wakeUpUser(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
		ThreadContext.push(request.getSession().getId());
		String errorCode = Constants.RESPONSE_OK;
		PrintUtil.printStartTag("wakeupuser");
		
		try {
			Object obj = parseBody(body);
			if(obj != null){
				WakeupUserForm wakeupUserForm = (WakeupUserForm) obj;
				ccService.wakeUpUser(wakeupUserForm);
			} else {
				return Constants.ERROR_MCM_IM_CCS_BODY_EMPTY;
			}
			
		} catch (Exception e) {
			if(e instanceof CCPServiceException) {
				errorCode = ((CCPServiceException) e).getErrorCode();
			} else {
				errorCode = Constants.ERROR_MCM_SERVER_ISSUE;
			}
			logger.error("wakeUpUser#error()", e);
		} finally {
			PrintUtil.printEndTag("wakeupuser");
			ThreadContext.removeStack();
		}
		
		String httpResponse = StringUtil.getHttpResponse(errorCode, null);
		logger.info("httpResponse: {}.", httpResponse);
		
		return httpResponse;
	}

	/**
	 * @Description: 解析响应内容
	 * @param body 
	 * @throws CCPServiceException
	 */
	private Object parseBody(String body) throws CCPServiceException{
		WakeupUserForm wakeupUserForm = null;
		if(StringUtils.isBlank(body)){
			throw new CCPServiceException(Constants.ERROR_MCM_IM_CCS_BODY_EMPTY);
		}
		
		logger.info("body: {}.", body);
		
		try {
			JSONObject jsonObject = JSONObject.fromObject(body);
			if(jsonObject.has(Constants.CC_RESPONSE_CODE)) {
				String code = jsonObject.getString(Constants.CC_RESPONSE_CODE);
				if(code.equals(Constants.RESPONSE_OK) && jsonObject.has(Constants.CC_RESPONSE_DATA)){
					String data = jsonObject.getString(Constants.CC_RESPONSE_DATA);
					if(StringUtils.isNotBlank(data)){
						wakeupUserForm = (WakeupUserForm)JSONUtil.jsonToObj(data, WakeupUserForm.class);
					} 
				} 
			}
		} catch (Exception e) {
			logger.error("parseBody#error()", e);
		}
		
		if(wakeupUserForm == null){
			logger.info("parser get wakeupUserForm is null.");
			throw new CCPServiceException(Constants.ERROR_MCM_IM_CCS_BODY_EMPTY);
		}
		
		return wakeupUserForm;
	}
}
