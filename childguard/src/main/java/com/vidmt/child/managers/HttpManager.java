package com.vidmt.child.managers;

import android.text.TextUtils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.vidmt.child.Config;

import java.io.File;

public class HttpManager {
	private static HttpManager sInstance;
	private HttpUtils mHttp;

	private HttpManager() {
		mHttp = new HttpUtils();
	}
	
	public static HttpManager get() {
		if (sInstance == null) {
			sInstance = new HttpManager();
		}
		return sInstance;
	}
	
	public void getConf(RequestCallBack<String> callback) {
		mHttp.send(HttpRequest.HttpMethod.GET, Config.URL_CONF, callback);
	}
	
	public void getResponse(String url, RequestCallBack<String> callback) {
		mHttp.send(HttpRequest.HttpMethod.GET, url, callback);
	}

	public void uploadFile(String url, File file, RequestCallBack<String> callback) {
		RequestParams params = new RequestParams(); // 默认编码UTF-8
		params.addBodyParameter("file", file);
		mHttp.send(HttpRequest.HttpMethod.POST, url, params, callback);
	}
	
	public void downloadFile(String url, File targetFile, RequestCallBack<File> callback) {
		mHttp.download(url, targetFile.getAbsolutePath(), callback);
	}
	
	// 同步请求 必须在异步块儿中执行
	private String doResponseSync(String url, HttpRequest.HttpMethod method, String... params) {
		RequestParams reqParams = new RequestParams();
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i += 2) {
				if (!TextUtils.isEmpty(params[i + 1])) {
					reqParams.addQueryStringParameter(params[i], params[i + 1]);
				}
			}
		}
		mHttp.configCurrentHttpCacheExpiry(20 * 1000);
		HttpUtils.sHttpCache.clear();// 清除http缓存,否则频繁访问，返回的是上一次缓存的结果
		try {
			ResponseStream responseStream = mHttp.sendSync(method, url, reqParams);
			return responseStream.readString();
		} catch (Exception e) {
			LogUtils.e(e.getMessage(), e);
		}
		return null;
	}
	
	public String getResponseSync(String url, String... params) {
		return doResponseSync(url, HttpRequest.HttpMethod.GET, params);
	}
	
	public String postResponseSync(String url, String...params) {
		return doResponseSync(url, HttpRequest.HttpMethod.POST, params);
	}
	
	public String getParamsUrl(String url, String... params) {
		HttpRequest httpReq = new HttpRequest(HttpRequest.HttpMethod.GET, url);
		RequestParams reqParams = new RequestParams();
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i += 2) {
				if (!TextUtils.isEmpty(params[i + 1])) {
					reqParams.addQueryStringParameter(params[i], params[i + 1]);
				}
			}
		}
		httpReq.setRequestParams(reqParams);
		return httpReq.getURI().toString();
	}

}
