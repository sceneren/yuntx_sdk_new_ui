package com.yuntongxun.mcm.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.yuntongxun.mcm.core.dao.BaseCassandraDao;
import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;
import com.yuntongxun.mcm.dao.UserDao;
import com.yuntongxun.mcm.mcm.model.UserLoginInfo;
import com.yuntongxun.mcm.util.ConverterUtil;

public class UserDaoImpl implements UserDao {

	public static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

	private BaseCassandraDao baseCassandraDao;

	public PreparedStatement ytx_user_login_list_select_statement;

	public void init() throws CCPCassandraDaoException {
		// prepare select ytx_user_login
		Select ytx_user_login_list_select = QueryBuilder.select("userinfo").from("ytx_user_login");
		ytx_user_login_list_select.setConsistencyLevel(ConsistencyLevel.ONE);
		Select.Where ytx_user_login_list_where = ytx_user_login_list_select.where(QueryBuilder.eq("useracc", QueryBuilder.bindMarker()));
		ytx_user_login_list_select_statement = baseCassandraDao.prepare(ytx_user_login_list_where);
		
	}

	@Override
	public List<UserLoginInfo> getUserLoginInfo(String userAcc) throws CCPCassandraDaoException {
		BoundStatement bindStatement = ytx_user_login_list_select_statement.bind(userAcc);
		logger.debug("[" + ytx_user_login_list_select_statement.getQueryString() + "]");
		try {
			ResultSet results = baseCassandraDao.execute(bindStatement);
			if (results != null) {
				List<Row> rows = results.all();
				if (rows != null && rows.size() > 0) {
					ArrayList<UserLoginInfo> userLoginList = new ArrayList<UserLoginInfo>();
					for (int i = 0; i < rows.size(); i++) {
						Row row = rows.get(i);
						Map<String, String> values = row.getMap(0, String.class, String.class);
						UserLoginInfo userLogin = (UserLoginInfo) ConverterUtil.populateResult(values,
								UserLoginInfo.class);
						userLoginList.add(userLogin);
					}
					return userLoginList;
				}
			}
		} catch (Exception e) {
			logger.error("getUserLoginInfo#error()", e);
			throw new CCPCassandraDaoException(e);
		}
		return null;
	}

	public void setBaseCassandraDao(BaseCassandraDao baseCassandraDao) {
		this.baseCassandraDao = baseCassandraDao;
	}

}
