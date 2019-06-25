package com.yuntongxun.mcm.mcm.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.ming.sample.util.JSONUtil;
import org.yuntongxun.tools.util.EncryptUtil;

import com.yuntongxun.mcm.core.exception.CCPServiceException;
import com.yuntongxun.mcm.mcm.enumerate.CommandEnum;
import com.yuntongxun.mcm.mcm.model.RMServerTransferData;
import com.yuntongxun.mcm.mcm.model.SeqInfo;
import com.yuntongxun.mcm.mcm.service.RMServerResponseService;
import com.yuntongxun.mcm.mcm.service.ResponseService;
import com.yuntongxun.mcm.util.PrintUtil;

public class ResponseServiceImpl implements ResponseService{

	public static final Logger logger = LogManager.getLogger(ResponseServiceImpl.class);
	
	private RMServerResponseService rmServerResponseService;
	
	private ExecutorService executorService;
	
	public void init() {
		executorService = Executors.newSingleThreadExecutor();
	}
	
	private SeqInfo parserSeqToSeqInfo(String seq){
		if(StringUtils.isBlank(seq)){
			return null;
		}
		
		SeqInfo seqInfo = null;
		try {
			seq = EncryptUtil.base64Decoder(seq);
			logger.debug("start parser seq[{}] to seqInfo.", seq);
			
			if(StringUtils.isNotBlank(seq) && seq.contains("{") && seq.contains("}")){
				seqInfo = (SeqInfo)JSONUtil.jsonToObj(seq, SeqInfo.class);	
			}
			
		} catch (Exception e) {
			logger.error("parserSeqToSeqInfo#error()", e);
			seqInfo = null;
		}
		
		if(seqInfo == null){
			logger.warn("parser seq get seqInfo is null.");
		}
		
		return seqInfo;
	}
	
	@Override
	public void handleRMResponse(final RMServerTransferData rmServerTransferData)
			throws CCPServiceException {
		final String sessionId = ThreadContext.peek();
		
		executorService.execute(new Runnable() {
			
			@Override
			public void run() {
				String command = rmServerTransferData.getCommand();
				SeqInfo seqInfo = parserSeqToSeqInfo(rmServerTransferData.getSeq());
				
				try {
					if(seqInfo != null && StringUtils.isNotBlank(seqInfo.getLogSessionId())){
						ThreadContext.push(seqInfo.getLogSessionId());
					}else{
						ThreadContext.push(sessionId);
					}
					
					PrintUtil.printStartTag("RmServer Res");
					
					if(CommandEnum.RESP_CMD_MCCC_MODULE_LOGIN.getValue().equals(command)){
						// 接收登录坐席管理模块响应
						rmServerResponseService.respMCCCModuleLogIn(rmServerTransferData);
						
					} else if(CommandEnum.RESP_AGENT_ON_WORK.getValue().equals(command)){
						// 接收IM坐席上班响应
						rmServerResponseService.respAgentOnWork(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_AGENT_OFF_WORK.getValue().equals(command)){
						// 接收IM坐席下班响应
						rmServerResponseService.respAgentOffWork(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_AGENT_READY.getValue().equals(command)){
						// 接收IM坐席就绪响应
						rmServerResponseService.respAgentReady(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_CM_UNEXPECTED_RESTART.getValue().equals(command)){
						// 接收座席批量就绪(异常重启) 响应
						rmServerResponseService.respCMUnexpectedRestart(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_LOCK_AGENT.getValue().equals(command)){
						// 接收锁定坐席响应
						rmServerResponseService.respLockAgent(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_AGENT_STATE_SWITCH.getValue().equals(command)){
						// 接收座席其他状态更新响应
						rmServerResponseService.respAgentStateSwitch(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_ENTER_CCS.getValue().equals(command)){
						// 接收用户进入排队响应
						rmServerResponseService.respEnterCCS(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_EXIT_CCS_QUEUE.getValue().equals(command)){
						// 接收用户退出排队响应
						rmServerResponseService.respExitCCSQueue(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_QUERY_QUEUE_INFO.getValue().equals(command)){
						// 接收查询信息排队人数和空闲座席响应
						rmServerResponseService.respQueryQueueInfo(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.CMD_WAKE_UP_USER.getValue().equals(command)){
						// 接收唤醒排队用户响应
						rmServerResponseService.cmdWakeUpUser(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_ALLOC_IM_AGENT.getValue().equals(command)){
						// 接收分配IM坐席响应
						rmServerResponseService.respAllocImAgent(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_IM_AGENT_SERVICE_END.getValue().equals(command)){
						// 接收IM坐席与用户服务结束响应
						rmServerResponseService.respImAgentServiceEnd(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_QUERY_QUEUE_INFO.getValue().equals(command)){
						// 查询队列响应
						rmServerResponseService.respQueryQueueInfo(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.RESP_AGENT_RESERVED_SERVICE.getValue().equals(command)){
						// 坐席预留服务响应
						rmServerResponseService.respAgentReservedService(rmServerTransferData, seqInfo);
						
					} else if(CommandEnum.CMD_NOTIFY_AGENT_OFF_WORK.getValue().equals(command)){
						// 坐席下班响应通知
						rmServerResponseService.cmdNotifyAgentOffWork(rmServerTransferData, seqInfo);
						
					} else{
						logger.warn("unknown command: {}.", command);
					}
					
				} catch (Exception e) {
					if(e instanceof CCPServiceException){
						logger.error("handleRMResponse#CCPServiceException()", ((CCPServiceException) e).getErrorCode());
					} else {
						logger.error("handleRMResponse#Exception()", e);
					}
				} finally{
					PrintUtil.printEndTag("RmServer Res");
					ThreadContext.removeStack();
				}
			}
			
		});
		
	}

	/**
	 * set inject
	 */
	public void setRmServerResponseService(RMServerResponseService rmServerResponseService) {
		this.rmServerResponseService = rmServerResponseService;
	}

}
