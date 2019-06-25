package com.yuntongxun.mcm.dao;

import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;
import com.yuntongxun.mcm.model.AppAttrs;

public interface AppDao {

	/**
	 * @Description: 获取App信息
	 * @param appKey 
	 * @throws CCPCassandraDaoException
	 */
	AppAttrs getAppAttrsByAppkey(String appKey) throws CCPCassandraDaoException;

}
