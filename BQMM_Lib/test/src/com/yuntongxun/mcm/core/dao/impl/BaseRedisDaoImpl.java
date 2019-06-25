package com.yuntongxun.mcm.core.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.dao.VersionDao;

public class BaseRedisDaoImpl implements BaseRedisDao {

	private static final Logger log = LogManager.getLogger(BaseRedisDaoImpl.class);

	private RedisTemplate<Serializable, Serializable> redisTemplate1;

	private JedisPool jedisPool;

	private int dbIndex = 1;

	public List<Object> incrBatch(List<String> keys) throws CCPRedisException {
		long t = System.currentTimeMillis();
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.select(dbIndex);
			Pipeline pipeline = jedis.pipelined();
			for (String key : keys) {
				pipeline.incr(VersionDao.KEY_MESSAGE_VERSION_PREFIX + key);
			}
			List<Object> data = pipeline.syncAndReturnAll();
			log.info("keys size: [" + keys.size() + "] batch version cost: " + (System.currentTimeMillis() - t) + " ms.");
			return data;
		} catch (Exception ex) {
			throw new CCPRedisException(ex);
		} finally {
			if (jedis != null){
			    jedisPool.returnResource(jedis);
			}
		}
	}

	/**
	 * 根据key查询redis上的value
	 * 
	 * @param key
	 *            key值
	 * @return
	 */
	public String getRedisValue(final String key) throws CCPRedisException {
		try {
			return redisTemplate1.execute(new RedisCallback<String>() {

				@Override
				public String doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] bkey = redisTemplate1.getStringSerializer().serialize(key);
					if (connection.exists(bkey)) {
						byte[] bvalue = connection.get(bkey);
						String value = redisTemplate1.getStringSerializer().deserialize(bvalue);
						log.debug("getRedisValue key: " + key + " value: " + value);
						return value;
					}
					return null;
				}

			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

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
	public void saveRedisValue(final String key, final String value, final long expire) throws CCPRedisException {
		try {
			redisTemplate1.execute(new RedisCallback<Object>() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] bkey = redisTemplate1.getStringSerializer().serialize(key);
					byte[] bvalue = redisTemplate1.getStringSerializer().serialize(value);
					connection.set(bkey, bvalue);
					connection.expire(bkey, expire);
					log.debug("saveRedisValue key: " + key + " value: " + value + " expire: " + expire);
					return null;
				}

			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

	/**
	 * 保存redis数据
	 * 
	 * @param key
	 *            key值
	 * @param value
	 *            value值
	 */
	public void saveRedisValue(final String key, final String value) throws CCPRedisException {
		try {
			redisTemplate1.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] bkey = redisTemplate1.getStringSerializer().serialize(key);
					byte[] bvalue = redisTemplate1.getStringSerializer().serialize(value);
					connection.set(bkey, bvalue);
					log.debug("saveRedisValue key: " + key + " value: " + value);
					return null;
				}

			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

	/**
	 * redis数据原子+1操作
	 * 
	 * @param key
	 *            key值
	 */
	public Long incrRedis(final String key) throws CCPRedisException {

		try {
			return (Long) redisTemplate1.execute(new RedisCallback<Object>() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					long t = System.currentTimeMillis();
					byte[] bkey = redisTemplate1.getStringSerializer().serialize(key);
					Long incr = connection.incr(bkey);
					log.debug("incrRedis key: " + key + "value: " + incr);
					log.info("single version cost: " + (System.currentTimeMillis() - t) + " ms.");
					return incr;
				}

			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

	public void deleteRedisValue(final String key) throws CCPRedisException {
		try {
			redisTemplate1.execute(new RedisCallback<Object>() {
				public Object doInRedis(RedisConnection connection) {
					Long del = connection.del(redisTemplate1.getStringSerializer().serialize(key));
					log.debug("deleteRedisValue key: " + key + " value: " + del);
					return del;
				}
			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

	public Long getRedisTTL(final String key) throws CCPRedisException {
		try {
			return (Long) redisTemplate1.execute(new RedisCallback<Object>() {

				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					byte[] bkey = redisTemplate1.getStringSerializer().serialize(key);
					Long ttl = connection.ttl(bkey);
					log.debug("getRedisTTL key: " + key + " TTL: " + ttl);
					return ttl;
				}

			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

	/**
	 * @return the redisTemplate1
	 */
	public RedisTemplate<Serializable, Serializable> getRedisTemplate1() {
		return redisTemplate1;
	}

	/**
	 * @param redisTemplate1
	 *            the redisTemplate1 to set
	 */
	public void setRedisTemplate1(RedisTemplate<Serializable, Serializable> redisTemplate1) {
		this.redisTemplate1 = redisTemplate1;
	}

	/**
	 * @return the jedisPool
	 */
	public JedisPool getJedisPool() {
		return jedisPool;
	}

	/**
	 * @param jedisPool
	 *            the jedisPool to set
	 */
	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	
	public List<Response<Long> > incrBatch(String userAcc, int size)  throws CCPRedisException {
		long t = System.currentTimeMillis();
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.select(dbIndex);
			Pipeline pipeline = jedis.pipelined();
			List<Response<Long> > versionList = new ArrayList<Response<Long> >();
			Response<Long> resp = null;
			for (int i = 0; i < size; i++) {
				resp = pipeline.incr(VersionDao.KEY_MESSAGE_VERSION_PREFIX + userAcc);
				versionList.add(resp);
			}
			pipeline.sync(); // this sync() isn't block method from source code
			log.info("++++weily++++redis-batch get version-cost time:" + (System.currentTimeMillis() - t) + " ms.");
			return versionList;
		} catch (Exception ex) {
			throw new CCPRedisException(ex);
		} finally {
			if (jedis != null){
			    jedisPool.returnResource(jedis);
			}
		}
	}
	
	@Override
	public List<String> mGetRedisValue(final List<String> keys)
			throws CCPRedisException {
		try {
			return redisTemplate1.execute(new RedisCallback<List<String>>() {
				@Override
				public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
					byte[][] bKeys = new byte[keys.size()][];
					for(int i=0; i<keys.size(); i++){
						byte[] bKey = redisTemplate1.getStringSerializer().serialize(keys.get(i));
						bKeys[i] = bKey;
					}
					List<String> values = new ArrayList<String>();
					List<byte[]> bValues = connection.mGet(bKeys);
					for(int i=0; i<bValues.size(); i++){
						byte[] bValue = bValues.get(i);
						String value = null;
						if(bValue!=null && bValue.length>0){
							value = redisTemplate1.getStringSerializer().deserialize(bValue);
						}
						values.add(value);
					}
					log.debug("getRedisValue key: {} value: {}", keys, values);
					return values;
				}
			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}
	
	@Override
	public void mset(final Map<byte[],byte[]> msetMap)
			throws CCPRedisException {
		try {
			redisTemplate1.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					connection.mSet(msetMap);
					return null;
				}
			});
		} catch (Exception e) {
			throw new CCPRedisException(e);
		}
	}

}
