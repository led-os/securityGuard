package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.utils.VidUtil;

public class InputDlg extends BaseDialog {
	private TextView mTitleTv;
	private EditText mContentEt;
	private int mTitleResId;
	private String mFillStr;
	private DialogClickListener mListener;

	public InputDlg(Activity context, int titleResId, String fillStr, DialogClickListener listener) {
		super(context, R.layout.dlg_input);
		mTitleResId = titleResId;
		mListener = listener;
		mFillStr = fillStr;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitleTv = (TextView) findViewById(R.id.title);
		mContentEt = (EditText) findViewById(R.id.content);
		
		int width = SysUtil.getDisplayMetrics().widthPixels * 4 / 5;
		LayoutParams param = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		mTitleTv.setLayoutParams(param);
		mTitleTv.setText(mTitleResId);
		setOnClickListener(mListener);
		VidUtil.setTextEndCursor(mContentEt, mFillStr);
		mView.findViewById(R.id.confirm).setOnClickListener(mClickListener);
		mView.findViewById(R.id.cancel).setOnClickListener(mClickListener);
	}
	
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.confirm:
				String content = mContentEt.getText().toString();
				if (TextUtils.isEmpty(content)) {
					MainThreadHandler.makeToast(R.string.empty_input);
					return;
				}
				Bundle bundle = new Bundle();
				bundle.putString(ExtraConst.EXTRA_TXT_CONTENT, content);
				mListener.onOK(bundle);
				dismiss();
				break;
			case R.id.cancel:
				dismiss();
				break;
			}
		}
	};

}
