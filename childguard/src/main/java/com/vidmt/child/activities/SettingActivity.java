package com.vidmt.child.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.fb.FeedbackAgent;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.Const;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.BeVipDlg;
import com.vidmt.child.dlgs.ChangeUserDlg;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.tasks.UpdateTask;
import com.vidmt.child.utils.AvatarUtil;
import com.vidmt.child.utils.Enums.CmdType;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

public class SettingActivity extends AbsVidActivity implements OnClickListener {
	private ImageView mAvatarIv, mChildAvatarIv, mHideIconIv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
	}

	private void initView() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.setting);
		initReconnectView(findViewById(R.id.reconnect_layout));

		mAvatarIv = (ImageView) findViewById(R.id.avatar);
		mChildAvatarIv = (ImageView) findViewById(R.id.avatar_child);
		mHideIconIv = (ImageView) findViewById(R.id.open_or_close);

		boolean hideIcon = SysUtil.getBooleanPref(PrefKeyConst.PREF_HIDE_ICON);
		if (hideIcon) {
			mHideIconIv.setImageResource(R.drawable.check_on);
		} else {
			mHideIconIv.setImageResource(R.drawable.check_off);
		}
		VidUtil.asyncCacheAndDisplayAvatar(mAvatarIv, UserUtil.getParentInfo().avatarUri, true);
		VidUtil.asyncCacheAndDisplayAvatar(mChildAvatarIv, UserUtil.getBabyInfo().avatarUri, false);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.set_avatar).setOnClickListener(this);
		findViewById(R.id.set_child_avatar).setOnClickListener(this);
		findViewById(R.id.vip_center).setOnClickListener(this);
		findViewById(R.id.hide_icon).setOnClickListener(this);
		findViewById(R.id.qq_group).setOnClickListener(this);
		findViewById(R.id.feedback).setOnClickListener(this);
		findViewById(R.id.introduction).setOnClickListener(this);
		findViewById(R.id.check_update).setOnClickListener(this);
		findViewById(R.id.lookup_movement).setOnClickListener(this);
		findViewById(R.id.replace_user).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.vip_center:
			startActivity(new Intent(this, VipCenterActivity.class));
			break;
		case R.id.set_avatar:
			Intent i = new Intent(this, ChoosePicActivity.class);
			i.putExtra(ExtraConst.EXTRA_IS_CHANGE_PARENT_AVATAR, true);
			startActivityForResult(i, 0);
			break;
		case R.id.set_child_avatar:
			Intent ci = new Intent(this, ChoosePicActivity.class);
			ci.putExtra(ExtraConst.EXTRA_IS_CHANGE_PARENT_AVATAR, false);
			startActivityForResult(ci, 0);
			break;
		case R.id.hide_icon:
			if (!AccManager.get().isSideOnline()) {
				AndrUtil.makeToast(R.string.baby_not_login);
				return;
			}
			LvlType code = UserUtil.getParentInfo().code;

			if (SysUtil.getBooleanPref(PrefKeyConst.PREF_HIDE_ICON) && (code == LvlType.NONE || code == null)) {
				mHideIconIv.setImageResource(R.drawable.check_off);
				SysUtil.savePref(PrefKeyConst.PREF_HIDE_ICON, false);
				AccManager.get().sendCommand(CmdType.SHOW_ICON);
			}
			if (code == LvlType.NONE || code == null) {
				new BeVipDlg(this, R.string.recharge, R.string.not_gold_vip).show();
				return;
			}

			boolean hideIcon = SysUtil.getBooleanPref(PrefKeyConst.PREF_HIDE_ICON);
			if (hideIcon) {
				mHideIconIv.setImageResource(R.drawable.check_off);
				SysUtil.savePref(PrefKeyConst.PREF_HIDE_ICON, false);
				AccManager.get().sendCommand(CmdType.SHOW_ICON);
			} else {
				mHideIconIv.setImageResource(R.drawable.check_on);
				SysUtil.savePref(PrefKeyConst.PREF_HIDE_ICON, true);
				AccManager.get().sendCommand(CmdType.HIDE_ICON);
			}
			break;
		case R.id.qq_group:
			joinQQGroup(Const.QQ_GROUP_KEY);
			break;
		case R.id.feedback:
			FeedbackAgent agent = new FeedbackAgent(this);
			agent.startFeedbackActivity();
			break;
		case R.id.introduction:
			startActivity(new Intent(this, IntroductionActivity.class));
			break;
		case R.id.check_update:
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					UpdateTask.launchUpdateTask(SettingActivity.this, true);
				}
			});
			break;
		case R.id.lookup_movement:
			startActivity(new Intent(this, SportsActivity.class));
			break;
			case R.id.replace_user:
				new ChangeUserDlg(this, R.string.change, R.string.change_user).show();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case ChoosePicActivity.ACT_AVATAR_RES_CODE:
			Bundle bundle = data.getExtras();
			Bitmap photo = bundle.getParcelable("data");
			mAvatarIv.setImageBitmap(AvatarUtil.toRoundCorner(photo));
			break;
		case ChoosePicActivity.ACT_CHILD_AVATAR_RES_CODE:
			Bundle b = data.getExtras();
			Bitmap bm = b.getParcelable("data");
			mChildAvatarIv.setImageBitmap(AvatarUtil.toRoundCorner(bm));
			break;
		}
	}

	private boolean joinQQGroup(String key) {
		Intent intent = new Intent();
		intent.setData(Uri
				.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
						+ key));
		// 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
		// //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			startActivity(intent);
			return true;
		} catch (Exception e) {
			// 未安装手Q或安装的版本不支持
			MainThreadHandler.makeLongToast(R.string.please_add_qq_group);
			return false;
		}
	}

}
