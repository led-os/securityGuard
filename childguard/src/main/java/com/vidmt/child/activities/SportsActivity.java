package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.ui.views.SportsPopWindow;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_sports)
public class SportsActivity extends AbsVidActivity {
	@ViewInject(R.id.date)
	private TextView mDateTv;
	private SportsPopWindow mPopWindow;
	private LoadingDlg mLoadingDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		mLoadingDlg = new LoadingDlg(this, R.string.loading);
		mLoadingDlg.show();

		List<String> dateList = new ArrayList<String>();
		dateList.add("10-28");// test
		dateList.add("10-27");
		dateList.add("10-26");
		mPopWindow = new SportsPopWindow(this, dateList);
	}

	public void setDate(String date) {
		mDateTv.setText(date);
	}

	@OnClick({ R.id.back, R.id.date })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.date:
			mPopWindow.show();
			break;
		}
	}

}
