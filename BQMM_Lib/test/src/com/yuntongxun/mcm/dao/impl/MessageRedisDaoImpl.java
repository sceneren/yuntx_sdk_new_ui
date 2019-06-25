package com.yuntongxun.mcm.dao.impl;

import org.ming.sample.util.JSONUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.dao.MessageRedisDao;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.util.Constants;

public class MessageRedisDaoImpl implements MessageRedisDao{

	private BaseRedisDao baseRedisDao;
	
	@Override
	public void saveMessage(String userAcc, MessageInfo messageInfo) throws CCPRedisException {
		StringBuilder tempKey = new StringBuilder();
		tempKey.append(Constants.KEY_PREFIX_PREFIX)
		   .append(Constants.KEY_SEPARATOR)
		   .append(userAcc)
		   .append(Constants.KEY_SEPARATOR)
		   .append(messageInfo.getVersion());
		
		String key = tempKey.toString();
		String value = JSONUtil.object2json(messageInfo);
		
		baseRedisDao.saveRedisValue(key, value, Constants.MESSAGE_TTL_SECONDS);
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

}
