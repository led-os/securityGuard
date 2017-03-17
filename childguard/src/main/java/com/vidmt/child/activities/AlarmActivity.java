package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.MainController;
import com.vidmt.child.utils.VidUtil;

@ContentView(R.layout.activity_alarm)
public class AlarmActivity extends AbsVidActivity {
	@ViewInject(R.id.msg)
	private TextView mMsgTv;
	private boolean mIsFenceAlarm;

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
		String fenceName = getIntent().getStringExtra(ExtraConst.EXTRA_FENCE_NAME);
		if (fenceName != null) {// 电子围栏，出围栏警报
			mIsFenceAlarm = true;
			mMsgTv.setText(R.string.baby_beyond_efence + "【" + fenceName + "】");
		} else {// 小孩发来的
			mMsgTv.setText(R.string.alarm_notify);
		}
	}

	@OnClick(R.id.confirm)
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.confirm:
			VidUtil.stopSound();
			if (mIsFenceAlarm) {
				MainController.get().stopEfenceAlarm();
			}
			finish();
			break;
		}
	}

	@Override
	public void onBackPressed() {
	}

}
