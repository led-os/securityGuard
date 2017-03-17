package com.vidmt.child.dlgs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.lidroid.xutils.ViewUtils;
import com.vidmt.child.R;

public class BaseDialog extends Dialog {
	private static BaseDialog mDialog;
	protected Activity mActivity;
	protected View mView;
	protected DialogClickListener mClickListener;

	public BaseDialog(Activity context, int resId) {
		super(context, R.style.CustomDialog);
		this.mActivity = context;
		mView = LayoutInflater.from(mActivity).inflate(resId, null);
		setContentView(mView);
		ViewUtils.inject(this, mView);// child can direct use @ViewInject
		mDialog = this;
	}
	
	@Override
	public void show() {// 可在任意线程
		// 防止View not attached to window manager
		if (!mActivity.isFinishing()) {
			BaseDialog.super.show();
		}
	}
	
	public void setOnClickListener(DialogClickListener listener) {
		mClickListener = listener;
	}
	
	public static class DialogClickListener {
		public void onOK(Bundle bundle) {// 带参数
			mDialog.dismiss();
		}
		
		public void onOK() {
			mDialog.dismiss();
		}

		public void onCancel() {
			mDialog.dismiss();
		}
	}
	
}
