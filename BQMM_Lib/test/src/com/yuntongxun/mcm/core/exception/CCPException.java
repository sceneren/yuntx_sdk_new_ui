package com.yuntongxun.mcm.core.exception;

import org.ming.sample.alarm.impl.AbstractMessage.AlarmLevel;
import org.ming.sample.core.exception.AbstractException;

import com.yuntongxun.mcm.util.ScriptManager;

public class CCPException extends AbstractException {

	private static final long serialVersionUID = 160401346168282408L;

	/**
	 * 比较明确的自定义异常
	 * 
	 * @param message
	 */
	public CCPException(String message) {
		super(message);
	}

	/**
	 * 包装虚拟机抛出的底层异常
	 * 
	 * @param cause
	 */
	public CCPException(Throwable cause) {
		super(cause);
	}

	public ErrorDesc getErrorDesc(Throwable cause) {
		String errorCode = "111000";
		String errorMsg = ScriptManager.getErrorDesc(errorCode);
		String customMsg = cause.getMessage();
		AlarmLevel level = AlarmLevel.NORMAL;
		
		if (cause instanceof java.security.NoSuchAlgorithmException) {
			errorCode = "112607";
			errorMsg = ScriptManager.getErrorDesc(errorCode);
			customMsg = cause.toString();
			level = AlarmLevel.IMPORTANT;
		} else if (cause instanceof java.security.cert.CertificateException) {
			errorCode = "112606";
			errorMsg = ScriptManager.getErrorDesc(errorCode);
			customMsg = cause.toString();
			level = AlarmLevel.IMPORTANT;
		} else if (cause instanceof java.security.KeyManagementException) {
			errorCode = "112608";
			errorMsg = ScriptManager.getErrorDesc(errorCode);
			customMsg = cause.toString();
			level = AlarmLevel.IMPORTANT;
		} else if (cause instanceof java.io.IOException) {
			errorCode = "112600";
			errorMsg = ScriptManager.getErrorDesc(errorCode);
			customMsg = cause.toString();
			level = AlarmLevel.IMPORTANT;
		}
		
		return new ErrorDesc(errorCode, errorMsg, customMsg, level);
	}

}
