package com.vidmt.acmn.utils.java;

import android.os.Process;

public final class DebugUtil {
	public static <T> void printArr(T[] arr) {
		if (arr != null) {
			for (T t : arr) {
				System.out.println(t);
			}
		}
	}

	public static StackTraceElement getCurStactrace() {
		return Thread.currentThread().getStackTrace()[0];
	}

	public static String getTInfo() {
		Thread t = Thread.currentThread();
		StackTraceElement ste = t.getStackTrace()[3];//0 vm/1getStatckTrace/2:tinfo
		return String.format("[p-%d t-%d c-%s m-%s:%d]", Process.myPid(),t.getId(), ste.getFileName(), ste.getMethodName(), ste
				.getLineNumber());
	}
}
