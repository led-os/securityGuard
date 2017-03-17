package com.vidmt.child.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "chatrecord")
public class ChatRecord extends EntityBase implements Parcelable {
	public static final Creator<ChatRecord> CREATOR = new Creator<ChatRecord>() {
		@Override
		public ChatRecord[] newArray(int size) {
			return new ChatRecord[size];
		}

		@Override
		public ChatRecord createFromParcel(Parcel in) {
			return new ChatRecord(in);
		}
	};
	@Column(column = "name")
	private String name;

	@Column(column = "isself")
	private boolean isSelf;

	@Column(column = "isread")
	private boolean isRead;

	@Column(column = "type")
	private String type;// ChatType

	@Column(column = "data")
	private String data;

	@Column(column = "during")
	private int during;

	@Column(column = "saytime")
	private long sayTime;// Date

	public ChatRecord() {
	}

	public ChatRecord(Parcel in) {
		name = in.readString();
		type = in.readString();
		data = in.readString();
		during = in.readInt();
		sayTime = in.readLong();
		isSelf = true;
		isRead = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getDuring() {
		return during;
	}

	public void setDuring(int during) {
		this.during = during;
	}

	public long getSayTime() {
		return sayTime;
	}

	// ////////////////////////////////////////////////////////////////////////

	public void setSayTime(long sayTime) {
		this.sayTime = sayTime;
	}

	@Override
	public String toString() {
		return "[uid=" + name + ",isSelf=" + isSelf + ",isRead=" + isRead + ",type=" + type + ",data=" + data
				+ ",during=" + during + ",sayTime=" + sayTime + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(type);
		dest.writeString(data);
		dest.writeInt(during);
		dest.writeLong(sayTime);
	}

}
