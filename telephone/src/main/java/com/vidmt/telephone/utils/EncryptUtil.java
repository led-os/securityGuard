package com.vidmt.telephone.utils;

import com.vidmt.acmn.utils.HexUtil;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.telephone.Config;

public class EncryptUtil {
	private static Blowfish pwdTransBlowfish = new Blowfish(Config.KEY_MSG_CYPHER_SEED);

	/*
	 * 加密
	 */
	public static String encryptLocalPwd(String pwd) {
		return pwdTransBlowfish.encryptString(pwd);
	}

	/*
	 * 解密
	 */
	public static String decryptLocalPwd(String encrypted) {
		return pwdTransBlowfish.decryptString(encrypted);
	}

	public static String encryptTransferPwd(String pwd) {
		String hexPwd = HexUtil.toHexString(CommUtil.getStrByte(pwd, "UTF-8"));
		return pwdTransBlowfish.encryptString(hexPwd);
	}

	public static String decryptTransferPwd(String encrypted) {
		String hexPwd = pwdTransBlowfish.decryptString(encrypted);
		String pwd = CommUtil.newString(HexUtil.toByteArray(hexPwd), "UTF-8");
		return pwd;
	}

	public static String encryptParam(String msg) {
		byte[] bytes = CommUtil.getStrByte(msg, "UTF-8");
		return HexUtil.toHexString(bytes);
	}

	public static String decryptParam(String param) {
		byte[] bytes = HexUtil.toByteArray(param);
		return CommUtil.newString(bytes, "UTF-8");
	}

}
