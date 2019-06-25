package com.yuntongxun.mcm.core.dao;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;
import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;

/**
 * 
 * 底层不能设置数据一致性级别
 * 
 * @author chao
 */
public interface BaseCassandraDao {

	/**
	 * 同步操作
	 * 
	 * @param statement
	 * @return
	 * @throws CCPCassandraDaoException
	 */
	PreparedStatement prepare(RegularStatement statement)
			throws CCPCassandraDaoException;

	/**
	 * 同步操作
	 * 
	 * @param statement
	 * @return
	 * @throws CCPCassandraDaoException
	 */
	ResultSet execute(Statement statement) throws CCPCassandraDaoException;

	/**
	 * 同步操作
	 * 
	 * @param cql
	 * @return
	 * @throws CCPCassandraDaoException
	 */
	ResultSet execute(String cql) throws CCPCassandraDaoException;

	/**
	 * 异步操作
	 * 
	 * @param statement
	 * @return
	 * @throws CCPCassandraDaoException
	 */
	ResultSetFuture executeAsync(Statement statement)
			throws CCPCassandraDaoException;

	/**
	 * 异步操作 底层不设置数据一致性级别
	 * 
	 * @param cql
	 * @return
	 * @throws CCPCassandraDaoException
	 */
	ResultSetFuture executeAsync(String cql) throws CCPCassandraDaoException;
}
