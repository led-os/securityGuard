package com.vidmt.child.vos;

import android.location.Location;

public class LocationVo {
	public String uid;
	public double lat;
	public double lon;
	public long time;

	public LocationVo() {
	}
	
	public LocationVo(String latStr, String lonStr) {
		this.lat = Double.parseDouble(latStr);
		this.lon = Double.parseDouble(lonStr);
	}
	
	public Location toLocation() {
		Location loc = new Location("BAIDU");
		loc.setLatitude(lat);
		loc.setLongitude(lon);
		return loc;
	}
}
