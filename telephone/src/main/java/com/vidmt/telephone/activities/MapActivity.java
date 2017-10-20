package com.vidmt.telephone.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.fragments.main.EfenceView;
import com.vidmt.telephone.fragments.main.TraceView;
import com.vidmt.telephone.managers.TmpMapManager;
import com.vidmt.telephone.utils.Enums.MapType;

/**
 * 点击足迹，查看地图页
 */
public class MapActivity extends AbsVidActivity {
	private TmpMapManager mMapManager;
	private MapType mMapType;
	private String mUid;
	private EfenceView mEfenceView;
	private TraceView mTraceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMapManager = TmpMapManager.get(this);
		setContentView(mMapManager.getMapView());
		View mapWidgetsView = LayoutInflater.from(this).inflate(R.layout.activity_map, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		addContentView(mapWidgetsView, params);

		mUid = getIntent().getStringExtra(ExtraConst.EXTRA_UID);
		mTraceView = new TraceView(this, mUid);
		mEfenceView = EfenceView.get(this);
		boolean isTraceMap = getIntent().getBooleanExtra(ExtraConst.EXTRA_MAP_TRACE, false);
		boolean isEfenceMap = getIntent().getBooleanExtra(ExtraConst.EXTRA_MAP_ADD_EFENCE, false);
		if (isTraceMap) {
			mMapType = MapType.TRACE;
			mTraceView.show();
		} else if (isEfenceMap) {
			mMapType = MapType.EFENCE;
			mEfenceView.show();
		}
		initTitle(mMapType);
	}

	private void initTitle(MapType mapType) {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		if (mapType == MapType.TRACE) {
			titleTv.setText(R.string.trace);
		} else if (mapType == MapType.EFENCE) {
			titleTv.setText(R.string.add_efence);
		}
		initReconnectView(findViewById(R.id.reconnect_layout));
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
		mEfenceView.destroy();
		super.onDestroy();
	}
}
