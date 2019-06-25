package com.yuntongxun.mcm.core.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.yuntongxun.mcm.core.exception.CCPDaoException;

/**
 * 存储过程直接用call_存储过程名字来命名整个API
 * 
 * @author chao
 * 
 */
public interface BaseJdbcDao {

	static final int PS_STATE_SUCCESS = 0;

	void executeSql(String sql) throws CCPDaoException;

	int update(String sql, Object[] array) throws CCPDaoException;

	int[] batchUpdate(String[] sql) throws CCPDaoException;

	int[] batchUpdate(String sql, BatchPreparedStatementSetter bpss)
			throws CCPDaoException;

	List<?> queryForList(String sql, Class<?> c) throws CCPDaoException;

	List<?> queryForList(String sql, Object[] array, Class<?> c)
			throws CCPDaoException;

	String queryForString(String sql) throws CCPDaoException;

	int queryForInt(String sql) throws CCPDaoException;

	Date queryForDate(String sql) throws CCPDaoException;

	int queryForCount(String sql) throws CCPDaoException;

	List<Map<String, Object>> queryForList(String sql, Object[] array)
			throws CCPDaoException;

	Object queryForObject(String sql, Object[] array, Class<?> c)
			throws CCPDaoException;

	Object queryForObject(String sql, Class<?> c) throws CCPDaoException;

	Map<String, Object> queryForMap(String sql, Object[] obj)
			throws CCPDaoException;

	String queryForJson(String sql, Object[] obj) throws CCPDaoException;

}
