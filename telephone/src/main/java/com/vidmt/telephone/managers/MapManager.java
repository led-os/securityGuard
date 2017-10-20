package com.vidmt.telephone.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.MapViewLayoutParams.ELayoutMode;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.IllegalNaviArgumentException;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.poi.IllegalPoiSearchArgumentException;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.MapActivity;
import com.vidmt.telephone.activities.PersonInfoActivity;
import com.vidmt.telephone.dlgs.BeVipDlg;
import com.vidmt.telephone.dlgs.NoBdNaviDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;

/**
 * 地图页控制
 */
public class MapManager {
	private Activity mContext;
	private static MapManager sInstance;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private GeoCoder mSearch;

	private Map<String, CustomView> mViewMap = new HashMap<String, CustomView>();
	private Map<String, Boolean> mClickedViewMap = new HashMap<String, Boolean>();// key:uid,value:topView在显示
	private View mVisibleTopView;// 当前显示的topView
	private Map<String, Overlay[]> mEfenceOverlayMap = new HashMap<String, Overlay[]>();

	public static MapManager get() {
		if (sInstance == null) {
			sInstance = new MapManager();
		}
		return sInstance;
	}

	private MapManager() {
	}

	public static boolean isInstantiated() {
		return sInstance != null;
	}

	public void init(Activity act, MapView mapView) {
		mContext = act;
		mMapView = mapView;// 首次要在主线程中执行
		// 此处代码不适用于在xml中定义mapview
//		BaiduMapOptions mapOptions = new BaiduMapOptions();
//		mapOptions.scaleControlEnabled(true); // 隐藏比例尺控件
//		mapOptions.zoomControlsEnabled(false);// 隐藏缩放按钮
//		mapOptions.compassEnabled(false);
//		mapOptions.overlookingGesturesEnabled(false);
//		mapOptions.rotateGesturesEnabled(false);
//		mapOptions.zoomGesturesEnabled(true);
		mMapView.showScaleControl(false);
		mMapView.showZoomControls(false);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14f);
		mBaiduMap.setMapStatus(msu);
		mBaiduMap.setMyLocationEnabled(true);// 设置是否允许定位图层
		//hideBaiduScale();
		// mSearch = GeoCoder.newInstance();
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public boolean onMapPoiClick(MapPoi mapPoi) {
				hideTopView();
				return false;
			}

			@Override
			public void onMapClick(LatLng latLng) {
				hideTopView();
			}
		});
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

	public void animateTo(String uid, Location loc) {
		if (mBaiduMap == null) {// 防止时序问题
			VidUtil.fLog("animateTo  mBaiduMap == null");
			return;
		}
		LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);
		showTopView(uid, loc);
	}

	public void snapshot(SnapshotReadyCallback callBack) {
		mBaiduMap.snapshot(callBack);// 注：通过addView到地图的view不会显示
	}

	public void refreshMyView(final Location loc, final boolean refreshAvatar) {
		if (mMapView == null) {// 防止时序问题
			VidUtil.fLog("refreshMyView uid mMapView == null");
			return;
		}
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				//huawei change;
				if(AccManager.get().getCurUser() == null){
					VidUtil.fLog("refreshMyView AccManager.get().getCurUser() == null  logout!!!");
					VidUtil.logoutApp();
					return;
				}
				String myUid = AccManager.get().getCurUser().uid;
				CustomView view = mViewMap.get(myUid);
				Drawable drawable = null;
				if (view != null && view.view != null) {
					VidUtil.fLog("refreshMyView if (view != null && view.view != null)");
					if (!refreshAvatar) {
						ImageView avatarIv = (ImageView) view.view.findViewById(R.id.avatar);
						drawable = avatarIv.getDrawable();
					}
					mMapView.removeView(view.view);
					view = null;
				}
				final View customView = SysUtil.inflate(R.layout.my_pin_view);
				View topView = customView.findViewById(R.id.pin_top);
				handleTopView(myUid, topView);
				final ImageView avatarIv = (ImageView) customView.findViewById(R.id.avatar);
				final Drawable fdrawable = drawable;

				try {
					//huawei change：登录了就默认自己在线；
					//boolean online = XmppManager.get().isUserOnline(AccManager.get().getCurUser().uid);
					boolean online = true;
					if (!XmppManager.get().isAuthenticated()) {
						online = false;
					}
					VidUtil.fLog("refreshMyView online: " + online);
					if (fdrawable != null) {
						avatarIv.setImageDrawable(fdrawable);
					} else {
						VidUtil.asyncCacheAndDisplayAvatar(avatarIv, AccManager.get().getCurUser().avatarUri, online);
					}
				} catch (Exception e) {
					VidUtil.fLog("refreshMyView ThreadPool.execute: Exception" + e.getMessage());
					VLog.e("test", e);
				}

//				ThreadPool.execute(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							//huawei change：登录了就默认自己在线；
//							//boolean online = XmppManager.get().isUserOnline(AccManager.get().getCurUser().uid);
//							boolean online = true;
//							if (!XmppManager.get().isAuthenticated()) {
//								online = false;
//							}
//							VidUtil.fLog("refreshMyView online: " + online);
//							if (fdrawable != null) {
//								avatarIv.setImageDrawable(fdrawable);
//							} else {
//								VidUtil.asyncCacheAndDisplayAvatar(avatarIv, AccManager.get().getCurUser().avatarUri, online);
//							}
//						} catch (Exception e) {
//							VidUtil.fLog("refreshMyView ThreadPool.execute: Exception" + e.getMessage());
//							VLog.e("test", e);
//						}
//					}
//				});


				MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
				builder.layoutMode(ELayoutMode.mapMode);
				LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
				VidUtil.fLog("refreshMyView new CustomView: AccManager.get().getCurUser().uid: " + AccManager.get().getCurUser().uid);
				CustomView myView = new CustomView(AccManager.get().getCurUser().uid, customView, loc);
				mMapView.addView(myView.view, builder.position(ll).build());
				mViewMap.put(myView.uid, myView);
			}
		});
	}

	public synchronized void refreshFriendView(final Location loc, final boolean refreshAvatar) {
		if (mMapView == null) {// 防止时序问题
			return;
		}
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				Bundle extras = loc.getExtras();
				//huawei add;
				if(extras == null){
					VidUtil.fLog("refreshFriendView, get friend's loc uid is null");
					return;
				}
				final String uid = extras.getString(ExtraConst.EXTRA_UID);
				CustomView view = mViewMap.get(uid);
				Drawable drawable = null;
				if (view != null && view.view != null) {
					if (!refreshAvatar) {
						ImageView avatarIv = (ImageView) view.view.findViewById(R.id.avatar);
						drawable = avatarIv.getDrawable();
					}
					mMapView.removeView(view.view);
					view = null;
				}
				final Drawable avatarDrawable = drawable;
				View customView = SysUtil.inflate(R.layout.friend_pin_view);
				View topView = customView.findViewById(R.id.pin_top);
				final boolean isOnline = XmppManager.get().isUserOnline(uid);
				if (!isOnline) {
					ImageView pinImg = (ImageView) customView.findViewById(R.id.pin);
					pinImg.setImageResource(R.drawable.offline_pin);
				}
				handleTopView(uid, topView);
				final ImageView avatarIv = (ImageView) customView.findViewById(R.id.avatar);
				final TextView nicknameTv = (TextView) customView.findViewById(R.id.nickname);
				final TextView updateTimeTv = (TextView) customView.findViewById(R.id.update_time);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						if (!XmppManager.get().isAuthenticated()) {
							return;// 防止刚退出瞬间，下面执行了
						}
						try {
							final User user = HttpManager.get().getUserInfo(uid);
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									if (avatarDrawable != null) {
										avatarIv.setImageDrawable(avatarDrawable);
									} else {
										VidUtil.asyncCacheAndDisplayAvatar(avatarIv, user == null ? null
												: user.avatarUri, isOnline);
									}
									nicknameTv.setText(user.getNick());
									String updateTimeStr = DateUtil.formatNearbyTime(loc.getTime());
									//如果用户在线，时间就显示为最新，因为好友位置不变时，不上传位置；
									if (isOnline == true) {
										updateTimeStr = "现在";
									}
									updateTimeTv.setText(updateTimeStr);
								}
							});
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
				MapViewLayoutParams.Builder builder = new MapViewLayoutParams.Builder();
				builder.layoutMode(ELayoutMode.mapMode);
				LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
				CustomView myView = new CustomView(uid, customView, loc);
				mMapView.addView(myView.view, builder.position(ll).build());
				mViewMap.put(myView.uid, myView);
			}
		});
	}

	public void addViewToMap(CustomView myView) {
		mMapView.addView(myView.view);
		mViewMap.put(myView.uid, myView);
	}

	public List<CustomView> getAllFriendsMapViews() {
		List<CustomView> list = new ArrayList<CustomView>();
		for (String uid : mViewMap.keySet()) {
			if (uid != null) {// not contains self
				list.add(mViewMap.get(uid));
			}
		}
		return list;
	}

	public CustomView getViewByTag(String tag) {
		return mViewMap.get(tag);
	}

	public Overlay addOverlay(OverlayOptions oo) {
		Overlay overlay = mBaiduMap.addOverlay(oo);
		return overlay;
	}

	public void addEfenceOverlay(String fenceName, Overlay[] overlayArr) {
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
	}

	public void removePinView(final String uid) {
		DefaultThreadHandler.post(new Runnable() {// strange but work!
					@Override
					public void run() {
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								CustomView view = mViewMap.get(uid);
								if (view != null && view.view != null) {
									mMapView.removeView(view.view);
									mViewMap.remove(uid);
								}
							}
						});
					}
				});
	}

	public void clearAllViews() {
		DefaultThreadHandler.post(new Runnable() {// strange but work!
					@Override
					public void run() {
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								CustomView myView = null;
								Set<String> uids = mViewMap.keySet();
								for (String uid : uids) {
									if (uid == null) {
										myView = mViewMap.get(null);
									} else {
										final CustomView view = mViewMap.get(uid);
										if (view != null && view.view != null) {
											mMapView.removeView(view.view);
										}
									}
								}
								mViewMap.clear();
								mViewMap.put(null, myView);
							}
						});
					}
				});
	}

	private void handleTopView(String uid, View topView) {
		boolean hideTopView = mClickedViewMap.get(uid) == null || mClickedViewMap.get(uid) == false;
		VidUtil.fLog("handleTopView hideTopView ：" + hideTopView);
		if (hideTopView) {
			topView.setVisibility(View.GONE);
		} else {
			mVisibleTopView = topView;
		}
	}

	private void hideTopView() {
		mClickedViewMap.clear();
		if (mVisibleTopView != null) {
			mVisibleTopView.setVisibility(View.GONE);
		}
	}

	private void showTopView(String uid, Location loc) {
		hideTopView();
		VidUtil.fLog("showTopView uid " + uid);
		mClickedViewMap.put(uid, true);
		if (uid == AccManager.get().getCurUser().uid) {
			VidUtil.fLog("showTopView uid == null || uid == AccManager.get().getCurUser().uid");
			refreshMyView(loc, false);
		} else {
			refreshFriendView(loc, false);
		}
	}

	public class CustomView {
		public String uid;
		public Location loc;
		public View view;

		public CustomView(final String uid, View view, final Location loc) {
			this.uid = uid;
			this.view = view;
			this.loc = loc;
			view.findViewById(R.id.pin).setOnClickListener(listener);
			final View mapview = view;
			if (uid != null) {

				// huawei add for user is null;
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							User mCurUser = AccManager.get().getCurUser();
							if(mCurUser == null){
								VidUtil.fLog("CustomView getCurUser，mCurUser is null; need relogin");
								final String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
								String encodedPassword = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
								if (account != null && encodedPassword != null) {
									VidUtil.fLog("CustomView getCurUser，account != null && encodedPassword != null");
									final String pwd = EncryptUtil.decryptLocalPwd(encodedPassword);
									mCurUser = HttpManager.get().login(account, pwd);
									AccManager.get().setCurUser(mCurUser);
								}
							}
							}catch (VidException e){
								VidUtil.fLog("CustomView getCurUser，relogin failed msg：" + e.getMessage());
							}
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									//huawei add self trace;
									if(AccManager.get().getCurUser() == null){
										VidUtil.fLog("CustomView AccManager.get().getCurUser() == null  logout!!!");
										VidUtil.logoutApp();
										return;
									}
									if(AccManager.get().getCurUser().uid == uid){
										mapview.findViewById(R.id.trace).setOnClickListener(listener);
									}else {
										mapview.findViewById(R.id.avatar).setOnClickListener(listener);
										mapview.findViewById(R.id.nav).setOnClickListener(listener);
										mapview.findViewById(R.id.trace).setOnClickListener(listener);
									}
								}
							});

					}
				});

			}
		}

		private OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.pin:
					showTopView(uid, loc);
					break;
				case R.id.avatar:
					Intent i = new Intent(mContext, PersonInfoActivity.class);
					i.putExtra(ExtraConst.EXTRA_UID, uid);
					mContext.startActivity(i);
					break;
				case R.id.nav:
					if (AccManager.get().getCurUser().isVip()) {
						LatLng selfLl = LocationManager.get().getCurLocation();
						if (selfLl != null) {
							startNavi(selfLl, GeoUtil.location2LatLng(loc));
						}
					} else {
						new BeVipDlg(mContext, R.string.recharge, R.string.be_vip_to_use_function_navigate).show();
					}
					break;
				case R.id.trace:
					if (AccManager.get().getCurUser().isVip()) {
						Intent ti = new Intent(mContext, MapActivity.class);
						ti.putExtra(ExtraConst.EXTRA_MAP_TRACE, true);
						ti.putExtra(ExtraConst.EXTRA_UID, uid);
						mContext.startActivity(ti);
					} else {
						new BeVipDlg(mContext, R.string.recharge, R.string.be_vip_to_use_function_trace).show();
					}
					break;
				}
			}
		};
	}

	public void setOnGetGeoCodeResultListener(OnGetGeoCoderResultListener listener) {
		mSearch.setOnGetGeoCodeResultListener(listener);
	}

	public void reverseGeoCode(Location loc) {
		LatLng curLl = new LatLng(loc.getLatitude(), loc.getLongitude());
		mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(curLl));
	}

	public void geocode(String city, String address) {// 如:city=北京，address=海淀区上地十街10号
		mSearch.geocode(new GeoCodeOption().city(city).address(address));
	}

	public abstract static class AbsOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener {
		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			// TODO Auto-generated method stub
		}
	}

	public void startNavi(LatLng parentLoc, LatLng childLoc) {
		BaiduMapNavigation.setSupportWebNavi(true);// 使不支持web导航调用
		NaviParaOption para = new NaviParaOption();
		para.startPoint(parentLoc);
		para.startName("起点");
		para.endPoint(childLoc);
		para.endName("终点");
		try {
			boolean open = BaiduMapNavigation.openBaiduMapNavi(para, mContext);
			//boolean open = BaiduMapNavigation.openBaiduMapWalkNavi(para, mContext);
			Log.i("lk", "百度地图 启动：" + open);
		} catch (BaiduMapAppNotSupportNaviException e) {
			VLog.e("test", e);
			new NoBdNaviDlg(mContext).show();
		} catch (IllegalPoiSearchArgumentException e) {
			VLog.e("test", e);
			new NoBdNaviDlg(mContext).show();
		} catch (IllegalNaviArgumentException e) {
			VLog.e("test", e);
		}
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