/*
 * @(#)BaseJdbcDao.java Copyright (c) 2015 by www.yuntongxun.com. All rights
 * reserved.
 */
package com.yuntongxun.mcm.core.dao.impl;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.yuntongxun.mcm.core.dao.BaseCassandraDao;
import com.yuntongxun.mcm.core.exception.CCPCassandraDaoException;

/**
 * @author chao
 */
public class BaseCassandraDaoImpl implements BaseCassandraDao {

	private Session session;

	private String keyspace;

	private String seedNode;

	private String username;

	private String password;

	public BaseCassandraDaoImpl() {

	}

	public void init() {
		PoolingOptions poolingOptions = new PoolingOptions();
		poolingOptions.setMaxSimultaneousRequestsPerConnectionThreshold(HostDistance.LOCAL, 32);
		poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, 2);
		poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, 4);

		AuthProvider authProvider = new PlainTextAuthProvider(username, password);
		Cluster cluster = Cluster.builder().addContactPoint(seedNode).withAuthProvider(authProvider).withCompression(
				Compression.LZ4).withPoolingOptions(poolingOptions).build();
		session = cluster.connect(keyspace);
	}

	@Override
	public PreparedStatement prepare(RegularStatement statement) throws CCPCassandraDaoException {
		return this.session.prepare(statement);
	}
	
	@Override
	public ResultSet execute(String cql) throws CCPCassandraDaoException {
		try {
			return this.session.execute(cql);
		} catch (Exception e) {
			throw new CCPCassandraDaoException(e);
		}
	}

	@Override
	public ResultSet execute(Statement statement) throws CCPCassandraDaoException {
		try {
			return this.session.execute(statement);
		} catch (Exception e) {
			throw new CCPCassandraDaoException(e);
		}
	}
	
	@Override
	public ResultSetFuture executeAsync(Statement statement) throws CCPCassandraDaoException {
		try {
			return this.session.executeAsync(statement);
		} catch (Exception e) {
			throw new CCPCassandraDaoException(e);
		}
	}

	@Override
	public ResultSetFuture executeAsync(String cql) throws CCPCassandraDaoException {
		try {
			return this.session.executeAsync(cql);
		} catch (Exception e) {
			throw new CCPCassandraDaoException(e);
		}
	}
	
	public void destroy() {
		System.out.println("BaseCassandraDaoImpl destroy");
	}

	/**
	 * @return the keyspace
	 */
	public String getKeyspace() {
		return keyspace;
	}

	/**
	 * @param keyspace
	 *            the keyspace to set
	 */
	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	/**
	 * @return the seedNode
	 */
	public String getSeedNode() {
		return seedNode;
	}

	/**
	 * @param seedNode
	 *            the seedNode to set
	 */
	public void setSeedNode(String seedNode) {
		this.seedNode = seedNode;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
