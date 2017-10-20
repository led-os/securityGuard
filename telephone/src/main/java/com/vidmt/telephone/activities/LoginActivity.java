package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.NetWarnDlg;
import com.vidmt.telephone.tasks.LogRegTask;
import com.vidmt.telephone.utils.VidUtil;

@ContentView(R.layout.activity_login)
public class LoginActivity extends AbsVidActivity {
	@ViewInject(R.id.account)
	private EditText mAccountEt;
	@ViewInject(R.id.pwd)
	private EditText mPwdEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		if (!TextUtils.isEmpty(account)) {
			mAccountEt.setText(account);
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
			final String account = mAccountEt.getText().toString();
			final String pwd = mPwdEt.getText().toString();
			if (TextUtils.isEmpty(account)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (TextUtils.isEmpty(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.input_pwd));
			} else if (!VidUtil.isPwdFormat(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.pwd_format_err));
			} else {
				new LogRegTask(LoginActivity.this, account, pwd, null).execute();
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

	@Override
	public void onBackPressed() {
		// 防止切换账户跳转过来BACK键时，退到MainActivity栈，异常产生
		AbsVidActivity.killProcess();
	}

}
