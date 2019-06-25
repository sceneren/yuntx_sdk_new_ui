package com.yuntongxun.mcm.sevenmoor.model;

import java.util.ArrayList;
import java.util.List;

import com.yuntongxun.mcm.util.JsonUtils;

public class SevenMoorLoginRInfo {

	private String userAcc;
	private List<String> receivedMsgIds = new ArrayList<String>();
	public String getUserAcc() {
		return userAcc;
	}
	public void setUserAcc(String userAcc) {
		this.userAcc = userAcc;
	}
	public List<String> getReceivedMsgIds() {
		return receivedMsgIds;
	}
	public void setReceivedMsgIds(List<String> receivedMsgIds) {
		this.receivedMsgIds = receivedMsgIds;
	}
	
	public String getReceivedMsgIdsString(){
		String result = "";
		StringBuffer sb = new StringBuffer();
		if(receivedMsgIds!=null&&receivedMsgIds.size()>0){
			for(String msgId:receivedMsgIds){
				System.out.println("!!!!!!!!!!!!!!!!!!!!!"+msgId);
				sb.append("\\\"").append(msgId).append("\\\"").append(",");
			}
			result = sb.toString();
			result = result.substring(0,result.length()-1);
		}
		System.out.println("$$$$$$$$$$$$$$$$$$"+result);
		return result;
	}
	
	public static void main(String[] args) {
		String json = "{\"userAcc\": \"20150314000000110000000000000010#18511251111\",\"receivedMsgIds\": [\"72009f40-06a6-11e6-9345-af831867f97b\"]}";
		SevenMoorLoginRInfo sevenMoorLoginRInfo = (SevenMoorLoginRInfo)JsonUtils.jsonToObj(json, SevenMoorLoginRInfo.class);
		
		
		System.out.println(sevenMoorLoginRInfo.getReceivedMsgIdsString());
		
	}
}
