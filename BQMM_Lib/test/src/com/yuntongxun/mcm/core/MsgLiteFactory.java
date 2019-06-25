package com.yuntongxun.mcm.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.ByteString;
import com.yuntongxun.mcm.core.protobuf.MsgLite;
import com.yuntongxun.mcm.core.protobuf.MsgLite.MsgLiteInner;

/**
 * MsgLie工厂 注意：响应的包体必须携带protoClientNo,推送的包体不能携带protoClientNo
 * 
 */
public class MsgLiteFactory {

	private MsgLiteFactory() {

	}

	public static final Logger logger = LogManager.getLogger(MsgLiteFactory.class);

	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoClient
	 * @param protoSource
	 * @param protoErrorCode
	 * @return
	 */
	public static MsgLite.MsgLiteInner getMsgLite(int protoType, byte[] protoData) {
		return getMsgLite(protoType, protoData, -1, null, -1);
	}

	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @return
	 */
	public static MsgLite.MsgLiteInner getMsgLite(int protoType, int protoClientNo, String protoBackExp, 
			int errorCode) {
		return getMsgLite(protoType, null, protoClientNo, protoBackExp, errorCode);
	}

	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @return
	 */
	public static MsgLite.MsgLiteInner getMsgLite(int protoType, byte[] protoData, String protoBackExp, 
			int errorCode) {
		return getMsgLite(protoType, protoData, -1, protoBackExp, errorCode);
	}

	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @return
	 */
	public static MsgLite.MsgLiteInner getMsgLite(int protoType, byte[] protoData, int protoClientNo,
			String protoBackExp, int errorCode) {
		return getMsgLite(protoType, protoData, protoClientNo, protoBackExp, null, errorCode, -1, null);
	}

	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoType
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @param protoSource
	 * @param errorCode
	 * @return
	 */
	public static MsgLite.MsgLiteInner getMsgLite(int protoType, byte[] protoData, int protoClientNo,
			String protoBackExp, String protoSource, int errorCode, 
			int protoEncrypt, String protoToken) {
		MsgLite.MsgLiteInner.Builder builder = MsgLite.MsgLiteInner.newBuilder();
		logger.debug("protoType: " + protoType + ", protoData length: "
				+ (protoData == null ? "null" : protoData.length) + ", protoClientNo: " + protoClientNo
				+ ", protoBackExp: " + protoBackExp + ", protoSource: " + protoSource + ", errorCode: " + errorCode);
		if (protoType > -1) {
			builder.setProtoType(protoType);
		}
		if (protoData != null) {
			builder.setProtoData(ByteString.copyFrom(protoData));
		}
		if (protoClientNo > -1) {
			builder.setProtoClientNo(protoClientNo);
		}
		if (protoBackExp != null) {
			builder.setProtoBackExp(protoBackExp);
		}
		if (protoSource != null) {
			builder.setProtoSource(protoSource);
		}
		if (errorCode > -1) {
			builder.setProtoErrorCode(errorCode);
		}
		if (protoEncrypt > -1) {
			builder.setProtoEncrypt(protoEncrypt);
		}
		if (protoToken != null) {
			builder.setProtoToken(protoToken);
		}
		return builder.build();
	}

	/**
	 * Print MsgLite fileds detail info.
	 * 
	 * @param msgLite
	 */
	public static String printMsgLite(MsgLite.MsgLiteInner msgLite) {
		StringBuffer sb = new StringBuffer();
		if (msgLite.hasProtoType()) {
			sb.append("ProtoType:" + msgLite.getProtoType() + ",");
		}
		if (msgLite.hasProtoClientNo()) {
			sb.append("ProtoClientNo:" + msgLite.getProtoClientNo() + ",");
		}
		if (msgLite.hasProtoErrorCode()) {
			sb.append("ProtoErrorCode:" + msgLite.getProtoErrorCode() + ",");
		}
		if (msgLite.hasProtoData()) {
			sb.append("ProtoDataLength:" + msgLite.getProtoData().size() + ",");
		}
		if (msgLite.hasProtoBackExp()) {
			sb.append("ProtoBackExp:" + msgLite.getProtoBackExp() + ",");
		}
		if (msgLite.hasProtoEncrypt()) {
			sb.append("protoEncrypt:" + msgLite.getProtoEncrypt() + ",");
		}
		if (msgLite.hasProtoToken()) {
			sb.append("protoToken:" + msgLite.getProtoToken() + ",");
		}
		if (msgLite.hasProtoSource()) {
			sb.append("ProtoSource:" + msgLite.getProtoSource());
		}
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoType
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @param protoSource
	 * @param errorCode
	 * @return
	 */
	public static MsgLiteInner buildMsgLite(int protoType, byte[] protoData, int protoClientNo,
			String protoBackExp, String protoSource, int errorCode, 
			int protoEncrypt, String protoToken) {
		MsgLiteInner.Builder builder = MsgLiteInner.newBuilder();
		logger.debug("protoType: {" + protoType + "}, protoData: {" +  (protoData == null ? "null" : protoData.length) + "}, protoClientNo: {" + protoClientNo + "}, protoBackExp: {" 
				+ protoBackExp + "}, protoSource: {" + protoSource + "}, errorCode: {" + errorCode+ "}");
		if (protoType > -1) {
			builder.setProtoType(protoType);
		}
		if (protoData != null) {
			builder.setProtoData(ByteString.copyFrom(protoData));
		}
		if (protoClientNo > -1) {
			builder.setProtoClientNo(protoClientNo);
		}
		if (protoBackExp != null) {
			builder.setProtoBackExp(protoBackExp);
		}
		if (protoSource != null) {
			builder.setProtoSource(protoSource);
		}
		if (errorCode > -1) {
			builder.setProtoErrorCode(errorCode);
		}
		if (protoEncrypt > -1) {
			builder.setProtoEncrypt(protoEncrypt);
		}
		if (protoToken != null) {
			builder.setProtoToken(protoToken);
		}
		return builder.build();
	}
	
	/**
	 * 通过指定参数创建MsgLite
	 * 
	 * @param protoData
	 * @param protoClientNo
	 * @param protoBackExp
	 * @return
	 */
	public static MsgLiteInner buildMsgLite(int protoType, byte[] protoData, int protoClientNo,
			String protoBackExp, int errorCode) {
		return buildMsgLite(protoType, protoData, protoClientNo, protoBackExp, null, errorCode, -1, null);
	}
}
