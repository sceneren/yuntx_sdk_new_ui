package com.yuntongxun.mcm.dao.impl;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.json.JSONUtil;

import com.yuntongxun.mcm.core.dao.BaseRedisDao;
import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.dao.UserRedisDao;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.RedisKeyConstant;

public class UserRedisDaoImpl implements UserRedisDao {

	public static final Logger logger = LogManager.getLogger(UserRedisDaoImpl.class);
	
	private BaseRedisDao baseRedisDao;

	@Override
	public List<UserLoginInfo> getUserLoginInfo(String userAcc) throws CCPRedisException {
		List<UserLoginInfo> userLoginInfoList = null;
		
		List<String> mgetKeyList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_ANDROIDPHONE);
		mgetKeyList.add(sb.toString());
		
		sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_IPHONE);
		mgetKeyList.add(sb.toString());
		
		sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_ANDROIDPAD);
		mgetKeyList.add(sb.toString());
		
		sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_IPAD);
		mgetKeyList.add(sb.toString());
		
		sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_PC);
		mgetKeyList.add(sb.toString());
		
		sb = new StringBuilder();
		sb.append(RedisKeyConstant.YTX_USER_LOGIN);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(userAcc);
		sb.append(RedisKeyConstant.REDIS_SEPARATOR).append(Constants.DEVICE_HTML5WEBSOCKET);
		mgetKeyList.add(sb.toString());
		
		List<String> valueList = baseRedisDao.mGetRedisValue(mgetKeyList);
		
		if(valueList!=null){
			userLoginInfoList = new ArrayList<UserLoginInfo>();
			UserLoginInfo tempLoginInfo = null;
			for(String jsonStr:valueList){
				if(StringUtils.isNotEmpty(jsonStr)){
					tempLoginInfo = (UserLoginInfo)JSONUtil.jsonToObj(jsonStr, UserLoginInfo.class);
					userLoginInfoList.add(tempLoginInfo);
				}
			}
		}
		return userLoginInfoList;
	}

	public void setBaseRedisDao(BaseRedisDao baseRedisDao) {
		this.baseRedisDao = baseRedisDao;
	}

}
