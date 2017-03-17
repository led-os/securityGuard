package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.vidmt.child.R;

public class PhoneNODlg extends BaseMsgDlg {
	private String phoneNO;

	public PhoneNODlg(Activity context, String phoneNO) {
		super(context);
		this.phoneNO = phoneNO;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.confirm);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.please_confirm_cur_phone_no);
		setMsg(formatPhoneNO());
	}

	private String formatPhoneNO() {
		return phoneNO.substring(0, 3) + "-" + phoneNO.substring(3, 7) + "-" + phoneNO.substring(7);
	}

}
