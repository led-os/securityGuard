package com.vidmt.acmn.utils.andr;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vidmt.acmn.abs.VLib;

public class SysUtil {
	private static final String TAG = "test";
	private static final String PREF_NAME_KV = "kv_pref";

	private static Application sApp;
	private static Resources sRes;
	private static LayoutInflater sInflater;
	private static NotificationManager sNotifManager;

	public static void init(Application app) {
		if (sApp != null) {
			throw new RuntimeException("the app has been inited!");
		}
		sApp = app;
		sRes = app.getResources();
		sInflater = LayoutInflater.from(app);
		sNotifManager = (NotificationManager) sApp.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static void savePref(String... kvs) {
		if (kvs == null || kvs.length < 2) {
			return;
		}
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		for (int i = 0; i < kvs.length; i = i + 2) {
			editor.putString(kvs[i], kvs[i + 1]);
		}
		editor.commit();
	}

	public static void savePref(String key, boolean value) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static void savePref(String key, int value) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public static void savePref(String key, float value) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public static void saveStringSet(String key, Set<String> set) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.remove(key);
		editor.commit();//须先remove，否则无效
		pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		editor = pref.edit();
		editor.putStringSet(key, set);
		editor.commit();
	}

	public static Set<String> getStringSetPref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getStringSet(key, new HashSet<String>());
	}

	public static String getPref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getString(key, null);
	}

	public static String getPref(String key, String defValue) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getString(key, defValue);
	}

	public static Boolean getBooleanPref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getBoolean(key, false);
	}

	public static Boolean getBooleanPref(String key, boolean defValue) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getBoolean(key, defValue);
	}

	public static int getIntPref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getInt(key, -1);
	}

	public static float getFloatPref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		return pref.getFloat(key, -1);
	}

	public static void removePref(String key) {
		SharedPreferences pref = sApp.getSharedPreferences(PREF_NAME_KV, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.remove(key);
		editor.commit();
	}

	public static View inflate(int layoutId) {
		return sInflater.inflate(layoutId, null);
	}

	public static View inflate(int layoutId, ViewGroup rootView) {
		return sInflater.inflate(layoutId, rootView);
	}

	public static String getString(int res) {
		return sRes.getString(res);
	}

	public static String getString(int resId, Object... formatArgs) {
		return sRes.getString(resId, formatArgs);
	}

	public static Drawable getDrawable(int resId) {
		return sRes.getDrawable(resId);
	}

	public static int getColor(int resId) {
		return sRes.getColor(resId);
	}

	public static Bitmap getBitmap(int resId) {
		BitmapDrawable bd = (BitmapDrawable) getDrawable(resId);
		Bitmap bm = bd.getBitmap();
		return bm;
	}

	public static AssetManager getAsset() {
		return sApp.getAssets();
	}

	public static InputStream getRaw(int id) {
		return sApp.getResources().openRawResource(id);
	}

	public static ContentResolver getContentResolver() {
		return sApp.getContentResolver();
	}

	public int getVerCode() {
		String pkgName = sApp.getPackageName();
		PackageManager pkgmgr = sApp.getPackageManager();
		try {
			PackageInfo pkgInfo = pkgmgr.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS);
			return pkgInfo.versionCode;
		} catch (NameNotFoundException e) {
			VLog.e(TAG, e);
			return 0;
		}
	}

	public String getVerName() {
		String pkgName = sApp.getPackageName();
		PackageManager pkgmgr = sApp.getPackageManager();
		try {
			PackageInfo pkgInfo = pkgmgr.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS);
			return pkgInfo.versionName;
		} catch (NameNotFoundException e) {
			VLog.e(TAG, e);
			return "";
		}
	}

	public static PackageInfo getPkgInfo() {
		String pkgName = sApp.getPackageName();
		PackageManager pkgmgr = sApp.getPackageManager();
		try {
			return pkgmgr.getPackageInfo(pkgName, PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			VLog.e(TAG, e);
			return null;
		}
	}

	/**
	 * @param key
	 *            values for IMEI/PHONE_NO/MODEL
	 * @return
	 */
	public static String getPhoneInfo(String key) {
		if ("IMEI".equals(key)) {
			TelephonyManager tm = (TelephonyManager) VLib.app().getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getSimSerialNumber();
		} else if ("PHONE_NO".equals(key)) {
			TelephonyManager tm = (TelephonyManager) VLib.app().getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getLine1Number();
		} else if ("MODEL".equals(key)) {
			return Build.MODEL;
		}
		throw new IllegalArgumentException("cant get info:" + key);
	}

	/**
	 * @return 获取屏幕分辨率信息
	 */
	public static DisplayMetrics getDisplayMetrics() {
		return sApp.getResources().getDisplayMetrics();
	}

	/**
	 * @param tag
	 *            the user of the notification
	 * @param id
	 *            the type of notification
	 * @param notification
	 */
	public static void showNotification(int id, String tag, Notification notification) {
		sNotifManager.notify(tag, id, notification);
	}

	public static void cancelNotification(int id, String tag) {
		sNotifManager.cancel(tag, id);
	}

}
