package com.vidmt.telephone.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.AgeSetDlg;
import com.vidmt.telephone.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.telephone.dlgs.BeVipDlg;
import com.vidmt.telephone.dlgs.GenderSetDlg;
import com.vidmt.telephone.dlgs.InputDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.utils.AvatarUtil;
import com.vidmt.telephone.utils.Enums.ResType;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 账户设置页
 */
public class AccountSetActivity extends AbsVidActivity implements OnClickListener {
	private ImageView mAvatarIv;
	private TextView mNickTv, mGenderTv, mAgeTv, mSignatureTv, mAddressTv;
	private CheckBox mLocSecretCb, mAvoidDisturbCb;
	private Dialog mExitDlg;
	private AccSetStatus mSetStatus;
	private User mCurUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_set);
		initTitle();
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.set_avatar).setOnClickListener(this);
		findViewById(R.id.set_nick).setOnClickListener(this);
		findViewById(R.id.set_gender).setOnClickListener(this);
		findViewById(R.id.set_age).setOnClickListener(this);
		findViewById(R.id.set_signature).setOnClickListener(this);
		findViewById(R.id.set_address).setOnClickListener(this);
		findViewById(R.id.loc_secret).setOnClickListener(this);
		findViewById(R.id.avoid_disturb).setOnClickListener(this);
		findViewById(R.id.exit).setOnClickListener(this);
		mAvatarIv = (ImageView) findViewById(R.id.avatar);
		mNickTv = (TextView) findViewById(R.id.nick);
		mGenderTv = (TextView) findViewById(R.id.gender);
		mAgeTv = (TextView) findViewById(R.id.age);
		mSignatureTv = (TextView) findViewById(R.id.signature);
		mAddressTv = (TextView) findViewById(R.id.address);
		mLocSecretCb = (CheckBox) findViewById(R.id.loc_secret_chk);
		mAvoidDisturbCb = (CheckBox) findViewById(R.id.avoid_disturb_chk);

		LinearLayout exitLayout = (LinearLayout) SysUtil.inflate(R.layout.dialog_logout);
		exitLayout.findViewById(R.id.logout).setOnClickListener(this);// 登出
		exitLayout.findViewById(R.id.shutdown).setOnClickListener(this);// 退出
		exitLayout.findViewById(R.id.cancel).setOnClickListener(this);// 取消
		mExitDlg = VidUtil.getBottomDialog(this, exitLayout);

		initData();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.account_set);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		mCurUser = AccManager.get().getCurUser();
		if(mCurUser == null){
			//huawei add;
			VidUtil.logoutApp();
			MainThreadHandler.makeToast("由于异常原因您已掉线，请重新登录！");
			return;
		}
		VidUtil.asyncCacheAndDisplayAvatar(mAvatarIv, mCurUser.avatarUri, true);
		mNickTv.setText(mCurUser.getNick());
		mGenderTv.setText(mCurUser.gender == 'M' ? R.string.male : R.string.female);
		mAgeTv.setText(mCurUser.age + "");
		mSignatureTv.setText(mCurUser.signature == null ? "" : mCurUser.signature);
		mAddressTv.setText(mCurUser.address == null ? "" : mCurUser.address);

		mLocSecretCb.setChecked(mCurUser.isLocSecret());
		mAvoidDisturbCb.setChecked(mCurUser.isAvoidDisturb());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.set_avatar:
			startActivityForResult(new Intent(this, ChoosePicActivity.class), 0);
			break;
		case R.id.set_nick:
			mSetStatus = AccSetStatus.NICK;
			new InputDlg(this, R.string.set_nick, mCurUser.getNick(), mDialogClickListener).show();
			break;
		case R.id.set_gender:
			mSetStatus = AccSetStatus.GENDER;
			boolean isMale = mCurUser.gender == 'M' ? true : false;
			new GenderSetDlg(this, isMale, mDialogClickListener).show();
			break;
		case R.id.set_age:
			mSetStatus = AccSetStatus.AGE;
			new AgeSetDlg(this, mCurUser.age, mDialogClickListener).show();
			break;
		case R.id.set_signature:
			mSetStatus = AccSetStatus.SIGNATURE;
			new InputDlg(this, R.string.set_signature, mCurUser.signature, mDialogClickListener).show();
			break;
		case R.id.set_address:
			mSetStatus = AccSetStatus.ADDRESS;
			new InputDlg(this, R.string.set_address, mCurUser.address, mDialogClickListener).show();
			break;
		case R.id.loc_secret:
			if (!mCurUser.isVip()) {
				new BeVipDlg(this, R.string.apply, R.string.be_vip_to_use_function_loc_secret).show();
				return;
			}
			if (mLocSecretCb.isChecked()) {// 状态：位置保密
				mLocSecretCb.setChecked(false);
			} else {
				mLocSecretCb.setChecked(true);
			}
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						boolean isLocSecret = mLocSecretCb.isChecked();
						AccManager.get().setUserInfo("locSecret", isLocSecret ? "T" : "F");
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});
			break;
		case R.id.avoid_disturb:
			if (!mCurUser.isVip()) {
				new BeVipDlg(this, R.string.apply, R.string.be_vip_to_use_function_avoid_disturb).show();
				return;
			}
			if (mAvoidDisturbCb.isChecked()) {// 状态：防骚扰
				mAvoidDisturbCb.setChecked(false);
			} else {
				mAvoidDisturbCb.setChecked(true);
			}
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						boolean isAvoidDisturb = mAvoidDisturbCb.isChecked();
						AccManager.get().setUserInfo("avoidDisturb", isAvoidDisturb ? "T" : "F");
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});
			break;
		case R.id.exit:
			mExitDlg.show();
			break;
		case R.id.logout:
			mExitDlg.dismiss();
			VidUtil.logoutApp();
//			AccManager.get().logout();
//			ServiceManager.get().stopService();
//			VidUtil.startNewTaskActivity(LoginActivity.class);
//			SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
//			AbsBaseActivity.exitAll();
			break;
		case R.id.shutdown:
			mExitDlg.dismiss();
			AccManager.get().logout();
			ServiceManager.get().stopService();
			AbsBaseActivity.exitAll();
			break;
		case R.id.cancel:
			mExitDlg.dismiss();
			break;
		}
	}

	private DialogClickListener mDialogClickListener = new DialogClickListener() {
		@Override
		public void onOK(Bundle bundle) {
			super.onOK(bundle);
			final String content = bundle.getString(ExtraConst.EXTRA_TXT_CONTENT);
			if (mSetStatus == AccSetStatus.NICK) {
				if (!VidUtil.isNickFormat(content)) {
					//VidUtil.setWarnText(mNickTv, getString(R.string.nick_format_err));
					MainThreadHandler.makeToast(getString(R.string.nick_format_err));
					return;
				} else if (!VidUtil.isNickTooLong(content)) {
					//VidUtil.setWarnText(mNickTv, getString(R.string.nick_too_long));
					MainThreadHandler.makeToast(getString(R.string.nick_too_long));
					return;
				}
				mNickTv.setText(content);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							AccManager.get().setUserInfo("nick", content);
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			} else if (mSetStatus == AccSetStatus.GENDER) {
				mGenderTv.setText("M".equals(content) ? R.string.male : R.string.female);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							AccManager.get().setUserInfo("gender", content);
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			} else if (mSetStatus == AccSetStatus.AGE) {
				mAgeTv.setText(content);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							AccManager.get().setUserInfo("age", content);
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			} else if (mSetStatus == AccSetStatus.SIGNATURE) {
				if (!VidUtil.isSignatureFormat(content)) {
					MainThreadHandler.makeToast(getString(R.string.signature_format_err));
					return;
				} else if (!VidUtil.isSignatureTooLong(content)) {
					MainThreadHandler.makeToast(getString(R.string.signature_too_long));
					return;
				}
				mSignatureTv.setText(content);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							AccManager.get().setUserInfo("signature", content);
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			} else if (mSetStatus == AccSetStatus.ADDRESS) {
				if (!VidUtil.isAddressFormat(content)) {
					MainThreadHandler.makeToast(getString(R.string.address_format_err));
					return;
				} else if (!VidUtil.isAddressTooLong(content)) {
					MainThreadHandler.makeToast(getString(R.string.address_too_long));
					return;
				}
				mAddressTv.setText(content);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							AccManager.get().setUserInfo("address", content);
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChoosePicActivity.ACT_RES_CODE) {
			Bundle bundle = data.getExtras();
			final Bitmap photo = bundle.getParcelable("data");
			mAvatarIv.setImageBitmap(AvatarUtil.toRoundCorner(photo));
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					HttpManager.get().uploadFile(ResType.AVATAR, photo);
				}
			});
		}
	}

	private enum AccSetStatus {
		NICK, GENDER, AGE, SIGNATURE, ADDRESS
	}

}
