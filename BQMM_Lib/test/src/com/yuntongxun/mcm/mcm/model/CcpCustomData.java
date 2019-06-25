package com.yuntongxun.mcm.mcm.model;

import org.ming.sample.util.JSONUtil;
import org.yuntongxun.tools.util.EncryptUtil;

public class CcpCustomData {

	private String transferAgentCount; // 转接者账号
	private String transferAgentId; // 转接者坐席
	private String transferAllocAgentCount; // 转接分配者
	private String transferAllocAgentId;  // 转接分配坐席
	private String srcAgentId;
	
	private int mcmEvent;
	
	public String getTransferAgentCount() {
		return transferAgentCount;
	}

	public void setTransferAgentCount(String transferAgentCount) {
		this.transferAgentCount = transferAgentCount;
	}

	public String getTransferAgentId() {
		return transferAgentId;
	}

	public void setTransferAgentId(String transferAgentId) {
		this.transferAgentId = transferAgentId;
	}

	public int getMcmEvent() {
		return mcmEvent;
	}

	public void setMcmEvent(int mcmEvent) {
		this.mcmEvent = mcmEvent;
	}

	public String getTransferAllocAgentCount() {
		return transferAllocAgentCount;
	}

	public void setTransferAllocAgentCount(String transferAllocAgentCount) {
		this.transferAllocAgentCount = transferAllocAgentCount;
	}

	public String getTransferAllocAgentId() {
		return transferAllocAgentId;
	}

	public void setTransferAllocAgentId(String transferAllocAgentId) {
		this.transferAllocAgentId = transferAllocAgentId;
	}
	
	public String getSrcAgentId() {
		return srcAgentId;
	}

	public void setSrcAgentId(String srcAgentId) {
		this.srcAgentId = srcAgentId;
	}

	@Override
	public String toString() {
		return EncryptUtil.base64Encoder(JSONUtil.object2json(this));
	}
	
}
