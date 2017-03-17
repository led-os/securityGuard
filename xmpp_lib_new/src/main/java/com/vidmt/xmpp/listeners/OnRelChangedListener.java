package com.vidmt.xmpp.listeners;

public interface OnRelChangedListener extends IBaseXmppListener {
	/**
	 * 对方请求添加我为好友
	 * @param uid 对方uid
	 */
	public void beRequested(String uid);

	/**
	 * 对方同意我的好友请求
	 * @param uid 对方uid
	 */
	public void beAgreed(String uid);

	/**
	 * 对方拒绝我的好友请求
	 * @param uid 对方uid
	 */
	public void beRefused(String uid);

	/**
	 * 我被好友删除
	 * @param uid 对方uid
	 */
	public void beDeleted(String uid);
	
	
	public static abstract class AbsOnRelChangedListener implements OnRelChangedListener {
		@Override
		public void beRequested(String uid) {
		}
		
		@Override
		public void beAgreed(String uid) {
		}
		
		@Override
		public void beRefused(String uid) {
		}
		
		@Override
		public void beDeleted(String uid) {
		}
	}
}
