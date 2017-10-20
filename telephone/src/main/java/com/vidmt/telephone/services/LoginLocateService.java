package com.vidmt.telephone.services;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.sasl.packet.SaslStreamElements.SASLFailure;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.LoginActivity;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.AccManager.IAccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.LocationManager;
import com.vidmt.telephone.managers.LocationManager.AbsLocationListener;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;

public class LoginLocateService extends Service {
	private static final String TAG = LoginLocateService.class.getSimpleName();
	private LocationListener mLocListener;
	private LocationManager mLocMgr;
	private boolean mIsLogining;
	// huawei add for test;
	private PowerManager.WakeLock wakeLock = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// huawei add for test;
		acquireWakeLock();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetReceiver, filter);
		initLocMgr();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (XmppManager.get().isAuthenticated() || SysUtil.getPref(PrefKeyConst.PREF_PASSWORD) == null) {
			unregisterReceiver();
			return START_STICKY;
		}
		login();
		return START_STICKY;
	}

	private void initLocMgr() {
		if (mLocMgr != null) {
			return;
		}
		mLocMgr = LocationManager.get();
		mLocMgr.start();
		mLocListener = new LocationListener();
		mLocMgr.addListener(mLocListener);
	}

	private void destoryLocMgr() {
		if (mLocMgr == null) {
			return;
		}
		mLocMgr.removeListener(mLocListener);
		mLocMgr.stop();
		mLocMgr = null;
	}

	private void unregisterReceiver() {
		try {
			unregisterReceiver(mNetReceiver);
		} catch (Throwable t) {
			// should do nothing
		}
	}

	private class LocationListener extends AbsLocationListener {
		@Override
		public void onLocationChanged(final Location loc) {
			//huawei change;
			//if (XmppManager.get().isAuthenticated())
			{
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							if(Config.DEBUG){
								VidUtil.fLog("LocationChanged, ready to upload location: " + loc.getLatitude() + loc.getLongitude());
							}
							HttpManager.get().uploadLocation(loc.getLatitude(), loc.getLongitude());
						} catch (VidException e) {
							VLog.e("test", e);
							if(Config.DEBUG){
								VidUtil.fLog("LocationChanged, uploadLocation : VidException e" + e);
							}
						}
					}
				});
			}
		}
	}

	private BroadcastReceiver mNetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				NetworkInfo networkInfo = bundle.getParcelable("networkInfo");
				if (networkInfo != null && networkInfo.isConnected() && !XmppManager.get().isAuthenticated()) {
					login();
				}
			}
		}
	};

	private void login() {
		if (mIsLogining || !VidUtil.isNetworkConnected() || XmppManager.get().isAuthenticated()) {
			return;// 已被另一方法调用处于正登录状态 或无网络 或 已登录
		}
		mIsLogining = true;
		final IAccManager accMgr = AccManager.get();
		final String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		String encodedPassword = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
		if (account != null && encodedPassword != null) {
			final String pwd = EncryptUtil.decryptLocalPwd(encodedPassword);
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						accMgr.login(account, pwd);
						// 登录成功
						mIsLogining = false;
						unregisterReceiver();
					} catch (VidException ve) {
						if (ve instanceof VHttpException) {
							VHttpException httpE = (VHttpException) ve;
							switch (httpE.getCode()) {
							case VHttpException.ERR_CODE_USER_NOT_EXISTS:
								MainThreadHandler.makeToast(R.string.account_error);
								notAuthorized();
								break;
							case VHttpException.ERR_CODE_USER_ALREADY_EXISTS:
								MainThreadHandler.makeToast(R.string.account_exist);
								break;
							//huawei add;
							case VHttpException.ERR_CODE_HTTP_SERVER_ERROR:
								mIsLogining = false;
								VidUtil.fLog("LoginLocateService login() ERR_CODE_HTTP_SERVER_ERROR  relogin()");
								if (VidUtil.isNetworkConnected()) {
							        login();
									return;
						        }
								break;
							//huawei add end;
							default:
								VidUtil.fLog(TAG, "VHttpException:" + ve);
								break;
							}
						}
						if(Config.DEBUG) {
							VidUtil.fLog("LoginLocateService login() ve : " + ve);
						}
						Throwable e = ve.getCause();
						if (e instanceof XMPPException) {
							VLog.e("test", e);
							if (e instanceof SASLErrorException) {
								SASLErrorException saslErrE = (SASLErrorException) e;
								SASLFailure failure = saslErrE.getSASLFailure();
								if (failure.getSASLError() == SASLError.not_authorized) {
									notAuthorized();
								} else {
									VidUtil.fLog(TAG, "SASLErrorException:" + e);
								}
							} else if (e instanceof XMPPErrorException) {
								XMPPErrorException xmppErrE = (XMPPErrorException) e;
								XMPPError xmppErr = xmppErrE.getXMPPError();
								Condition condition = xmppErr.getCondition();
								if (condition == Condition.not_authorized) {
									notAuthorized();
								} else {
									VidUtil.fLog(TAG, "XMPPErrorException:" + e);
								}
							}
						} else if (e instanceof IOException) {
							MainThreadHandler.makeToast(R.string.net_error);
							VidUtil.fLog(TAG, "IOException:" + e);
						} else if (e instanceof SmackException) {
							if (e instanceof ConnectionException) {
								MainThreadHandler.makeToast(R.string.connect_error);
							} else if (e instanceof NoResponseException) {
							} else {
								SmackException smackE = (SmackException) e;
								VidUtil.fLog(TAG, "SmackException:" + smackE);
							}
						} else {
							// 网络变化时，碰到过e==null情况；
							if(Config.DEBUG) {
								VidUtil.fLog("LoginLocateService ve.getCause() : " + e);
								MainThreadHandler.makeToast(R.string.error_unknown);
							}
						}
						// 登录失败
						mIsLogining = false;
						// 若有网络,直接再重连。无网络刚交给BroadcastReceiver处理
						//huawei change 避免有网登录不上进入死循环，比如server有故障或在操作；
						if (VidUtil.isNetworkConnected()) {
							login();
						}
					}
				}
			});
		}else {
			mIsLogining = false; // huawei add;
		}
	}

	private void notAuthorized() {
		SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
		VidUtil.startNewTaskActivity(LoginActivity.class);
		stopSelf();
	}

	@Override
	public void onDestroy() {
		destoryLocMgr();
		releaseWakeLock();
		VLog.i("test", "LoginLocateService onDestroy");
		super.onDestroy();
	}

	//huawei add for test
	private void acquireWakeLock() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, getClass()
					.getCanonicalName());
			if (null != wakeLock) {
				//   Log.i(TAG, "call acquireWakeLock");
				wakeLock.acquire();
			}
		}
	}

	// 释放设备电源锁
	private void releaseWakeLock() {
		if (null != wakeLock && wakeLock.isHeld()) {
			//   Log.i(TAG, "call releaseWakeLock");
			wakeLock.release();
			wakeLock = null;
		}
	}

	//huawei test end;

}
