package com.vidmt.acmn.utils.java;

import java.lang.reflect.Field;

import com.vidmt.acmn.utils.andr.VLog;

public class ReflectUtil {

	public static Object getField(Object obj, String fieldName) {

		Class<?> clz;
		if (obj instanceof Class) {
			clz = (Class<?>) obj;
		} else {
			clz = obj.getClass();
		}
		obj.getClass();
		Field f = getField(clz, fieldName);
		f.setAccessible(true);
		try {
			if (clz == obj) {
				return f.get(null);
			} else {
				return f.get(obj);
			}
		} catch (Throwable e) {
			VLog.e("test", "set field error", e);
			return null;
		}
	}

	public static void changeField(Object obj, String fieldName, Object value) {
		Class<?> clz = obj.getClass();
		Field f = getField(clz, fieldName);
		f.setAccessible(true);
		try {
			f.set(obj, value);
		} catch (Throwable e) {
			VLog.e("test", "set field error", e);
		}
	}

	private static Field getField(Class<?> clz, String fieldName) {
		while (true) {
			try {
				return clz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				// VLog.w("test", "no field:" + clz + "/" + fName +
				// ", try super");
				clz = clz.getSuperclass();
			}
		}
	}
}
