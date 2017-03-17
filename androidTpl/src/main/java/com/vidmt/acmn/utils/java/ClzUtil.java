package com.vidmt.acmn.utils.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClzUtil {
	public static Object runMethod(Object obj, String mtd, Object... args) {
		Method[] methods = obj.getClass().getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals(mtd)) {
				m.setAccessible(true);
				try {
					return m.invoke(obj, args);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		throw new IllegalArgumentException("invalid method name:" + mtd);
	}

}
