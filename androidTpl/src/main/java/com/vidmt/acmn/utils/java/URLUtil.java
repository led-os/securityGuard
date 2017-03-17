package com.vidmt.acmn.utils.java;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtil {
	public static String encode(String url, String charset) {
		// 转换中文编码
		String split[] = url.split("/");
		for (int i = 1; i < split.length; i++) {
			try {
				split[i] = URLEncoder.encode(split[i], charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			split[0] = split[0] + "/" + split[i];
		}
		split[0] = split[0].replaceAll("\\+", "%20");// 处理空格
		return split[0];
	}
}
