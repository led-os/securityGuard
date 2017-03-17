package com.vidmt.child.activities.main;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.App;
import com.vidmt.child.R;
import com.vidmt.child.activities.MapActivity;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.managers.TmpMapManager;
import com.vidmt.child.utils.GeoUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.child.vos.FenceVo;
import com.vidmt.xmpp.utils.XmppUtil;

import java.util.List;

public class EfenceView {
	private static final int DEF_EFENCE_RANGE = 2 * 1000;// 米
	private static final int MAP_CENTER_OFFSET = 80; // dp
	private MapActivity mMapActivity;
	private TmpMapManager mTmpMapMgr;
	private Overlay mTmpEfencePointOverlay, mTmpEfenceCircleOverlay;
	private FenceVo mFence = new FenceVo();
	@ViewInject(R.id.efence_name)
	private EditText mEfenceEt;
	@ViewInject(R.id.seekbar)
	private SeekBar mSeekBar;
	@ViewInject(R.id.distance)
	private TextView mDistanceTv;
	private List<String> mCurFenceNames;
	private LatLng mMapCenterLl;
	private OnMapStatusChangeListener mOnMapStatusChangeListener = new OnMapStatusChangeListener() {
		@Override
		public void onMapStatusChange(MapStatus status) {
			if (DistanceUtil.getDistance(status.target, mMapCenterLl) > 0) {
				updateTmpEfenceRange(mFence.radius);
			}
		}

		@Override
		public void onMapStatusChangeFinish(MapStatus status) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onMapStatusChangeStart(MapStatus status) {
			// TODO Auto-generated method stub
		}
	};

	public EfenceView(MapActivity mapActivity) {
		mMapActivity = mapActivity;
		mTmpMapMgr = TmpMapManager.get(mapActivity);
		View view = mapActivity.findViewById(R.id.efence_show_bar);
		ViewUtils.inject(this, view);

		int defKm = DEF_EFENCE_RANGE / 1000;
		mSeekBar.setProgress((defKm - 1) * 10);
		mDistanceTv.setText(App.get().getString(R.string._km, defKm + ""));
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// mEfenceInfo.progress = seekBar.getProgress();
				int distance = (int) Math.round(seekBar.getProgress() / 10D) + 1;
				mDistanceTv.setText(App.get().getString(R.string._km, distance + ""));
				seekBar.setProgress((distance - 1) * 10);
				updateTmpEfenceRange(distance * 1000);
			}
		});
		mTmpMapMgr.getBaiduMap().setOnMapStatusChangeListener(mOnMapStatusChangeListener);
		MainThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateTmpEfenceRange(DEF_EFENCE_RANGE);
			}
		}, 1 * 1000);
	}

	public void updateTmpEfenceRange(int radius) {
		mFence.radius = radius;

		removeTmpEfence();
		addTmpEfence();
	}

	public void removeTmpEfence() {
		if (mTmpEfencePointOverlay == null) {
			return;
		}
		mTmpEfencePointOverlay.remove();
		mTmpEfenceCircleOverlay.remove();
	}

	public void addTmpEfence() {
		int radius = mFence.radius;

		LatLng ll = mTmpMapMgr.getMapStatus().target;
		mMapCenterLl = ll;
		LatLng fenceCenter = GeoUtil.offsetY(ll, PixUtil.dp2px(MAP_CENTER_OFFSET));
		mTmpEfenceCircleOverlay = mTmpMapMgr.addOverlay(GeoUtil.getCircle(fenceCenter, radius, 0x3300C1A7, 0xCC00C1A7));

		BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.green_dot);
		OverlayOptions pointOo = new MarkerOptions().position(fenceCenter).icon(bmDesc);
		mTmpEfencePointOverlay = mTmpMapMgr.addOverlay(pointOo);

		mFence.lat = fenceCenter.latitude;
		mFence.lon = fenceCenter.longitude;
	}

	// public FenceVo getFence() {
	// return mFence;
	// }

	// public void clearEfenceInputName() {
	// mEfenceEt.setText("");
	// mEfenceEt.setHintTextColor(Color.GRAY);
	// mEfenceEt.setHint(R.string.input_efence_name);
	// }

	public void show() {
		mMapActivity.findViewById(R.id.efence_show_bar).setVisibility(View.VISIBLE);
	}

	public void setCurFenceNames(List<String> fenceNames) {
		mCurFenceNames = fenceNames;
	}

	public void saveEfence() {
		String fenceName = mEfenceEt.getText().toString();
		if (TextUtils.isEmpty(fenceName)) {
			VidUtil.setWarnText(mEfenceEt, mMapActivity.getString(R.string.input_efence_name_notify));
			AndrUtil.makeToast(R.string.no_efence_name);
			return;
		} else if (mCurFenceNames != null && mCurFenceNames.contains(fenceName)) {
			VidUtil.setWarnText(mEfenceEt, mMapActivity.getString(R.string.fence_already_exists_please_rename));
			mEfenceEt.setText("");
			return;
		}
		mFence.jid = XmppUtil.buildJid(VidUtil.getSideName());
		mFence.name = fenceName;
		AccManager.get().uploadFence(mFence);
		MapManager.get(null).addEfenceOverlay(fenceName, new LatLng(mFence.lat, mFence.lon), mFence.radius);
		mMapActivity.finish();
	}

}
