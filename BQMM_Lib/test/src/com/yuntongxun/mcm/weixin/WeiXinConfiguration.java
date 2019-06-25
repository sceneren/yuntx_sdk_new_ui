package com.yuntongxun.mcm.weixin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONObject;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.util.Constants;

public class WeiXinConfiguration {
	
	public static final Logger logger = LogManager.getLogger(WeiXinConfiguration.class);
	
	private static WeiXinConfiguration instance = null; 

	private static String xmlFileName = "weixin-account-config.xml";
	
	private static String xmlFilePath = null;
	
	private static Map<String,WeiXinConfigData> publicAccMap = null;
	
	private BaseRedisDao baseRedisDao;

	private String moduleCode;
	
	public static  WeiXinConfiguration getInstance(){
		if(instance==null){
			instance = new WeiXinConfiguration();
			instance.init();
			return instance;
		}else{
			return instance;
		}
	}
	
	//初始化微信配置数据，先从redis获取，若获取到，初始化到内存对象，并同步至xml文件；若无法从redis获取，从xml文件获取
	public void init(){
		try {

			//获取xml文件路径，例：file:/D:/deploy/apache-tomcat-7.0.62-luna/wtpwebapps/ECMCMServer/WEB-INF/classes/
			String configFilePath = this.getClass().getResource("/").toString();
			configFilePath = configFilePath.substring(6);
			xmlFilePath = configFilePath + xmlFileName;
						
			String configKey = Constants.WEIXIN_CONFIG_REDIS_KEY+moduleCode;
			String weixinAccountsStr = baseRedisDao.getRedisValue(configKey);
			//从redis初始化数据到内存，并同步至xml文件
			if(StringUtils.isNotEmpty(weixinAccountsStr)){
				String[] weixinAccountArray = weixinAccountsStr.split(",");
				JSONObject weixinJsonObj = null;
				String weixinJsonStr = null;
				if(weixinAccountArray!=null&&weixinAccountArray.length>0){
					rebuildXmlDataFile();
					publicAccMap = new Hashtable<String,WeiXinConfigData>();
					WeiXinConfigData configData = null;
					for(String account:weixinAccountArray){
						configData = new WeiXinConfigData();
						weixinJsonStr = baseRedisDao.getRedisValue(account);
						weixinJsonObj = new JSONObject(weixinJsonStr);
						configData.setOpenID(weixinJsonObj.getString("wx_account"));
						configData.setAppID(weixinJsonObj.getString("wxappID"));
						configData.setAppSecret(weixinJsonObj.getString("wxappSecret"));
						configData.setRonglianAppId(weixinJsonObj.getString("appid"));
						publicAccMap.put(weixinJsonObj.getString("wx_account"), configData);
						addAccountToXml(configData);
					}
				    logger.info("init weixin account data from redis success.");
				}
			}else{
			//从xml初始化数据到内存
				logger.info("can't read weixin account data from redis,init data from xml.");
				initAllXmlData();
			}
			instance = this;
		} catch (ParseException e) {
			logger.error("init weixin account config error:"+e.getMessage());
		}
		
	}
	
	 /**
     * 添加账号信息
     * @author weily
     * @param data
     * @return
     */
   public static boolean addAccountToXml(WeiXinConfigData data){
       SAXReader saxReader = new SAXReader();
       try {
           Document document = saxReader.read(new File(xmlFilePath));
           Element root =  document.getRootElement();
           Element account = root.addElement("publicAccount");
           account.addAttribute("accountId",data.getOpenID());
           
           Element attr = account.addElement("openID");
           attr.setText(data.getOpenID());
           attr = account.addElement("appID");
           attr.setText(data.getAppID());
           attr = account.addElement("appSecret");
           attr.setText(data.getAppSecret());
           
           attr = account.addElement("ronglianAppId");
           attr.setText(data.getRonglianAppId());
           
           attr = account.addElement("accessToken");
           attr.setText("");

           attr = account.addElement("tokenExpirationTime");
           attr.setText("");
           
           OutputFormat format = OutputFormat.createPrettyPrint();
           format.setEncoding("UTF-8");

           XMLWriter writer = new XMLWriter(new FileWriter(new File(xmlFilePath)),format);
           writer.write(document);
           writer.flush();
           writer.close();
	       logger.info("add weixin account to xml success,account:"+data.getOpenID());
       } catch (Exception e) {
           logger.error("add weixin account to xml file error:"+e.getMessage());
           return false;
       }
       return true;
   }
   
   /**
    * 内存中添加公众号配置数据
    * @param data
    * @return
    */
   public static boolean addAccountToCache(WeiXinConfigData data){
	   if(publicAccMap==null){
		   publicAccMap = new Hashtable<String,WeiXinConfigData>();
	   }
	   publicAccMap.put(data.getOpenID(), data);
	   return true;
   }
   
   /**
    * 获取所有xml中的微信公众号数据
    * @return
    */
   public Map<String,WeiXinConfigData> initAllXmlData() {
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(new File(xmlFilePath));
			String xpath = "/configWeiXin/publicAccount";
			publicAccMap = new Hashtable<String, WeiXinConfigData>();
			List list = document.selectNodes(xpath);
			for (int i = 0; i < list.size(); i++) {
				Element account = (Element) list.get(i);
				WeiXinConfigData accountData = new WeiXinConfigData();
				Element attr = account.element("openID");
				accountData.setOpenID(attr.getText());
				
				attr = account.element("appID");
				accountData.setAppID(attr.getText());
				
				attr = account.element("appSecret");
				accountData.setAppSecret(attr.getText());
				
				attr = account.element("ronglianAppId");
				accountData.setRonglianAppId(attr.getText());
				
				attr = account.element("accessToken");
				accountData.setAccessToken(attr.getText());
				
				attr = account.element("tokenExpirationTime");
				accountData.setTokenExpirationTime(attr.getText());
				publicAccMap.put(accountData.getOpenID(), accountData);
			}
	        logger.info("init weixin account xml data success.");
		} catch (Exception e) {
	        logger.error("init all xml weixin data error:"+e.getMessage());
		}
		return publicAccMap;
	}
   
   /**
    * 删除账号信息
    * @param accountId
    * @return
    */
   public static boolean removeAccountFromXml(String accountId){
       SAXReader saxReader = new SAXReader();
       try{
           Document document = saxReader.read(new File(xmlFilePath));
           String xpath = "/configWeiXin/publicAccount[@accountId='"+ accountId +"']";
           List list = document.selectNodes(xpath);
           if(list.size() == 1){
               Element account = (Element) list.get(0);
               Element root = document.getRootElement();
               root.remove(account);
           } else {
               logger.info("find multi accountId["+ accountId +"] from data.....");
               for(int i=0;i<list.size();i++){
                   Element account = (Element) list.get(i);
                   Element root = document.getRootElement();
                   root.remove(account);
               }
           }

           OutputFormat format = OutputFormat.createPrettyPrint();
           format.setEncoding("UTF-8");

           XMLWriter writer = new XMLWriter(new FileWriter(new File(xmlFilePath)),format);
           writer.write(document);
           writer.close();
           writer.flush();
           logger.info("delete weixin account success,account:"+accountId);
       } catch (Exception e){
           logger.error("remove weixin account from xml error:"+e.getMessage());
           return false;
       }
       return true;
   }
   
   /**
    * 从内存中删除公众号数据
    * @param accountId
    * @return
    */
   public static boolean removeAccountFromCache(String accountId){
	   if(publicAccMap!=null&&publicAccMap.get(accountId)!=null){
		   publicAccMap.remove(accountId);
	   }
	   return true;
   }
   
   /**
    * 更新账号信息
    * @param data
    * @return
    */
   public static boolean updateAccountToXml(WeiXinConfigData data){
       SAXReader saxReader = new SAXReader();
       try{
           Document document = saxReader.read(new File(xmlFilePath));
           String xpath = "/configWeiXin/publicAccount[@accountId='"+ data.getOpenID() +"']";
           List list = document.selectNodes(xpath);
           if(list.size() == 1){
               Element account = (Element) list.get(0);          
               Element attr = account.element("openID");
               attr.setText(data.getOpenID());
               
               if(StringUtils.isNotEmpty(data.getAppID())){
	               attr = account.element("appID");
	               attr.setText(data.getAppID());
               }
               
               if(StringUtils.isNotEmpty(data.getAppSecret())){
                   attr = account.element("appSecret");
                   attr.setText(data.getAppSecret());   
               }
                
               if(StringUtils.isNotEmpty(data.getAccessToken())){
                   attr = account.element("accessToken");
                   attr.setText(data.getAccessToken());
               }
               
               if(StringUtils.isNotEmpty(data.getTokenExpirationTime())){
                   attr = account.element("tokenExpirationTime");
                   attr.setText(data.getTokenExpirationTime());
               }
               
               if(StringUtils.isNotEmpty(data.getRonglianAppId())){
                   attr = account.element("ronglianAppId");
                   attr.setText(data.getRonglianAppId());
               }
               

               OutputFormat format = OutputFormat.createPrettyPrint();
               format.setEncoding("UTF-8");

               XMLWriter writer = new XMLWriter(new FileWriter(new File(xmlFilePath)),format);
               writer.write(document);
               writer.flush();
               writer.close();
               logger.info("update weixin account success,account:"+data.getOpenID());
           } else {
               logger.error("find more than one accountId["+ data.getOpenID()+"] from data.....");
               return false;
           }
       } catch (Exception e ){
           logger.error("update weixin account error:"+e.getMessage());
           return false;
       }
       return true;
   }
   
   /**
    * 更新内存中的公众号数据
    * @return
    */
   public static void updateAccountToCache(WeiXinConfigData configData){
	   if(publicAccMap==null){
		   publicAccMap = new Hashtable<String,WeiXinConfigData>();
	   }
	   publicAccMap.put(configData.getOpenID(), configData);
   }
   
   /**
    * 重新创建xml文件
    * @return
    */
   public static boolean rebuildXmlDataFile(){
	  	  File file = new File(xmlFilePath);
	  	  //若配置文件存在，先删除
	  	  if(file.exists()){
	  		  file.delete();
	  	  }
	  	  FileWriter writer = null;
	  	  //创建新文件
	  	  try {
	            file.createNewFile();
	            Document document = DocumentHelper.createDocument();
	            document.addElement("configWeiXin");
	            writer = new FileWriter(file);
	            document.write(writer);
	            logger.info("rebuild xml data file success.");
	        } catch (IOException e) {
				logger.error("rebuild data file error："+e.getMessage());
	            return false;
	        }finally{
	      	  try {
	      		  if(writer!=null){
	      			  writer.close();
	      		  }
				} catch (IOException e) {
					logger.error("rebuild data file error："+e.getMessage());
				}
	        }
	  	  return true;
	  }

	public BaseRedisDao getBaseRedisDao() {
		return baseRedisDao;
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public Map<String, WeiXinConfigData> getPublicAccMap() {
		return publicAccMap;
	}

	public void setPublicAccMap(Map<String, WeiXinConfigData> publicAccMap) {
		this.publicAccMap = publicAccMap;
	}

}
