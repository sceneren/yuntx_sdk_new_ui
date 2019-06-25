package com.yuntongxun.mcm.dao;

import java.util.List;

import redis.clients.jedis.Response;

import com.yuntongxun.mcm.core.exception.CCPRedisException;

public interface VersionDao {

	// MCM消息
	public static final String KEY_MESSAGE_VERSION_PREFIX = "IM";

	/**
	 * @param userAcc
	 * @return
	 * @throws CCPRedisException
	 */
	List<Object> getBatchMessageVersion(List<String> userAcc) throws CCPRedisException;

	/**
	 * Get Message Version
	 * 
	 * @param userAcc
	 * @return
	 * @throws CCPRedisException
	 */
	long getMessageVersion(String userAcc) throws CCPRedisException;

	/**
	 * @param userAcc
	 * @param size
	 * @return
	 * @throws CCPRedisException
	 */
	List<Response<Long>> getBatchMessageVersionByOneUserAcc(int size, String userAcc) throws CCPRedisException;
	
}
