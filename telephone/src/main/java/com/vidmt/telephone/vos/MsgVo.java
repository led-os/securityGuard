package com.vidmt.telephone.vos;

public class MsgVo {
	public long time;
	public String msgTxt;
	public int unReadNum;

	public MsgVo(long time, String msgTxt, int unReadNum) {
		this.time = time;
		this.msgTxt = msgTxt;
		this.unReadNum = unReadNum;
	}

	@Override
	public String toString() {
		return "MsgVo [time=" + time + ", msgTxt=" + msgTxt + ", unReadNum=" + unReadNum + "]";
	}
	
}
