package com.vidmt.child.managers;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.Dot;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.vidmt.child.utils.GeoUtil;

public class TmpMapManager {
	private static TmpMapManager sInstance;
	private Activity mActivity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	
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
		mBaiduMap.setMyLocationEnabled(true);// 设置是否允许定位图层
		//hideBaiduScale();
	}

	public static TmpMapManager get(Activity act) {
		if (sInstance == null) {
			sInstance = new TmpMapManager(act);
		}
		return sInstance;
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
	
	public void animateTo(LatLng ll) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}
	
	public Overlay addOverlay(OverlayOptions oo) {
		Overlay overlay = mBaiduMap.addOverlay(oo);
		return overlay;
	}
	
	public void clearOverlays() {
		mBaiduMap.clear();
	}
	
	public Overlay[] addEfenceOverlay(String fenceName, LatLng center, Integer radius) {
		Overlay[] overlayArr = new Overlay[2];
		OverlayOptions circleOo = GeoUtil.getCircle(center, radius);
		Dot dotOverlay = (Dot) addOverlay(GeoUtil.getDot(center));
		Circle circleOverlay = (Circle) addOverlay(circleOo);
		overlayArr[0] = dotOverlay;
		overlayArr[1] = circleOverlay;
		return overlayArr;
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