package com.vidmt.child.utils;

import com.alibaba.fastjson.JSONException;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.exceptions.VidException;

public class JsonUtil {

	public static <T> JsonResult<T> getCorrectJsonResult(String rawJson, Class<T> clz) throws VidException {
		if (rawJson == null) {
			throw new VidException("NULL");
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
				throw new VidException(result.getCode(), result.getMsg());
			}
			return result;
		} catch (JSONException e) {
			VLog.e("test", e);
			FLog.d("JSONException", json + "#" + e);
			throw new VidException(e.getMessage());
		}
	}

	public static JsonResult<String> getCorrectJsonResult(String rawJson) throws VidException {
		if (rawJson == null) {
			throw new VidException("NULL");
		}
		int start = rawJson.indexOf("{");
		int end = rawJson.lastIndexOf("}");
		String json = rawJson.substring(start, end + 1);
		try {
			JsonResult<String> result = new JsonResult<String>(json);
			if (result.getCode() > 0) {
				throw new VidException(result.getCode(), result.getMsg());
			}
			return result;
		} catch (JSONException e) {
			VLog.e("test", e);
			FLog.d("JSONException", json + "#" + e);
			throw new VidException(e.getMessage());
		}
	}

}
