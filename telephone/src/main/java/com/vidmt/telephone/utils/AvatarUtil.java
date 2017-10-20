package com.vidmt.telephone.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.FileStorage;

public class AvatarUtil {
	private static final int BITMAP_ROUND_CORNER = PixUtil.dp2px(55);

	private static final int SAMPLE_SIZE_WIDTH = 480;
	private static final int SAMPLE_SIZE_HEIGHT = 800;

	private static final int COMPRESS_FIRST_SIZE = 64 * 1024;
	private static final int COMPRESS_SECOND_SIZE = 128 * 1024;
	private static final int COMPRESS_THIRD_SIZE = 512 * 1024;
	private static final int COMPRESS_FOURTH_SIZE = 2 * 1024 * 1024;

	private static final int FACTOR_BEYOND_FIRST_SIZE = 60;
	private static final int FACTOR_BEYOND_SECOND_SIZE = 40;
	private static final int FACTOR_BEYOND_THIRD_SIZE = 30;
	private static final int FACTOR_BEYOND_FOURTH_SIZE = 20;

	public static boolean saveBitmap2file(Bitmap bmp, int quality, File destFile) {
		boolean successCompressed = false;
		if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(destFile);
			successCompressed = bmp.compress(format, quality, stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return successCompressed;
	}

	/**
	 * 类型JPEG
	 */
	public static boolean saveBitmap2file(Bitmap bmp, File path) {
		return saveBitmap2file(bmp, 100, path);
	}

	/**
	 * 根据路径获得图片，压缩返回bitmap用于显示
	 */
	public static Bitmap getSmallBitmap(String filePath) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);// a must
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, SAMPLE_SIZE_WIDTH, SAMPLE_SIZE_HEIGHT);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap smallBm = BitmapFactory.decodeFile(filePath, options);
		return toRGB565Bitmap(smallBm);
	}

	/**
	 * 由颜色数组重建Bitmap
	 */
	private static Bitmap toRGB565Bitmap(Bitmap srcBitmap) {
		int w = srcBitmap.getWidth();
		int h = srcBitmap.getHeight();
		int[] pix = new int[w * h];
		srcBitmap.getPixels(pix, 0, w, 0, 0, w, h);
		Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		img.setPixels(pix, 0, w, 0, 0, w, h);
		return img;
	}

	/**
	 * 计算图片的缩放值
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and
			// width
			int heightRatio = Math.round((float) height / (float) reqHeight);
			int widthRatio = Math.round((float) width / (float) reqWidth);
			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/*
	 * 获取压缩后的图片文件
	 */
	public static File getCompressedImgFile(File srcFile) {
		int compressFactor = 100;
		long picSize = srcFile.length();
		String tmpImgName = "tmp" + System.currentTimeMillis() + ".jpg";
		File tmpFile = new File(VLib.getSdcardDir(), FileStorage.buildCachePath(tmpImgName));
		if (picSize > COMPRESS_FIRST_SIZE) {// 压缩
			if (picSize < COMPRESS_SECOND_SIZE) {
				compressFactor = FACTOR_BEYOND_FIRST_SIZE;
			} else if (picSize >= COMPRESS_SECOND_SIZE && picSize < COMPRESS_THIRD_SIZE) {
				compressFactor = FACTOR_BEYOND_SECOND_SIZE;
			} else if (picSize >= COMPRESS_THIRD_SIZE && picSize < COMPRESS_FOURTH_SIZE) {
				compressFactor = FACTOR_BEYOND_THIRD_SIZE;
			} else {
				compressFactor = FACTOR_BEYOND_FOURTH_SIZE;
			}
			try {
				Bitmap albumPic = AvatarUtil.getSmallBitmap(srcFile.getAbsolutePath());
				AvatarUtil.saveBitmap2file(albumPic, compressFactor, tmpFile);
				albumPic.recycle();
			} catch (Throwable e) {
				VLog.e("test", e);
			}
		} else {
			VidUtil.copyFile(srcFile, tmpFile);
		}
		return tmpFile;
	}

	public static Bitmap getBitmapFromFile(String filePath) {
		Bitmap bm = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filePath);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			//bm = BitmapFactory.decodeStream(fis);
			bm = BitmapFactory.decodeStream(fis, null, options);
		} catch (FileNotFoundException e) {
			VLog.e("test", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bm;
	}

	public static byte[] bitmap2Bytes(Bitmap bm) {// Bitmap → byte[]
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bytes = stream.toByteArray();
		try {
			stream.close();
		} catch (IOException e) {
			VLog.e("test", e);
		}
		return bytes;
	}

	public static Bitmap bytes2Bimap(byte[] b) {// byte[] → Bitmap
		if (b != null && b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public static Bitmap toRoundCorner(Bitmap bitmap, int corner) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = corner;// 圆角像素
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	public static Bitmap toRoundCorner(Bitmap bitmap) { // 给图片加上圆角
		return toRoundCorner(bitmap, BITMAP_ROUND_CORNER);
	}

	/***
	 * 图片的缩放方法
	 * 
	 * @param srcBm
	 *            ：源图片资源
	 * @param newWidth
	 *            ：缩放后宽度
	 * @param newHeight
	 *            ：缩放后高度
	 * @return
	 */
	public static Bitmap zoomImage(Bitmap srcBm, double newWidth, double newHeight) {
		// 获取这个图片的宽和高
		float width = srcBm.getWidth();
		float height = srcBm.getHeight();
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 计算宽高缩放率
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(srcBm, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	public static Bitmap zoomImage(Bitmap srcBm, float scale) {
		float width = srcBm.getWidth();
		float height = srcBm.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap bitmap = Bitmap.createBitmap(srcBm, 0, 0, (int) width, (int) height, matrix, true);
		return bitmap;
	}

	public static Bitmap rotateBitmap(Bitmap bm, int orientationDegree) {
		Matrix m = new Matrix();
		m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
		float targetX, targetY;
		if (orientationDegree == 90) {
			targetX = bm.getHeight();
			targetY = 0;
		} else {
			targetX = bm.getHeight();
			targetY = bm.getWidth();
		}
		final float[] values = new float[9];
		m.getValues(values);
		float x1 = values[Matrix.MTRANS_X];
		float y1 = values[Matrix.MTRANS_Y];
		m.postTranslate(targetX - x1, targetY - y1);
		Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bm1);
		canvas.drawBitmap(bm, m, paint);
		return bm1;
	}
	
	/**
	 * 图片透明度处理
	 */
	public static Bitmap toTranslucent(Bitmap sourceImg) {
		int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
		sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
		int number = 50;// number:0全透明-100不透明
		number = number * 255 / 100;
		for (int i = 0; i < argb.length; i++) {
			argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);// 修改最高2位的值
		}
		sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);
		return sourceImg;
	}

}
