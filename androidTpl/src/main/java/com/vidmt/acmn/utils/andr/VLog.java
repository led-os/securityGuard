package com.vidmt.acmn.utils.andr;

import android.util.Log;

import com.vidmt.acmn.abs.VLib;

public class VLog {
	public static void v(String tag, String msg) {
		if (VLib.DEBUG) {
			Log.v(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (VLib.DEBUG) {
			Log.i(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (VLib.DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (VLib.DEBUG) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		Log.e(tag, msg);
	}

	// //////////////////////////////////////////////
	public static void v(String tag, Throwable e) {
		if (VLib.DEBUG) {
			Log.v(tag, "", e);
		}
	}

	public static void i(String tag, Throwable e) {
		if (VLib.DEBUG) {
			Log.i(tag, "", e);
		}
	}

	public static void d(String tag, Throwable e) {
		if (VLib.DEBUG) {
			Log.d(tag, "", e);
		}
	}

	public static void w(String tag, Throwable e) {
		if (VLib.DEBUG) {
			Log.w(tag, "", e);
		}
	}

	public static void e(String tag, Throwable e) {
		Log.e(tag, "", e);
	}

	public static void e(String tag, String msg, Throwable e) {
		Log.e(tag, msg, e);
	}

}
