package com.vidmt.child.managers;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.Dot;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.IllegalNaviArgumentException;
import com.baidu.mapapi.navi.NaviParaOption;
//import com.baidu.mapapi.search.geocode.GeoCodeOption;
//import com.baidu.mapapi.search.geocode.GeoCodeResult;
//import com.baidu.mapapi.search.geocode.GeoCoder;
//import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.poi.IllegalPoiSearchArgumentException;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.App;
import com.vidmt.child.R;
import com.vidmt.child.activities.MainActivity;
import com.vidmt.child.activities.main.MainController;
import com.vidmt.child.dlgs.NoBdNaviDlg;
import com.vidmt.child.utils.GeoUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapManager {
	private static MapManager sInstance;
	private MainActivity mActivity;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
//	private GeoCoder mSearch;

	private Marker mParentMarker, mChildMarker;
	private Map<String, Overlay[]> mEfenceOverlayMap = new HashMap<String, Overlay[]>();
	private OnMapClickListener mOnMapClickListener = new OnMapClickListener() {
		@Override
		public boolean onMapPoiClick(MapPoi mapPoi) {
			mBaiduMap.hideInfoWindow();
			return false;
		}

		@Override
		public void onMapClick(LatLng latLng) {
			mBaiduMap.hideInfoWindow();
		}
	};

	private MapManager(MainActivity ctx) {
		mActivity = ctx;
		SDKInitializer.initialize(App.get());
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
		mBaiduMap.setOnMapClickListener(mOnMapClickListener);
		// mSearch = GeoCoder.newInstance();
	}

	public static MapManager get(MainActivity ctx) {
		if (sInstance == null) {
			sInstance = new MapManager(ctx);
		}
		return sInstance;
	}

	public static boolean isInstantiated() {
		return sInstance != null;
	}

	@Deprecated
	public MapView getMapView() {
		return mMapView;
	}

	@Deprecated
	public BaiduMap getBaiduMap() {
		return mBaiduMap;
	}

	public void animateTo(LatLng ll) {
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
	}

	public void updateParentMarker() {
		updateParentMarker(false);
	}

	public void updateParentMarker(boolean toTop) {
		LatLng ll = LocationManager.get().getCurLocation();
		if (ll == null) {
			return;
		}
		if (mParentMarker == null) {
			Bitmap newAvatar = SysUtil.getBitmap(R.drawable.parent_marker1);
			int bh = newAvatar.getHeight();
			ll = GeoUtil.offsetY(ll, -bh / 2);
			BitmapDescriptor bmDesc1 = BitmapDescriptorFactory.fromResource(R.drawable.parent_marker1);
			BitmapDescriptor bmDesc2 = BitmapDescriptorFactory.fromResource(R.drawable.parent_marker2);
			BitmapDescriptor bmDesc3 = BitmapDescriptorFactory.fromResource(R.drawable.parent_marker3);
			ArrayList<BitmapDescriptor> bmDescList = new ArrayList<BitmapDescriptor>();
			bmDescList.add(bmDesc1);
			bmDescList.add(bmDesc2);
			bmDescList.add(bmDesc3);
			OverlayOptions oo = new MarkerOptions().position(ll).icons(bmDescList);
			mParentMarker = (Marker) (mBaiduMap.addOverlay(oo));
		}
		mParentMarker.setPosition(ll);
		if (toTop) {
			mParentMarker.setToTop();
		}
	}

	public void updateChildMarker(boolean changeAvatar, boolean toTop) {
		updateChildMarker(changeAvatar, null, toTop);
	}

	public void updateChildMarker(Bitmap avatarBm) {
		updateChildMarker(true, avatarBm, false);
	}

	private void updateChildMarker(boolean changeAvatar, Bitmap avatarBm, boolean toTop) {
		Location childLoc = LocationManager.get().getChildLoc();
		if (childLoc == null) {
			return;
		}
		LatLng ll = GeoUtil.location2LatLng(childLoc);
		if (mChildMarker == null) {
			BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.green_dot);
			OverlayOptions oo = new MarkerOptions().position(ll).icon(bmDesc);
			mChildMarker = (Marker) (mBaiduMap.addOverlay(oo));
		}
		mChildMarker.setPosition(ll);
		if (toTop) {
			mChildMarker.setToTop();
		}
		MainController.get().warnBeyondEfence(ll);
		if (changeAvatar) {
			if (avatarBm != null) {
				VidUtil.setChildMarkerAvatar(mChildMarker, avatarBm);
				return;
			}
			String childAvatarUri = UserUtil.getBabyInfo().avatarUri;
			Bitmap newAvatar = SysUtil.getBitmap(R.drawable.child_def_pin);
			if (!AccManager.get().isSideOnline()) {
				newAvatar = SysUtil.getBitmap(R.drawable.child_def_pin_offline);
			}
			if (childAvatarUri == null) {
				BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromBitmap(newAvatar);
				mChildMarker.setIcon(bmDesc);
			} else {
				VidUtil.asyncCacheAndDisplayChildMapPinAvatar(mChildMarker, childAvatarUri);
			}
		}
	}

	private void showInfoWindow(LatLng ll, int offset, String msg, OnInfoWindowClickListener listener) {
		Point p = GeoUtil.latLng2Point(ll);
		p.y -= PixUtil.dp2px(offset);
		View popInfoView = SysUtil.inflate(R.layout.pop_info_window);
		TextView msgTv = (TextView) popInfoView.findViewById(R.id.msg);
		msgTv.setText(msg);
		BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromView(popInfoView);
		LatLng llInfo = GeoUtil.point2LatLng(p);
		InfoWindow infoWindow = new InfoWindow(bmDesc, llInfo, 0, listener);
		mBaiduMap.showInfoWindow(infoWindow);
	}

	public MapStatus getMapStatus() {
		return mBaiduMap.getMapStatus();
	}

	public void snapshot(SnapshotReadyCallback callBack) {
		mBaiduMap.snapshot(callBack);// 注：通过addView到地图的view不会显示
	}

	public Overlay addOverlay(OverlayOptions oo) {
		Overlay overlay = mBaiduMap.addOverlay(oo);
		return overlay;
	}

	public Overlay[] addCircleOverlay(LatLng center, Integer radius) {
		Overlay[] overlayArr = new Overlay[2];
		OverlayOptions circleOo = GeoUtil.getCircle(center, radius);
		Dot dotOverlay = (Dot) addOverlay(GeoUtil.getDot(center));
		Circle circleOverlay = (Circle) addOverlay(circleOo);
		overlayArr[0] = dotOverlay;
		overlayArr[1] = circleOverlay;
		return overlayArr;
	}

	public void addEfenceOverlay(String fenceName, LatLng center, Integer radius) {
		Overlay[] overlayArr = addCircleOverlay(center, radius);
		mEfenceOverlayMap.put(fenceName, overlayArr);
	}

	public Map<String, Overlay[]> getEfenceOverlayMap() {
		return mEfenceOverlayMap;
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

	public void clearOverlays() {
		mBaiduMap.clear();
		mParentMarker = mChildMarker = null;
	}
//
//	public void setOnGetGeoCodeResultListener(OnGetGeoCoderResultListener listener) {
//		mSearch.setOnGetGeoCodeResultListener(listener);
//	}
//
//	public void reverseGeoCode(Location loc) {
//		LatLng curLl = new LatLng(loc.getLatitude(), loc.getLongitude());
//		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(curLl));
//	}
//
//	public void geocode(String city, String address) {// 如:city=北京，address=海淀区上地十街10号
//		mSearch.geocode(new GeoCodeOption().city(city).address(address));
//	}

	public void startNavi(LatLng parentLoc, LatLng childLoc) {
		BaiduMapNavigation.setSupportWebNavi(false);// 使不支持web导航调用
		NaviParaOption para = new NaviParaOption();
		para.startPoint(parentLoc);
		para.startName("从这里开始");
		para.endPoint(childLoc);
		para.endName("到这里结束");
		try {
			boolean open = BaiduMapNavigation.openBaiduMapNavi(para, mActivity);
		} catch (BaiduMapAppNotSupportNaviException e) {
			VLog.e("test", e);
			new NoBdNaviDlg(mActivity).show();
		} catch (IllegalPoiSearchArgumentException e) {
			VLog.e("test", e);
			new NoBdNaviDlg(mActivity).show();
		} catch (IllegalNaviArgumentException e) {
			VLog.e("test", e);
		}
	}

	public void onResume() {
		mMapView.onResume();
	}

	public void onPause() {
		mMapView.onPause();
	}

	public void destroy() {
		mMapView.onDestroy();
		mMapView = null;
		sInstance = null;
	}
//
//	public abstract static class AbsOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener {
//		@Override
//		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//			// TODO Auto-generated method stub
//		}
//
//		@Override
//		public void onGetGeoCodeResult(GeoCodeResult result) {
//			// TODO Auto-generated method stub
//		}
//	}

}