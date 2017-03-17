package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.child.R;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.ui.views.RoundProgressBar;
import com.vidmt.child.ui.views.RoundProgressBar.OnCountListener;
import com.vidmt.child.utils.Enums.CmdType;

@ContentView(R.layout.activity_send_alarm)
public class SendAlarmActivity extends AbsVidActivity {
	private static int COUNT = 3;
	@ViewInject(R.id.alarm_time)
	private TextView mAlarmTv;
	@ViewInject(R.id.round_progress_bar)
	private RoundProgressBar mRoundPb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		mAlarmTv.setText(COUNT + "");

		mRoundPb.startCount(COUNT);
		mRoundPb.setOnCountListener(new OnCountListener() {
			@Override
			public void onCount(int num) {
				mAlarmTv.setText((COUNT - num) + "");
			}

			@Override
			public void onCountFinish() {
				AccManager.get().sendCommand(CmdType.ALARM);
				finish();
			}
		});
	}

	@OnClick(R.id.cancel)
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel:
			mRoundPb.stopCount();
			finish();
			break;
		}
	}

	@Override
	public void onBackPressed() {
	}

}
