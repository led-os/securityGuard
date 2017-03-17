package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;

@ContentView(R.layout.activity_login_mode)
public class LoginModeActivity extends AbsVidActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
	}

	@OnClick({ R.id.login_baby, R.id.login_parent })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_baby:
			SysUtil.savePref(PrefKeyConst.PREF_IS_BABY_CLIENT, true);
			break;
		case R.id.login_parent:
			SysUtil.savePref(PrefKeyConst.PREF_IS_BABY_CLIENT, false);
			break;
		}
		startActivity(new Intent(this, LoginActivity.class));
	}
}
