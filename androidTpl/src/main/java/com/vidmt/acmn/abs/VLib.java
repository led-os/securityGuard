package com.vidmt.acmn.abs;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;

import com.vidmt.acmn.utils.andr.SysUtil;

public final class VLib {
	public static boolean DEBUG = false;
	private static Application sApp;
	private static File sSdCardDir;

	public static Application app() {
		return sApp;
	}

	private static boolean sInited = false;

	public static void init(Application app, boolean debug, File sdCardDir) {
		if (sInited) {
			throw new IllegalStateException("Vidlib has been inited!!");
		}
		sInited = true;
		sApp = app;
		sSdCardDir = sdCardDir;
		DEBUG = debug;
		SysUtil.init(sApp);
	}

	public static File getSdcardDir() {
		return sSdCardDir;
	}

}
