package com.yuntongxun.mcm.genesys.enumerate;

/**
 * 坐席状态
 */
public enum AgentStatus {

	Unknown, //未知
    LoggedOut, //签出
    LoggedIn, //签入
    Ready, //就绪
    NotReady, //未就绪
    ACW //后续呼叫工作模式
    
}
