package com.vidmt.child.vos;

import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class TraceSegVo {
	public OverlayOptions oo;
	public String startTime;
	public String endTime;
	public LatLng startLoc;
	public LatLng endLoc;
	public String distance;// km

	public TraceSegVo(OverlayOptions oo, String startTime, String endTime, LatLng startLoc, LatLng endLoc,
			String distance) {
		this.oo = oo;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
		this.distance = distance;
	}

}
