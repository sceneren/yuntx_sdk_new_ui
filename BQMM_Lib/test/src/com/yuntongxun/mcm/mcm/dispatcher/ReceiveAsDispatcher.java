package com.yuntongxun.mcm.mcm.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.form.McmInfoForm;
import com.yuntongxun.mcm.service.AsService;
import com.yuntongxun.mcm.util.StringUtil;

public class ReceiveAsDispatcher {

	public static final Logger logger = LogManager.getLogger(ReceiveAsDispatcher.class);
	
	private AsService asService;
	
	private int receiveAsMsgThreadNumber;
	
	private ExecutorService executorService;
	
	public void init() {
		executorService = Executors.newFixedThreadPool(receiveAsMsgThreadNumber);
	}
	
	public void addTask(final McmInfoForm mcm) {
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				String sessionId = EncryptUtil.md5(StringUtil.getUUID());
				
				try {
					ThreadContext.push(sessionId);
					logger.info("-------------------------------[ReceiveAs Start]-------------------------------");
					
					//处理从rest返回的As消息
					asService.handleRevicedMessage(mcm);
					
					logger.info("handleReceiveAsMessage used total time: {}.", (System.currentTimeMillis() - t1));
					
				} catch (CCPServiceException e) {
					logger.error("Handle ReceiveAsMessage Task#run()", e);
					
				} finally{
					logger.info("-------------------------------[onMessage end]-------------------------------\r\n\r\n");
					ThreadContext.removeStack();
				}
			}
			
		});

	}
	
	public void destroy() {
		if (executorService != null) {
			executorService.shutdown();
		}
	}

	/**
	 * set inject 
	 */
	public void setReceiveAsMsgThreadNumber(int receiveAsMsgThreadNumber) {
		this.receiveAsMsgThreadNumber = receiveAsMsgThreadNumber;
	}
	
	public void setAsService(AsService asService) {
		this.asService = asService;
	}
	
}
