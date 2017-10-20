package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.vidmt.telephone.R;

public class UpdateDlg extends BaseMsgDlg {
	private int mMsgResId;

	public UpdateDlg(Activity context, int msgResId) {
		super(context);
		mMsgResId = msgResId;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.update);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.check_update);
		setMsg(mMsgResId);
	}

}
