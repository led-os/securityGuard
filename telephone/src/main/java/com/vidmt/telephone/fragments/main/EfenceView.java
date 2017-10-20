package com.vidmt.telephone.fragments.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.MapViewLayoutParams.ELayoutMode;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.AlarmActivity;
import com.vidmt.telephone.activities.MapActivity;
import com.vidmt.telephone.managers.TmpMapManager;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.FenceVo;
import com.vidmt.xmpp.utils.XmppUtil;

public class EfenceView {
	private static EfenceView sInstance;
	private MapActivity mMapActivity;
	private TmpMapManager mMapMgr;
	private BaiduMap mBaiduMap;
	private MapView mMapView;
	private Overlay mTmpEfencePointOverlay, mTmpEfenceCircleOverlay;
	private View mTmpEfenceTopView;
	private final int DEF_EFENCE_RANGE = 2 * 1000;
	private FenceVo mFence = new FenceVo();
	private List<String> mInEfenceList = new ArrayList<String>();
	private String mAlarmingEfenceName;
	private LatLng mTarget;
	@ViewInject(R.id.efence_name)
	private EditText mEfenceEt;
	private List<String> mCurFenceNames;

	public static EfenceView get(MapActivity mapActivity) {
		if (sInstance == null) {
			sInstance = new EfenceView(mapActivity);
		}
		return sInstance;
	}

	private EfenceView(MapActivity mapActivity) {
		mMapActivity = mapActivity;
		mMapMgr = TmpMapManager.get(mapActivity);
		mBaiduMap = mMapMgr.getBaiduMap();
		mMapView = mMapMgr.getMapView();
		View view = mapActivity.findViewById(R.id.efence_show_bar);
		ViewUtils.inject(this, view);
		mBaiduMap.setOnMapStatusChangeListener(mOnMapStatusChangeListener);
	}

	private OnMapStatusChangeListener mOnMapStatusChangeListener = new OnMapStatusChangeListener() {
		@Override
		public void onMapStatusChange(MapStatus status) {
			if (DistanceUtil.getDistance(status.target, mTarget) > 0) {
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

	public FenceVo getFence() {
		return mFence;
	}

	public void startEfence() {
		mFence.radius = DEF_EFENCE_RANGE;
	}

	public void clearEfenceInputName() {
		mEfenceEt.setText("");
		mEfenceEt.setHintTextColor(Color.GRAY);
		mEfenceEt.setHint(R.string.input_efence_name);
	}

	public void show() {
		mMapActivity.findViewById(R.id.efence_show_bar).setVisibility(View.VISIBLE);
	}

	public void addTmpEfence() {
		int radius = mFence.radius;

		LatLng ll = mMapMgr.getMapStatus().target;
		mTarget = ll;
		mTmpEfenceCircleOverlay = mMapMgr.addOverlay(GeoUtil.getCircle(ll, radius));

		BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.loc_pin);
		OverlayOptions pointOo = new MarkerOptions().position(ll).icon(bmDesc);
		mTmpEfencePointOverlay = mMapMgr.addOverlay(pointOo);

		addTmpEfenceTopView(radius);
		mFence.lat = mTarget.latitude;
		mFence.lon = mTarget.longitude;
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
		mMapView.removeView(mTmpEfenceTopView);
	}

	private void addTmpEfenceTopView(int radius) {
		MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
		builder.layoutMode(ELayoutMode.mapMode);
		View view = SysUtil.inflate(R.layout.efence_top_view);
		Point centerPoint = mMapMgr.getMapStatus().targetScreen;
		centerPoint.y -= PixUtil.dp2px(52);
		mMapView.addView(view, builder.position(GeoUtil.point2LatLng(centerPoint)).build());
		RadioButton rb1 = (RadioButton) view.findViewById(R.id.one_km);
		RadioButton rb2 = (RadioButton) view.findViewById(R.id.two_km);
		RadioButton rb3 = (RadioButton) view.findViewById(R.id.three_km);
		RadioButton rb4 = (RadioButton) view.findViewById(R.id.four_km);
		RadioButton rb5 = (RadioButton) view.findViewById(R.id.five_km);
		switch (radius) {
		case 1 * 1000:
			rb1.setChecked(true);
			break;
		case 2 * 1000:
			rb2.setChecked(true);
			break;
		case 3 * 1000:
			rb3.setChecked(true);
			break;
		case 4 * 1000:
			rb4.setChecked(true);
			break;
		case 5 * 1000:
			rb5.setChecked(true);
			break;
		}
		rb1.setOnClickListener(mEfenceTopViewClickListener);
		rb2.setOnClickListener(mEfenceTopViewClickListener);
		rb3.setOnClickListener(mEfenceTopViewClickListener);
		rb4.setOnClickListener(mEfenceTopViewClickListener);
		rb5.setOnClickListener(mEfenceTopViewClickListener);
		mTmpEfenceTopView = view;
	}

	private OnClickListener mEfenceTopViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.one_km:
				updateTmpEfenceRange(1 * 1000);
				break;
			case R.id.two_km:
				updateTmpEfenceRange(2 * 1000);
				break;
			case R.id.three_km:
				updateTmpEfenceRange(3 * 1000);
				break;
			case R.id.four_km:
				updateTmpEfenceRange(4 * 1000);
				break;
			case R.id.five_km:
				updateTmpEfenceRange(5 * 1000);
				break;
			}
		}
	};

	public void removeEfenceByName(String name) {
//		AccManager.get().deleteFence(XmppUtil.buildJid("xxx"), name);
		mMapMgr.removeEfenceOverlay(name);
		mInEfenceList.remove(name);
		stopEfenceAlarm();
	}

	public void stopEfenceAlarm() {
		mAlarmingEfenceName = null;
	}

	public void warnBeyondEfence(LatLng ll) {
		Map<String, Overlay[]> efenceOverlayMap = mMapMgr.getEfenceOverlayMap();
		if (efenceOverlayMap.size() == 0) {
			return;
		}
		for (final String fenceName : efenceOverlayMap.keySet()) {
			Circle circleOverlay = (Circle) efenceOverlayMap.get(fenceName)[1];
			LatLng centerLl = circleOverlay.getCenter();
			double distance = DistanceUtil.getDistance(centerLl, ll);
			if (distance <= circleOverlay.getRadius() && !mInEfenceList.contains(fenceName)) {// 刚进入到此围栏
				mInEfenceList.add(fenceName);
				MainThreadHandler.makeToast(VLib.app().getString(R.string.enter_efence) + "【" + fenceName + "】");
				VidUtil.stopSound();// 只要在围栏内就不报警
				mAlarmingEfenceName = null;
			} else if (distance > circleOverlay.getRadius() && mInEfenceList.contains(fenceName)) {// 进入后又离开围栏
				mInEfenceList.remove(fenceName);
				final String toastStr = mMapActivity.getString(R.string.beyond_efence) + "【" + fenceName + "】";
				MainThreadHandler.makeToast(toastStr);
				if (mInEfenceList.size() != 0) {// 还在其他围栏里面
					return;
				}
				mAlarmingEfenceName = fenceName;
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mAlarmingEfenceName == null) {
							return;
						}
						AndrUtil.makeToast(toastStr);
						MainThreadHandler.postDelayed(this, 2 * 1000);
					}
				});
				Intent i = new Intent(mMapActivity, AlarmActivity.class);
				i.putExtra(ExtraConst.EXTRA_FENCE_NAME, mAlarmingEfenceName);
				mMapActivity.startActivity(i);
			}
		}
	}

	public void initCurFenceNames(List<String> fenceNames) {
		mCurFenceNames = fenceNames;
	}

	@OnClick({ R.id.efence_confirm, R.id.efence_cancel })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.efence_confirm:
			String fenceName = mEfenceEt.getText().toString();
			if (TextUtils.isEmpty(fenceName)) {
				VidUtil.setWarnText(mEfenceEt, mMapActivity.getString(R.string.input_efence_name));
				AndrUtil.makeToast(R.string.no_efence_name);
				return;
			} else if (mCurFenceNames != null && mCurFenceNames.contains(fenceName)) {
				VidUtil.setWarnText(mEfenceEt, mMapActivity.getString(R.string.fence_already_exists_please_rename));
				mEfenceEt.setText("");
				return;
			}
			mFence.jid = XmppUtil.buildJid("xxx");
			mFence.name = fenceName;
//			AccManager.get().uploadFence(mFence);
			mMapActivity.finish();
			break;
		case R.id.efence_cancel:
			mMapActivity.finish();
			break;
		}
	}
	
	public void destroy() {
		sInstance = null;
	}

}
