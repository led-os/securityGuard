package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.child.R;

@ContentView(R.layout.activity_disclaimer)
public class DisclaimerActivity extends AbsVidActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.disclaimer);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@OnClick(R.id.back)
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		}
	}
}
