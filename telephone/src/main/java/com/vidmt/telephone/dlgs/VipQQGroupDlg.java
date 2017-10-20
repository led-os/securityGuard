package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;

public class VipQQGroupDlg extends BaseMsgDlg {
	public VipQQGroupDlg(Activity context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.apply);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(R.string.be_vip_to_enter_vip_group);
		setOnClickListener(new DialogClickListener(){
			@Override
			public void onOK() {
				super.onOK();
				mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
			}
		});
	}

}
