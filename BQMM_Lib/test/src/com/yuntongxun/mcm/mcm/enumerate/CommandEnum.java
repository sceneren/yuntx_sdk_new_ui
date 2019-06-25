package com.yuntongxun.mcm.mcm.enumerate;

public enum CommandEnum {
	
	CMD_AGENT_ON_WORK("CmdAgentOnWork"), // 坐席上班
	RESP_AGENT_ON_WORK("RespAgentOnWork"), // 坐席上班响应
	
	CMD_AGENT_OFF_WORK("CmdAgentOffWork"), // 坐席下班
	RESP_AGENT_OFF_WORK("RespAgentOffWork"), // 坐席下班响应
	
	CMD_AGENT_READY("CmdAgentReady"), //坐席就绪
	RESP_AGENT_READY("RespAgentReady"), // 坐席就绪响应
	
	CMD_CM_UNEXPECTED_RESTART("CmdCMUnexpectedRestart"), // 座席批量就绪
	RESP_CM_UNEXPECTED_RESTART("RespCMUnexpectedRestart"), // 座席批量就绪响应
	
	CMD_LOCK_AGENT("CmdLockAgent"), // 锁定空闲坐席, 若座席为IDLE也可以锁定
	RESP_LOCK_AGENT("RespLockAgent"), // 响应锁定空闲坐席响应
	
	CMD_AGENT_STATE_SWITCH("CmdAgentStateSwitch"), // 座席其他状态更新
	RESP_AGENT_STATE_SWITCH("RespAgentStateSwitch"), // 座席其他状态更新响应
	
	CMD_ENTER_CCS("CmdEnterCCS"), // 用户进入排队
	RESP_ENTER_CCS("RespEnterCCS"), // 用户进入排队响应
	
	CMD_EXIT_CCS_QUEUE("CmdExitCCSQueue"), // 用户退出排队
	RESP_EXIT_CCS_QUEUE("RespExitCCSQueue"), // 用户退出排队响应
	
	CMD_QUERY_QUEUE_INFO("CmdQueryQueueInfo"), // 查询信息排队人数和空闲座席
	RESP_QUERY_QUEUE_INFO("RespQueryQueueInfo"), // 查询信息排队人数和空闲座席响应
	
	CMD_WAKE_UP_USER("CmdWakeUpUser"), // 唤醒排队用户
	RESP_WAKE_UP_USER("RespWakeUpUser"), // 唤醒排队用户响应
	
	CMD_ALLOC_IM_AGENT("CmdAllocImAgent"), // 分配IM座席
	RESP_ALLOC_IM_AGENT("RespAllocImAgent"), // 分配IM座席响应
	
	CMD_IM_AGENT_SERVICE_END("CmdImAgentServiceEnd"), // IM座席与用户服务结束
	RESP_IM_AGENT_SERVICE_END("RespImAgentServiceEnd"), // IM座席与用户服务结束响应
	
	CMD_AGENT_RESERVED_SERVICE("CmdAgentReservedService"), // 座席预留服务接口
	RESP_AGENT_RESERVED_SERVICE("RespAgentReservedService"), // 座席预留服务接口 响应
	
	CMD_MCCC_MODULE_LOGIN("CmdMCCCModuleLogin"), //	登录座席管理模块
	RESP_CMD_MCCC_MODULE_LOGIN("RespMCCCModuleLogin"), // 登录座席管理模块响应
	
	CMD_CREATE_QUEUE("CmdCreateQueue"), // 创建队列
	RESP_CREATE_QUEUE("RespCreateQueue"), // 创建队列响应
	
	CMD_DEL_QUEUE("CmdDelQueue"), //删除队列
	RESP_DEL_QUEUE("RespDelQueue"), //删除队列响应
	
	CMD_NOTIFY_AGENT_OFF_WORK("CmdNotifyAgentOffWork"), // 坐席下班通知
	RESP_NOTIFY_AGENT_OFF_WORK("RespNotifyAgentOffWork"), // 坐席下班通知响应
	
	CMD_MODIFY_QUEUE("CmdModifyQueue"), //修改队列
	RESP_MODIFY_QUEUE("RespModifyQueue");  //修改队列响应
	
	private String value;
	
	private CommandEnum(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}

}
