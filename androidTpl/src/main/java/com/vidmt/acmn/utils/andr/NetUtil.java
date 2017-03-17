package com.vidmt.acmn.utils.andr;

import java.net.InetSocketAddress;
import java.util.Locale;

import org.apache.http.HttpHost;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Proxy;

import com.vidmt.acmn.abs.VLib;

public class NetUtil {
	public static final int NETWORN_NONE = 0;
	public static final int NETWORN_WIFI = 1;
	public static final int NETWORN_MOBILE = 2;

	public static int getNetworkState() {
		Context context = VLib.app();
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// Wifi
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_WIFI;
		}
		// 2G、3G
		state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_MOBILE;
		}
		return NETWORN_NONE;
	}

	// 判断网线状态,true:有，false:无
	public static boolean isNetworkAvaiable() {
		Context context = VLib.app();
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			NetworkInfo activeNet = connectivityManager.getActiveNetworkInfo();
			if (activeNet == null) {
				return false;
			}
			return activeNet.isConnected();
			// NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
			// if (info != null) {
			// for (NetworkInfo network : info) {
			// if (network.getState() == NetworkInfo.State.CONNECTED) {
			// return true;
			// }
			// }
			// }
		}
		// return false;
	}

	public static boolean isNetConnected(int type) {
		Context context = VLib.app();
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mgr.getNetworkInfo(type);
		if (info != null) {
			return info.isConnected();
		}
		return false;
	}

	public static java.net.Proxy getApnJavaProxy() {
		String apnName = getApnName();
		if (apnName != null && apnName.length() > 0) {
			apnName = apnName.toLowerCase(Locale.CHINA);
			if (apnName.contains("uninet") || apnName.contains("3gnet") || apnName.contains("cmnet")
					|| apnName.contains("ctnet")) {
				return null;
			}
			String host = "";
			int port;
			if (apnName.contains("cmwap") || apnName.contains("3gwap") || apnName.contains("uniwap")) {
				host = "10.0.0.172";
				port = 80;
			} else if (apnName.contains("ctwap")) {
				host = "10.0.0.200";
				port = 80;
			} else {
				host = Proxy.getDefaultHost();
				port = Proxy.getDefaultPort();
			}
			if (host != null) {
				java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));
				return p;
			}

		}
		return null;
	}

	public static HttpHost getApnHostProxy() {
		String apnName = getApnName();
		if (apnName != null && apnName.length() > 0) {
			apnName = apnName.toLowerCase();
			if (apnName.contains("uninet") || apnName.contains("3gnet") || apnName.contains("cmnet")
					|| apnName.contains("ctnet")) {
				return null;
			}

			String host = "";
			int port;
			if (apnName.contains("cmwap") || apnName.contains("3gwap") || apnName.contains("uniwap")) {
				host = "10.0.0.172";
				port = 80;
			} else if (apnName.contains("ctwap")) {
				host = "10.0.0.200";
				port = 80;
			} else {
				host = Proxy.getDefaultHost();
				port = Proxy.getDefaultPort();
			}

			if (host != null) {
				return new HttpHost(host, port);
			}
		}
		return null;
	}

	// ////////////////////////
	private static String getApnName() {
		Context context = VLib.app();
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiConn = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiConn.isConnected()) { // 如果有wifi连接，不需要代理
			return null;
		}
		NetworkInfo net = mgr.getActiveNetworkInfo();
		if (net != null) {
			return net.getExtraInfo();
		}
		return null;
	}

}
