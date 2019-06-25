package com.yuntongxun.mcm.dao;

import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.model.AppAttrs;

public interface AppRedisDao {

	/**
	 * @Description: 获取App信息 
	 * @param appKey
	 * @throws CCPRedisException
	 */
	public AppAttrs getAppAttrsByAppkey(String appKey) throws CCPRedisException;
	
}
