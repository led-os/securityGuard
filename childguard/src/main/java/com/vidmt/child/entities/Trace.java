package com.vidmt.child.entities;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "trace")
public class Trace extends EntityBase {
	@Column(column = "date")
    private String date;
	@Column(column = "traceid")
	private int traceid;
	@Column(column = "jsonpoints")
	private String jsonpoints;
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getTraceid() {
		return traceid;
	}
	public void setTraceid(int traceid) {
		this.traceid = traceid;
	}
	
}