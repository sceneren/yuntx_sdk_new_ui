package com.yuntongxun.mcm.mcm.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.yuntongxun.mcm.core.dao.BaseJdbcDao;
import com.yuntongxun.mcm.core.exception.CCPDaoException;
import com.yuntongxun.mcm.mcm.dao.M3CSMessageHistoryDao;
import com.yuntongxun.mcm.mcm.model.M3CSMessageHistory;

public class M3CSMessageHistoryDaoImpl implements M3CSMessageHistoryDao{

	private BaseJdbcDao baseJdbcDao;
	
	@Override
	public void insert(M3CSMessageHistory m3csMessageHistory) throws CCPDaoException {
		Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
		String sql = "INSERT INTO ytx_m3cs_history_message(" +
				"sessionId, unity_account, ccs_type, event_type, skill_group_id," +
				"channel, app_id_sender, msg_sender, app_id_receiver, msg_receiver," +
				"device_no, device_type, msg_len, msg_type, msg_content, " +
				"msg_file_url, msg_file_name, version, group_id, msg_id, " +
				"msg_file_size, local_file_name, msg_domain, msg_compress_len, mcm_event," +
				"expired, status, type, msg_domain_flag, date_created, " +
				"update_time, agent_id, result_code) VALUES( " +
				"? , ? , ? , ? , ? ," +
				"? , ? , ? , ? , ? ," +
				"? , ? , ? , ? , ? ," +
				"? , ? , ? , ? , ? ," +
				"? , ? , ? , ? , ? ," +
				"? , ? , ? , ? , ? ," +
				"? , ? , ?)";
		List<Object> objList = new ArrayList<Object>();
		objList.add(m3csMessageHistory.getSid());
		objList.add(m3csMessageHistory.getOsUnityAccount());
		objList.add(m3csMessageHistory.getCCSType());
		objList.add(m3csMessageHistory.getEventType());
		objList.add(m3csMessageHistory.getSkillGroupId());
		
		objList.add(m3csMessageHistory.getChannel());
		objList.add(m3csMessageHistory.getAppIdSender());
		objList.add(m3csMessageHistory.getMsgSender());
		objList.add(m3csMessageHistory.getAppIdReceiver());
		objList.add(m3csMessageHistory.getMsgReceiver());
		
		objList.add(m3csMessageHistory.getDeviceNo());
		objList.add(m3csMessageHistory.getDeviceType());
		objList.add(m3csMessageHistory.getMsgLen());
		objList.add(m3csMessageHistory.getMsgType());
		objList.add(m3csMessageHistory.getMsgContent());
		
		objList.add(m3csMessageHistory.getMsgFileUrl());
		objList.add(m3csMessageHistory.getMsgFileName());
		objList.add(m3csMessageHistory.getVersion());
		objList.add(m3csMessageHistory.getGroupId());
		objList.add(m3csMessageHistory.getMsgId());
		
		objList.add(m3csMessageHistory.getMsgFileSize());
		objList.add(m3csMessageHistory.getLocalFileName());
		objList.add(m3csMessageHistory.getMsgDomain());
		objList.add(m3csMessageHistory.getMsgCompressLen());
		objList.add(m3csMessageHistory.getMcmEvent());
		
		objList.add(m3csMessageHistory.getExpired());
		objList.add(m3csMessageHistory.getStatus());
		objList.add(m3csMessageHistory.getType());
		objList.add(m3csMessageHistory.getMsgDomainFlag());
		objList.add(currentTime);
		
		objList.add(currentTime);
		objList.add(m3csMessageHistory.getAgentId());
		objList.add(m3csMessageHistory.getResultCode());
		
		baseJdbcDao.update(sql, objList.toArray());
	}

	/**
	 * set inject
	 */
	public void setBaseJdbcDao(BaseJdbcDao baseJdbcDao) {
		this.baseJdbcDao = baseJdbcDao;
	}
	
}
