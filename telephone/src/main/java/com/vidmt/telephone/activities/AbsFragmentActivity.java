package com.vidmt.telephone.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.StreamErrorException;
import org.jivesoftware.smack.packet.StreamError;

public abstract class AbsFragmentActivity extends FragmentActivity {
	public static final String ACTION_EXIT = "com.vidmt.action.EXIT";
	private BroadcastReceiver receiver;
	private View mReconnectLayout;
	private TextView mReconnectMsgTv;
	private boolean isReconnecting;

	public static final void exitAll() {
		VLib.app().sendBroadcast(new Intent(ACTION_EXIT));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_EXIT);
		receiver = new InnerReceiver(this);
		this.registerReceiver(receiver, filter);
		XmppManager.get().addXmppListener(mOnConnectionListener);
	}

	protected void initReconnectView(View reconnectView) {
		mReconnectLayout = reconnectView.findViewById(R.id.reconnect_layout);
		mReconnectMsgTv = (TextView) mReconnectLayout.findViewById(R.id.msg);
		if (!isReconnecting) {
			mReconnectLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		//防止curUser为空的情况；
		//AccManager.get().getCurUser();
		//防止回到前台已经登录上的情况还显示正在重连中。
		if (XmppManager.get().isAuthenticated()) {
			if(mReconnectLayout != null){
				mReconnectMsgTv.setText(R.string.reconnect_success);
				mReconnectLayout.setVisibility(View.GONE);
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		receiver = null;
		XmppManager.get().removeXmppListener(mOnConnectionListener);
		super.onDestroy();
	}

	private static class InnerReceiver extends BroadcastReceiver {
		public Activity mActivity;

		public InnerReceiver(Activity act) {
			mActivity = act;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mActivity != null) {
				mActivity.finish();
				mActivity = null;
			}
		}
	}

	private AbsOnConnectionListener mOnConnectionListener = new AbsOnConnectionListener() {
		@Override
		public void onConnected(){
			if(Config.DEBUG){
				VidUtil.fLog("onConnected", "===onConnected====");
			}
			if (mReconnectLayout == null) {
				return;
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mReconnectMsgTv.setText(R.string.reconnect_success);
					mReconnectLayout.setVisibility(View.GONE);
				}
			});
		}
		@Override
		public void connectionClosed() {
			if(Config.DEBUG){
				VidUtil.fLog("RECONNECTION", "===connectionClosed====");
			}
			VLog.i("test", "conn:connectionClosed");
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			if(Config.DEBUG){
				VidUtil.fLog("RECONNECTION", "===connectionClosedOnError====msg:"+e.getMessage());
				VidUtil.playSound(R.raw.beep, true);
			}
			if (e != null) {
				//huawei change;
				if(e instanceof StreamErrorException){
					StreamErrorException streamError = (StreamErrorException)e;
					if(streamError.getStreamError().getCondition() == StreamError.Condition.conflict){
						VidUtil.logoutApp();
						MainThreadHandler.makeLongToast(R.string.same_account_logined);
						return;
					}
				}
				VidUtil.fLog("RECONNECTION", "=========connectionClosedOnError=========msgs:"+ e.getMessage());
				VLog.d("test", e);
			} else {
				VLog.i("test", "conn:" + XmppManager.get().isAuthenticated());
			}
		}

		@Override
		public void reconnectingIn(final int seconds) {
			if(Config.DEBUG){
				VidUtil.fLog("RECONNECTION", "=========reconnectingIn=========seconds:"+seconds+"秒");
			}
			if (mReconnectLayout == null) {
				return;
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mReconnectLayout.setVisibility(View.VISIBLE);
					if (seconds != 0) {
						mReconnectMsgTv.setText(seconds + " " + getString(R.string.seconds_later_reconnect));
					} else {
						mReconnectMsgTv.setText(R.string.reconnecting);
					}
				}
			});
		}

		@Override
		public void reconnectionFailed(Exception e) {
			if(Config.DEBUG){
				VidUtil.fLog("RECONNECTION", "=========reconnectionFailed=========msg:"+e.getMessage());
			}
			if (mReconnectLayout == null) {
				return;
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					//huawei change;
					//mReconnectLayout.setVisibility(View.VISIBLE);
					//mReconnectMsgTv.setText(R.string.reconnect_fail_and_reconnecting);
					mReconnectMsgTv.setText(R.string.reconnect_failed);
					mReconnectLayout.setVisibility(View.GONE);
				}
			});
			VLog.d("test", e);
		}

		@Override
		public void reconnectionSuccessful() {
			if(Config.DEBUG){
				VidUtil.fLog("RECONNECTION", "=========reconnectionSuccessful=========");
				VidUtil.stopSound();
			}
			if (mReconnectLayout == null) {
				return;
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mReconnectMsgTv.setText(R.string.reconnect_success);
					mReconnectLayout.setVisibility(View.GONE);
				}
			});
		}
	};

}
