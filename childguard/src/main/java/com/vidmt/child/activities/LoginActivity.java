package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.NetWarnDlg;
import com.vidmt.child.tasks.LogRegTask;
import com.vidmt.child.utils.VidUtil;

@ContentView(R.layout.activity_login)
public class LoginActivity extends AbsVidActivity {
	@ViewInject(R.id.account)
	private EditText mAccountEt;
	@ViewInject(R.id.pwd)
	private EditText mPwdEt;
	@ViewInject(R.id.login_type)
	private TextView mLoginTypeTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		if (!TextUtils.isEmpty(account)) {
			mAccountEt.setText(account);
		}
		Boolean isBabaLogin = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
		if (isBabaLogin) {
			mLoginTypeTv.setText(R.string.login_baby);
		} else {
			mLoginTypeTv.setText(R.string.login_parent);
		}
	}

	@OnClick({ R.id.login, R.id.find_pwd, R.id.register })
	public void onClick(View view) {
		if (!NetUtil.isNetworkAvaiable()) {
			new NetWarnDlg(this).show();
			return;
		}
		switch (view.getId()) {
		case R.id.login:
			String account = mAccountEt.getText().toString();
			String pwd = mPwdEt.getText().toString();
			if (TextUtils.isEmpty(account)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(account)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else if (TextUtils.isEmpty(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.input_pwd));
			} else if (!VidUtil.isPwdFormat(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.pwd_format_err));
			} else {
				new LogRegTask(LoginActivity.this, account, pwd, false).execute();
			}
			break;
		case R.id.find_pwd:
			startActivity(new Intent(this, FindPwdActivity.class));
			finish();
			break;
		case R.id.register:
			startActivity(new Intent(this, RegisterActivity.class));
			finish();
			break;
		}
	}

}
