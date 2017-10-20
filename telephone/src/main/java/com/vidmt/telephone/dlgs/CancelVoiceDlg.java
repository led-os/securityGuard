package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.vidmt.telephone.R;

public class CancelVoiceDlg extends BaseMsgDlg {

	public CancelVoiceDlg(Activity context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.send);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(R.string.will_cancel_voice);
	}

}
