/**
 * 
 */
package com.yuntongxun.mcm.dao.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Response;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.dao.VersionDao;

public class VersionDaoImpl implements VersionDao {

	public static final Logger logger = LogManager.getLogger(VersionDaoImpl.class);

	private BaseRedisDao baseRedisDao;

	@Override
	public List<Object> getBatchMessageVersion(List<String> userAcc) throws CCPRedisException {
		return baseRedisDao.incrBatch(userAcc);
	}

	@Override
	public long getMessageVersion(String userAcc) throws CCPRedisException {
		long incr = baseRedisDao.incrRedis(KEY_MESSAGE_VERSION_PREFIX + userAcc);
		logger.debug("[{}] InstantMessage Version: " + incr);
		return incr;
	}

	/**
	 * @return the baseRedisDao
	 */
	public BaseRedisDao getBaseRedisDao() {
		return baseRedisDao;
	}

	/**
	 * @param baseRedisDao
	 *            the baseRedisDao to set
	 */
	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

	@Override
	public List<Response<Long>> getBatchMessageVersionByOneUserAcc(int size, String userAcc) 
			throws CCPRedisException {
		return baseRedisDao.incrBatch(userAcc,size);
	}

}
