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
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.form.ResponseMessage;
import com.yuntongxun.mcm.mcm.form.WeiXinGWResponseMessage;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.service.WeiXinGWService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.PrintUtil;

@Controller
@RequestMapping("/weixin")
public class MCMWeiXinGWController {

	public static final Logger logger = LogManager.getLogger(MCMWeiXinGWController.class);

	@Autowired
	private WeiXinGWService weixinGWService;
	
	
	/**
	 * 接收微信网关推送的消息
	 * 数据结构：
	 * {
		    "MCMEvent": "*******",
		    "openID": "*******",
		    "msgType": "*******",
		    "userID": "*******",
		    "createTime": "*******",
		    "content": "*******",
		    "msgId": "*******"
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/push")
	@ResponseBody
	public String receiveWeiXinGWMsg(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		
		WeiXinGWResponseMessage resp=new WeiXinGWResponseMessage();
		try {
			ThreadContext.push(request.getSession().getId());
			PrintUtil.printStartTag("Receive Wechat Msg");
			body = URLDecoder.decode(URLDecoder.decode(body, "UTF-8"), "UTF-8");
			logger.info("weixingw push a message to mcmgw,message body:"+body);
			//将json转化成对象
	        McmWeiXinMsgInfo requestData = (McmWeiXinMsgInfo)JSONObject.toBean(JSONObject.fromObject(body),McmWeiXinMsgInfo.class);

	        //如果EventKey有值，将mcmEvent给修改为当前的EventKey值
//	        if(StringUtils.isNotEmpty(requestData.getEventKey())){
//	        	if(MCMEventDefInner.UserEvt_StartAsk_VALUE == Integer.parseInt(requestData.getEventKey())){
//	        		requestData.setMCMEvent(Integer.parseInt(requestData.getEventKey()));
//	        	}
//	        }
	        
	        weixinGWService.handleWeiXinMsg(requestData);
			resp.setStatusCode(Constants.RESPONSE_OK);
			resp.setSuccess(true);
		} catch (CCPServiceException e) {
			resp.setStatusCode(e.getErrorCode());
			resp.setMessage(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			resp.setStatusCode(Constants.ERROR_MCM_MAILGW_URL_DECODE_ERROR);
			resp.setMessage(e.getMessage());
			resp.setSuccess(false);
		} finally{
			PrintUtil.printEndTag("Receive Wechat Msg");
			ThreadContext.removeStack();
		}
		return resp.toJson();
	}
	
	/**
	 * 发送微信接口
	 * 数据结构：
	 * {
		    "userID": "*******",
		    "openID": "*******",
		    "msgType": "xx", 
		    "content": "*******",
		    "filePath": "*******"
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/send")
	@ResponseBody
	public String sendWeiXin(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		ResponseMessage resp=new  ResponseMessage();
		try {
			ThreadContext.push(request.getSession().getId());
			PrintUtil.printStartTag("Send Wechat Msg");
			
			//将json转化成对象
			Map<String,Object> classMap = new HashMap<String,Object>();
			McmWeiXinMsgInfo requestData = (McmWeiXinMsgInfo)JSONObject.toBean(JSONObject.fromObject(body),McmWeiXinMsgInfo.class , classMap);
	        weixinGWService.sendWeiXinMsg(requestData);
			resp.setStatusCode(Constants.RESPONSE_OK);
			
		} catch (Exception e) {
			resp.setStatusCode(Constants.ERROR_MCM_MAILGW_URL_DECODE_ERROR);
			resp.setMessage(e.getMessage());
		} finally{
			PrintUtil.printEndTag("Send Wechat Msg");
			ThreadContext.removeStack();
		}
		
		return resp.toString();
	}
	
}
