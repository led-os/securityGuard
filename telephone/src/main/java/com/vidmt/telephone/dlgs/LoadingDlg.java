package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.vidmt.telephone.R;

public class LoadingDlg extends BaseDialog {
	private int mResId;

	public LoadingDlg(Activity context, int resId) {
		super(context, R.layout.dlg_loading);
		mResId = resId;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView loadingTv = (TextView) mView.findViewById(R.id.loading);
		loadingTv.setText(mResId);
	}
	
}
