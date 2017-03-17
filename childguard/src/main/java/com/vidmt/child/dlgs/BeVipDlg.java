package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.child.R;
import com.vidmt.child.activities.VipCenterActivity;

public class BeVipDlg extends BaseMsgDlg {
	private int mConfirmBtnNameResId, mMsgResId;

	public BeVipDlg(Activity context, int confirmBtnNameResId, int msgResId) {
		super(context);
		mConfirmBtnNameResId = confirmBtnNameResId;
		mMsgResId = msgResId;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, mConfirmBtnNameResId);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(mMsgResId);
		setOnClickListener(new DialogClickListener(){
			@Override
			public void onOK() {
				super.onOK();
				mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
			}
		});
	}

}
