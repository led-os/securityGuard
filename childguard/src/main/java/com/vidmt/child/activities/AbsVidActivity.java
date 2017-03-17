package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

public class AbsVidActivity extends AbsBaseActivity {
	private static AbsVidActivity mInstance;
	private View mReconnectLayout;
	private TextView mReconnectMsgTv;
	private AbsOnConnectionListener mOnConnectionListener = new AbsOnConnectionListener() {
		@Override
		public void connectionClosed() {
			VLog.i("test", "conn:connectionClosed");
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			if (e != null) {
				if (e.getMessage().contains("stream:error") && e.getMessage().contains("conflict")) {
					VidUtil.loginConflict();
					MainThreadHandler.makeLongToast(R.string.same_account_logined);
					return;
				} else {
					MainThreadHandler.makeLongToast(R.string.net_go_wrong_please_check);
				}
				VLog.d("test", e);
			} else {
				VLog.i("test", "conn:" + XmppManager.get().isAuthenticated());
			}
		}

		@Override
		public void reconnectingIn(final int seconds) {
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
		public void reconnectionFailed(final Exception e) {
			if (mReconnectLayout == null) {
				return;
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mReconnectLayout.setVisibility(View.VISIBLE);
					mReconnectMsgTv.setText(R.string.reconnect_fail_and_reconnecting);
				}
			});
			VLog.d("test", e);
		}

		@Override
		public void reconnectionSuccessful() {
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

	public static String getLastestActivityClzName() {
		if (mInstance == null) {
			return null;
		}
		return mInstance.getClass().getName();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		XmppManager.get().addXmppListener(mOnConnectionListener);
	}

	protected void initReconnectView(View reconnectView) {
		mReconnectLayout = reconnectView.findViewById(R.id.reconnect_layout);
		mReconnectMsgTv = (TextView) mReconnectLayout.findViewById(R.id.msg);
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mInstance = this;
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		XmppManager.get().removeXmppListener(mOnConnectionListener);
		super.onDestroy();
	}

}
