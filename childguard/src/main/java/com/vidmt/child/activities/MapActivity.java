package com.vidmt.child.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.EfenceView;
import com.vidmt.child.activities.main.TraceView;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.LocationManager;
import com.vidmt.child.managers.TmpMapManager;
import com.vidmt.xmpp.exts.FenceIQ;

import java.util.List;

public class MapActivity extends AbsVidActivity implements View.OnClickListener {
	private TmpMapManager mMapManager;
	private EfenceView mEfenceView;
	private TraceView mTraceView;
	private TextView mTitleTv;
	private Button mTitleRightBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMapManager = TmpMapManager.get(this);
		setContentView(mMapManager.getMapView());
		View mapWidgetsView = LayoutInflater.from(this).inflate(R.layout.activity_map, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		addContentView(mapWidgetsView, params);
		initTitle();

		boolean isTraceMap = getIntent().getBooleanExtra(ExtraConst.EXTRA_MAP_TRACE, false);
		boolean isEfenceMap = getIntent().getBooleanExtra(ExtraConst.EXTRA_MAP_ADD_EFENCE, false);
		if (isTraceMap) {
			mTitleTv.setText(R.string.trace);
			mTitleRightBtn.setVisibility(View.GONE);
			mTraceView = new TraceView(this);
			mTraceView.show();
		} else if (isEfenceMap) {
			mTitleTv.setText(R.string.set_efence);
			mTitleRightBtn.setBackgroundResource(R.drawable.selector_green_btn);
			mTitleRightBtn.setTextColor(Color.WHITE);
			mTitleRightBtn.setText(R.string.save);
			mEfenceView = new EfenceView(this);
			List<String> curFenceNames = getIntent().getStringArrayListExtra(ExtraConst.EXTRA_CURRENT_FENCE_NAMES);
			mEfenceView.setCurFenceNames(curFenceNames);
			mEfenceView.show();
			initFence();
		}
		LatLng ll = LocationManager.get().getCurLocation();
		if (ll != null) {
			mMapManager.animateTo(ll);
		}
	}

	private void initTitle() {
		mTitleTv = (TextView) findViewById(R.id.title);
		mTitleRightBtn = (Button) findViewById(R.id.right);
		findViewById(R.id.back).setOnClickListener(this);
		mTitleRightBtn.setOnClickListener(this);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.right:
			if (mEfenceView != null) {
				mEfenceView.saveEfence();
			}
			break;
		}
	}

	private void initFence() {
		FenceIQ fenceIq = AccManager.get().getFence();// get all fence
		if (fenceIq != null && fenceIq.jid != null) {// 有围栏数据
			for (int i = 0; i < fenceIq.nameList.size(); i++) {
				double lat = fenceIq.latList.get(i);
				double lon = fenceIq.lonList.get(i);
				int radius = fenceIq.radiusList.get(i);
				LatLng center = new LatLng(lat, lon);
				mMapManager.addEfenceOverlay(fenceIq.nameList.get(i), center, radius);
			}
		}
	}

	@Override
	public void onResume() {
		mMapManager.onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		mMapManager.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mMapManager.onDestroy();
		mMapManager = null;
		mEfenceView = null;
		super.onDestroy();
	}
}
