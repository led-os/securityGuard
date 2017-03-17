package com.vidmt.child.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.managers.ServiceManager;
import com.vidmt.xmpp.inner.XmppManager;

public class ExitDlg extends BaseDialog implements View.OnClickListener {
	private CheckBox mCheckbox;
	
	public ExitDlg(Activity context) {
		super(context, R.layout.dlg_exit);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView titleTv = (TextView) mView.findViewById(R.id.title);
		int width = SysUtil.getDisplayMetrics().widthPixels * 4 / 5;
		LayoutParams param = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
		titleTv.setLayoutParams(param);
		
		mCheckbox = (CheckBox) mView.findViewById(R.id.checkbox);
		boolean isChecked = SysUtil.getBooleanPref(PrefKeyConst.PREF_EXIT_BUT_RECEIVE_MSG, true);
		mCheckbox.setChecked(isChecked);
		
		mView.findViewById(R.id.check_layout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCheckbox.isChecked()) {
					mCheckbox.setChecked(false);
				} else {
					mCheckbox.setChecked(true);
				}
			}
		});
		
		mView.findViewById(R.id.confirm).setOnClickListener(this);
		mView.findViewById(R.id.cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.confirm:
			if (mCheckbox.isChecked()) {// 接收消息
				SysUtil.savePref(PrefKeyConst.PREF_EXIT_BUT_RECEIVE_MSG, true);
			} else {
				SysUtil.savePref(PrefKeyConst.PREF_EXIT_BUT_RECEIVE_MSG, false);
				ServiceManager.get().stopService();
			}
			XmppManager.get().logout();
			AbsBaseActivity.exitAll();
			break;
		case R.id.cancel:
			break;
		}
		ExitDlg.this.dismiss();
	}

}
