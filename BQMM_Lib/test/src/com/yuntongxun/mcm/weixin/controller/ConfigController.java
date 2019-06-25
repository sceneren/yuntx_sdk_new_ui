package com.yuntongxun.mcm.weixin.controller;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.form.WeiXinGWResponseMessage;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.weixin.WeiXinConfigData;
import com.yuntongxun.mcm.weixin.WeiXinConfiguration;

@Controller("weixinConfigController")
@RequestMapping("/weixin/config")
public class ConfigController {

	public static final Logger logger = LogManager.getLogger(ConfigController.class);

	/**
	 * 新增微信公众号
	 * 数据结构：
	 * {
		    "openID": "*******",
		    "appID": "*******",
		    "appSecret": "*******",
		    "ronglianAppId": "*******"
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/add")
	@ResponseBody
	public String add(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		WeiXinGWResponseMessage resp=new WeiXinGWResponseMessage();
		try {
			WeiXinConfigData configData = (WeiXinConfigData) JSONObject.toBean(JSONObject.fromObject(body), WeiXinConfigData.class);
			WeiXinConfiguration weiXinConfig = WeiXinConfiguration.getInstance();
			if(StringUtils.isEmpty(configData.getOpenID())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("openID is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getAppID())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("appID is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getAppSecret())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("appSecret is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getRonglianAppId())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("ronglianAppId is null");
				return resp.toJson();
			}
			

			WeiXinConfiguration.getInstance();
			WeiXinConfiguration.addAccountToXml(configData);
			WeiXinConfiguration.addAccountToCache(configData);
	        
			resp.setStatusCode(Constants.RESPONSE_OK);
			resp.setSuccess(true);
		} catch (Exception e) {
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
		} 
		return resp.toJson();
	}
	
	/**
	 * 更新微信公众号
	 * 数据结构：
	 * {
		    "openID": "*******",
		    "appID": "*******",
		    "appSecret": "*******",
		    "ronglianAppId": "*******"
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/update")
	@ResponseBody
	public String update(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		WeiXinGWResponseMessage resp=new WeiXinGWResponseMessage();
		try {
			WeiXinConfigData configData = (WeiXinConfigData) JSONObject.toBean(JSONObject.fromObject(body), WeiXinConfigData.class);
			if(StringUtils.isEmpty(configData.getOpenID())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("openID is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getAppID())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("appID is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getAppSecret())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("appSecret is null");
				return resp.toJson();
			}
			if(StringUtils.isEmpty(configData.getRonglianAppId())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("ronglianAppId is null");
				return resp.toJson();
			}
			
			WeiXinConfiguration.getInstance();
			WeiXinConfiguration.updateAccountToXml(configData);
			WeiXinConfiguration.updateAccountToCache(configData);
			
			resp.setStatusCode(Constants.RESPONSE_OK);
			resp.setSuccess(true);
		} catch (Exception e) {
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
		} 
		return resp.toJson();
	}
	
	/**
	 * 删除微信公众号
	 * 数据结构：
	 * {
		    "openID": "*******"
		}
	 * @param body json格式的请求BODY
	 * @return
	 * @throws CCPDaoException
	 * @throws UnsupportedEncodingException 
	 */
	
	@RequestMapping(method = RequestMethod.POST, value = "/delete")
	@ResponseBody
	public String delete(@RequestBody String body,
			HttpServletRequest request, HttpServletResponse response){
		WeiXinGWResponseMessage resp=new WeiXinGWResponseMessage();
		try {
			WeiXinConfigData configData = (WeiXinConfigData) JSONObject.toBean(JSONObject.fromObject(body), WeiXinConfigData.class);
			WeiXinConfiguration weiXinConfig = WeiXinConfiguration.getInstance();
			if(StringUtils.isEmpty(configData.getOpenID())){
				resp.setStatusCode(Constants.ERROR_MCM_WEIXIN_COFIG_PARAM_ERROR);
				resp.setSuccess(false);
				resp.setMessage("openID is null");
				return resp.toJson();
			}
			
			WeiXinConfiguration.getInstance();
			WeiXinConfiguration.removeAccountFromXml(configData.getOpenID());
			WeiXinConfiguration.removeAccountFromCache(configData.getOpenID());
			
			resp.setStatusCode(Constants.RESPONSE_OK);
			resp.setSuccess(true);
		} catch (Exception e) {
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
		} 
		return resp.toJson();
	}
	
	
	
}
