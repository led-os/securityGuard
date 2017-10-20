package com.vidmt.telephone.utils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;

public class JsonUtil {

	public static <T> JsonResult<T> getCorrectJsonResult(String rawJson, Class<T> clz) throws VidException {
		if (rawJson == null) {
			throw new VHttpException(VHttpException.ERR_CODE_HTTP_SERVER_ERROR,
					VHttpException.ERR_MSG_HTTP_SERVER_ERROR);
		}
		int start = rawJson.indexOf("{");
		int end = rawJson.lastIndexOf("}");
		String json = null;
		try {
			json = rawJson.substring(start, end + 1);
		} catch (StringIndexOutOfBoundsException e) {
			throw new RuntimeException("getCorrectJsonResult：" + e + "," + rawJson);
		}
		try {
			JsonResult<T> result = new JsonResult<T>(json, clz);
			if (result.getCode() > 0) {
				int code = result.getCode();
				String msg = result.getMsg();
				throw new VHttpException(code, msg);
			}
			return result;
		} catch (JSONException e) {
			VLog.e("test", e);
			VidUtil.fLog("JSONException", json + "#" + e);
			throw new VidException(e.getMessage());
		}
	}

	public static JsonResult<String> getCorrectJsonResult(String rawJson) throws VidException {
		if (rawJson == null) {
			throw new VHttpException(VHttpException.ERR_CODE_HTTP_SERVER_ERROR,
					VHttpException.ERR_MSG_HTTP_SERVER_ERROR);
		}
		int start = rawJson.indexOf("{");
		int end = rawJson.lastIndexOf("}");
		String json = rawJson.substring(start, end + 1);
		try {
			JsonResult<String> result = new JsonResult<String>(json);
			int code = result.getCode();
			String msg = result.getMsg();
			if (result.getCode() > 0) {
				throw new VHttpException(code, msg);
			}
			return result;
		} catch (JSONException e) {
			VLog.e("test", e);
			VidUtil.fLog("JSONException", json + "#" + e);
			throw new VidException(e.getMessage());
		}
	}


	public static JSONObject getCorrectRawjson(String rawJson) throws VidException {
		if (rawJson == null) {
			throw new VHttpException(VHttpException.ERR_CODE_HTTP_SERVER_ERROR,
					VHttpException.ERR_MSG_HTTP_SERVER_ERROR);
		}
		int start = rawJson.indexOf("{");
		int end = rawJson.lastIndexOf("}");
		String json = rawJson.substring(start, end + 1);
		try {
			JsonResult<String> result = new JsonResult<String>(json);
			int code = result.getCode();
			String msg = result.getMsg();
			if (result.getCode() > 0) {
				throw new VHttpException(code, msg);
			}
			return result.getRawJson().getJSONObject("d");
		} catch (JSONException e) {
			VLog.e("test", e);
			VidUtil.fLog("JSONException", json + "#" + e);
			throw new VidException(e.getMessage());
		}
	}

}
