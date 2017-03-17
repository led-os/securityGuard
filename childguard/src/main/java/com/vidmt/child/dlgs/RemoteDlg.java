package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.vidmt.child.R;

public class RemoteDlg extends BaseMsgDlg {
	
	public RemoteDlg(Activity context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.startup);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(R.string.will_start_remote_record);
	}

}
