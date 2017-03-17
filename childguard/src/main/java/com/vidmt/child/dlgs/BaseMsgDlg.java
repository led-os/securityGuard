package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.App;
import com.vidmt.child.R;

public class BaseMsgDlg extends BaseDialog implements View.OnClickListener {
	private TextView mTitleTv, mMsgTv;
	private Button mConfirmBtn, mCancelBtn;

	public BaseMsgDlg(Activity context) {
		super(context, R.layout.dlg_msg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ViewUtils.inject(mActivity, mView);
		super.onCreate(savedInstanceState);
		mTitleTv = (TextView) mView.findViewById(R.id.title);
		int width = SysUtil.getDisplayMetrics().widthPixels * 6 / 7;
		LayoutParams param = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
		mTitleTv.setLayoutParams(param);
		mMsgTv = (TextView) mView.findViewById(R.id.msg);
		mMsgTv.setLayoutParams(param);
		mConfirmBtn = (Button) mView.findViewById(R.id.confirm);
		mCancelBtn = (Button) mView.findViewById(R.id.cancel);
		mConfirmBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
	}

	public void setTitle(int resId) {
		mTitleTv.setText(resId);
	}

	public void setMsg(int resId) {
		mMsgTv.setText(resId);
	}
	
	public void setMsg(String msg) {
		mMsgTv.setText(msg);
	}
	
	public void setMsg(String msg, int textSize, int colorResId) {
		mMsgTv.setTextSize(textSize);
		mMsgTv.setTextColor(App.get().getResources().getColor(colorResId));
		mMsgTv.setText(msg);
	}

	public void setBtnName(int whichBtn, int btnResId) {
		switch (whichBtn) {
		case DialogInterface.BUTTON_POSITIVE:
			mConfirmBtn.setText(btnResId);
			mConfirmBtn.setVisibility(View.VISIBLE);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			mCancelBtn.setText(btnResId);
			mCancelBtn.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirm:
			if (mClickListener != null) {
				mClickListener.onOK();
			}
			break;
		case R.id.cancel:
			if (mClickListener != null) {
				mClickListener.onCancel();
			}
			break;
		}
		dismiss();
	}

}
