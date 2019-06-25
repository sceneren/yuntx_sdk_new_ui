package com.yuntongxun.mcm.core.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.sql.SqlSetupSupport;
import org.ming.sample.util.JSONUtil;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.yuntongxun.mcm.core.dao.BaseJdbcDao;
import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.util.Constants;

public class BaseJdbcDaoImpl extends JdbcDaoSupport implements BaseJdbcDao {
	
	public static final Logger log = LogManager.getLogger(BaseJdbcDaoImpl.class);

	public void executeSql(final String sql) throws CCPDaoException {
		try {
			getJdbcTemplate().execute(sql);
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@Override
	public int update(String sql, Object[] array) throws CCPDaoException {
		int count = 0;
		try {
			if (array == null) {
				count = getJdbcTemplate().update(sql);
			} else {
				count = getJdbcTemplate().update(sql, array);
			}
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
		return count;
	}

	@Override
	public int[] batchUpdate(String[] sql) throws CCPDaoException {
		try {
			return getJdbcTemplate().batchUpdate(sql);
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@Override
	public int[] batchUpdate(String sql, BatchPreparedStatementSetter bpss) throws CCPDaoException {
		try {
			return getJdbcTemplate().batchUpdate(sql, bpss);
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}
	
	@Override
	public List<?> queryForList(String sql, Object[] array, Class<?> c) throws CCPDaoException {
		try {
			List<?> list = (array == null ? getJdbcTemplate().queryForList(sql) : getJdbcTemplate().queryForList(sql,
					array));
			if (list != null && list.size() > 0) {
				return SqlSetupSupport.populateResult(list, c);
			}
			return list;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@Override
	public List<?> queryForList(String sql, Class<?> c) throws CCPDaoException {
		return queryForList(sql, null, c);
	}

	@Override
	public List<Map<String, Object>> queryForList(String sql, Object[] array) throws CCPDaoException {
		List<Map<String, Object>> list = null;
		try {
			if (array == null) {
				list = getJdbcTemplate().queryForList(sql);
			} else {
				list = getJdbcTemplate().queryForList(sql, array);
			}
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
		return list;
	}

	@Override
	public Object queryForObject(final String sql, Object[] array, final Class<?> c) throws CCPDaoException {
		try {
			List<?> list = array == null ? getJdbcTemplate().queryForList(sql) : getJdbcTemplate().queryForList(sql,
					array);
			if (list != null && list.size() > 0) {
				return SqlSetupSupport.populateResult(list, c).get(0);
			}
			return null;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@Override
	public Object queryForObject(String sql, Class<?> c) throws CCPDaoException {
		return queryForObject(sql, null, c);
	}

	@Override
	public Map<String, Object> queryForMap(String sql, Object[] obj) throws CCPDaoException {
		Map<String, Object> map = null;
		List<Map<String, Object>> list = null;
		try {
			if (obj == null) {
				list = getJdbcTemplate().queryForList(sql);
			} else {
				list = getJdbcTemplate().queryForList(sql, obj);
			}
			if (list != null && !list.isEmpty()) {
				map = list.get(0);
			}
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
		return map;
	}

	@Override
	public String queryForJson(String sql, Object[] obj) throws CCPDaoException {
		String json = null;
		List<Map<String, Object>> list = null;
		try {
			if (obj == null) {
				list = getJdbcTemplate().queryForList(sql);
			} else {
				list = getJdbcTemplate().queryForList(sql, obj);
			}
			if (list != null && !list.isEmpty()) {
				json = JSONUtil.list2json(list);
			}
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
		return json;
	}

	public String queryForString(String sql) throws CCPDaoException {
		try {
			return (String) getJdbcTemplate().queryForObject(sql, String.class);
		} catch (IncorrectResultSizeDataAccessException irsae) {
			return null;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	public int queryForInt(String sql) throws CCPDaoException {
		try {
			return getJdbcTemplate().queryForObject(sql, Integer.class);
		} catch (IncorrectResultSizeDataAccessException irsae) {
			return Constants.DB_EMPTY;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	public Date queryForDate(String sql) throws CCPDaoException {
		try {
			return getJdbcTemplate().queryForObject(sql, Date.class);
		} catch (IncorrectResultSizeDataAccessException irsae) {
			return null;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	public int queryForCount(String sql) throws CCPDaoException {
		try {
			return getJdbcTemplate().queryForObject(sql, Integer.class);
		} catch (IncorrectResultSizeDataAccessException irsae) {
			return Constants.DB_COUNT_EMPTY;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	/*@SuppressWarnings("unchecked")
	@Override
	public List<String> call_PS_GET_MOBILES(final String accountId, final int type) throws CCPDaoException {
		try {
			String callsql = "{call PS_GET_MOBILES(?,?,?)}";
			List<String> result = (List<String>) getJdbcTemplate().execute(callsql, new CallableStatementCallback() {

				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
						DataAccessException {
					log.info("'" + accountId + "'," + type + ",@p");
					cs.setString(1, accountId);
					cs.setInt(2, type);
					cs.registerOutParameter(3, Types.TINYINT);
					ResultSet rs = cs.executeQuery();
					try {
						int status = cs.getInt(3);
						if (status == PS_STATE_SUCCESS) {
							ArrayList<String> result = new ArrayList<String>();
							while (rs.next()) {
								result.add(rs.getString("mobile"));
							}
							return result;
						} else {
							throw new CCPRuntimeException(ScriptManager.buildError("111009",
									"call_PS_GET_MOBILES by accountId [" + accountId + "] type [" + type + "] return ["
											+ status + "]"));
						}
					} finally {
						if (!rs.isClosed()) {
							rs.close();
						}
					}
				}
			});
			return result;
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void call_PS_CREATEQUEUE(final String appId, final String queueType, final String typeDes,
			final int maxMember, final String worktime, final String offworkprompt, final String offworkdate,
			final String offworkweekday) throws CCPDaoException {
		try {
			String callsql = "{call PS_CREATEQUEUE_V2 (?,?,?,?,?,?,?,?,?)}";
			getJdbcTemplate().execute(callsql, new CallableStatementCallback() {
				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
						DataAccessException {
					log.info("'" + queueType + "','" + appId + "','" + typeDes + "'," + maxMember + ",'" + worktime
							+ "','" + offworkprompt + "','" + offworkdate + "','" + offworkweekday + "',@p");
					cs.setString(1, queueType);
					cs.setString(2, appId);
					cs.setString(3, typeDes);
					cs.setInt(4, maxMember);
					cs.setString(5, worktime);
					cs.setString(6, offworkprompt);
					cs.setString(7, offworkdate);
					cs.setString(8, offworkweekday);
					cs.registerOutParameter(9, Types.TINYINT);
					cs.executeQuery();
					int status = cs.getInt(9);
					if (status == PS_STATE_SUCCESS) {
						return status;
					} else {
						throw new CCPRuntimeException(ScriptManager.buildError("111009",
								"call_PS_CREATEQUEUE by appId [" + appId + "] queueType [" + queueType + "] typeDes ["
										+ typeDes + "] maxMember [" + maxMember + "]  worktime [" + worktime
										+ "]  offworkprompt [" + offworkprompt + "]  offworkdate [" + offworkdate
										+ "]  offworkweekday [" + offworkweekday + "]  return [" + status + "]"));
					}
				}
			});
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void call_PS_DELETEQUEUE(final String appId, final String queueType) throws CCPDaoException {
		try {
			String callsql = "{call ps_deletequeue(?,?,?)}";
			getJdbcTemplate().execute(callsql, new CallableStatementCallback() {

				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
						DataAccessException {
					log.info("'" + queueType + "','" + appId + "',@p");
					cs.setString(1, queueType);
					cs.setString(2, appId);
					cs.registerOutParameter(3, Types.TINYINT);
					cs.executeQuery();
					int status = cs.getInt(3);
					if (status == PS_STATE_SUCCESS) {
						return status;
					} else {
						throw new CCPRuntimeException(ScriptManager.buildError("111009",
								"call_PS_DELETEQUEUE by appId [" + appId + "] queueType [" + queueType + "] return ["
										+ status + "]"));
					}
				}
			});
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RouteTable> call_PS_GET_CONFIGAGENT_ADDR(final String appId) throws CCPDaoException {
		try {
			String callsql = "{call PS_GET_CONFIGAGENT_ADDR(?,?)}";
			return (List<RouteTable>) getJdbcTemplate().execute(callsql, new CallableStatementCallback<Object>() {

				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
						DataAccessException {
					log.info("'" + appId + "', @p");
					cs.setString(1, appId);
					cs.registerOutParameter(2, Types.TINYINT);
					ResultSet rs = cs.executeQuery();
					try {
						int status = cs.getInt(2);
						if (status == PS_STATE_SUCCESS) {
							List<RouteTable> tableList = new ArrayList<RouteTable>();
							while (rs.next()) {
								String host = rs.getString(1);
								String port = rs.getString(2);
								RouteTable rt = new RouteTable(host, port);
								tableList.add(rt);
							}
							return tableList;
						} else {
							throw new CCPRuntimeException(ScriptManager.buildError("112561",
									"call_PS_GET_CONFIGAGENT_ADDR by appId [" + appId + "] return [" + status + "]"));
						}
					} finally {
						if (!rs.isClosed()) {
							rs.close();
						}
					}
				}
			});
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}

	@Override
	public int call_PS_CHECK_MEDIAUPLOAD_STATE(final String accountSid) throws CCPDaoException {
		try {
			String callsql = "{call PS_CHECK_MEDIAUPLOAD_STATE(?,?,?)}";
			return (Integer) getJdbcTemplate().execute(callsql, new CallableStatementCallback<Object>() {

				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
						DataAccessException {
					log.info("'" + accountSid + "', @p, @u");
					cs.setString(1, accountSid);
					cs.registerOutParameter(2, Types.TINYINT);
					cs.registerOutParameter(3, Types.SMALLINT);
					cs.execute();
					int status = cs.getInt(2);
					if (status == PS_STATE_SUCCESS) {
						return cs.getInt(3);
					} else {
						throw new CCPRuntimeException(ScriptManager.buildError("112561",
								"call_PS_CHECK_MEDIAUPLOAD_STATE by accountSid [" + accountSid + "] return [" + status
										+ "]"));
					}
				}
			});
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}
	
	@Override
	public int call_PS_CHECK_SMSALARM_STATE(final String accountSid) throws CCPDaoException {
		try {
			String callsql = "{call PS_CHECK_SMSALARM_STATE(?,?,?)}";
			return (Integer) getJdbcTemplate().execute(callsql, new CallableStatementCallback<Object>() {
				
				@Override
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException,
				DataAccessException {
					log.info("'" + accountSid + "', @p, @u");
					cs.setString(1, accountSid);
					cs.registerOutParameter(2, Types.TINYINT);
					cs.registerOutParameter(3, Types.SMALLINT);
					cs.execute();
					int status = cs.getInt(2);
					if (status == PS_STATE_SUCCESS) {
						return cs.getInt(3);
					} else {
						throw new CCPRuntimeException(ScriptManager.buildError("112561",
								"call_PS_CHECK_SMSALARM_STATE by accountSid [" + accountSid + "] return [" + status
								+ "]"));
					}
				}
			});
		} catch (Exception e) {
			throw new CCPDaoException(e);
		}
	}*/
}