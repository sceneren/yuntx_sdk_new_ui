package com.yuntongxun.mcm.mcm.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.service.RMServerRequestService;
import com.yuntongxun.mcm.util.Constants;
import com.yuntongxun.mcm.util.StringUtil;

public class EvtHeartBeatQuartz{

	public static final Logger logger = LogManager.getLogger(EvtHeartBeatQuartz.class);

	private RMServerRequestService rmServerRequestService;
	
	public void sendHeartBeat() throws CCPServiceException{
		String seq = EncryptUtil.md5(StringUtil.getUUID());
		String request = "{\"cmserial\":" +Constants.M3C_SERIAL+ ",\"command\":\"EvtHeartBeat\",\"seq\":\"" + seq + "\"}";
		rmServerRequestService.doPushMessage(request);
	}

	/**
	 * set inject
	 */
	public void setRmServerRequestService(RMServerRequestService rmServerRequestService) {
		this.rmServerRequestService = rmServerRequestService;
	}
	
}
