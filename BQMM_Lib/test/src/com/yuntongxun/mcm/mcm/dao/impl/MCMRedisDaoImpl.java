package com.yuntongxun.mcm.mcm.dao.impl;

import java.util.List;

import org.ming.sample.util.JSONUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.dao.MCMDao;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.util.Constants;

public class MCMRedisDaoImpl implements MCMDao{

	public static final String KEY_PREFIX_PREFIX = "ytx042";
	public static final String KEY_SEPARATOR = "|";

	private BaseRedisDao baseRedisDao;
	
	@Override
	public void saveInstantMessage(String userAcc, long version, MessageInfo messageInfo, 
			boolean async) throws CCPDaoException {
		String key = getMessageKey(userAcc, String.valueOf(version));
		String value = JSONUtil.object2json(messageInfo);
		baseRedisDao.saveRedisValue(key, value, Constants.MESSAGE_TTL_SECONDS);
	}

	@Override
	public void saveInstantMessage(String receiver, MessageInfo messageInfo)
			throws CCPDaoException {
		String key = getMessageKey(receiver, String.valueOf(messageInfo.getVersion()));
		String value = JSONUtil.object2json(messageInfo);
		baseRedisDao.saveRedisValue(key, value, Constants.MESSAGE_TTL_SECONDS);
	}

	@Override
	public void saveBatchInstantMessage(String receiver,
			List<MessageInfo> msgInfoList) throws CCPDaoException {
		
	}

	/**
	 * 根据版本号获取存储redis中的消息key
	 * key:ytx042|userAcc|version
	 * value:{"dateCreated":"1450942745417","expired":"604800000","fileUrl":"","groupId":"","localFileName":"","mcmEvent":"0","msgCompressLen":"0","msgContent":"MjM0","msgDomain":"","msgFileName":"","msgFileSize":"","msgId":"DE423A8996C8B9B00C654678E7024CB5|6","msgLength":"24","msgReceiver":"54321","msgSender":"12345","msgType":"1","version":"2"}
	 * @param userAcc
	 * @param version
	 * @return
	 */
	private String getMessageKey(String userAcc,String version){
		StringBuilder key = new StringBuilder();
		key.append(KEY_PREFIX_PREFIX).append(KEY_SEPARATOR).append(userAcc).append(KEY_SEPARATOR).append(version);
		return key.toString();
	}
	
	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}
	
}
