package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.AddFriendDlg;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.Enums.AddFriendType;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;

/**
 * 他人信息页，类似账户设置页
 */
public class PersonInfoActivity extends AbsVidActivity implements OnClickListener {
	private ImageView mAvatarIv;
	private TextView mNickTv, mGenderTv, mAgeTv, mSignatureTv, mAddressTv;
	private CheckBox mLocSecretCb, mAvoidDisturbCb;
	private String mUid;
	private Button mActionBtn;
	private AddFriendType mAddType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personinfo);
		initTitle();
		mAvatarIv = (ImageView) findViewById(R.id.avatar);
		mNickTv = (TextView) findViewById(R.id.nick);
		mGenderTv = (TextView) findViewById(R.id.gender);
		mAgeTv = (TextView) findViewById(R.id.age);
		mSignatureTv = (TextView) findViewById(R.id.signature);
		mAddressTv = (TextView) findViewById(R.id.address);
		mLocSecretCb = (CheckBox) findViewById(R.id.loc_secret_chk);
		mAvoidDisturbCb = (CheckBox) findViewById(R.id.avoid_disturb_chk);
		mActionBtn = (Button) findViewById(R.id.action);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.action).setOnClickListener(this);
		initData();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.person_info);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		final LoadingDlg loadingDlg = new LoadingDlg(this, R.string.loading);
		loadingDlg.show();
		mUid = getIntent().getStringExtra(ExtraConst.EXTRA_UID);
		if (mUid.equals(AccManager.get().getCurUser().uid)) {
			mActionBtn.setVisibility(View.GONE);
		}
		mActionBtn.setEnabled(false);
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final User user = HttpManager.get().getUserInfo(mUid);
					final boolean isSideVip = user == null ? false : user.isVip();
					final boolean isSelfVip = AccManager.get().getCurUser().isVip();
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							VidUtil.asyncCacheAndDisplayAvatar(mAvatarIv, user.avatarUri, true);
							mNickTv.setText(user.getNick());
							mGenderTv.setText(user.gender == 'M' ? R.string.male : R.string.female);
							mAgeTv.setText(user.age + "");
							mSignatureTv.setText(user.signature == null ? "" : user.signature);
							mAddressTv.setText(user.address == null ? "" : user.address);

							setStatus(isSelfVip, isSideVip);
							mLocSecretCb.setChecked(user.isLocSecret());
							mAvoidDisturbCb.setChecked(user.isAvoidDisturb());
							mActionBtn.setEnabled(true);
							loadingDlg.dismiss();
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	private void setStatus(boolean isSelfVip, boolean isSideVip) {
		Relationship relation = XmppManager.get().getRelationship(mUid);
		if (relation == Relationship.FRIEND || (relation == Relationship.WAIT_BE_AGREE && isSelfVip && !isSideVip)) {
			mAddType = AddFriendType.ALREADY_FRIEND;
			mActionBtn.setText(R.string.send_msg);
		} else {
			mAddType = VidUtil.getAddFriendStatus(isSelfVip, isSideVip);
			mActionBtn.setText(R.string.add_to_friend);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.action:
			if (mAddType == AddFriendType.ALREADY_FRIEND) {
				Intent i = new Intent(this, ChattingActivity.class);
				i.putExtra(ExtraConst.EXTRA_UID, mUid);
				startActivity(i);
			} else if (mAddType != AddFriendType.NOT_ALLOWED && mAvoidDisturbCb.isChecked()) {// 对方防骚扰
				MainThreadHandler.makeToast(R.string.side_open_avoid_disturb);
			} else if (mAddType == AddFriendType.DIRECT) {
				if (ServerConfInfoTask.canDirectAddFriend()) {
					boolean success = AccManager.get().addFriend(mUid);
					if (success) {
						new AddFriendDlg(this, mAddType).show();
						mActionBtn.setText(R.string.send_msg);
						mAddType = AddFriendType.ALREADY_FRIEND;
					}
				} else {
					boolean success = AccManager.get().addFriend(mUid);
					if (success) {
						new AddFriendDlg(this, AddFriendType.NORMAL_WAIT).show();
					}
				}
			} else if (mAddType == AddFriendType.WAIT || mAddType == AddFriendType.VIP_WAIT) {
				boolean success = AccManager.get().addFriend(mUid);
				if (success) {
					new AddFriendDlg(this, mAddType).show();
				}
			} else if (mAddType == AddFriendType.NOT_ALLOWED) {
				new AddFriendDlg(this, mAddType).show();
			}
			break;
		}
	}

	@Override
	protected void onResume() {// 成为会员后返回
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final User user = HttpManager.get().getUserInfo(mUid);
					final boolean isSideVip = user == null ? false : user.isVip();
					final boolean isSelfVip = AccManager.get().getCurUser().isVip();
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							setStatus(isSelfVip, isSideVip);
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
		super.onResume();
	}

}