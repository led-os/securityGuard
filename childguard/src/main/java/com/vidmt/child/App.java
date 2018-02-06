package com.vidmt.child;

import android.app.Application;
import android.os.Bundle;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.child.activities.SplashActivity;
import com.vidmt.child.listeners.IQExtReceivedListener;
import com.vidmt.child.listeners.ShowMsgNotifListener;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

public class App extends Application implements UncaughtExceptionHandler {
	private static App sApp;
	private UncaughtExceptionHandler mDefaultExceptionHandler;

	public static App get() {
		return sApp;
	}

	private static void initXmpp() {
		final IXmppManager xmppMgr = XmppManager.get();
		try {
			xmppMgr.init(Config.DEBUG, Config.XMPP_HOST, Config.XMPP_PORT, Config.XMPP_SERVICE, Config.XMPP_RESOURCE);
			xmppMgr.addXmppListener(new AbsOnConnectionListener() {
				@Override
				public void authenticated(XMPPConnection connection, boolean resumed) {
					xmppMgr.addXmppListener(ShowMsgNotifListener.get());
					xmppMgr.addXmppListener(IQExtReceivedListener.get());
				}
			});
		} catch (SmackException | IOException | XMPPException e) {
			VLog.e("test", e);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;
		File sdcadDir = new File(Environment.getExternalStorageDirectory(), Config.SDCARD_DIR);
		VLib.init(this, Config.DEBUG, sdcadDir);
		SDKInitializer.initialize(this);
		UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
//		MobclickAgent.setDebugMode(Config.DEBUG);
		FileStorage.init();
		mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		initXmpp();
	}

	@Override
	public void onTerminate() {
		FLog.d("app terminate");
		FLog.endAllTag();
		super.onTerminate();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		FLog.d(thread.getName() + "," + CommUtil.formatException(ex));
		if (!Config.DEBUG) {
			Bundle bundle = new Bundle();
			bundle.putBoolean(ExtraConst.EXTRA_RESTART_FROM_CRASH, true);
			VidUtil.startNewTaskActivity(SplashActivity.class, bundle);//崩溃，立马重启
		}
		FLog.endAllTag();
		if (mDefaultExceptionHandler != null) {
			mDefaultExceptionHandler.uncaughtException(thread, ex);
		}
	}

}