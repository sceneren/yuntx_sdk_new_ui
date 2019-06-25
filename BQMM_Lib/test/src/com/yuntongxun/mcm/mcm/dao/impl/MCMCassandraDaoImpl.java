package com.yuntongxun.mcm.mcm.dao.impl;

import java.util.List;

import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ming.sample.util.JSONUtil;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.yuntongxun.mcm.core.dao.BaseCassandraDao;
import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;
import com.yuntongxun.mcm.mcm.dao.MCMDao;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.util.Constants;

/**
 * 为了提高性能,Prepare方式预编译语句必须启动时完成。
 * 
 */
public class MCMCassandraDaoImpl implements MCMDao {

	public static final  Logger logger = LogManager.getLogger(MCMCassandraDaoImpl.class);

	private BaseCassandraDao baseCassandraDao;

	public PreparedStatement ytx_instant_message_insert_statement;

	public void init() throws CCPCassandraDaoException {
		Insert ytx_instant_message_insert = QueryBuilder.insertInto("ytx_instant_message").values(
				new String[] { "useracc", "version","type", "messageinfo" },
				new Object[] { QueryBuilder.bindMarker(), QueryBuilder.bindMarker(), QueryBuilder.bindMarker(), 
						QueryBuilder.bindMarker() });
		ytx_instant_message_insert.setConsistencyLevel(ConsistencyLevel.ONE);
		ytx_instant_message_insert_statement = baseCassandraDao.prepare(ytx_instant_message_insert);

	}

	@Override
	public void saveInstantMessage(String userAcc, long version, MessageInfo messageInfo, 
			boolean async) throws CCPCassandraDaoException {
		long t = System.currentTimeMillis();
		BoundStatement statement = ytx_instant_message_insert_statement.bind(userAcc, version, Constants.MSG_TYPE_IM, 
				JSONUtil.objToMap(messageInfo));
		statement.setConsistencyLevel(ConsistencyLevel.ONE);
		if (async) {
			baseCassandraDao.executeAsync(statement);
		} else {
			baseCassandraDao.execute(statement);
		}
		logger.debug("[" + ytx_instant_message_insert_statement.getQueryString() + "]");
		logger.info("pack data:" + (System.currentTimeMillis() - t));
	}

	@Override
	public void saveInstantMessage(String receiver, MessageInfo messageInfo) 
			throws CCPCassandraDaoException {
		long t = System.currentTimeMillis();
		BoundStatement statement = ytx_instant_message_insert_statement.bind(receiver, messageInfo.getVersion(), Constants.MSG_TYPE_IM, 
				JSONUtil.objToMap(messageInfo));
		String data = JSONObject.fromObject(messageInfo).toString();
		statement.setConsistencyLevel(ConsistencyLevel.ONE);
		try {
			baseCassandraDao.execute(statement);
			logger.error("save data to cassandra success,data:"+data);
		} catch (CCPCassandraDaoException e) {
			logger.error("save data to cassandra error,data:"+data+",exception:"+e.getMessage());
		}
		logger.info("[" + ytx_instant_message_insert_statement.getQueryString() + "]");
		logger.info("pack data:" + (System.currentTimeMillis() - t));
	}
	
	@Override
	public void saveBatchInstantMessage(String receiver, List<MessageInfo> msgInfoList)
			throws CCPCassandraDaoException{
		long t1 = System.currentTimeMillis();
		BatchStatement batchStatement = new BatchStatement();
		BoundStatement bindStatement = null;
		for (MessageInfo messageInfo : msgInfoList) {
			bindStatement = new BoundStatement(ytx_instant_message_insert_statement).bind(receiver, messageInfo.getVersion(), 
					Constants.MSG_TYPE_IM, JSONUtil.objToMap(messageInfo));
			batchStatement.add(bindStatement);
		}
		baseCassandraDao.executeAsync(batchStatement);
		logger.info("++++weily++++cassandra-消息存库-cost time:" + (System.currentTimeMillis() - t1)+" ms");
	}

	public void setBaseCassandraDao(BaseCassandraDao baseCassandraDao) {
		this.baseCassandraDao = baseCassandraDao;
	}
	
}
