package com.vidmt.acmn.utils.java;

public class NumUtil {
	public static final float parseFloat(String s) {
		if (s == null || s.length() == 0) {
			return 0;
		}
		return Float.parseFloat(s);
	}

	public static final double parseDouble(String s) {
		if (s == null || s.length() == 0) {
			return 0;
		}
		return Double.parseDouble(s);
	}

	public static final int parseInt(String s) {
		if (s == null || s.length() == 0) {
			return 0;
		}
		return Integer.parseInt(s);
	}

	public static <T> float[] toFloatArr(double[] arr) {
		float[] fArr = new float[arr.length];
		for (int i = 0, n = arr.length; i < n; i++) {
			fArr[i] = (float) arr[i];
		}
		return fArr;
	}

	public static <T> int[] toIntArr(float[] arr) {
		int[] fArr = new int[arr.length];
		for (int i = 0, n = arr.length; i < n; i++) {
			fArr[i] = (int) arr[i];
		}
		return fArr;
	}

	public static <T> int[] toIntArr(double[] arr) {
		int[] fArr = new int[arr.length];
		for (int i = 0, n = arr.length; i < n; i++) {
			fArr[i] = (int) arr[i];
		}
		return fArr;
	}

}
