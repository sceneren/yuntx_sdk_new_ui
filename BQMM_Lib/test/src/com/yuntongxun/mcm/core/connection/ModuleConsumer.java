package com.yuntongxun.mcm.core.connection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.yuntongxun.tools.balance.MessageListener;

import com.yuntongxun.mcm.core.AbstractController;
import com.yuntongxun.mcm.core.MsgLiteFactory;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;
import com.yuntongxun.mcm.util.StringUtil;

public class ModuleConsumer implements MessageListener {

	public static final Logger logger = LogManager.getLogger(ModuleConsumer.class);

	private ExecutorService executorService;
	
	private int threadNumber;
	
	public void init() {
		executorService = Executors.newFixedThreadPool(threadNumber);
	}

	@Override
	public void OnModuleMessage(final String channelId, final Object message)
			throws Exception {
		executorService.execute(new Runnable() {
			public void run() {
				try {
					MsgLiteInner msgLite = (MsgLiteInner) message;
					String sessionId = StringUtils.isBlank(msgLite.getProtoSource()) ? StringUtil.getUUID() : msgLite.getProtoSource();
					ThreadContext.push(sessionId);
					logger.info("-------------------------------[OnModuleMessage start]-------------------------------");
					logger.info("by module channel: {}", channelId);
					logger.info("Received MsgLite: {}", MsgLiteFactory.printMsgLite(msgLite));
					
					AbstractController controller = AbstractController.getController(msgLite.getProtoType());
					if (controller != null) {
						controller.postRequest(channelId, msgLite);
					} else {
						logger.warn("can't get controller from [{}], please check it...", msgLite.getProtoType());
					}
				} catch (Exception e) {
					logger.error("ModuleConsumer#run()", e);
				} finally {
					logger.info("-------------------------------[OnModuleMessage end]-------------------------------\r\n\r\n");
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

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

}
