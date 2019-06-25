package com.yuntongxun.mcm.mcm.cache;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;
import org.yuntongxun.tools.cache.EhCacheManager;

import com.yuntongxun.mcm.model.AppAttrs;

public class AppAttrsCache {

	public static final Logger logger = LogManager.getLogger(AppAttrsCache.class);
	
	private EhCacheManager cacheManager; 
	
	/**
	 * @Description: 添加缓存信息。
	 * @param key
	 * @param value
	 * @return
	 */
	public synchronized void put(String key, String value) {
		cacheManager.putDataInCache("appAttrsCache", key, value);
	}
	
	/**
	 * @Description: 获取缓存中的信息。
	 * @param key
	 * @return
	 */
	public synchronized String get(String key) {
		Object temp = cacheManager.getCacheData("appAttrsCache", key);
		if(temp != null){
			return (String)temp;
		}
		return null;
	}
	
	/**
	 * @Description: 根据key获取app缓存信息
	 * @param key
	 * @return appAttrs
	 */
	public AppAttrs getAppAttrs(String key){
		String value = get(key);
		try {
			if(StringUtils.isNotBlank(value)){
				AppAttrs AppAttrs = (AppAttrs)JSONUtil.jsonToObj(value, AppAttrs.class);
				return AppAttrs;
			}
		} catch (Exception e) {
			logger.error("getAppAttrs#error()");
			return null;
		}
		
		return null;
	}

	public void setCacheManager(EhCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
}
