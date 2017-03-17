package com.vidmt.child;

import com.vidmt.acmn.abs.VLib;

import java.io.File;
import java.io.IOException;

public class FileStorage {
	public static final File baseDir = new File(VLib.getSdcardDir().getAbsolutePath());
	public static final String cacheDir = "cache";
	private static final String apkDir = "apk";
	private static final String avatarDir = "avatar";
	private static final String shareDir = "share";
	private static final String hideFolderFileName = ".nomedia";
	private static final String chatrecordDir = "chatrecord";
	private static final String histAudioDir = chatrecordDir + "/audio";
	private static final String histImageDir = chatrecordDir + "/image";
	private static final String histVideoDir = chatrecordDir + "/video";

	public static final String buildApkPath(String fileName) {
		return apkDir + "/" + fileName;
	}
	
	public static final String buildCachePath(String fileName) {
		return cacheDir + "/" + fileName;
	}
	
	public static final String buildSharePath(String fileName) {
		return shareDir + "/" + fileName;
	}

	public static final String buildAvatarPath(String fileName) {
		return avatarDir + "/" + fileName;
	}

	public static final String buildChatAudioPath(String fileName) {
		return histAudioDir + "/" + fileName;
	}

	public static final String buildChatImgPath(String fileName) {
		return histImageDir + "/" + fileName;
	}
	
	public static final String buildChatVideoPath(String fileName) {
		return histVideoDir + "/" + fileName;
	}

	public static void init() {
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}

		File f = new File(baseDir, apkDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, avatarDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, shareDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, hideFolderFileName);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		f = new File(baseDir, cacheDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, chatrecordDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, histAudioDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, histImageDir);
		if (!f.exists()) {
			f.mkdir();
		}
		f = new File(baseDir, histVideoDir);
		if (!f.exists()) {
			f.mkdir();
		}
	}
	
}
