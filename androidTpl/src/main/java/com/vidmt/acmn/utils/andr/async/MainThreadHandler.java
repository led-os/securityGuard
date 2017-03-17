package com.vidmt.acmn.utils.andr.async;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.os.Handler;
import android.os.Looper;

import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.optional.java.DynProxyUtil;

public class MainThreadHandler {
	private static Handler sMainLooperHandler;

	private MainThreadHandler() {
	}

	private static void init() {
		if (sMainLooperHandler == null) {
			sMainLooperHandler = new Handler(Looper.getMainLooper());
		}
	}

	public static void post(Runnable r) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			r.run();
			return;
		}
		init();
		sMainLooperHandler.post(r);
	}

	public static void postDelayed(Runnable r) {
		init();
		sMainLooperHandler.post(r);
	}

	public static void postDelayed(Runnable r, long delayed) {
		init();
		sMainLooperHandler.postDelayed(r, delayed);
	}

	public static void makeToast(final String msg) {
		init();
		sMainLooperHandler.post(new Runnable() {

			@Override
			public void run() {
				AndrUtil.makeToast(msg);
			}
		});
	}
	
	public static void makeToast(final int res) {
		init();
		sMainLooperHandler.post(new Runnable() {

			@Override
			public void run() {
				AndrUtil.makeToast(res);
			}
		});
	}
	
	public static void makeLongToast(final String msg) {
		init();
		sMainLooperHandler.post(new Runnable() {

			@Override
			public void run() {
				AndrUtil.makeLongToast(msg);
			}
		});
	}
	
	public static void makeLongToast(final int res) {
		init();
		sMainLooperHandler.post(new Runnable() {

			@Override
			public void run() {
				AndrUtil.makeLongToast(res);
			}
		});
	}
	
	public static Object proxy(Object obj) {
		return DynProxyUtil.newProxy(obj, new InvocationHandler() {
			@Override
			public Object invoke(final Object tgtObj, final Method method, final Object[] args) {
				post(new Runnable() {

					@Override
					public void run() {
						try {
							method.invoke(tgtObj, args);
						} catch (Throwable e) {
							throw new IllegalArgumentException(e);
						}
					}
				});
				return null;
			}
		});
	}
}
