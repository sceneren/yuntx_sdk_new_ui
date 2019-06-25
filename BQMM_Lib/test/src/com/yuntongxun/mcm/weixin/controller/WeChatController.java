package com.yuntongxun.mcm.weixin.controller;

import java.io.UnsupportedEncodingException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.enumerate.WeiXinMsgTypeEnum;
import com.yuntongxun.mcm.fileserver.service.FileServerService;
import com.yuntongxun.mcm.fileserver.util.FileServerUtils;
import com.yuntongxun.mcm.model.McmWeiXinMsgInfo;
import com.yuntongxun.mcm.weixin.WeiXinPushMsg;
import com.yuntongxun.mcm.weixin.service.MCMWeiXinService;
import com.yuntongxun.mcm.weixin.util.WeiXinUtils;

@Controller
@RequestMapping("/wechat")
public class WeChatController {

	public static final Logger logger = LogManager.getLogger(WeChatController.class);

	@Autowired
	private MCMWeiXinService weiXinService;
	
	@Autowired
	private FileServerService fileServerService;
	/**
	 * 
	 * 验证服务器有效性接口
	 *
	 * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp	时间戳
	 * @param nonce	随机数
	 * @param echostr	随机字符串
	 * @param request
	 * @param response
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "")
	@ResponseBody
	public String verifyMsg(@RequestParam String signature,
						   @RequestParam String timestamp,
						   @RequestParam String nonce,
						   @RequestParam String echostr,
			HttpServletRequest request, HttpServletResponse response){
		String result = null;
		try {
			result = weiXinService.verifyMsg(signature, timestamp, nonce, echostr);
		} catch (CCPServiceException e) {
			logger.error("verify wei xin message error code:"+e.getErrorCode()+",error message:"+e.getMessage());
		}
		return result;
	}
	
	/**
	 * 推送消息（文本消息，语音消息，图片消息）
	 * @param body xml格式的请求BODY,格式参见WeiXinUtils.parsePushMsg()方法注释或微信官方文档
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/pushMsg")
	@ResponseBody
	public String pushMsg(@RequestBody String msgBody,
			   @RequestParam String signature,
			   @RequestParam String timestamp,
			   @RequestParam String nonce,
			   @RequestParam String echostr,
			HttpServletRequest request, HttpServletResponse response){
		try {
			String sessionId = request.getSession().getId();
			ThreadContext.push(sessionId);
			//校验消息真实性
			String verifyResult = weiXinService.verifyMsg(signature, timestamp, nonce, echostr);
			
			if(echostr.equals(verifyResult)){
				//解析消息字符串，构建消息对象
				WeiXinPushMsg pushMsg = WeiXinUtils.parsePushMsg(msgBody, null);
				if(pushMsg != null){
					//处理消息
					weiXinService.pushMsg(pushMsg);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally{
			ThreadContext.removeStack();
		}
		return "";
	}
	
	/**
	 * 推送消息（文本消息，语音消息，图片消息）
	 * @param body xml格式的请求BODY,格式参见WeiXinUtils.parsePushMsg()方法注释或微信官方文档
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/test/sendMsg")
	@ResponseBody
	public String testSendMsg(
			HttpServletRequest request, HttpServletResponse response){
		try {
			McmWeiXinMsgInfo weixinMsg = new McmWeiXinMsgInfo();
			String url = fileServerService.uploadFile(FileServerUtils.getFileServerSig(), "20150314000000110000000000000010", "gh_59458b2faa40", "logo.jpg", "D:/weixin_temp_files/logo.jpg");
			weixinMsg.setOpenID("gh_59458b2faa40");
			weixinMsg.setUserID("oW1BJwGATAtELhr6xtWl4iFdGwG0");
			weixinMsg.setMsgType(WeiXinMsgTypeEnum.IMAGE.getValue());
			weixinMsg.setUrl(url);
			weixinMsg.setContent("test_text_content_中文");
			weiXinService.sendMsg(weixinMsg);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} 
		return "";
	}
	
	
	
}
