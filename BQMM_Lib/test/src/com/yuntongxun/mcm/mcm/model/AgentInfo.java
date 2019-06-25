package com.yuntongxun.mcm.mcm.model;


public class AgentInfo {

	private String agentId;
	private String agentAccount;
	private int type; // 0.普通坐席  1.监听者 
	private int accept; // 0.未接起 1.已接起

	public AgentInfo() {
		super();
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getAgentAccount() {
		return agentAccount;
	}

	public void setAgentAccount(String agentAccount) {
		this.agentAccount = agentAccount;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAccept() {
		return accept;
	}

	public void setAccept(int accept) {
		this.accept = accept;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AgentInfo){
			AgentInfo ai = (AgentInfo) obj;
			return (agentId.equals(ai.getAgentId()) && (agentAccount.equals(ai.getAgentAccount())));
		}
		  return super.equals(obj);
	}
	
	@Override
    public int hashCode(){
		return agentId.hashCode() * agentAccount.hashCode();	
    }
}