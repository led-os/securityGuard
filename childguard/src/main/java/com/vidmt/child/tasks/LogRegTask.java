package com.vidmt.child.tasks;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Config;
import com.vidmt.child.Const;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.ChattingActivity;
import com.vidmt.child.activities.MainActivity;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.AccManager.IAccManager;
import com.vidmt.child.managers.ServiceManager;
import com.vidmt.child.utils.EncryptUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.listeners.OnConnectionListener;
import com.vidmt.xmpp.listeners.XmppListenerHolder;
import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.sasl.packet.SaslStreamElements.SASLFailure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogRegTask extends AsyncTask<String, Integer, VidException> {
	private Activity mActivity;
	private String mAccount, mPwd;
	private boolean mForRegister;
	private LoadingDlg mLoadingDlg;

	public LogRegTask(Activity act, String account, String pwd, boolean forRegister) {
		mActivity = act;
		mAccount = account;
		mPwd = pwd;
		mForRegister = forRegister;
		mLoadingDlg = new LoadingDlg(act, R.string.loading);
	}

	@Override
	protected void onPreExecute() {
		mLoadingDlg.show();
		mLoadingDlg.setCancelable(false);
		super.onPreExecute();
	}

	@Override
	protected VidException doInBackground(String... params) {
		VidException vidEx = null;
		IAccManager accMgr = AccManager.get();
		if (mForRegister) {// 注册
			try {
				Map<String, String> attributes = new HashMap<String, String>();
				attributes.put(ExtraConst.EXTRA_ACCOUNT_IDENTIFY, Config.APP_ID + "," + Config.APP_OS);
				accMgr.register(mAccount, mPwd, attributes);
				// 注册成功
				mLoadingDlg.dismiss();
				SysUtil.savePref(PrefKeyConst.PREF_FIRST_LOGIN, true);
			} catch (VidException e) {// 注册失败
				mLoadingDlg.dismiss();
				return e;
			}
		}
		try {
			String account = null;
			boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
			if (isBabyClient) {
				account = Const.BABY_ACCOUNT_PREFIX + mAccount;
			} else {
				account = mAccount;
			}
			accMgr.login(account, mPwd);
			// 登录成功
			SysUtil.savePref(PrefKeyConst.PREF_ACCOUNT, mAccount);
			SysUtil.savePref(PrefKeyConst.PREF_PASSWORD, EncryptUtil.encryptLocalPwd(mPwd));

			CgUserIQ selfInfoIq = accMgr.getUserInfo(null);// 得到自己所有资料
			CgUserIQ sideInfoIq = accMgr.getUserInfo(VidUtil.getSideName());// 得到对方资料
			LvlIQ lvlInfoIq = null;
			if (isBabyClient) {
				UserUtil.initBabyInfo(selfInfoIq);
				UserUtil.initParentInfo(sideInfoIq);
				lvlInfoIq = accMgr.getLvlInfo(sideInfoIq.code == null ? LvlType.NONE : sideInfoIq.code);// 得到等级信息
			} else {
				UserUtil.initParentInfo(selfInfoIq);
				UserUtil.initBabyInfo(sideInfoIq);
				lvlInfoIq = accMgr.getLvlInfo(selfInfoIq.code == null ? LvlType.NONE : selfInfoIq.code);
			}
			UserUtil.initLvl(lvlInfoIq);
			XmppListenerHolder.callListeners(OnConnectionListener.class,
					new IXmppListenerExecutor<OnConnectionListener>() {
						@Override
						public void execute(OnConnectionListener listener) {
							listener.onConnected();
						}
					});
			ServiceManager.get().startService(true);// 启动Service服务
			mLoadingDlg.dismiss();
			if (isBabyClient) {
				mActivity.startActivity(new Intent(mActivity, ChattingActivity.class));
			} else {
				mActivity.startActivity(new Intent(mActivity, MainActivity.class));
			}
			mActivity.finish();
		} catch (VidException e) {
			mLoadingDlg.dismiss();
			vidEx = e;
		}
		return vidEx;
	}

	@Override
	protected void onPostExecute(VidException result) {
		if (result != null) {
			VLog.e("test", result);
			Throwable e = result.getCause();
			if (e instanceof XMPPException) {
				XMPPException xmppE = (XMPPException) e;
				if (e instanceof SASLErrorException) {
					SASLErrorException saslErrE = (SASLErrorException) e;
					SASLFailure failure = saslErrE.getSASLFailure();
					if (failure.getSASLError() == SASLError.not_authorized) {
						MainThreadHandler.makeToast(R.string.account_error);
					} else {
						MainThreadHandler.makeToast("出错:" + failure.getSASLErrorString());
					}
				} else if (e instanceof XMPPErrorException) {
					XMPPErrorException xmppErrE = (XMPPErrorException) e;
					XMPPError xmppErr = xmppErrE.getXMPPError();
					Condition condition = xmppErr.getCondition();
					if (condition == Condition.not_authorized) {
						MainThreadHandler.makeToast(R.string.account_error);
					} else if (condition == Condition.conflict) {
						MainThreadHandler.makeToast(R.string.account_exist);
					} else if (condition == Condition.internal_server_error
							|| condition == Condition.remote_server_not_found) {
						MainThreadHandler.makeToast(R.string.remote_server_error);
					} else if (condition == Condition.remote_server_timeout) {
						MainThreadHandler.makeToast(R.string.timeout);
					} else {
						MainThreadHandler.makeToast("出错:" + xmppErr.getConditionText());
					}
				}
			} else if (e instanceof IOException) {
				IOException ioE = (IOException) e;
				MainThreadHandler.makeToast(R.string.net_error);
			} else if (e instanceof SmackException) {
				SmackException smackE = (SmackException) e;
				MainThreadHandler.makeToast("出错:" + smackE.getMessage());
			} else {
				MainThreadHandler.makeToast(R.string.error_unknown);
			}
		}
		super.onPostExecute(result);
	}

}