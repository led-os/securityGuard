package com.vidmt.acmn.abs;

import java.util.Hashtable;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ParamActivity extends AbsBaseActivity {
	private static Map<Class<? extends ParamActivity>, Object> paramCache = new Hashtable<Class<? extends ParamActivity>, Object>();

	public static void startActivity(Context context, Class<? extends ParamActivity> clz, Object param) {
		Intent intent = new Intent(context, clz);
		if (clz != null) {
			paramCache.put(clz, param);
		}
		context.startActivity(intent);
	}

	public static void startActivity(Context context, Intent intent, Object param) {
		ComponentName cmpName = intent.getComponent();
		if (cmpName == null) {
			throw new IllegalArgumentException("component name cannot be null");
		}
		String clzName = cmpName.getClassName();
		if (clzName != null) {
			@SuppressWarnings("unchecked")
			Class<? extends ParamActivity> clz;
			try {
				clz = (Class<? extends ParamActivity>) Class.forName(clzName);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
			paramCache.put(clz, param);
		}
		context.startActivity(intent);
	}

	protected Object getParam(){
		return paramCache.remove(this.getClass());
	}

}
