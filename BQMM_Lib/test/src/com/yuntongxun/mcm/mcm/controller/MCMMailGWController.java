package com.yuntongxun.mcm.mcm.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.form.ResponseMessage;
import com.yuntongxun.mcm.model.mail.McmMailAttchmentInfo;
import com.yuntongxun.mcm.model.mail.McmMailMsgInfo;
import com.yuntongxun.mcm.service.MailGWService;
import com.yuntongxun.mcm.util.Constants;

@Controller
@RequestMapping("/mail")
public class MCMMailGWController {

	public static final Logger logger = LogManager.getLogger(MCMMailGWController.class);

	@Autowired
	private MailGWService mailService;
	
	/**
	 * 邮箱配置接口
	 * 数据结构：
	 * 新增或更新
	 * {
		    "configType": "1",  1:新增或更新，2：删除
		    "userAccount": "*******",
		    "osUnityAccount": "*******",
		    "appId": "*******",
		    "type": "POP",
		    "password": "*******",
		    "mailDisplayName": "*******",
		    "receiveServer": "*******",
		    "sendServer": "*******",
		    "sendServerPort": "*******"
		    
		}
		删除
	 	{
		    "configType": "2",  1:新增或更新，2：删除
		    "userAccount": "*******",
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/config")
	@ResponseBody
	public String mailConfig(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		ResponseMessage resp=new  ResponseMessage();
		try {
			//将json转化成对象
	        McmMailMsgInfo requestData = (McmMailMsgInfo)JSONObject.toBean(JSONObject.fromObject(body),McmMailMsgInfo.class);
	        
	        //处理从rest返回的As消息
			String result = mailService.handleMailConfig(requestData);
			resp.setStatusCode(result);
		} catch (CCPServiceException e) {
			resp.setStatusCode(e.getErrorCode());
			resp.setMessage(e.getMessage());
		}
		return resp.toString();
	}
	
	/**
	 * 接收邮件消息相关的请求消息
	 * 数据结构：
	 * {
		    "MCMEvent": "*******",
		    "userAccount": "*******",
		    "msgType": "*******",
		    "msgDate": "*******",
		    "msgContent": "*******",
		    "mailTitle": "*******",
		    "mailFromDisplayName": "*******",
		    "attachment":
			 [
		    	{
		    		"fileName":"xxx",
		    		"url":"xxx"
		    	},
		    	{
		    		"fileName":"xxx",
		    		"url":"xxx"
		    	}
			 ]
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/push")
	@ResponseBody
	public String receiveMailGWMsg(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		ResponseMessage resp=new  ResponseMessage();
		try {
			body = URLDecoder.decode(URLDecoder.decode(body, "UTF-8"), "UTF-8");
			//将json转化成对象
			Map<String,Object> classMap = new HashMap<String,Object>();
	        classMap.put("attachment", McmMailAttchmentInfo.class);
	        McmMailMsgInfo requestData = (McmMailMsgInfo)JSONObject.toBean(JSONObject.fromObject(body),McmMailMsgInfo.class , classMap);
	        
	        //处理从rest返回的As消息
			mailService.handleMailMsg(requestData);
			resp.setStatusCode(Constants.RESPONSE_OK);
		} catch (CCPServiceException e) {
			resp.setStatusCode(e.getErrorCode());
			resp.setMessage(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			resp.setStatusCode(Constants.ERROR_MCM_MAILGW_URL_DECODE_ERROR);
			resp.setMessage(e.getMessage());
		}
		return resp.toString();
	}
	
	/**
	 * 发送邮件接口
	 * 数据结构：
	 * {
		    "userAccount": "*******",
		    "appId": "*******",
		    "to": "xx", //收件人 可以多个用逗号（,）分开
		    "cc": "*******",//抄送人可以多个用逗号（,）分开
		    "title": "*******",//邮件标题 需要进行URL编码
		    "content": "*******",//邮件内容 需要进行 url 编码
		    "sendServer": "*******",
		    "sendServerPort": "*******",
		    "signature": "*******", //mailId+password md5 值
		    "attachment":
		    [
		    	{
		    		"fileName":"xxx",
		    		"url":"xxx"
		    	},
		    	{
		    		"fileName":"xxx",
		    		"url":"xxx"
		    	}
			 ]
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/send")
	@ResponseBody
	public String sendMail(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		ResponseMessage resp=new  ResponseMessage();
		//将json转化成对象
		Map<String,Object> classMap = new HashMap<String,Object>();
        classMap.put("attachment", McmMailAttchmentInfo.class);
        McmMailMsgInfo requestData = (McmMailMsgInfo)JSONObject.toBean(JSONObject.fromObject(body),McmMailMsgInfo.class , classMap);
        //处理从rest返回的As消息
		mailService.sendMail(requestData);
		resp.setStatusCode(Constants.RESPONSE_OK);
		return resp.toString();
	}
	
}
