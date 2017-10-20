package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.fragments.main.EfenceView;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 报警页
 */
@ContentView(R.layout.activity_alarm)
public class AlarmActivity extends AbsVidActivity {
	@ViewInject(R.id.msg)
	private TextView mMsgTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initMsg();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		initMsg();
	}

	private void initMsg() {
		VidUtil.playSound(R.raw.beep, true);
		String fenceName = getIntent().getStringExtra(ExtraConst.EXTRA_FENCE_NAME);// 电子围栏，出围栏警报
		mMsgTv.setText(getString(R.string.beyond_efence) + "：【" + fenceName + "】");
	}

	@OnClick(R.id.confirm)
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirm:
			VidUtil.stopSound();
			EfenceView.get(null).stopEfenceAlarm();
			finish();
			break;
		}
	}

	@Override
	public void onBackPressed() {
	}

}
