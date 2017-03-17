package com.vidmt.child.tasks;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.HttpManager;
import com.vidmt.child.utils.JsonResult;
import com.vidmt.child.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class AdInfoTask {
	public static final String SHOW_AD_WALL = "show_ad_wall";
	public static final String SHOW_AD_WIDGET = "show_ad_widget";
	public static final String SHOW_AD_BANNER = "show_ad_banner";

	public static Map<String, Boolean> getAdInfo() {
		final Map<String, Boolean> adInfoMap = new HashMap<String, Boolean>();
		adInfoMap.put(SHOW_AD_WALL, false);
		adInfoMap.put(SHOW_AD_WIDGET, false);
		adInfoMap.put(SHOW_AD_BANNER, false);
		HttpManager.get().getConf(new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String response = responseInfo.result;
				try {
					JsonResult<String> result = JsonUtil.getCorrectJsonResult(response);
					Map<String, String> map = result.getDataAsMap();
					String wall = map.get(SHOW_AD_WALL);
					String widget = map.get(SHOW_AD_WIDGET);
					String banner = map.get(SHOW_AD_BANNER);
					adInfoMap.put(SHOW_AD_WALL, "true".equals(wall));
					adInfoMap.put(SHOW_AD_WIDGET, "true".equals(widget));
					adInfoMap.put(SHOW_AD_BANNER, "true".equals(banner));
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				VLog.e("test", error);
			}
		});
		return adInfoMap;
	}

}
