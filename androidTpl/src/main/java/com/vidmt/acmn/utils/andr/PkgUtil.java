package com.vidmt.acmn.utils.andr;

import java.lang.reflect.Field;
import java.util.Enumeration;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class PkgUtil {

	private static Field dexField;

	static {
		try {
			Field[] fileds = PathClassLoader.class.getDeclaredFields();
			dexField = PathClassLoader.class.getDeclaredField("mDexs");
			dexField.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void scan() {
		try {
			PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();

			DexFile[] dexs = (DexFile[]) dexField.get(classLoader);
			for (DexFile dex : dexs) {
				Enumeration<String> entries = dex.entries();
				while (entries.hasMoreElements()) {
					String entry = entries.nextElement();

					Class<?> entryClass = dex.loadClass(entry, classLoader);
					if (entryClass != null) {
						System.out.println("name=" + entryClass + "; class=" + entryClass.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}