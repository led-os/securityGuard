package com.vidmt.telephone.vos;

import com.vidmt.telephone.ExtraConst;

import android.location.Location;
import android.os.Bundle;

public class LocVo {
	public String uid;
	public double lat;
	public double lon;
	public long time;
	public double distance;// 距自己的距离

	public LocVo() {
	}

	public Location toLocation() {
		Location loc = new Location("BAIDU");
		loc.setLatitude(lat);
		loc.setLongitude(lon);
		loc.setTime(time);
		Bundle bundle = new Bundle();
		bundle.putString(ExtraConst.EXTRA_UID, uid);
		loc.setExtras(bundle);
		return loc;
	}
}
