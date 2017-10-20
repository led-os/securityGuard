package com.vidmt.telephone.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.location.Location;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.R;
import com.vidmt.telephone.managers.MapManager;
import com.vidmt.telephone.vos.PointVo;
import com.vidmt.telephone.vos.TraceSegVo;

@SuppressLint("SimpleDateFormat")
public class GeoUtil {
	/*
	 * 单位为： 米,转换错误时返回-1
	 */
	public static double getDistance(Location loc1, Location loc2) {
		LatLng ll1 = new LatLng(loc1.getLatitude(), loc1.getLongitude());
		LatLng ll2 = new LatLng(loc2.getLatitude(), loc2.getLongitude());
		return DistanceUtil.getDistance(ll1, ll2);
	}

	public static double getSpeed(Location loc1, Location loc2){
		double speed;
		double distance = getDistance(loc1, loc2);
		long deltime = Math.abs(loc1.getTime() - loc2.getTime());
		if(deltime == 0) {
			//默认最短间隔为20s
			deltime = 20 *1000;
		}
		speed = Math.abs(distance) / (deltime / 1000);
		return speed;
	}

	public static Location getLocation(double lat, double lon) {
		Location loc = new Location("BAIDU");
		loc.setLatitude(lat);
		loc.setLongitude(lon);
		return loc;
	}

	public static LatLng location2LatLng(Location loc) {
		LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
		return ll;
	}

	public static Location latlng2Location(LatLng ll) {
		Location loc = new Location("BAIDU");
		loc.setLatitude(ll.latitude);
		loc.setLongitude(ll.longitude);
		return loc;
	}

	public static Location bdLocation2Location(BDLocation bdLoc) {
		Location loc = new Location("BAIDU");
		loc.setLatitude(bdLoc.getLatitude());
		loc.setLongitude(bdLoc.getLongitude());
		return loc;
	}
	
	public static LatLng fromScreenLocation(Point point) {
		Projection projection = MapManager.get().getBaiduMap().getProjection();
		return projection.fromScreenLocation(point);
	}
	
	public static Point toScreenLocation(LatLng location) {
		Projection projection = MapManager.get().getBaiduMap().getProjection();
		return projection.toScreenLocation(location);
	}

	public static OverlayOptions getDot(LatLng center) {
		DotOptions dotOo = new DotOptions().center(center).color(0xFFFF0000).radius(3);
		return dotOo;
	}

	public static OverlayOptions getCircle(LatLng center, int R) {
		OverlayOptions circleOo = new CircleOptions().fillColor(0x88F7C4D7).center(center)
				.stroke(new Stroke(1, 0xAA00FF00)).radius(R);// 圆半径，单位：米
		return circleOo;
	}

	public static OverlayOptions getLine(LatLng nowll, LatLng lastll) {
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(nowll);
		points.add(lastll);
		OverlayOptions ooPolyline = new PolylineOptions().width(3).color(0xAA00FF00).points(points);
		return ooPolyline;
	}

	public static List<TraceSegVo> getRoutes(List<PointVo> tracePoints) {
		if (tracePoints.size() == 0) {
			return null;
		}
		// 足迹-起/终点时间
		Map<List<LatLng>, long[]> pointsMap = new LinkedHashMap<List<LatLng>, long[]>();// LinkedHashMap:按存放顺序迭代
		List<LatLng> points = new ArrayList<LatLng>();
		int k = 0;
		for (int i = 0; i < tracePoints.size(); i++) {
			PointVo point = tracePoints.get(i);
			if (point.lat == -1 && point.lon == -1) {// 分段点
				//huawei change from 5 to 2;
				if (points.size() >= 2) {// 一段足迹，至少2个点,否则无效
					pointsMap.put(points, new long[] { tracePoints.get(k).time, tracePoints.get(i - 1).time });
				}
				k = i + 1;
				points = new ArrayList<LatLng>();
				continue;
			}
			LatLng ll = new LatLng(point.lat, point.lon);
			points.add(ll);
		}
		if (pointsMap.size() == 0) {
			MainThreadHandler.makeToast(R.string.no_trace_data);
			return null;
		}
		List<TraceSegVo> list = new ArrayList<TraceSegVo>();
		// TmpMapManager mapMgr = TmpMapManager.get(null);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		for (List<LatLng> latlngs : pointsMap.keySet()) {
			long[] timeArr = pointsMap.get(latlngs);
			String startTimeStr = dateFormat.format(new Date(timeArr[0]));
			String endTimeStr = dateFormat.format(new Date(timeArr[1]));
			// mapMgr.setTimeMarker(latlngs.get(0), startTimeStr,
			// R.drawable.trace_start);
			// if (i == 0) {
			// mapMgr.animateTo(GeoUtil.latlng2Location(latlngs.get(0)));
			// }
			// mapMgr.setTimeMarker(latlngs.get(latlngs.size() - 1), endTimeStr,
			// R.drawable.trace_end);
			OverlayOptions ooPolyline = new PolylineOptions().width(5).color(0xAA0000FF).points(latlngs);
			double distance = 0;
			for (int i = 0; i < latlngs.size() && i + 1 < latlngs.size(); i++) {
				LatLng ll_0 = latlngs.get(i);
				LatLng ll_1 = latlngs.get(i + 1);
				double dist = DistanceUtil.getDistance(ll_0, ll_1);
				distance += dist;
			}
			if (distance < 10) {
				continue;
			}
			DecimalFormat df = new DecimalFormat("######0.00");
			String distStr = df.format(distance / 1000) + "";
			LatLng startLoc = latlngs.get(0);
			LatLng endLoc = latlngs.get(latlngs.size() - 1);
			TraceSegVo seg = new TraceSegVo(ooPolyline, startTimeStr, endTimeStr, startLoc, endLoc, distStr);
			list.add(seg);
		}
		return list;
	}

	public static Point latLng2Point(LatLng ll) {
		return MapManager.get().getBaiduMap().getProjection().toScreenLocation(ll);
	}

	public static LatLng point2LatLng(Point p) {
		return MapManager.get().getBaiduMap().getProjection().fromScreenLocation(p);
	}

	public static String formatNearbyDistance(double distance) {
		String distStr;
		String pattern_km = "#####.00";
		String pattern_m = "###";
		DecimalFormat df = null;
		if (distance < 1000) {
			df = new DecimalFormat(pattern_m);
			distStr = df.format(distance);
			if ("0".equals(distStr)) {
				distStr = "1";
			}
			distStr += "m";
		} else {
			distance = distance / 1000;
			df = new DecimalFormat(pattern_km);
			distStr = df.format(distance);
			if (distStr.charAt(distStr.length() - 1) == '0') {
				distStr = distStr.substring(0, distStr.length() - 1);
			}
			distStr += "km";
		}
		return distStr;
	}

}
