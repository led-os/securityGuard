package com.vidmt.telephone.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.net.http.HttpResponseCache;
import android.text.TextUtils;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.util.LogUtils;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;

public class HttpUtil {

	//huawei change timeoout time;
	//private final static int CONN_TIMEOUT = 1000 * 40;
	//private static HttpUtils mHttp = new HttpUtils(CONN_TIMEOUT);
	private static HttpUtils mHttp = new HttpUtils();

	public static void getConf(RequestCallBack<String> callback) {
		mHttp.send(HttpRequest.HttpMethod.GET, Config.URL_CONF, callback);
	}

	public static void getResponse(String url, RequestCallBack<String> callback) {
		mHttp.send(HttpRequest.HttpMethod.GET, url, callback);
	}

//	public static void uploadFile(String url, File file, RequestCallBack<String> callback) {
//		RequestParams params = new RequestParams(); // 默认编码UTF-8
//		params.addBodyParameter("file", file);
//		mHttp.send(HttpRequest.HttpMethod.POST, url, params, callback);
//	}

	public static ResponseStream uploadFile(String url, File file) throws HttpException {
		RequestParams params = new RequestParams(); // 默认编码UTF-8
		params.addBodyParameter("file", file);
		return 	mHttp.sendSync(HttpRequest.HttpMethod.POST, url, params);
	}


	public static void downloadFile(String url, File targetFile, RequestCallBack<File> callback) {
		mHttp.download(url, targetFile.getAbsolutePath(), callback);
	}

	public static String getPhoneAddr(String url) {
		URL Url = null;
		BufferedReader in = null;
		StringBuffer sb = new StringBuffer();
		try {
			Url = new URL(url);
			in = new BufferedReader(new InputStreamReader(Url.openStream(), "utf-8"));
			String str = null;
			while ((str = in.readLine()) != null) {
				sb.append(str);
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			VLog.e("test", e);
		} catch (IOException e) {
			VLog.e("test", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					VLog.e("test", e);
				}
				in = null;
			}
		}
		return null;
	}

	// 同步请求 必须在异步块儿中执行
	private static String doResponseSync(String url, HttpRequest.HttpMethod method, String... params) {
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
			String s = responseStream.readString();
			responseStream.close();
			return s;
		} catch (Exception e) {
			//huawei add for test server;
			if(Config.DEBUG){
				VidUtil.playSound(R.raw.beep, false);
				FLog.d("doResponseSync: ", "url:  " + url + "Exception :" + e.getMessage());
			}
			LogUtils.e(e.getMessage(), e);
		}
		return null;
	}

	public static String getResponseSync(String url, String... params) {
		return doResponseSync(url, HttpRequest.HttpMethod.GET, params);
	}

	public static String postResponseSync(String url, String... params) {
		return doResponseSync(url, HttpRequest.HttpMethod.POST, params);
	}

	public static String getParamsUrl(String url, String... params) {
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

	public static String getParamsUrl(String url, Map<String, String> paramMap) {
		HttpRequest httpReq = new HttpRequest(HttpRequest.HttpMethod.GET, url);
		RequestParams reqParams = new RequestParams();
		if (paramMap != null && paramMap.size() > 0) {
			for (String key : paramMap.keySet()) {
				reqParams.addQueryStringParameter(key, paramMap.get(key));
			}
		}
		httpReq.setRequestParams(reqParams);
		return httpReq.getURI().toString();
	}
}
