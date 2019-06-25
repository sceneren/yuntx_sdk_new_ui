package com.yuntongxun.mcm.mcm.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.dispatcher.ReceiveAsDispatcher;
import com.yuntongxun.mcm.mcm.form.McmInfoForm;
import com.yuntongxun.mcm.mcm.form.ResponseMessage;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

@Controller
public class MCMReceiveController {

	public static final Logger logger = LogManager
			.getLogger(MCMReceiveController.class);

	@Autowired
	private ReceiveAsDispatcher asDispatcher;

	/**
	 * 接收从rest返回的As消息
	 * 
	 * @param body
	 *            XML格式的请求BODY
	 * 
	 * @return
	 * @throws CCPDaoException
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/mcm/receiveASMCM")
	@ResponseBody
	public String receiveMsg(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response)
			throws CCPDaoException {
		ResponseMessage resp = new ResponseMessage();
		// 将xml转化成对象
		McmInfoForm mcm = McmInfoForm.prase(body);
		String appId = mcm.getAppId();
		if (appId == null || appId.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_APPID_EMPTY);
			return resp.toString();
		}
		String userAccount = mcm.getUserAccount();
		if (userAccount == null || userAccount.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
			return resp.toString();
		}

		if (StringUtil.length(userAccount) > Constants.MCM_USERACCOUNTLENGTH) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_OUT_BAND);
			return resp.toString();
		}

		String osUnityAccount = mcm.getOsUnityAccount();

		if (osUnityAccount == null || osUnityAccount.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
			return resp.toString();
		}

		if (StringUtil.length(osUnityAccount) > Constants.MCM_OSUNITYACCOUNTLENGTH) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
			return resp.toString();
		}

		// //处理从rest返回的As消息
		asDispatcher.addTask(mcm);

		resp.setStatusCode(Constants.RESPONSE_OK);
		return resp.toString();
	}

	/**
	 * 接收七陌发送的消息
	 * 
	 * @param body
	 *            XML格式的请求BODY
	 * 
	 * @return
	 * @throws CCPDaoException
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/2013-12-26/Accounts/{accountSid}/mcm/sendmcm")
	@ResponseBody
	public String receiveMsg(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("accountSid") String accountSid,
			@RequestParam("sig") String sig) throws CCPDaoException {
		ResponseMessage resp = new ResponseMessage();
		// 将xml转化成对象
		McmInfoForm mcm = McmInfoForm.prase(body);
		String appId = mcm.getAppId();
		if (appId == null || appId.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_APPID_EMPTY);
			return resp.toString();
		}
		String userAccount = mcm.getUserAccount();
		if (userAccount == null || userAccount.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_EMPTY);
			return resp.toString();
		}

		if (StringUtil.length(userAccount) > Constants.MCM_USERACCOUNTLENGTH) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_USERACCOUNT_OUT_BAND);
			return resp.toString();
		}

		String osUnityAccount = mcm.getOsUnityAccount();

		if (osUnityAccount == null || osUnityAccount.equals("")) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
			return resp.toString();
		}

		if (StringUtil.length(osUnityAccount) > Constants.MCM_OSUNITYACCOUNTLENGTH) {
			resp.setStatusCode(Constants.ERROR_MCM_RESPONSE_OSUNITYACCOUNT_EMPTY);
			return resp.toString();
		}

		asDispatcher.addTask(mcm);

		resp.setStatusCode(Constants.RESPONSE_OK);
		return resp.toString();
	}

}
