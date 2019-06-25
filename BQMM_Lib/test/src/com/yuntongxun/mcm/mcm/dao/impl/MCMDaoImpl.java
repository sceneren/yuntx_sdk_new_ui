package com.yuntongxun.mcm.mcm.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.dao.MCMDao;
import com.yuntongxun.mcm.mcm.model.MessageInfo;
import com.yuntongxun.mcm.util.Constants;

public class MCMDaoImpl implements MCMDao {
	
	public static final Logger logger = LogManager.getLogger(MCMDaoImpl.class);
	
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private MCMDao mcmCassandraDao;
	
	private MCMDao mcmRedisDao;
	
	private List<MCMDao> writeDao = new ArrayList<MCMDao>();
	
	private String writeSequence;
	
	public void init(){
		if(StringUtils.isEmpty(writeSequence)){
			//如果没有配置，默认值
			writeSequence = Constants.MESSAGE_WRITE_SEQUENCE;
		}
		
		String[] arr = writeSequence.split(",");
		for(String str : arr){
			if("1".equals(str)){
				writeDao.add(mcmCassandraDao);
			} else if("2".equals(str)){
				writeDao.add(mcmRedisDao);
			}
		}
	}
	
	@Override
	public void saveInstantMessage(String userAcc, long version,
			MessageInfo messageInfo, boolean async)
			throws CCPDaoException {
	}

	@Override
	public void saveInstantMessage(String receiver, MessageInfo messageInfo)
			throws CCPDaoException {
		try {
			MCMDao majorDao = writeDao.get(0);
			majorDao.saveInstantMessage(receiver, messageInfo);
			
			saveInstantMessageSync(writeDao.subList(1, writeDao.size()), receiver, messageInfo);
		} catch (CCPDaoException e) {
			logger.error("saveInstantMessage:CCPRedisException:err:",e);
			throw new CCPDaoException(e);
		} 
	}

	@Override
	public void saveBatchInstantMessage(String receiver, List<MessageInfo> msgInfoList) 
			throws CCPDaoException {
	}
	
	private void saveInstantMessageSync(final List<MCMDao> imDaos, final String receiver, 
			final MessageInfo messageInfo) throws CCPDaoException {
		final String CSID = ThreadContext.peek();
		if(imDaos == null || imDaos.size()==0){
			logger.info("saveInstantMessageSync, slave dao is null, return");
			return ;
		}
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().setName("DaoExcetorThread");
					ThreadContext.push(CSID);
					for(MCMDao mcmDao : imDaos){
						mcmDao.saveInstantMessage(receiver, messageInfo);
					}
				} catch (CCPDaoException e) {
					logger.error("saveInstantMessageSync:CCPRedisException:err:",e);
				} finally {
					ThreadContext.removeStack();
				}
			}
		});
		logger.info("put request to exector");
	}

	public void setMcmCassandraDao(MCMDao mcmCassandraDao) {
		this.mcmCassandraDao = mcmCassandraDao;
	}

	public void setMcmRedisDao(MCMDao mcmRedisDao) {
		this.mcmRedisDao = mcmRedisDao;
	}

	public void setWriteSequence(String writeSequence) {
		this.writeSequence = writeSequence;
	}
	
}
