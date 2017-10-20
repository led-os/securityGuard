package com.vidmt.telephone.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener.AbsOnConnectionListener;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.StreamError;

public class AbsVidActivity extends AbsBaseActivity {
	private View mReconnectLayout;
	private TextView mReconnectMsgTv;

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
		XmppManager.get().removeXmppListener(mOnConnectionListener);
		super.onDestroy();
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
				if(e instanceof XMPPException.StreamErrorException){
					XMPPException.StreamErrorException streamError = (XMPPException.StreamErrorException)e;
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
			if (XmppManager.get().isAuthenticated()) {//防止未通知到reconnect success消息
				reconnectionSuccessful();
			}
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
