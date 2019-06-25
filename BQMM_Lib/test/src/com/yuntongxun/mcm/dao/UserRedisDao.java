package com.yuntongxun.mcm.dao;

import java.util.List;

import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;

public interface UserRedisDao {

	/**
	 * @Description: 获取用户登录信息
	 * @param userAcc 
	 * @throws CCPRedisException
	 */
	public List<UserLoginInfo> getUserLoginInfo(String userAcc) throws CCPRedisException;
	
}
