package com.yuntongxun.mcm.sevenmoor.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.sevenmoor.model.TransferData;
import com.yuntongxun.mcm.sevenmoor.service.SevenMoorService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.JsonUtils;
import com.yuntongxun.mcm.util.PrintUtil;

@Controller
@RequestMapping("/7moor")
public class SevenMoorController {

	public static final Logger logger = LogManager.getLogger(SevenMoorController.class);

	@Autowired
	private SevenMoorService sevenMoorService;
	
	/**
	 * 接收七陌客服发送消息通知
	 * 
	 * @param body json格式的请求BODY
	 *            
	 * @return
	 * @throws CCPDaoException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/action")
	@ResponseBody
	public String action(@RequestBody String body, HttpServletRequest request, HttpServletResponse response) {
		TransferData resp = new TransferData();
		try {
			ThreadContext.push(request.getSession().getId());
			PrintUtil.printStartTag("Receive 7moor client service msg");
			
			body = URLDecoder.decode(URLDecoder.decode(body, "UTF-8"), "UTF-8");
			
			if("\n".equals(body.substring(body.length() - 2, body.length() - 1))) {
				body = body.substring(0,body.length()-2);
			}
			
			logger.info("message body: {}.", body);
			
			//将json转化成对象
			TransferData requestData = (TransferData)JsonUtils.jsonToObj(body, TransferData.class);
			sevenMoorService.processAction(requestData);
			
			resp.setStatusCode(Constants.RESPONSE_OK);
			resp.setSuccess(true);
		} 
		catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException");
			resp.setStatusCode(Constants.ERROR_MCM_MAILGW_URL_DECODE_ERROR);
			resp.setMessage(e.getMessage());
			resp.setSuccess(false);
		} 
		catch (Exception e) {
			logger.error("SevenMoorController#error()",e);
			resp.setMessage(e.getMessage());
		}  
		finally{
			PrintUtil.printEndTag("Receive 7moor client service msg");
			ThreadContext.removeStack();
		}
		
		return resp.toRespJson();
	}
}
