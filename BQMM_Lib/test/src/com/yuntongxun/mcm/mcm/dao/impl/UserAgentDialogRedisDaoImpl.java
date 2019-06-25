package com.yuntongxun.mcm.mcm.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.json.JSONUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.mcm.dao.UserAgentDialogRedisDao;
import com.yuntongxun.mcm.mcm.model.AgentInfo;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;
import com.yuntongxun.mcm.util.RedisKeyConstant;

public class UserAgentDialogRedisDaoImpl implements UserAgentDialogRedisDao{

	public static final Logger logger = LogManager.getLogger(UserAgentDialogRedisDaoImpl.class);
	
	private BaseRedisDao baseRedisDao;
	
	@Override
	public void saveDialog(String userAcc, UserAndAgentDialog userAndAgentDialog, long ttl)
			throws CCPRedisException {
		String value = JSONUtil.object2json(userAndAgentDialog);
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG + userAcc;
		logger.info("save user agent dialog, key: {}.", key);
		logger.info("save user agent dialog, value: {}.", value);
		
		baseRedisDao.saveRedisValue(key, value, ttl);
	}

	@Override
	public void deleteDialog(String userAcc) throws CCPRedisException {
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG + userAcc;
		logger.info("delete user agent dialog, key: {}", key);
		
		baseRedisDao.deleteRedisValue(key);
	}

	@Override
	public UserAndAgentDialog getDialog(String userAcc) throws CCPRedisException {
		UserAndAgentDialog userAndAgentDialog = null;
		
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG + userAcc;
		String value = baseRedisDao.getRedisValue(key);
		logger.info("get user agent dialog, key: {}.", key);
		logger.info("get user agent dialog, value: {}.", value);
		
		if(StringUtils.isNotEmpty(value)){
			try {
				@SuppressWarnings("rawtypes")
				Map<String, Class> classMap = new HashMap<String, Class>();
				classMap.put("agentInfoSet", AgentInfo.class);
				
				userAndAgentDialog = (UserAndAgentDialog)JSONUtil.jsonToObj(value, UserAndAgentDialog.class, classMap);
			} catch (Exception e) {
				logger.info("getDialog fail, parse value error.", e);
				return null;
			}
		}
		
		return userAndAgentDialog;
	}

	@Override
	public void saveDialogSid(String sid, String value, long ttl)
			throws CCPRedisException {
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG_SID + sid;
		logger.info("save user agent dialog sid, key: {}.", key);
		logger.info("save user agent dialog sid, value: {}.", value);
		
		baseRedisDao.saveRedisValue(key, value, ttl);
	}
	
	@Override
	public void deleteDialogSid(String sid) throws CCPRedisException {
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG_SID + sid;
		logger.info("delete user agent dialog sid, key: {}", key);
		
		baseRedisDao.deleteRedisValue(key);
	}
	
	@Override
	public String getUserAccBySid(String sid) throws CCPRedisException {
		String key = RedisKeyConstant.YTX_USER_AGENT_DIALOG_SID + sid;
		String value = baseRedisDao.getRedisValue(key);
		logger.info("get userAcc by sid, key: {}.", key);
		logger.info("get userAcc by sid, value: {}", value);
		
		return value;
	}

	/**
	 * set inject
	 */
	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

}
