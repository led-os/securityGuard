package com.vidmt.telephone;

import java.io.IOException;
import java.util.Properties;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Config {
	public static final boolean DEBUG;
	public static final boolean DEBUG_UNCHECK_VERCODE;
	public static final boolean DEBUG_LOCATION;
	public static final String APP_ID;
	public static final String XMPP_HOST;
	public static final int XMPP_PORT;
	public static final String XMPP_SERVICE;
	public static final String WEB_RES_SERVER;
	public static final String WEB_API_SERVER;
	public static final String WEB_UTIL_SERVER;
	public static final String SDCARD_DIR;
	public static final String XMPP_RESOURCE;
	public static final String KEY_MSG_CYPHER_SEED;
	public static final String URL_CONF;
	public static final String URL_LATEST_APK;
	public static final String URL_CG_APK;
	public static final String WXPAY_APP_ID;
	public static final boolean MMPAY_DEBUG;
	public static final String MMPAY_APP_ID;
	public static final String MMPAY_APP_KEY;
	public static final String MMPAY_PAYCODE_MOBILE;
	public static final String MMPAY_PAYCODE_UNICOM;
	public static final String MMPAY_PAYCODE_TELECOM;
	public static final String SMS_APPKEY;
	public static final String SMS_APPSECRET;

	static {
		Properties prop = new Properties();
		try {
			prop.load(App.get().getResources().openRawResource(R.raw.conf));
		} catch (IOException e) {
			e.printStackTrace();
		}
		DEBUG = Boolean.parseBoolean(prop.getProperty("system.debug"));
		DEBUG_UNCHECK_VERCODE = Boolean.parseBoolean(prop.getProperty("system.vercode.debug"));
		DEBUG_LOCATION = Boolean.parseBoolean(prop.getProperty("system.location.debug"));
		APP_ID = getProp(prop, "app.id");
		SDCARD_DIR = getProp(prop, "sdcardDir.name");
		XMPP_SERVICE = getProp(prop, "xmpp.service");
		int ver = 10000;
		String channel = "official";
		try {
			ver = App.get().getPackageManager().getPackageInfo(App.get().getPackageName(),
					PackageManager.GET_CONFIGURATIONS).versionCode;
			//huawei add for channel;
			channel = App.get().getPackageManager().getApplicationInfo(App.get().getPackageName(),
					PackageManager.GET_META_DATA).metaData.getString("UMENG_CHANNEL");
		} catch (NameNotFoundException e) {
		}
		XMPP_RESOURCE = getProp(prop, "xmpp.resource") + ver + "-" + channel;
		KEY_MSG_CYPHER_SEED = getProp(prop, "key.message.cypherSeed");
		WEB_RES_SERVER = getProp(prop, "web.url.res");
		WEB_API_SERVER = getProp(prop, "web.url.api");
		WEB_UTIL_SERVER = getProp(prop, "web.url.util");
		XMPP_HOST = getProp(prop, "xmpp.host");
		XMPP_PORT = Integer.parseInt(getProp(prop, "xmpp.port"));
		URL_CONF = getProp(prop, "web.url.conf");
		URL_LATEST_APK = getProp(prop, "web.url.latestApk");
		URL_CG_APK = getProp(prop, "web.url.childguard.apk");
		
		WXPAY_APP_ID = getProp(prop, "web.wxpay.appId");
		
		MMPAY_APP_ID = getProp(prop, "web.mmpay.appId");
		MMPAY_APP_KEY = getProp(prop, "web.mmpay.appKey");
		MMPAY_DEBUG = MMPAY_APP_KEY.equals(getProp(prop, "mmpay.debugK"));
		MMPAY_PAYCODE_MOBILE = getProp(prop, "web.mmpay.paycode.mobile");
		MMPAY_PAYCODE_UNICOM = getProp(prop, "web.mmpay.paycode.unicom");
		MMPAY_PAYCODE_TELECOM = getProp(prop, "web.mmpay.paycode.telecom");
		
		SMS_APPKEY = getProp(prop, "sms.appkey");
		SMS_APPSECRET = getProp(prop, "sms.appsecret");
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
