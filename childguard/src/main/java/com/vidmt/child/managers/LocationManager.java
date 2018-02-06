package com.vidmt.child.managers;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Config;
import com.vidmt.child.Const;
import com.vidmt.child.PrefKeyConst;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocationManager {
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat sDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static LocationManager sInstance;
	private LocationClient mLocationClient;
	private List<LocationListener> mLocationListeners = new CopyOnWriteArrayList<LocationListener>();
	private Location mChildLoc;
	private BDLocationListener mBDLocationListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location.getLocType() == BDLocation.TypeNone) {// 无效定位结果
				return;
			}
			if (Math.abs(location.getLatitude()) < 0.0001 && Math.abs(location.getLongitude()) < 0.0001) {
				FLog.d("location",
						"invalid location from baidu:" + location.getLatitude() + "," + location.getLongitude());
				return;
			}
			Location curLoc = new Location("BAIDU");
			curLoc.setLatitude(location.getLatitude());
			curLoc.setLongitude(location.getLongitude());
			curLoc.setAltitude(location.getAltitude());
			curLoc.setAccuracy(location.getRadius());
			curLoc.setBearing(location.getDirection());
			curLoc.setSpeed(location.getSpeed());

			if (Config.DEBUG_SHARE_LOC) {// for test,let loc change
				curLoc.setLatitude(curLoc.getLatitude() + Math.random() * 0.03 - 0.015);
				curLoc.setLongitude(curLoc.getLongitude() + Math.random() * 0.03 - 0.015);
			}

			try {
				curLoc.setTime(sDf.parse(location.getTime()).getTime());
			} catch (Throwable e) {
				VLog.e("test", e);
				curLoc.setTime(System.currentTimeMillis());
			}
			for (LocationListener listener : mLocationListeners) {
				listener.onLocationChanged(curLoc);
			}
		}
	};

	private LocationManager() {
		final LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setScanSpan(Const.DEFAUL_LOC_FREQUENCY);// 设置发起定位请求的间隔时间
		int frequency = SysUtil.getIntPref(PrefKeyConst.PREF_LOC_FREQUENCY);
		if (frequency > -1) {
			option.setScanSpan(frequency);
		}
		option.setIsNeedAddress(false);// 设置是否需要地址信息
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mLocationClient = new LocationClient(VLib.app().getApplicationContext());// 须在主线程中声明
				mLocationClient.setLocOption(option);
			}
		});
	}

	public static LocationManager get() {
		if (sInstance == null) {
			sInstance = new LocationManager();
		}
		return sInstance;
	}

	public static boolean isInstantiated() {
		return sInstance != null;
	}

	public void start() {
		mLocationClient.registerLocationListener(mBDLocationListener);
		mLocationClient.start();// 启动终端
		requestLocation();
	}

	/**
	 * @param interval
	 *            : time in ms;
	 */
	public void setLocationInterval(int interval) {// 要重新注册并重启终端才会生效
		stop();
		LocationClientOption option = mLocationClient.getLocOption();
		option.setScanSpan(interval);
		start();
	}

	public void stop() {
		mLocationClient.unRegisterLocationListener(mBDLocationListener);
		mLocationClient.stop();
	}

	public void destroy() {
		stop();
		sInstance = null;
	}

	public int requestLocation() {// 请求定位
		// 0：离线定位请求成功 1:service没有启动 2：无监听函数 6：两次请求时间太短
		return mLocationClient.requestLocation();
	}

	public LatLng getCurLocation() {
		BDLocation bdLoc = mLocationClient.getLastKnownLocation();
		if (bdLoc == null || bdLoc.getLatitude() < 1E-10 || bdLoc.getLongitude() < 1E-10) {
			return null;
		}
		return new LatLng(bdLoc.getLatitude(), bdLoc.getLongitude());
	}

	public void setChildLocation(Location loc) {
		mChildLoc = loc;
	}

	public Location getChildLoc() {
		return mChildLoc;
	}

	public void addListener(LocationListener listener) {
		mLocationListeners.add(listener);
	}

	public void removeListener(LocationListener listener) {
		mLocationListeners.remove(listener);
	}

	public static abstract class AbsLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}
	}

}
