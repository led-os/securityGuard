package com.vidmt.child;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.io.IOException;
import java.util.Properties;

public class Config {
	public static final boolean DEBUG;
	public static final String BUILD_TIME;
	public static final String XMPP_HOST;
	public static final int XMPP_PORT;
	public static final String XMPP_SERVICE;
	public static final String WEB_RES_SERVER;
	public static final String WEB_API_SERVER;
	public static final String SDCARD_DIR;
	public static final String APP_ID;
	public static final String APP_CHILD_ID;
	public static final String APP_OS;
	public static final String XMPP_RESOURCE;
	public static final String KEY_MSG_CYPHER_SEED;
	public static final String URL_CONF;
	public static final String URL_LATEST_APK;
	public static final String URL_CHILD_LATEST_APK;
	public static final String WXPAY_APP_ID;
	public static final String SMS_APPKEY;
	public static final String SMS_APPSECRET;

	public static final boolean DEBUG_SHARE_LOC;

	static {
		Properties prop = new Properties();
		try {
			prop.load(App.get().getResources().openRawResource(R.raw.conf));
		} catch (IOException e) {
			e.printStackTrace();
		}
		DEBUG = Boolean.parseBoolean(prop.getProperty("system.debug"));
		BUILD_TIME = prop.getProperty("system.buildTime");
		XMPP_PORT = Integer.parseInt(getProp(prop, "xmpp.port"));
		SDCARD_DIR = getProp(prop, "sdcardDir.name");
		APP_ID = getProp(prop, "app.id");
		APP_CHILD_ID = getProp(prop, "app.child.id");
		APP_OS = getProp(prop, "app.os");
		int ver = 10000;
		try {
			ver = App.get().getPackageManager().getPackageInfo(App.get().getPackageName(),
					PackageManager.GET_CONFIGURATIONS).versionCode;
		} catch (NameNotFoundException e) {
		}
		XMPP_RESOURCE = getProp(prop, "xmpp.resource");// + ver;
		XMPP_SERVICE = getProp(prop, "xmpp.service");
		WEB_RES_SERVER = getProp(prop, "web.url.res");
		WEB_API_SERVER = getProp(prop, "web.url.api");
		KEY_MSG_CYPHER_SEED = getProp(prop, "key.message.cypherSeed");
		XMPP_HOST = getProp(prop, "xmpp.host");
		URL_CONF = getProp(prop, "web.url.conf");
		URL_LATEST_APK = getProp(prop, "web.url.latestApk");
		URL_CHILD_LATEST_APK = getProp(prop, "web.url.child.latestApk");
		
		WXPAY_APP_ID = getProp(prop, "web.wxpay.appId");
		
		SMS_APPKEY = getProp(prop, "sms.appkey");
		SMS_APPSECRET = getProp(prop, "sms.appsecret");
		if (DEBUG) {
			DEBUG_SHARE_LOC = Boolean.parseBoolean(getProp(prop, "system.debug.share_location"));
		} else {
			DEBUG_SHARE_LOC = false;
		}
	}

	private static String getProp(Properties prop, String key) {
		String value = null;
		if (DEBUG) {
			value = prop.getProperty(key + ".debug");
		}
		if (value == null) {
			value = prop.getProperty(key);
		}
		return value;
	}

}
