package com.vidmt.child.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Administrator
 * 
 * @param <T>
 *            can be String/Jsonable/pojo
 */
public class JsonResult<T> {
	private int code;
	private String msg;
	private JSONObject rawJson;
	private Class<T> clz;

	public JsonResult(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public JsonResult(String json) {
		JSONObject jObj = JSON.parseObject(json);
		Integer c = jObj.getInteger("c");
		if (c != null && c > 0) {
			code = c;
		}else{
			code = 0;
		}
		msg = jObj.getString("m");
		if (code == 0) {
			rawJson = jObj;
		}
	}

	public JsonResult(String json, Class<T> clz) {
		JSONObject jObj = JSON.parseObject(json);
		Integer c = jObj.getInteger("c");
		if (c != null && c > 0) {
			code = c;
		}else{
			code = 0;
		}
		msg = jObj.getString("m");
		if (code == 0) {
			rawJson = jObj;
		}
		this.clz = clz;
	}

	public boolean isOK() {
		return this.code == 0;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<T> getArrayData() {
		if (clz == null) {
			return null;
		} else {
			JSONArray jArr = rawJson.getJSONArray("list");
			return JSONArray.parseArray(jArr.toJSONString(), clz);
		}
	}

	@SuppressWarnings({ "unchecked" })
	public T getData() {
		if (clz == null) {
			return (T) rawJson.values().iterator().next();
		} else {
			return JSON.parseObject(rawJson.toJSONString(), clz);
		}
	}

	public String getData(String key) {
		return rawJson.getString(key);
	}
	public Map<String,String> getDataAsMap(){
		Set<Entry<String, Object>> entries = rawJson.entrySet();
		Map<String,String> map = new HashMap<String, String>();
		for(Entry<String, Object> entry:entries){
			map.put(entry.getKey(), entry.getValue().toString());
		}
		return map;
	}
	
	public JSONObject getRawJson(){
		return rawJson;
	}

	// response.put("list", list);
	// response.put("currentPage", pageCtrl.getCurrentPage());
	// response.put("total", pageCtrl.getRecordCount());
	// response.put("pageSize", pageCtrl.getPageSize());
	// response.put("pageSum", pageCtrl.getPageCount());

}
