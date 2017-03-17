package com.vidmt.xmpp.enums;

public class XmppEnums {
	public static enum ChatType {
		TXT, HTML, AUDIO, VIDEO, IMAGE, LOCATION
	};

	public static enum LvlType {
		NONE, Cu, Ag, Au
	}

	public static enum Relationship {
		/**
		 * 无任何好友关系；
		 */
		NONE,
		/**
		 * 等待对方同意；
		 */
		WAIT_BE_AGREE,
		/**
		 * 未处理的加好友请求；
		 */
		WAIT_TO_REPLY,
		/**
		 * 好友关系（双方都在线时能确保是both关系）。
		 */
		FRIEND
	}
}
