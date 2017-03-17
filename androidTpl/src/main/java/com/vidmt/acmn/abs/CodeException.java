package com.vidmt.acmn.abs;

public class CodeException extends 	Exception {
	private static final long serialVersionUID = 1L;
	private int code = ERR_UNKOWN;

	public static final int ERR_UNKOWN = 1;

	public CodeException(int code) {
		super("code:" + code);
		this.code = code;
	}

	public CodeException(int code, String msg) {
		super("code:" + code + "->" + msg);
		this.code = code;
	}

	public CodeException(int code, Throwable e) {
		super("code:" + code, e);
		this.code = code;
	}

	public CodeException(Throwable e) {
		this(ERR_UNKOWN, e);
	}

	public int getCode() {
		return code;
	}

}
