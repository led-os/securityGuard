package com.vidmt.telephone.activities;

import android.content.Intent;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;

/**
 * 被申请添加好友界面
 */
@ContentView(R.layout.activity_add_friend)
public class AddFriendActivity extends AbsVidActivity {
	@ViewInject(R.id.avatar)
	private ImageView mAvatarIv;
	@ViewInject(R.id.nick)
	private TextView mNickTv;
	@ViewInject(R.id.gender)
	private TextView mGenderTv;
	@ViewInject(R.id.age)
	private TextView mAgeTv;
	private String mUid;
	private User mUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!XmppManager.get().isAuthenticated()) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			return;
		}
		ViewUtils.inject(this);
		initTitle();
		mUid = getIntent().getStringExtra(ExtraConst.EXTRA_UID);
		initData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (!XmppManager.get().isAuthenticated()) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			return;
		}
		mUid = getIntent().getStringExtra(ExtraConst.EXTRA_UID);

		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SysUtil.cancelNotification(Const.NOTIF_ID_CHAT_RCV, mUid);
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.friend_apply_for);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					mUser = HttpManager.get().getUserInfo(mUid);
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							VidUtil.asyncCacheAndDisplayAvatar(mAvatarIv, mUser.avatarUri, true);
							mNickTv.setText(mUser.getNick());
							mGenderTv.setText(mUser.gender == 'M' ? R.string.male : R.string.female);
							mAgeTv.setText(getString(R.string._age, mUser.age + ""));
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	@OnClick({ R.id.back, R.id.detail, R.id.agree, R.id.reject })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.detail:
			Intent intent = new Intent(this, PersonInfoActivity.class);
			intent.putExtra(ExtraConst.EXTRA_UID, mUser.uid);
			startActivity(intent);
			break;
		case R.id.agree:
			AccManager.get().addFriend(mUid);
			finish();
			break;
		case R.id.reject:
			AccManager.get().deleteFriend(mUid);
			finish();
			break;
		}
	}
}
