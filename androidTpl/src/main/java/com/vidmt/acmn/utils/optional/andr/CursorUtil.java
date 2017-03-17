package com.vidmt.acmn.utils.optional.andr;

import java.util.Date;

import android.database.Cursor;

public class CursorUtil {
	public static String getString(Cursor c, String colomnName) {
		return c.getString(c.getColumnIndex(colomnName));
	}

	public static char getChar(Cursor c, String colomnName) {
		return c.getString(c.getColumnIndex(colomnName)).charAt(0);
	}

	public static short getShort(Cursor c, String colomnName) {
		return c.getShort(c.getColumnIndex(colomnName));
	}

	public static int getInt(Cursor c, String colomnName) {
		return c.getInt(c.getColumnIndex(colomnName));
	}

	public static long getLong(Cursor c, String colomnName) {
		return c.getLong(c.getColumnIndex(colomnName));
	}

	public static boolean getBoolean(Cursor c, String colomnName) {
		return 'Y' == getChar(c, colomnName);
	}
	
	public static byte[] getBlob(Cursor c, String columnName) {
		return c.getBlob(c.getColumnIndex(columnName));
	}
	
	public static Date getDate(Cursor c, String colomnName) {
		String s = getString(c, colomnName);
		if (s != null) {
			return new Date(Long.parseLong(s));
		}
		return null;
	}
}
