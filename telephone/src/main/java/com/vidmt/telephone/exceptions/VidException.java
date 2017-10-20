package com.vidmt.telephone.exceptions;

import com.vidmt.acmn.abs.CodeException;

public class VidException extends CodeException {
	private static final long serialVersionUID = 1L;

	// public static final int HTTP_SERVER_ERR_CODE = 100;
	// public static final int HTTP_USER_NOT_EXISTS = 101;
	// public static final int HTTP_USER_NOT_LOGIN = 102;
	// public static final String HTTP_USER_NOT_EXISTS_MSG = "user not exists";
	// public static final String HTTP_USER_NOT_LOGIN_MSG = "not login";
	//
	// public static final int ERR_CODE_USER_ALREADY_EXISTS = 101;
	// public static final String ERR_MSG_USER_ALREADY_EXISTS =
	// "user already exists";
	// public static final int ERR_CODE_USER_NOT_LOGIN = 102;
	// public static final String ERR_MSG_USER_NOT_LOGIN = "user not login";

	public VidException(int code) {
		super(code);
	}

	public VidException(int code, String msg) {
		super(code, msg);
	}

	public VidException(int code, Throwable e) {
		super(code, e);
	}

	public VidException(Throwable e) {
		super(e);
	}

	public VidException(String msg) {
		super(CodeException.ERR_UNKOWN, msg);
	}

}
