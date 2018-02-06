package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.NetWarnDlg;
import com.vidmt.telephone.dlgs.UnauthAppDlg;
import com.vidmt.telephone.managers.AdManager;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.tasks.UpdateTask;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.utils.VipInfoUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

public class SplashActivity extends AbsVidActivity {
	private boolean mIsRestartFromCrash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!VidUtil.isAppAuthroized()) {
			new UnauthAppDlg(this).show();
			return;
		}
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				VipInfoUtil.init();
				UpdateTask.launchUpdateTask(SplashActivity.this, false);
			}
		});
		mIsRestartFromCrash = getIntent().getBooleanExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, false);
		if (mIsRestartFromCrash) {// 崩溃后重启
			moveTaskToBack(false);
		}
		AdManager.get().init(SplashActivity.this);// 初始化广告(目前已经去掉豌豆广告)和“直接添加好友”权限
		XmppManager.get().addXmppListener(mOnConnectionListener);
		boolean enteredFirstTime = SysUtil.getBooleanPref(ExtraConst.EXTRA_IS_FIRST_ENTERED, true);
		if (enteredFirstTime) {// 首次进入，展示引导页
			setContentView(R.layout.view_first_enter);
			SysUtil.savePref(ExtraConst.EXTRA_IS_FIRST_ENTERED, false);
		} else {
			setContentView(R.layout.activity_splash);
			String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
			String encryptedPwd = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
			if (TextUtils.isEmpty(account) || TextUtils.isEmpty(encryptedPwd)) {
				startActivity(new Intent(this, LoginActivity.class));
				finish();
			} else {
				if (!NetUtil.isNetworkAvaiable()) {
					new NetWarnDlg(this).show();
					return;
				}
				// 退出后仍接收消息
				ServiceManager.get().startService();
				if (XmppManager.get().isAuthenticated()) {
					startMain();
					finish();
				}
			}
		}
	}

	private AbsOnConnectionListener mOnConnectionListener = new AbsOnConnectionListener() {
		@Override
		public void onConnected() {
			startMain();
			finish();
		}
	};

	private void startMain() {
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, mIsRestartFromCrash);
		startActivity(i);
	}

	@Override
	protected void onDestroy() {
		XmppManager.get().removeXmppListener(mOnConnectionListener);
		super.onDestroy();
	}

}