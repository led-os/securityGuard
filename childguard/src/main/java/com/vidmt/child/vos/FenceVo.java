package com.vidmt.child.vos;

public class FenceVo {
	// public int id;
	public String jid;
	public String name;
	public double lat;
	public double lon;
	public int radius;

	@Override
	public String toString() {
		return "FenceVo [jid=" + jid + ", name=" + name + ", lat=" + lat + ", lon=" + lon + ", radius=" + radius + "]";
	}
}
