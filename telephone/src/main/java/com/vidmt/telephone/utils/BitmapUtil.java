package com.vidmt.telephone.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapCommonUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;

public class BitmapUtil {
	private static BitmapUtils bitmapUtils;
	private static BitmapDisplayConfig bigPicDisplayConfig;
	private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

	public static void init() {
		bitmapUtils = new BitmapUtils(App.get().getApplicationContext());
		bitmapUtils.configDefaultLoadingImage(R.drawable.common_loading);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.default_avatar);
		bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
		// bitmapUtils.configMemoryCacheEnabled(false);
		// bitmapUtils.configDiskCacheEnabled(false);
		// bitmapUtils.configDefaultAutoRotation(true);
		// ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
		// Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		// animation.setDuration(800);
		// AlphaAnimation 在一些android系统上表现不正常, 造成图片列表中加载部分图片后剩余无法加载, 目前原因不明.
		// 可以模仿下面示例里的fadeInDisplay方法实现一个颜色渐变动画。
		// AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
		// animation.setDuration(1000);
		// bitmapUtils.configDefaultImageLoadAnimation(animation);
		// 设置最大宽高, 不设置时更具控件属性自适应.
		// bitmapUtils.configDefaultBitmapMaxSize(BitmapCommonUtils.getScreenSize(activity).scaleDown(3));
		// 滑动时加载图片，快速滑动时不加载图片
		// imageListView.setOnScrollListener(new
		// PauseOnScrollListener(bitmapUtils, false, true));
	}

	public static <T extends View> void display(T container, String uri) {
		if (uri != null && uri.contains("-")) {
			uri = uri.split("-")[0];
		}
		if (uri != null) {
			uri = uri.replace("/static", "");
			Log.i("lk", "display:" + Config.WEB_RES_SERVER + uri);
			VidUtil.fLog("avatarUri is: " + Config.WEB_RES_SERVER + uri);
			bitmapUtils.display(container, Config.WEB_RES_SERVER + uri);
		}
	}

	public static <T extends View> void display(T container, String uri, BitmapLoadCallBack<T> callBack) {
		VidUtil.fLog("avatarUri is: " + Config.WEB_RES_SERVER + uri);
		bitmapUtils.display(container, Config.WEB_RES_SERVER + uri, callBack);
	}

	public static <T extends View> void display(T container, String uri, BitmapDisplayConfig displayConfig) {
		VidUtil.fLog("avatarUri is: " + Config.WEB_RES_SERVER + uri);
		bitmapUtils.display(container, Config.WEB_RES_SERVER + uri, displayConfig);
	}

	public static <T extends View> void display(T container, String uri, BitmapDisplayConfig displayConfig,
			BitmapLoadCallBack<T> callBack) {
		// 读取assets中的图片"assets/img/xx.jpg"
		// 读取sdcard中的图片"/sdcard/xx.jpg"
		bitmapUtils.display(container, uri, bigPicDisplayConfig, callBack);
	}

	public static void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
		final TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[] { TRANSPARENT_DRAWABLE,
				new BitmapDrawable(imageView.getResources(), bitmap) });
		imageView.setImageDrawable(transitionDrawable);
		transitionDrawable.startTransition(500);
	}

	// public class CustomBitmapLoadCallBack extends
	// DefaultBitmapLoadCallBack<ImageView> {
	// private final ImageItemHolder holder;
	//
	// public CustomBitmapLoadCallBack(ImageItemHolder holder) {
	// this.holder = holder;
	// }
	//
	// @Override
	// public void onLoading(ImageView container, String uri,
	// BitmapDisplayConfig config, long total, long current) {
	// this.holder.imgPb.setProgress((int) (current * 100 / total));
	// }
	//
	// @Override
	// public void onLoadCompleted(ImageView container, String uri, Bitmap
	// bitmap, BitmapDisplayConfig config,
	// BitmapLoadFrom from) {
	// // super.onLoadCompleted(container, uri, bitmap, config, from);
	// fadeInDisplay(container, bitmap);
	// this.holder.imgPb.setProgress(100);
	// }
	// }

	public static void showImage(ImageView view, String url) {
		bigPicDisplayConfig = new BitmapDisplayConfig();
		// bigPicDisplayConfig.setShowOriginal(true); // 显示原始图片,不压缩, 尽量不要使用,
		// 图片太大时容易OOM。
		bigPicDisplayConfig.setBitmapConfig(Bitmap.Config.RGB_565);
		bigPicDisplayConfig.setBitmapMaxSize(BitmapCommonUtils.getScreenSize(App.get().getApplicationContext()));
		BitmapLoadCallBack<ImageView> callback = new DefaultBitmapLoadCallBack<ImageView>() {
			@Override
			public void onLoadStarted(ImageView container, String uri, BitmapDisplayConfig config) {
				super.onLoadStarted(container, uri, config);
				MainThreadHandler.makeToast(uri);
			}

			@Override
			public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config,
					BitmapLoadFrom from) {
				super.onLoadCompleted(container, uri, bitmap, config, from);
				MainThreadHandler.makeToast(bitmap.getWidth() + "*" + bitmap.getHeight());
			}
		};
		bitmapUtils.display(view, url, bigPicDisplayConfig, callback);
	}

}
