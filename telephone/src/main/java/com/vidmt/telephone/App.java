package com.vidmt.telephone;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Application;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;
import com.umeng.analytics.MobclickAgent;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.telephone.listeners.IQExtReceivedListener;
import com.vidmt.telephone.listeners.PEPEventListener;
import com.vidmt.telephone.listeners.RelationshipChangedListener;
import com.vidmt.telephone.listeners.ShowMsgNotifListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

public class App extends Application implements UncaughtExceptionHandler {
	private static App sApp;
	private UncaughtExceptionHandler mDefaultExceptionHandler;

	public static App get() {
		return sApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;
		SDKInitializer.initialize(this);
		MobclickAgent.setDebugMode(Config.DEBUG);
		File sdcadDir = new File(Environment.getExternalStorageDirectory(), Config.SDCARD_DIR);
		VLib.init(this, Config.DEBUG, sdcadDir);
		FileStorage.init();
		mDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		initXmpp();
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
					xmppMgr.addXmppListener(RelationshipChangedListener.get());
					//huawei change;
					if(!xmppMgr.containsXmppListener(PEPEventListener.get())) {
						VidUtil.fLog("app addXmppListener(PEPEventListener.get())");
						xmppMgr.addXmppListener(PEPEventListener.get());
					}else {
						VidUtil.fLog("app containsXmppListener(PEPEventListener.get())");
					}
				}
			});
		} catch (SmackException | IOException | XMPPException e) {
			VLog.e("test", e);
		}
	}

	@Override
	public void onTerminate() {
		VidUtil.fLog("app terminate");
		FLog.endAllTag();
		super.onTerminate();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		VidUtil.fLog("uncaughtException: " + thread.getName() + "," + CommUtil.formatException(ex));
		// Bundle bundle = new Bundle();
		// bundle.putBoolean(ExtraConst.EXTRA_RESTART_FROM_CRASH, true);
		AccManager.get().logout();
		// TODO: 2016-08-23   huawei add test
		ServiceManager.get().stopService();
		// VidUtil.startNewTaskActivity(SplashActivity.class, bundle);// 崩溃，立马重启
		FLog.endAllTag();
		AbsBaseActivity.exitAll();
		if (mDefaultExceptionHandler != null) {
			mDefaultExceptionHandler.uncaughtException(thread, ex);
		}
	}
}
