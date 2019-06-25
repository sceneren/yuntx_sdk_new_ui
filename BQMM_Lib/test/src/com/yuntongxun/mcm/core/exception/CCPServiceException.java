package com.yuntongxun.mcm.core.exception;

public class CCPServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	private String errorCode;

	/**
	 * @param message
	 */
	public CCPServiceException(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
