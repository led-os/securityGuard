package com.vidmt.acmn.utils.optional.andr;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public final class BitmapUtil {
	public static final byte[] bitmap2Array(Bitmap bmp) {
		int size = bmp.getWidth() * bmp.getHeight() * 4;
		// 创建一个字节数组输出流,流的大小为size
		ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
		bmp.compress(CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static final Bitmap array2Bitmap(byte[] array) {
		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	public static final Bitmap grey(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	}
}
