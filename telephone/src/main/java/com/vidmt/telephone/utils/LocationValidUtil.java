package com.vidmt.telephone.utils;

import android.location.Location;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.telephone.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * lihuichao
 * 判断点是否有效
 */
public class LocationValidUtil {
	// 500 km/h=138m/s, 高铁每小时300km，取上限
	private static String tag = "LocationValid";
	private static final int SPEED_LIMIT = (int) (500 / 3.6);
	private static List<Location> list = new ArrayList<>(5);
	private static Location lastLoc = null;
	private static boolean lastvalid = false;

	/**
	 * 原理：如果有任何两个点的速度合法，那么就认为当前点是第一个合法点。后续点根据前一个点计算是否合法。
	 * 在所有点都不能确定是否合法的情况下，默认都合法。
	 * 所谓合法点，是指两个点之间的速度小于400Km/h=111m/s,因为高铁300km/h,坐飞机等速度过快情况时没有定位信号，不做考虑。
	 * @throws IllegalStateException
	 */
	public static void assertValid(Location loc) throws IllegalStateException {
		// 如果已经确定最后一个点合法，那么直接计算当前点是否合法
		if (lastvalid) {
			int speed = (int) GeoUtil.getSpeed(loc, lastLoc);
			if (speed < SPEED_LIMIT) {
				lastLoc = loc;
			} else {
				throw new IllegalStateException();
			}
			//否则，确定当前点是否是第一个合法点
		} else if (list.isEmpty()) {
			//如果当前点是第一个点，默认为合法，用来验证其他点
			list.add(loc);
		} else {
			//如果不是第一个点，那么用当前点和之前所有点比较，如果有任何两个点合法，那么即认为当前点是第一个合法点
			for (Location tmploc : list) {
				int speed = (int) GeoUtil.getSpeed(loc, tmploc);
				if (speed < SPEED_LIMIT) {
					lastvalid = true;
					lastLoc = loc;
					break;
				}
			}
			//如果当前点和之前所有点都不合法，那么之前的所有点都不合法，那么就把当前点加入非法点集合，等待下个点进来时验证
			if (!lastvalid) {
				list.add(loc);
			}
		}
	}

	public static Boolean assertLocationValid(Location currentloc, Location lastloc){
		int speed = (int) GeoUtil.getSpeed(currentloc, lastloc);
		if (Config.DEBUG){
			VidUtil.fLog(tag , "currentloc："+currentloc.getLatitude() + "," +currentloc.getLongitude() + "time:" + currentloc.getTime());
			VidUtil.fLog(tag , "lastloc："+lastloc.getLatitude() + "," +lastloc.getLongitude() + "time:" + lastloc.getTime());
			VidUtil.fLog(tag , "speed："+ speed);
		}
		if(speed > SPEED_LIMIT){
			if (Config.DEBUG){
				VidUtil.fLog(tag , "invalid points shown：speed is："+ speed);
			}
			return false;
		}else {
			return true;
		}
	}
	/**
	 * 调用登录接口时，需要重置状态
	 */
	public static void reset(){
		list.clear();
		lastLoc=null;
		lastvalid=false;
	}
}
