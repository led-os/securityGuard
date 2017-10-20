package com.vidmt.telephone.managers;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

/**
 * 地图功能操作控制（overlay，animal什么的）
 */
public class TmpMapManager {
	private static TmpMapManager sInstance;
	private Activity mActivity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private Map<String, Overlay[]> mEfenceOverlayMap = new HashMap<String, Overlay[]>();
	
	public static TmpMapManager get(Activity act) {
		if (sInstance == null) {
			sInstance = new TmpMapManager(act);
		}
		return sInstance;
	}
	
	private TmpMapManager(Activity ctx) {
		mActivity = ctx;
		BaiduMapOptions mapOptions = new BaiduMapOptions();
		mapOptions.scaleControlEnabled(true); // 隐藏比例尺控件
		mapOptions.zoomControlsEnabled(false);// 隐藏缩放按钮
		mapOptions.compassEnabled(false);
		mapOptions.overlookingGesturesEnabled(false);
		mapOptions.rotateGesturesEnabled(false);
		mapOptions.zoomGesturesEnabled(true);
		mMapView = new MapView(ctx, mapOptions);
		mMapView.setClickable(true);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14f);
		mBaiduMap.setMapStatus(msu);
		mBaiduMap.setMyLocationEnabled(false);// 设置是否允许定位图层
		//hideBaiduScale();
	}
	
	@Deprecated
	public MapView getMapView() {
		return mMapView;
	}
	
	@Deprecated
	public BaiduMap getBaiduMap() {
		return mBaiduMap;
	}
	
	public MapStatus getMapStatus() {
		return mBaiduMap.getMapStatus();
	}
	
	private void hideBaiduScale() {
		// 隐藏缩放控件
		int childCount = mMapView.getChildCount();
		View zoom = null;
		for (int i = 0; i < childCount; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				zoom = child;
				break;
			}
		}
		if(zoom != null) {
			zoom.setVisibility(View.GONE);
		}
	}
	
	public void addMarker(LatLng ll, int resId) {
		ImageView iv = new ImageView(mActivity);
		iv.setImageResource(resId);
		BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromView(iv);
		OverlayOptions oo = new MarkerOptions().position(ll).icon(bmDesc);
		mBaiduMap.addOverlay(oo);
	}
	
	public void animateTo(Location loc) {
		LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}
	
	public Overlay addOverlay(OverlayOptions oo) {
		Overlay overlay = mBaiduMap.addOverlay(oo);
		return overlay;
	}
	
	public void removeEfenceOverlay(String fenceName) {
		Overlay[] overlayArr = mEfenceOverlayMap.get(fenceName);
		overlayArr[0].remove();// 百度api不好使
		overlayArr[1].remove();
		overlayArr[0].setVisible(false);
		overlayArr[1].setVisible(false);
		overlayArr[0] = null;
		overlayArr[1] = null;
		mEfenceOverlayMap.remove(fenceName);
	}
	
	public Map<String, Overlay[]> getEfenceOverlayMap() {
		return mEfenceOverlayMap;
	}
	
	public void addEfenceOverlay(String fenceName, Overlay[] overlayArr) {
		mEfenceOverlayMap.put(fenceName, overlayArr);
	}
	
	public void clearOverlays() {
		mBaiduMap.clear();
	}
	
	public void onResume() {
		mMapView.onResume();
	}

	public void onPause() {
		mMapView.onPause();
	}

	public void onDestroy() {
		mMapView.onDestroy();
		mMapView = null;
		sInstance = null;
	}

}