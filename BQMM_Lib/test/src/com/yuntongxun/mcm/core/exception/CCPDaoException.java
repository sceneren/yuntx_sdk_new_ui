package com.yuntongxun.mcm.core.exception;

/**
 * 数据层的异常
 * 
 */
public class CCPDaoException extends Exception {

	private static final long serialVersionUID = 5793505615829263043L;

	public CCPDaoException(){
		super();
	}
	
	/**
	 * 比较明确的自定义错误
	 * 
	 * @param message
	 */
	public CCPDaoException(String message) {
		super(message);
	}

	/**
	 * 包装虚拟机抛出的底层异常
	 * 
	 * @param cause
	 */
	public CCPDaoException(Throwable cause) {
		super(cause);
	}

}
