package com.yuntongxun.mcm.mcm.dao;

import com.yuntongxun.mcm.core.exception.CCPRedisException;
import com.yuntongxun.mcm.mcm.model.UserAndAgentDialog;

public interface UserAgentDialogRedisDao {

	/**
	 * @Description: 记录用户会话实时数据，在开启咨询时创建，在结束咨询时删除，设置TTL，超时即删除
	 * @param userAcc 
	 * @param userAndAgentDialog
	 * @param ttl 有效时间 
	 * @throws CCPRedisException
	 */
	void saveDialog(String userAcc, UserAndAgentDialog userAndAgentDialog, long ttl) throws CCPRedisException;

	/**
	 * @Description: 删除会话实时表
	 * @param userAcc   
	 * @throws CCPRedisException
	 */
	void deleteDialog(String userAcc) throws CCPRedisException;

	/**
	 * @Description: 获取用户会话实时数据
	 * @param userAcc 
	 * @throws CCPRedisException
	 */
	UserAndAgentDialog getDialog(String userAcc) throws CCPRedisException;
	
	/**
	 * @Description: 用户会话实时数据反查表，可根据sid反查当前用户会话实时数据
	 * @param sid 会话sessionId
	 * @param value userAcc
	 * @ttl 有效时间 
	 * @throws CCPRedisException
	 */
	void saveDialogSid(String sid, String value, long ttl) throws CCPRedisException;
	
	/**
	 * @Description: 删除用户会话实时数据反查表
	 * @param sid 会话sessionId
	 * @throws CCPRedisException
	 */
	void deleteDialogSid(String sid) throws CCPRedisException;
	
	/**
	 * @Description: 根据会话Id获取用户userAcc
	 * @param sid 会话ID
	 * @throws CCPRedisException
	 */
	String getUserAccBySid(String sid) throws CCPRedisException;
	
}
