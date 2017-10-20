package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.UpdateActivity;

public class UnauthAppDlg extends BaseMsgDlg {

	public UnauthAppDlg(Activity context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.download_app);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warn);
		setMsg(R.string.unauthorized_app_notify);
		setCancelable(false);

		setOnClickListener(new DialogClickListener() {
			@Override
			public void onOK() {
				super.onOK();
				Intent intent = new Intent(mActivity, UpdateActivity.class);
				intent.putExtra(ExtraConst.EXTRA_UPDATE_URL, Config.URL_LATEST_APK);
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
