/**
 * 
 */
package com.yuntongxun.mcm.mcm.model;

/**
 * @author chao
 */
public class UserLoginInfo {

//	public static final int WIFI = 1;
//	public static final int TDLTE_4G = 2;
//	public static final int WCDMA_3G = 3;
//	public static final int EDGE_2G = 4;
//	public static final int OTHER = 6;
	
	private String sessionId;
	private String remoteHost;
	private String connectorId;
	private String connectorAddr;
	private int clientType;
	private long lastLoginTime;
	private String userName;
	private String appId;
	private String softVersion;
	private String deviceno;
	private String network;//1: WIFI 2: 4G  3: 3G  5: 2G(EDGE)  6: other
	private String useracc;
	private String kickOff;

	public UserLoginInfo() {
	}

	public String getDeviceno() {
		return deviceno;
	}

	public void setDeviceno(String deviceno) {
		this.deviceno = deviceno;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the remoteHost
	 */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @param remoteHost
	 *            the remoteHost to set
	 */
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	/**
	 * @return the connectorId
	 */
	public String getConnectorId() {
		return connectorId;
	}

	/**
	 * @param connectorId
	 *            the connectorId to set
	 */
	public void setConnectorId(String connectorId) {
		this.connectorId = connectorId;
	}

	/**
	 * @return the clientType
	 */
	public int getClientType() {
		return clientType;
	}

	/**
	 * @param clientType
	 *            the clientType to set
	 */
	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	/**
	 * @return the lastLoginTime
	 */
	public long getLastLoginTime() {
		return lastLoginTime;
	}

	/**
	 * @param lastLoginTime
	 *            the lastLoginTime to set
	 */
	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	/**
	 * @return the connectorAddr
	 */
	public String getConnectorAddr() {
		return connectorAddr;
	}

	/**
	 * @param connectorAddr
	 *            the connectorAddr to set
	 */
	public void setConnectorAddr(String connectorAddr) {
		this.connectorAddr = connectorAddr;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}

	/**
	 * @param appId
	 *            the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}

	/**
	 * @return the softVersion
	 */
	public String getSoftVersion() {
		return softVersion;
	}

	/**
	 * @param softVersion
	 *            the softVersion to set
	 */
	public void setSoftVersion(String softVersion) {
		this.softVersion = softVersion;
	}

	/**
	 * @return the networks
	 */
	public String getNetwork() {
		return network;
	}

	/**
	 * @param networks
	 *            the networks to set
	 */
	public void setNetwork(String network) {
		this.network = network;
	}

	public String getUseracc() {
		return useracc;
	}

	public void setUseracc(String useracc) {
		this.useracc = useracc;
	}

	public String getKickOff() {
		return kickOff;
	}

	public void setKickOff(String kickOff) {
		this.kickOff = kickOff;
	}
	
}
