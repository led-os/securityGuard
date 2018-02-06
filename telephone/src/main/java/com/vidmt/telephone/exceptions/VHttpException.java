package com.vidmt.telephone.exceptions;

/**
 * VHttpException中错误码+错误消息都是server发过来的，保持一致!
 */
public class VHttpException extends VidException {
	private static final long serialVersionUID = 1L;

	private static final int ERR_CODE_UNKNOWN = 100;
	public static final int ERR_CODE_USER_ALREADY_EXISTS = 101;
	public static final String ERR_MSG_USER_ALREADY_EXISTS = "user already exists";
	public static final int ERR_CODE_USER_NOT_LOGIN = 102;
	public static final String ERR_MSG_USER_NOT_LOGIN = "user not login";
	public static final int ERR_CODE_USER_NOT_EXISTS = 103;
	public static final String ERR_MSG_USER_NOT_EXISTS = "user not exists";
	public static final int ERR_CODE_HTTP_SERVER_ERROR = 104;
	public static final String ERR_MSG_HTTP_SERVER_ERROR = "http server error";
	public static final int ERR_CODE_PARAMS_ERROR = 201;

	public VHttpException(String msg) {
		super(ERR_CODE_UNKNOWN, msg);
	}

	public VHttpException(int code, String msg) {
		super(code, msg);
	}

}
