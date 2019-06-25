package com.yuntongxun.mcm.weixin.util;
/**
 * 项目：ECMCMServer
 * 描述：微信业务常量类
 * 创建人：weily
 * 创建时间：2015年8月5日 上午11:59:15 
 */
public class WeiXinConstant {

	//获取accessToken,get请求：https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET
	public final static String URL_GET_ACCESS_TOKEN = "https://api.weixin.qq.com/cgi-bin/token";

	//发送消息,post请求：https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN
	public final static String URL_SEND_MSG = "https://api.weixin.qq.com/cgi-bin/message/custom/send";
	
	//新增临时素材，post/form请求：https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE
	public final static String URL_UPLOAD_TEMP_FILE = "https://api.weixin.qq.com/cgi-bin/media/upload";
	
	//获取临时素材，get请求：https://api.weixin.qq.com/cgi-bin/media/get?access_token=ACCESS_TOKEN&media_id=MEDIA_ID
	public final static String URL_GET_TEMP_FILE = "https://api.weixin.qq.com/cgi-bin/media/get";
	
	//新增永久素材，post请求：https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN
	public final static String URL_UPLOAD_PERMANENT_FILE = "https://api.weixin.qq.com/cgi-bin/material/add_material";
	
	//获取永久素材，post请求：https://api.weixin.qq.com/cgi-bin/material/get_material?access_token=ACCESS_TOKEN
	public final static String URL_GET_PERMANENT_FILE = "https://api.weixin.qq.com/cgi-bin/material/get_material";
	
	

}
