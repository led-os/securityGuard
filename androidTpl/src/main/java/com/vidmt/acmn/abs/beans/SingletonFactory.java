package com.vidmt.acmn.abs.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public final class SingletonFactory {
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.TYPE)
	public static @interface Singleton {
	}

	private SingletonFactory() {
	}

	private static final Map<Class<?>, Object> singletonMap = new HashMap<Class<?>, Object>();

	public static final <T> void register(T obj) {
		singletonMap.put(obj.getClass(), obj);
	}

	public static final <T> void unRegitster(Class<T> clz) {
		singletonMap.remove(clz);
	}

	public static final <T> void clear() {
		singletonMap.clear();
	}

	public static final <T> T get(Class<T> clz) {
		@SuppressWarnings("unchecked")
		T obj = (T) singletonMap.get(clz);
		if (obj == null) {
			try {
				obj = clz.newInstance();
				singletonMap.put(clz, obj);
				return obj;
			} catch (Exception e) {
				return null;
			}
		}
		return obj;
	}

}
