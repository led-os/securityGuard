package com.vidmt.child.exceptions;

import com.vidmt.acmn.abs.CodeException;

public class VidException extends CodeException {
	private static final long serialVersionUID = 1L;

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
