package com.yuntongxun.mcm.dao;

import java.util.List;

import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;

public interface UserDao {

	/**
	 * @Description: 获取用户登录信息
	 * @param userAcc 
	 * @throws CCPCassandraDaoException
	 */
	public List<UserLoginInfo> getUserLoginInfo(String userAcc) throws CCPCassandraDaoException;
	
}
