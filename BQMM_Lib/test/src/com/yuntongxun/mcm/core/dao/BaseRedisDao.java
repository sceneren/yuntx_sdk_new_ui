package com.yuntongxun.mcm.core.dao;

import java.util.List;
import java.util.Map;

import redis.clients.jedis.Response;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.core.exception.CCPRedisException;

/**
 * @author chao
 */
public interface BaseRedisDao {

	/**
	 * 批量KEY+1
	 * 
	 * @param keys
	 * @return
	 */
	public List<Object> incrBatch(List<String> keys);
	
	/**
	 * 根据key查询redis上的value
	 * 
	 * @param key
	 *            key值
	 * @return
	 */
	String getRedisValue(String key) throws CCPRedisException;

	/**
	 * 保存redis数据
	 * 
	 * @param key
	 *            key值
	 * @param value
	 *            value值
	 * @param expire
	 *            过期时间，单位秒
	 */
	void saveRedisValue(String key, String value, long expire) throws CCPRedisException;

	/**
	 * 保存redis数据
	 * 
	 * @param key
	 *            key值
	 * @param value
	 *            value值
	 */
	void saveRedisValue(String key, String value) throws CCPRedisException;

	/**
	 * redis数据原子+1操作
	 * 
	 * @param key
	 *            key值
	 */
	Long incrRedis(String key) throws CCPRedisException;

	/**
	 * 删除redis 数据
	 * 
	 * @param key
	 * @throws CCPDaoException
	 */
	void deleteRedisValue(final String key) throws CCPRedisException;

	/**
	 * 获取key过期时间
	 * 
	 * @param key
	 * @throws CCPDaoException
	 */
	Long getRedisTTL(final String key) throws CCPRedisException;

	public List<Response<Long>> incrBatch(String userAcc, int size);

	public List<String> mGetRedisValue(List<String> keys) throws CCPRedisException;
	
	public void mset(Map<byte[], byte[]> msetMap) throws CCPRedisException;
}
