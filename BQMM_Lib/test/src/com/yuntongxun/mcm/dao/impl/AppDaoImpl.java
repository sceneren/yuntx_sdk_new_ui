package com.yuntongxun.mcm.dao.impl;

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
import com.yuntongxun.mcm.dao.AppDao;
import com.yuntongxun.mcm.model.AppAttrs;
import com.yuntongxun.mcm.util.ConverterUtil;

public class AppDaoImpl implements AppDao {

	public static final Logger logger = LogManager.getLogger(AppDaoImpl.class);

	private BaseCassandraDao baseCassandraDao;
	
	public PreparedStatement ytx_apps_select_statement;

	public void init() throws CCPCassandraDaoException {
		// prepare select ytx_apps
		Select ytx_apps_select = QueryBuilder.select("appattrs").from("ytx_apps");
		ytx_apps_select.setConsistencyLevel(ConsistencyLevel.ONE);
		Select.Where where = ytx_apps_select.where(QueryBuilder.eq("appkey", QueryBuilder.bindMarker()));
		ytx_apps_select_statement = baseCassandraDao.prepare(where);
	}

	@Override
	public AppAttrs getAppAttrsByAppkey(String appKey) throws CCPCassandraDaoException {
		BoundStatement bindStatement = ytx_apps_select_statement.bind(appKey);
		logger.debug("[" + ytx_apps_select_statement.getQueryString() + "]");
		try {
			ResultSet results = baseCassandraDao.execute(bindStatement);
			if (results != null) {
				Row row = results.one();
				if (row != null) {
					Map<String, String> values = row.getMap(0, String.class, String.class);
					if (values != null) {
						return (AppAttrs) ConverterUtil.populateResult(values, AppAttrs.class);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e);
			throw new CCPCassandraDaoException(e);
		}
		return null;
	}

	public void setBaseCassandraDao(BaseCassandraDao baseCassandraDao) {
		this.baseCassandraDao = baseCassandraDao;
	}

}
