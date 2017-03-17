package com.vidmt.child.utils;

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
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.vos.PointVo;
import com.vidmt.child.vos.TraceSegVo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoUtil {
	/*
	 * 单位为： 米,转换错误时返回-1
	 */
	public static double getDistance(Location loc1, Location loc2) {
		LatLng ll1 = new LatLng(loc1.getLatitude(), loc1.getLongitude());
		LatLng ll2 = new LatLng(loc2.getLatitude(), loc2.getLongitude());
		return DistanceUtil.getDistance(ll1, ll2);
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

	public static LatLng offsetY(LatLng ll, int offsetInPixel) {
		Point p = GeoUtil.latLng2Point(ll);
		if (p == null) {
			return ll;
		}
		p.y += offsetInPixel;
		ll = GeoUtil.point2LatLng(p);
		return ll;
	}

	public static OverlayOptions getDot(LatLng center) {
		DotOptions dotOo = new DotOptions().center(center).color(0xEE00FF00).radius(PixUtil.dp2px(1));
		return dotOo;
	}
	
	public static OverlayOptions getCircle(LatLng center, int R, int fillColor, int strokeColor) {
		OverlayOptions circleOo = new CircleOptions().fillColor(fillColor).center(center)
				.stroke(new Stroke(PixUtil.dp2px(1), strokeColor)).radius(R);// 圆半径，单位：米
		return circleOo;
	}

	public static OverlayOptions getCircle(LatLng center, int R) {
		OverlayOptions circleOo = new CircleOptions().fillColor(0x3300C1A7).center(center)
				.stroke(new Stroke(PixUtil.dp2px(1), 0xEEE8E9E9)).radius(R);// 圆半径，单位：米
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
				if (points.size() >= 5) {// 一段足迹，至少5个点,否则无效
					pointsMap.put(points, new long[] { tracePoints.get(k).date, tracePoints.get(i - 1).date });
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
			System.out.println("TIME:" + timeArr[0] + "," + timeArr[1]);
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
		Projection projection = MapManager.get(null).getBaiduMap().getProjection();
		if (projection == null) {
			return null;
		}
		return projection.toScreenLocation(ll);
	}

	public static LatLng point2LatLng(Point p) {
		Projection projection = MapManager.get(null).getBaiduMap().getProjection();
		if (projection == null) {
			return null;
		}
		return projection.fromScreenLocation(p);
	}

}
