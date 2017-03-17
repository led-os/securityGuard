package com.vidmt.acmn.utils.optional.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.java.CommUtil;

public class HttpConn {
	private static final int CONN_TIMEOUT = 30 * 1000;
	private static final int READ_TIMEOUT = 30 * 1000;

	static {
		System.setProperty("http.keepAlive", "true");
		System.setProperty("http.maxConnections", "5");
	}

	public static String getStr(String url) throws IOException {
		return getStr(url, null, null).getContent();
	}

	public static String postStr(String url, Map<String, String> param) throws IOException {
		return postStr(url, param, null).getContent();
	}

	public static HttpStrResponse postStr(String url, Map<String, String> params, Map<String, String> customHttpHeader) throws IOException {
		return httpStr("POST", url, params, customHttpHeader);
	}

	public static HttpStrResponse getStr(String url, Map<String, String> params, Map<String, String> customHttpHeader) throws IOException {
		return httpStr("GET", url, params, customHttpHeader);
	}

	public static HttpInputStreamResponse postInputSteam(String url, Map<String, String> params,
			Map<String, String> customHttpHeader) throws IOException {
		return httpInputStream("POST", url, params, customHttpHeader);
	}

	public static HttpInputStreamResponse getInputSteam(String url, Map<String, String> params,
			Map<String, String> customHttpHeader) throws IOException {
		return httpInputStream("GET", url, params, customHttpHeader);
	}

	private static HttpInputStreamResponse httpInputStream(String method, String urlStr, Map<String, String> params,
			Map<String, String> customHttpHeader) throws IOException {
		HttpURLConnection conn = null;
		StringBuilder sb = new StringBuilder();
		if (params != null && params.size() > 0) {
			for (Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue()).append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		try {
			if (method == null || ("GET".equalsIgnoreCase(method) && sb.length() > 0)) {
				urlStr = urlStr + "?" + sb.toString();
			}
			URL url = new URL(urlStr);
			java.net.Proxy javaProxy = NetUtil.getApnJavaProxy();
			if (javaProxy == null) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection(javaProxy);
			}

			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONN_TIMEOUT);
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			// conn.setRequestProperty("Referer",
			// "http://localhost:8080/Arithmancy/index.html");
			conn.setRequestProperty("Accept-Encoding", "gzip");

			if (customHttpHeader != null && customHttpHeader.size() > 0) {
				for (Entry<String, String> entry : customHttpHeader.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			if ("POST".equalsIgnoreCase(method) && sb.length() > 0) {
				byte[] content = sb.toString().getBytes("utf-8");
				// conn.setRequestProperty("Content-Length",
				// String.valueOf(content.length));

				if (VLib.DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr + "?" + sb);
					logReqHead(conn);
				}
				conn.getOutputStream().write(content);

			} else {
				if (VLib.DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + urlStr);
					logReqHead(conn);
				}
			}

			String respEncoding = conn.getContentEncoding();
			InputStream is = null;
			if ("gzip".equalsIgnoreCase(respEncoding)) {
				is = new GZIPInputStream(conn.getInputStream());
			} else {
				is = conn.getInputStream();
			}

			Map<String, List<String>> origHeaders = conn.getHeaderFields();
			Map<String, String> headerMap = new HashMap<String, String>();
			for (Entry<String, List<String>> entry : origHeaders.entrySet()) {
				String s = "";
				for (String ss : entry.getValue()) {
					s += ss + "|";
				}
				headerMap.put(entry.getKey(), s.substring(0, s.length() - 1));
			}
			if (VLib.DEBUG) {
				System.out.println("-----------------------------");
				for (Entry<String, String> entry : headerMap.entrySet()) {
					System.out.println(entry.getKey() + ":" + entry.getValue());
				}
			}

			return new HttpInputStreamResponse(is, headerMap);
		} catch (IOException e) {
			if (e instanceof UnknownHostException || e instanceof ConnectException) {
				throw e;
			}
			if (conn == null) {
				return null;
			}
			InputStream es = conn.getErrorStream();
			if (es != null) {
				try {
					String errorMsg = CommUtil.readerFromInputStream(es, null);
					System.err.println("http error:" + errorMsg);
					es.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return null;
	}

	public static HttpStrResponse httpStr(String method, String urlStr, Map<String, String> params,
			Map<String, String> customHttpHeader) throws IOException {
		HttpURLConnection conn = null;
		Reader reader = null;
		StringBuilder sb = new StringBuilder();
		if (params != null && params.size() > 0) {
			for (Entry<String, String> entry : params.entrySet()) {
				sb.append(entry.getKey() + "=" + entry.getValue()).append("&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		try {
			if (method == null || ("GET".equalsIgnoreCase(method) && sb.length() > 0)) {
				urlStr = urlStr + "?" + sb.toString();
			}
			URL url = new URL(urlStr);
			java.net.Proxy javaProxy = NetUtil.getApnJavaProxy();
			if (javaProxy == null) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection(javaProxy);
			}
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONN_TIMEOUT);
			conn.setRequestMethod(method);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
			// conn.setRequestProperty("Referer",
			// "http://localhost:8080/Arithmancy/index.html");
			conn.setRequestProperty("Accept-Encoding", "gzip");

			if (customHttpHeader != null && customHttpHeader.size() > 0) {
				for (Entry<String, String> entry : customHttpHeader.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			if ("POST".equalsIgnoreCase(method) && sb.length() > 0) {
				byte[] content = sb.toString().getBytes("utf-8");
				// conn.setRequestProperty("Content-Length",
				// String.valueOf(content.length));

				if (VLib.DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + method + "->" + urlStr + "?" + sb);
					logReqHead(conn);
				}
				conn.getOutputStream().write(content);

			} else {
				if (VLib.DEBUG) {
					System.out.println("======BELLOW DEBUG INFO============");
					System.out.println("url is:" + urlStr);
					logReqHead(conn);
				}
			}

			reader = decodeRespInputStream(conn);
			String msg = CommUtil.readerFromReader(reader);

			Map<String, List<String>> origHeaders = conn.getHeaderFields();
			Map<String, String> headerMap = new HashMap<String, String>();
			for (Entry<String, List<String>> entry : origHeaders.entrySet()) {
				String s = "";
				for (String ss : entry.getValue()) {
					s += ss + "|";
				}
				headerMap.put(entry.getKey(), s.substring(0, s.length() - 1));
			}
			if (VLib.DEBUG) {
				System.out.println("-----------------------------");
				for (Entry<String, String> entry : headerMap.entrySet()) {
					System.out.println(entry.getKey() + ":" + entry.getValue());
				}

				System.out.println("HTTP RETURN:\n" + msg);
			}

			return new HttpStrResponse(msg, headerMap);
		} catch (IOException e) {
			if (e instanceof UnknownHostException || e instanceof ConnectException) {
				throw e;
			}
			if (conn == null) {
				return null;
			}
			InputStream es = conn.getErrorStream();
			if (es != null) {
				try {
					String errorMsg = CommUtil.readerFromInputStream(es, null);
					System.err.println("http error:" + errorMsg);
					es.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} finally {
			CommUtil.close(reader);
		}
		return null;
	}

	/************************************************************************************/
	private static abstract class HttpResponse {
		public HttpResponse(Map<String, String> headers) {
			this.headers = headers;
		}

		private Map<String, String> headers;

		public Map<String, String> getHeaders() {
			return headers;
		}

		public String getHeader(String key) {
			return headers.get(key);
		}
	}

	public static class HttpStrResponse extends HttpResponse {
		private String content;

		public HttpStrResponse(String content, Map<String, String> headers) {
			super(headers);
			this.content = content;
		}

		public String getContent() {
			return content;
		}
	}

	public static class HttpInputStreamResponse extends HttpResponse {
		private InputStream mInputStream;

		public HttpInputStreamResponse(InputStream inputStream, Map<String, String> headers) {
			super(headers);
			mInputStream = inputStream;
		}

		public InputStream getInputSteam() {
			return mInputStream;
		}
	}

	private static Reader decodeRespInputStream(HttpURLConnection conn) throws IOException {
		String respEncoding = conn.getContentEncoding();
		InputStream is = null;
		if ("gzip".equalsIgnoreCase(respEncoding)) {
			is = new GZIPInputStream(conn.getInputStream());
		} else {
			is = conn.getInputStream();
		}

		String charset = null;
		String contentType = conn.getContentType();
		if (contentType != null) {
			String[] arr = contentType.replace(" ", "").split(";");
			for (String s : arr) {
				if (s.startsWith("charset=")) {
					charset = s.split("=", 2)[1];
				}
			}
		}

		if (charset != null) {
			return new InputStreamReader(is, charset);
		} else {
			return new InputStreamReader(is);
		}
	}

	private static void logReqHead(HttpURLConnection conn) {
		Map<String, List<String>> headers = conn.getRequestProperties();
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String s = entry.getKey() + ":";
			for (String ss : entry.getValue()) {
				s += ss + "|";
			}

			System.out.println(s.substring(0, s.length() - 1));
		}
	}
}
