package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.VidUtil;

public class InviteUserDlg extends BaseMsgDlg {
	private String phoneAddr;

	public InviteUserDlg(Activity context, String phoneAddr) {
		super(context);
		this.phoneAddr = phoneAddr;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.search_result);
		final boolean isVip = AccManager.get().getCurUser().isVip();
		if (!isVip) {
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.apply);
		} else {
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.invite);
		}
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		String msgPostPix = "\n";
		if (isVip) {
			msgPostPix += mActivity.getString(R.string.invite_to_use);
		} else {
			msgPostPix += mActivity.getString(R.string.be_vip_for_more_services);
		}
		if (phoneAddr == null) {
			setMsg(mActivity.getString(R.string.phone_no_not_exists) + msgPostPix);
		} else {
			setMsg(mActivity.getString(R.string.phone_no_belong_to, phoneAddr) + msgPostPix);
		}
		setOnClickListener(new DialogClickListener() {
			@Override
			public void onOK() {
				super.onOK();
				if (isVip) {
					if (phoneAddr == null) {
						return;
					}
					String s = App.get().getString(R.string.share_app_msg) + Config.URL_LATEST_APK;
					VidUtil.share(s, null);
				} else {
					mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
					mActivity.finish();
				}
			}
		});
	}
}
