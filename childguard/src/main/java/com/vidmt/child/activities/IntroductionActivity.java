package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.R;

@ContentView(R.layout.activity_introduction)
public class IntroductionActivity extends AbsVidActivity {
	private final int SHOW_BSNS_CLICK_CONUT = 10;
	@ViewInject(R.id.version)
	private TextView mVersionTv;
	private int mClickCount = 0;
	private long mLastClickTime = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		mVersionTv.setText(SysUtil.getPkgInfo().versionName);
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.software_introduction);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@OnClick({ R.id.back, R.id.icon })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.icon:
			long clickTime = System.currentTimeMillis();
			if (clickTime - mLastClickTime > 1000) {
				mClickCount = 0;
			} else {
				mClickCount++;
				if (mClickCount >= SHOW_BSNS_CLICK_CONUT - 1) {
					startActivity(new Intent(this, DebugActivity.class));
					mClickCount = 0;
				}
			}
			mLastClickTime = System.currentTimeMillis();
			break;
		}
	}

}
