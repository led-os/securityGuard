package com.vidmt.child.entities;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

// 建议加上注解， 混淆后表名不受影响
@Table(name = "efence")
public class Efence extends EntityBase {
	@Column(column = "name") // 建议加上注解， 混淆后列名不受影响
    private String name;
	
	@Column(column = "lat")
	private double lat;
	
	@Column(column = "lon")
	private double lon;
	
	@Column(column = "radius")
	private int radius;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
}
