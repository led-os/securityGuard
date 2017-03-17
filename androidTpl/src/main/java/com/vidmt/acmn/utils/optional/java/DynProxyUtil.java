package com.vidmt.acmn.utils.optional.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynProxyUtil {
	public static Object newProxy(Object targetObject,
			InvocationHandler aopHandler) {
		ClassLoader cl = targetObject.getClass().getClassLoader();
		Class<?>[] interfaces = targetObject.getClass().getInterfaces();
		MyInvocationHandler myHandler = new MyInvocationHandler(targetObject,
				aopHandler);
		return Proxy.newProxyInstance(cl, interfaces, myHandler);
	}

	private static class MyInvocationHandler implements InvocationHandler {
		private Object tartetObj;
		private InvocationHandler aopHandler;

		public MyInvocationHandler(Object targetObj,
				InvocationHandler aopHandler) {// 非必须，但是为了给invoke传目的接口实现对象，需要它
			this.tartetObj = targetObj;
			this.aopHandler = aopHandler;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { // 由代理类调用
				if (aopHandler != null) {
					return aopHandler.invoke(tartetObj, method, args);
				}
				return null;
		}
	}
}
