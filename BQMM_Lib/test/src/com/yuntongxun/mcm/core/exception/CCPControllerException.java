package com.yuntongxun.mcm.core.exception;

/**
 * Controller层的业务异常（消息应该是具体的错误描述）
 * 
 */
public class CCPControllerException extends CCPServiceException {

	private static final long serialVersionUID = 3994674076528026821L;

	/**
	 * @see CCPServiceException#CCPServiceException(String)
	 * 
	 * @param message
	 */
	public CCPControllerException(String message) {
		super(message);
	}
}
