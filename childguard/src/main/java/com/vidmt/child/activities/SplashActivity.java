package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.igexin.sdk.PushManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.NetWarnDlg;
import com.vidmt.child.dlgs.UnauthAppDlg;
import com.vidmt.child.managers.ServiceManager;
import com.vidmt.child.services.LoginLocateService;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

@ContentView(R.layout.activity_splash)
public class SplashActivity extends AbsVidActivity {
	private boolean mIsRestartFromCrash;
	private AbsOnConnectionListener mOnConnectionListener = new AbsOnConnectionListener() {
		@Override
		public void onConnected() {
			startMain();
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!VidUtil.isAppAuthroized()) {
			new UnauthAppDlg(this).show();
			return;
		}
		mIsRestartFromCrash = getIntent().getBooleanExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, false);
		if (mIsRestartFromCrash) {// 崩溃后重启
			moveTaskToBack(false);
		}
		ViewUtils.inject(this);
		PushManager.getInstance().initialize( this.getApplicationContext() );
		XmppManager.get().addXmppListener(mOnConnectionListener);

		String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		String encryptedPwd = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(encryptedPwd)) {
			startActivity(new Intent(this, LoginModeActivity.class));
			finish();
		} else {
			if (!NetUtil.isNetworkAvaiable()) {
				new NetWarnDlg(this).show();
				return;
			}
			if (ServiceManager.isServiceRunning(LoginLocateService.class) && XmppManager.get().isAuthenticated()) {
				ServiceManager.get().startService(true);
				startMain();
				finish();
			} else {
				ServiceManager.get().startService(false);
			}
		}
	}

	private void startMain() {
		boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
		Intent i = new Intent(this, MainActivity.class);
		if (isBabyClient) {
			i = new Intent(this, ChattingActivity.class);
		}
		i.putExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, mIsRestartFromCrash);
		startActivity(i);
	}

	@Override
	protected void onDestroy() {
		XmppManager.get().removeXmppListener(mOnConnectionListener);
		super.onDestroy();
	}

}
