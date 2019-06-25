package com.yuntongxun.mcm.mcm.dao;

import java.util.List;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.model.MessageInfo;

public interface MCMDao {

	public void saveInstantMessage(String userAcc, long version, MessageInfo messageInfo, boolean async)
			throws CCPDaoException;

	public void saveInstantMessage(String receiver, MessageInfo messageInfo)
			throws CCPDaoException;
		
	public void saveBatchInstantMessage(String receiver, List<MessageInfo> msgInfoList)
			throws CCPDaoException;

}
