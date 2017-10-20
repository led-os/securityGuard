package com.vidmt.telephone.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.vidmt.telephone.Config;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.LocationValidUtil;
import com.vidmt.telephone.utils.VidUtil;

public class LocationManager {
	private LocationClient mLocationClient;
	private List<LocationListener> mLocationListeners = new CopyOnWriteArrayList<LocationListener>();
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat sDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static LocationManager sInstance;
	private Location mCurLoc;
	private Location lastLoc;

	public static LocationManager get() {
		if (sInstance == null) {
			sInstance = new LocationManager();
		}
		return sInstance;
	}

	public static boolean isInstantiated() {
		return sInstance != null;
	}

	private LocationManager() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式:高精度模式
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
			}
		});
		mLocationClient.setLocOption(option);
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
		if (mCurLoc == null) {
			return null;
		}
		return GeoUtil.location2LatLng(mCurLoc);
	}
	
	public void setCurLocation(Location loc) {
		mCurLoc = loc;
	}

	public void addListener(LocationListener listener) {
		mLocationListeners.add(listener);
	}

	public void removeListener(LocationListener listener) {
		mLocationListeners.remove(listener);
	}

	private BDLocationListener mBDLocationListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location.getLocType() == BDLocation.TypeNone) {// 无效定位结果
				VidUtil.fLog("location",
						"TypeNone location from baidu:" + location.getLatitude() + "," + location.getLongitude());
				//TODO huawei changed for test;
				return;
			}
			if (Math.abs(location.getLatitude()) < 0.0001 && Math.abs(location.getLongitude()) < 0.0001) {
				VidUtil.fLog("location",
						"invalid location from baidu: type: " + location.getLocType() + "location:" + location.getLatitude() + "," + location.getLongitude());
				return;
			}
			VidUtil.fLog("location mBDLocationListener curLoc is show location:" + location.getLatitude() + "," + location.getLongitude());
			Location curLoc = new Location("BAIDU");
			curLoc.setLatitude(location.getLatitude());
			curLoc.setLongitude(location.getLongitude());
			curLoc.setAltitude(location.getAltitude());
			curLoc.setAccuracy(location.getRadius());
			curLoc.setBearing(location.getDirection());
			curLoc.setSpeed(location.getSpeed());
			try {
				VidUtil.fLog("location mBDLocationListener curLoc.setTime");
				curLoc.setTime(System.currentTimeMillis());
				//curLoc.setTime(sDf.parse(location.getTime()).getTime());
			} catch (Throwable e) {
				VLog.e("test", e);
				VidUtil.fLog("location mBDLocationListener curLoc.setTime exception : e" + e);
				curLoc.setTime(System.currentTimeMillis());
			}
			if (Config.DEBUG_LOCATION) {// for test,let loc change
				curLoc.setLatitude(curLoc.getLatitude() + Math.random() * 0.03 - 0.015);
				curLoc.setLongitude(curLoc.getLongitude() + Math.random() * 0.03 - 0.015);
			}
			//排除定位偏差过大的点；
			if(lastLoc == null){
				VidUtil.fLog("location mBDLocationListener lastLoc == null");
				mCurLoc = curLoc;
				lastLoc = curLoc;
			}else {
				VidUtil.fLog("location mBDLocationListener lastLoc != null");
				mCurLoc = curLoc;
				//先判断位置是否和上次一样，一样不需要再传输到server；
				if((mCurLoc.getLatitude() == lastLoc.getLatitude())&&
						(mCurLoc.getLongitude() == lastLoc.getLongitude())){
					lastLoc = curLoc;
					VidUtil.fLog("location mBDLocationListener lastLoc = curLoc; return");
					return;
				}
				//位置不一样，判断是否为有效的点，避免“飞点”，出现无效点抛弃，并恢复原位重新定位；
				Boolean isValid = LocationValidUtil.assertLocationValid(mCurLoc, lastLoc);
				if(false == isValid){
					lastLoc = null;
					VidUtil.fLog("location mBDLocationListener lastLoc = false == isValid return");
					return;
				}else {
					lastLoc = curLoc;
				}
			}


			mCurLoc = curLoc;
			VidUtil.fLog("location mBDLocationListener mCurLoc = curLoc;");
			if(Config.DEBUG){
				VidUtil.fLog("onReceiveLocation from baidu: " + curLoc.getLatitude() + curLoc.getLongitude());
			}
			for (LocationListener listener : mLocationListeners) {
				listener.onLocationChanged(curLoc);
			}
		}
	};

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
