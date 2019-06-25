package com.yuntongxun.mcm.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.json.JSONUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.dao.AppRedisDao;
import com.yuntongxun.mcm.mcm.cache.AppAttrsCache;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.util.RedisKeyConstant;

public class AppRedisDaoImpl implements AppRedisDao {

	public static final Logger logger = LogManager.getLogger(AppRedisDaoImpl.class);
	
	private BaseRedisDao baseRedisDao;
	
	private AppAttrsCache appAttrsCache; 

	@Override
	public AppAttrs getAppAttrsByAppkey(String appKey) throws CCPRedisException {
		AppAttrs appAttrs = appAttrsCache.getAppAttrs(appKey);
		if(appAttrs == null){
			logger.debug("from ehcache get appAttrs is null, start by redis.");
			
			String key = RedisKeyConstant.YTX_APPS + RedisKeyConstant.REDIS_SEPARATOR + appKey;
			logger.debug("from redis get appAttrs, key: {}",key);
			
			String jsonStr = baseRedisDao.getRedisValue(key);
			logger.debug("from redis get appAttrs, value: {}",jsonStr);
			
			if(StringUtils.isNotBlank(jsonStr)){
				try {
					appAttrs = (AppAttrs)JSONUtil.jsonToObj(jsonStr, AppAttrs.class); 
				} catch (Exception e) {
					logger.error("getAppAttrsByAppkey#error()", e);
					return null;
				}
				
				appAttrsCache.put(appKey, jsonStr);
			}
			
			if(appAttrs == null){
				logger.warn("from redis get appAttrs is null.");
			}
			
		} else{
			logger.debug("from ehcache get appAttrs is not null.");
		}
		
		return appAttrs;
	}

	/**
	 * set inject 
	 */
	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	public void setAppAttrsCache(AppAttrsCache appAttrsCache) {
		this.appAttrsCache = appAttrsCache;
	}
	
}
