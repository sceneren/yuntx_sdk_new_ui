package com.yuntongxun.mcm.mcm.dao;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.model.M3CSMessageHistory;

public interface M3CSMessageHistoryDao {

	/**
	* @Description: 插入客服用户历史消息记录
	* @param m3csMessageInfo 
	* @throw CCPDaoException
	 */
	void insert(M3CSMessageHistory m3csMessageHistory) throws CCPDaoException; 
	
}
