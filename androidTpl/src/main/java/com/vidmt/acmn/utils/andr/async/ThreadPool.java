package com.vidmt.acmn.utils.andr.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
	private static ExecutorService cachedThreadPool ;

	public static void execute(Runnable r) {
		if (cachedThreadPool == null) {
			cachedThreadPool = Executors.newCachedThreadPool();
		}
		cachedThreadPool.execute(r);
	}
}
