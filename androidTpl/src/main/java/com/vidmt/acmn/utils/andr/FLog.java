package com.vidmt.acmn.utils.andr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.acmn.utils.java.FileUtil;

/**
 * 该程序用来在sd卡上logs/flog/目录下自动生成log, 最多10条,超过会自动删除以前的.
 * 使用方式:
 * 在需要的时候用 Flog.log("tag",msg);此时生成一个tag_时间.txt文件.
 * 当该log结束的时候用Flog.endTag("tag"),此时会把缓存写入txt文件.
 * 也可以用FLog.endAllTag()来结束所有tag.
 * 可以考虑在Thread.getDefaultHandler中调用此方法,则每次程序无故崩溃时,可以收集没有被捕获的异常
 * 
 *  
 */
public class FLog {
	private static final String TAG = "FLog";
	private static final int MAX_LOG = 10;
	private static final DateFormat mDf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final DateFormat mMsgTimeDf = new SimpleDateFormat("HH:mm:ss");
	private static Map<String, PrintWriter> sTagMap = new HashMap<String, PrintWriter>();
	private final static File LOG_DIR;
	static {
		File f = null;
		File sdPath = VLib.getSdcardDir();
		if (sdPath != null) {
			try {
				f = new File(sdPath, "logs/logf");
				if (!f.exists()) {
					f.mkdirs();
				} else {
					String[] subFile = f.list();
					if (subFile != null && subFile.length > MAX_LOG) {
						FileUtil.deleteFile(f);
						f.mkdir();
					}
				}
			} catch (Throwable e) {
				VLog.w(TAG, e);
				f = null;
			}
		}
		LOG_DIR = f;
	}

	public static void d(String msg) {
		d("default", msg);
	}

	public static void df(String fmt, Object... args) {
		d("default", String.format(fmt, args));
	}

	public static void d(Throwable e) {
		d("default", e);
	}

	public static void d(String tag, Throwable e) {
		d(tag, CommUtil.formatException(e));
	}

	public static void endTag() {
		endTag("default");
	}

	// remember to call "endTag" method!!!
	public static void d(final String tag, final String msg) {
		if("default".equals(tag)){
			VLog.d("vid-default", msg);
		}else{
			VLog.d(tag, msg);
		}
		if (LOG_DIR == null) {
			return;
		}
		if (!"default".equals(tag)) {
			write("default", msg);
		}
		write(tag, msg);
	}


	public synchronized static void endTag(String tag) {
		PrintWriter pw = sTagMap.remove(tag);
		if (pw != null) {
			pw.close();
			File f = new File(LOG_DIR, tag + ".txt");
			if (f.exists()) {
				f.renameTo(new File(LOG_DIR, tag + "_" + mDf.format(new Date()) + ".txt"));
			}
		}
	}

	public synchronized static void endAllTag() {
		for (PrintWriter pw : sTagMap.values()) {
			pw.close();
		}
		for (String tag : sTagMap.keySet()) {
			File f = new File(LOG_DIR, tag + ".txt");
			if (f.exists()) {
				f.renameTo(new File(LOG_DIR, tag + "_" + mDf.format(new Date()) + ".txt"));
			}
		}
		sTagMap.clear();
	}

	private synchronized static void write(String tag, String msg) {
		PrintWriter pw = null;
		try {
			pw = sTagMap.get(tag);
			if (pw == null) {
				File f = new File(LOG_DIR, tag + ".txt");
				if(f.exists()){
					f.renameTo(new File(LOG_DIR, tag + "_" + mDf.format(new Date()) + "_N.txt"));
				}
				pw = new PrintWriter(new FileWriter(f, true));
				sTagMap.put(tag, pw);
			}

			pw.println("【" + mMsgTimeDf.format(new Date()) + "】" + msg);
			pw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
