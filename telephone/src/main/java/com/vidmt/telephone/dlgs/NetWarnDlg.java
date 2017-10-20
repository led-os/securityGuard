package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.telephone.R;

public class NetWarnDlg extends BaseMsgDlg {

	public NetWarnDlg(Activity context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.open);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(R.string.will_open_network);
		setOnClickListener(new DialogClickListener() {
			@Override
			public void onOK() {
				super.onOK();
				Intent intent = null;
				// 判断手机系统的版本 即API大于10 就是3.0或以上版本
				if (android.os.Build.VERSION.SDK_INT > 10) {
					intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
				} else {
					intent = new Intent();
					ComponentName component = new ComponentName("com.android.settings",
							"com.android.settings.WirelessSettings");
					intent.setComponent(component);
					intent.setAction("android.intent.action.VIEW");
				}
				mActivity.startActivity(intent);
				mActivity.finish();
			}

			@Override
			public void onCancel() {
				super.onCancel();
				mActivity.finish();
			}
		});
	}

}
