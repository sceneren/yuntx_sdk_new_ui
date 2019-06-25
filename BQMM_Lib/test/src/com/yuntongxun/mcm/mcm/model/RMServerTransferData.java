package com.yuntongxun.mcm.mcm.model;

import java.util.ArrayList;
import java.util.List;


public class RMServerTransferData {

	private String command;
	private String appid;
	private String seq;
	private String sid;
	private String agentid;
	
	private int queuetype;
	private boolean force;
	private String useraccount;
	private int keytype;
	private String customcontent;
	
	private String servicecap;
	private String number;
	private String customaccnum;
	private String acdcalltype;
	private String firstnumber;
	
	private String agenttype;
	private String serverqueuetype;
	private int queuepriority;
	private int agentstate;
	//private String agentstate;
	//private int imagentstate;
	
	private int maximuser;
	private String pushvoipacc;
	private String delaycall;
	private String userinfocallbackurl;
	private String answertimeout;
	
	private int agentservermode;
	private String callidfirst;
	private int cmserial;
	//private int state;
	private String state;
	private String priority;
	
	private String sidfirst;
	private String statuscode;
	private int queuecount;
	private String welcome;
	//private boolean imstate;
	
	private String origAgentid;
	private String customaccount;
	
	private int idlecount;
	private String callid; // 退出排队标识,同sid
	private String reservedkey; // 预留服务的关键字
	private boolean opttype; // 操作类型，true表示启用预留服务；false取消预留服务，reservedkey为空时是取消全部预留，有值则取消指定用户服务。
	
	private List<RMServerAgent> idleagents = new ArrayList<RMServerAgent>();
	private String useraccounts;
	
	public RMServerTransferData() {
		this.queuetype = -1;
	}

	/**
	 * CmdAllocImAgent
	 * 
	 * @return
	 */
	public String toJsonForCmdAllocImAgent() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"sid\":\"");
		sb.append(sid == null ? "" : sid);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		if(queuetype >= 0){
			sb.append("\",\"queuetype\":\"");
			sb.append(queuetype);
		}
		
		sb.append("\",\"force\":\"");
		sb.append(force);
		
		sb.append("\",\"useraccount\":\"");
		sb.append(useraccount == null ? "" : useraccount);
		
		sb.append("\",\"keytype\":\"");
		sb.append(keytype);
		
		sb.append("\",\"cmserial\":");
		sb.append(cmserial);
		
		sb.append(",\"customcontent\":\"");
		sb.append(customcontent == null ? "" : customcontent);
		
		sb.append("\"");
		
		sb.append("}");
		return sb.toString();
	}

	/**
	 * CmdImAgentServiceEnd
	 * 
	 * @return
	 */
	public String toJsonForCmdImAgentServiceEnd() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"sid\":\"");
		sb.append(sid == null ? "" : sid);

		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);

		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * CmdAgentOnWork
	 * 
	 * @return
	 */
	public String toJsonForCmdAgentOnWork() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		sb.append("\",\"servicecap\":\"");
		sb.append(servicecap == null ? "" : servicecap);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"number\":\"");
		sb.append(number == null ? "" : number);
		
		sb.append("\",\"customaccnum\":\"");
		sb.append(customaccnum == null ? "" : customaccnum);
		
		sb.append("\",\"pushvoipacc\":\"");
		sb.append(pushvoipacc == null ? "" : pushvoipacc);
		
		sb.append("\",\"delaycall\":\"");
		sb.append(delaycall == null ? "" : delaycall);
		
		sb.append("\",\"userinfocallbackurl\":\"");
		sb.append(userinfocallbackurl == null ? "" : userinfocallbackurl);
		
		sb.append("\",\"answertimeout\":\"");
		sb.append(answertimeout == null ? "" : answertimeout);
		
		sb.append("\",\"acdcalltype\":\"");
		sb.append(acdcalltype == null ? "" : acdcalltype);
		
		sb.append("\",\"firstnumber\":\"");
		sb.append(firstnumber == null ? "" : firstnumber);
		
		sb.append("\",\"agenttype\":\"");
		sb.append(agenttype == null ? "" : agenttype);
		
		/*sb.append("\",\"serverqueuetype\":\"");
		sb.append(serverqueuetype == null ? "" : serverqueuetype);*/
		
		sb.append("\",\"queuepriority\":\"");
		sb.append(queuepriority);
		
		sb.append("\",\"agentstate\":");
		sb.append(agentstate);
		
		/*sb.append("\",\"imagentstate\":\"");
		sb.append(imagentstate);*/
		
		sb.append(",\"maximuser\":\"");
		sb.append(maximuser);
		
		sb.append("\",\"agentservermode\":\"");
		sb.append(agentservermode);
		
		sb.append("\",\"callidfirst\":\"");
		sb.append(callidfirst == null ? "" : callidfirst);
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * CmdAgentOffWork
	 * 
	 * @return
	 */
	public String toJsonForCmdAgentOffWork() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		sb.append("\",\"cmserial\":");
		sb.append(cmserial);
		
		sb.append(",\"number\":\"");
		sb.append(number == null ? "" : number);
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * CmdAgentReady
	 * 
	 * @return
	 */
	public String toJsonForCmdAgentReady() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"cmserial\":\"");
		sb.append(cmserial);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		sb.append("\",\"state\":\"");
		sb.append(state);
		
		sb.append("\",\"priority\":\"");
		sb.append(priority == null ? "" : priority);
		
		sb.append("\",\"force\":\"");
		sb.append(force);
		
		sb.append("\",\"callidfirst\":\"");
		sb.append(callidfirst == null ? "" : callidfirst);
		
		/*sb.append("\",\"agentstate\":\"");
		sb.append(agentstate);*/
		
		/*sb.append("\",\"imstate\":\"");
		sb.append(imstate);*/
		
		sb.append("\",\"sidfirst\":\"");
		sb.append(sidfirst == null ? "" : sidfirst);
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * CmdWakeUpUser
	 * 
	 * @return
	 */
	public String toJsonForCmdWakeUpUser() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		sb.append("\",\"number\":\"");
		sb.append(number == null ? "" : number);
		
		sb.append("\",\"customaccnum\":\"");
		sb.append(customaccnum == null ? "" : customaccnum);
		
		sb.append("\",\"acdcalltype\":\"");
		sb.append(acdcalltype == null ? "" : acdcalltype);
		
		sb.append("\",\"firstnumber\":\"");
		sb.append(firstnumber == null ? "" : firstnumber);
		
		sb.append("\",\"pushvoipacc\":\"");
		sb.append(pushvoipacc == null ? "" : pushvoipacc);
		
		sb.append("\",\"delaycall\":\"");
		sb.append(delaycall == null ? "" : delaycall);
		
		sb.append("\",\"userinfocallbackurl\":\"");
		sb.append(userinfocallbackurl == null ? "" : userinfocallbackurl);
		
		sb.append("\",\"answertimeout\":\"");
		sb.append(answertimeout == null ? "" : answertimeout);
		
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * RespWakeUpUser
	 * 
	 * @return
	 */
	public String toJsonForResp() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"statuscode\":");
		sb.append(statuscode == null ? 0 : Integer.parseInt(statuscode));
		
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * CmdQueryQueueInfo
	 * 
	 * @return
	 */
	public String toJsonForCmdQueryQueueInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"cmserial\":\"");
		sb.append(cmserial);
		
		sb.append("\",\"queuetype\":\"");
		sb.append(queuetype);
		sb.append("\"");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * CmdExitCCSQueue
	 * 
	 */
	public String toJsonForCmdExitCCSQueue() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"callid\":\"");
		sb.append(callid == null ? "" : callid);
		
		sb.append("\",\"cmserial\":");
		sb.append(cmserial);
		
		sb.append("}");
		return sb.toString();
	}

	public String toJsonForCmdAgentReservedService() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"command\":\"");
		sb.append(command == null ? "" : command);
		
		sb.append("\",\"appid\":\"");
		sb.append(appid == null ? "" : appid);
		
		sb.append("\",\"agentid\":\"");
		sb.append(agentid == null ? "" : agentid);
		
		sb.append("\",\"seq\":\"");
		sb.append(seq == null ? "" : seq);
		
		sb.append("\",\"reservedkey\":\"");
		sb.append(reservedkey == null ? "" : reservedkey);
		
		sb.append("\",\"keytype\":\"");
		sb.append(keytype);
		
		sb.append("\",\"opttype\":\"");
		sb.append(opttype);
		
		sb.append("\"");
		
		sb.append("}");
		return sb.toString();
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getAgentid() {
		return agentid;
	}

	public void setAgentid(String agentid) {
		this.agentid = agentid;
	}

	public int getQueuetype() {
		return queuetype;
	}

	public void setQueuetype(int queuetype) {
		this.queuetype = queuetype;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public String getUseraccount() {
		return useraccount;
	}

	public void setUseraccount(String useraccount) {
		this.useraccount = useraccount;
	}

	public int getKeytype() {
		return keytype;
	}

	public void setKeytype(int keytype) {
		this.keytype = keytype;
	}

	public String getCustomcontent() {
		return customcontent;
	}

	public void setCustomcontent(String customcontent) {
		this.customcontent = customcontent;
	}

	public String getServicecap() {
		return servicecap;
	}

	public void setServicecap(String servicecap) {
		this.servicecap = servicecap;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCustomaccnum() {
		return customaccnum;
	}

	public void setCustomaccnum(String customaccnum) {
		this.customaccnum = customaccnum;
	}

	public String getAcdcalltype() {
		return acdcalltype;
	}

	public void setAcdcalltype(String acdcalltype) {
		this.acdcalltype = acdcalltype;
	}

	public String getFirstnumber() {
		return firstnumber;
	}

	public void setFirstnumber(String firstnumber) {
		this.firstnumber = firstnumber;
	}

	public String getAgenttype() {
		return agenttype;
	}

	public void setAgenttype(String agenttype) {
		this.agenttype = agenttype;
	}

	public String getServerqueuetype() {
		return serverqueuetype;
	}

	public void setServerqueuetype(String serverqueuetype) {
		this.serverqueuetype = serverqueuetype;
	}

	public int getQueuepriority() {
		return queuepriority;
	}

	public void setQueuepriority(int queuepriority) {
		this.queuepriority = queuepriority;
	}

	public int getAgentstate() {
		return agentstate;
	}

	public void setAgentstate(int agentstate) {
		this.agentstate = agentstate;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getMaximuser() {
		return maximuser;
	}

	public void setMaximuser(int maximuser) {
		this.maximuser = maximuser;
	}

	public String getPushvoipacc() {
		return pushvoipacc;
	}

	public void setPushvoipacc(String pushvoipacc) {
		this.pushvoipacc = pushvoipacc;
	}

	public String getDelaycall() {
		return delaycall;
	}

	public void setDelaycall(String delaycall) {
		this.delaycall = delaycall;
	}

	public String getUserinfocallbackurl() {
		return userinfocallbackurl;
	}

	public void setUserinfocallbackurl(String userinfocallbackurl) {
		this.userinfocallbackurl = userinfocallbackurl;
	}

	public String getAnswertimeout() {
		return answertimeout;
	}

	public void setAnswertimeout(String answertimeout) {
		this.answertimeout = answertimeout;
	}

	public int getAgentservermode() {
		return agentservermode;
	}

	public void setAgentservermode(int agentservermode) {
		this.agentservermode = agentservermode;
	}

	public String getCallidfirst() {
		return callidfirst;
	}

	public void setCallidfirst(String callidfirst) {
		this.callidfirst = callidfirst;
	}

	public int getCmserial() {
		return cmserial;
	}

	public void setCmserial(int cmserial) {
		this.cmserial = cmserial;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getSidfirst() {
		return sidfirst;
	}

	public void setSidfirst(String sidfirst) {
		this.sidfirst = sidfirst;
	}

	public String getStatuscode() {
		return statuscode;
	}

	public void setStatuscode(String statuscode) {
		this.statuscode = statuscode;
	}

	public int getQueuecount() {
		return queuecount;
	}

	public void setQueuecount(int queuecount) {
		this.queuecount = queuecount;
	}

	public String getWelcome() {
		return welcome;
	}

	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}

	public String getOrigAgentid() {
		return origAgentid;
	}

	public void setOrigAgentid(String origAgentid) {
		this.origAgentid = origAgentid;
	}
	
	public String getCustomaccount() {
		return customaccount;
	}

	public void setCustomaccount(String customaccount) {
		this.customaccount = customaccount;
	}

	public int getIdlecount() {
		return idlecount;
	}

	public void setIdlecount(int idlecount) {
		this.idlecount = idlecount;
	}

	public String getCallid() {
		return callid;
	}

	public void setCallid(String callid) {
		this.callid = callid;
	}

	public String getReservedkey() {
		return reservedkey;
	}

	public void setReservedkey(String reservedkey) {
		this.reservedkey = reservedkey;
	}

	public boolean isOpttype() {
		return opttype;
	}

	public void setOpttype(boolean opttype) {
		this.opttype = opttype;
	}

	public List<RMServerAgent> getIdleagents() {
		return idleagents;
	}

	public void setIdleagents(List<RMServerAgent> idleagents) {
		this.idleagents = idleagents;
	}

	public String getUseraccounts() {
		return useraccounts;
	}

	public void setUseraccounts(String useraccounts) {
		this.useraccounts = useraccounts;
	}
	
}
