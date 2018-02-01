package com.vidmt.child.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.App;
import com.vidmt.child.Config;
import com.vidmt.child.Const;
import com.vidmt.child.FileStorage;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.LoginActivity;
import com.vidmt.child.activities.SplashActivity;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.HttpManager;
import com.vidmt.child.managers.ServiceManager;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VidUtil {
	private static final int VERSION_CODES_KITKAT = 19;
	// ///////////////////////////////////////////////////////////////////////////////
	private static Map<SoundPool, Integer> mSoundPoolMap = new HashMap<SoundPool, Integer>();
	// ////////////////////////////////////////////////////////////////////////////////
	private static File mRecordTempFile;
	private static MediaRecorder mRecorder;

	public static boolean isPhoneNO(String mobile) {
		 Pattern p = Pattern.compile("^1\\d{10}$");
//		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();
	}

	public static boolean isPwdFormat(String pwd) {
		Pattern p = Pattern.compile("[a-z0-9A-Z_]+");
		Matcher m = p.matcher(pwd);
		return m.matches();
	}

	public static void setWarnText(EditText et, String s) {
		et.setText("");
		et.setHintTextColor(Color.RED);
		et.setHint(s);
	}

	/**
	 * 获取组件绝对坐标位置
	 *
	 * @param view
	 * @return [x,y]
	 */
	public static int[] getAbsolutePosition(View view) {
		int[] xy = new int[2];
		view.getLocationOnScreen(xy);
		return xy;
	}

	/**
	 * 获取组件尺寸
	 *
	 * @param view
	 * @return [width,height]
	 */
	public static int[] getViewMeasure(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		int[] measure = new int[] { view.getMeasuredWidth(), view.getMeasuredHeight() };
		return measure;
	}

	/**
	 * positions:左上角x、右下角x、左上角y、右下角y
	 */
	public static int[] getWindowPosition(View view) {
		int[] positions = new int[4];
		int width = SysUtil.getDisplayMetrics().widthPixels;
		int height = SysUtil.getDisplayMetrics().heightPixels;
		int[] wh = VidUtil.getViewMeasure(view);
		positions[0] = width / 2 - wh[0] / 2;// 左上角x
		positions[2] = width / 2 + wh[0] / 2;// 右下角x
		positions[1] = height / 2 - wh[1] / 2;// 左上角y
		positions[3] = height / 2 + wh[1] / 2;// 右下角y
		return positions;
	}

	public static void share(String text, Bitmap bm) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (bm != null) {// 分享图片
			String picName = "share" + System.currentTimeMillis() + ".png";
			File imgFile = new File(VLib.getSdcardDir(), FileStorage.buildSharePath(picName));
			try {
				bm.compress(CompressFormat.PNG, 100, new FileOutputStream(imgFile));
			} catch (Exception e) {
				VLog.e("test", e);
			}
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgFile));
		} else {
			intent.setType("text/plain");
		}
		intent.putExtra(Intent.EXTRA_SUBJECT, "选择分享方式");
		// "您好,我在使用软件 - 儿童卫士，感觉非常棒，邀请您一起来使用。软件下载地址:" + Config.URL_LATEST_APK
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		VLib.app().startActivity(intent);
	}

	// ///////////////////////////////////////////////////////////////////////

	public static void playSound(final int resId, final boolean repeat) {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100);
				int soundID = soundPool.load(VLib.app(), resId, 1);
				mSoundPoolMap.put(soundPool, soundID);
				AudioManager mgr = (AudioManager) VLib.app().getSystemService(Context.AUDIO_SERVICE);
				float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
				float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
				final float volume = streamVolumeCurrent / streamVolumeMax;
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				soundPool.play(soundID, volume, volume, 1, repeat ? -1 : 0, 1.0f);// null-pointer
			}
		});
	}

	public static void stopSound() {
		try {
			for (SoundPool sp : mSoundPoolMap.keySet()) {
				sp.release();
				sp.stop(mSoundPoolMap.get(sp));
				sp = null;
			}
		} catch (Throwable t) {
			VLog.e("test", t);
		}
		mSoundPoolMap.clear();
	}

	public static SpannableString replaceToFace(String content) {
		if (content == null) {
			return new SpannableString(App.get().getString(R.string.child_record_no_permission));
		}
		SpannableString ss = new SpannableString(content);
		String[] faceStrs = VLib.app().getResources().getStringArray(R.array.face_strs);
		for (int i = 0; i < faceStrs.length; i++) {
			String face = faceStrs[i];
			while (content.contains(face)) {
				int start = content.indexOf(face);
				int end = start + face.length();
				TypedArray faceIdTypes = VLib.app().getResources().obtainTypedArray(R.array.face_res_ids);
				Drawable d = faceIdTypes.getDrawable(i);
				int screenWidth = SysUtil.getDisplayMetrics().widthPixels;
				d.setBounds(0, 0, screenWidth / Const.FACE_COLUMNS / 3, screenWidth / Const.FACE_COLUMNS / 3);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				ss.setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);// 包括前后
				String fill = "";
				for (int k = 0; k < face.length() - 2; k++) {
					fill += "#";
				}
				content = content.substring(0, start) + "[" + fill + "]" + content.substring(end);
			}
		}
		return ss;
	}

	// ///////////////////////////////////////////////////////////

	public static boolean isTopActivity() {
		ActivityManager activityMgr = (ActivityManager) VLib.app().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityMgr.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			PackageInfo pkgInfo = SysUtil.getPkgInfo();
			String pkgName = pkgInfo.packageName;
			if (pkgName.equals(tasksInfo.get(0).topActivity.getPackageName())) { // 应用程序位于堆栈的顶层
				return true;
			}
		}
		return false;
	}

	public static Dialog getBottomDialog(Activity context, View view) {
		Dialog dialog = new Dialog(context, R.style.MenuDialogStyle);
		dialog.setContentView(view);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		int screenW = SysUtil.getDisplayMetrics().widthPixels;
		lp.width = screenW;
		window.setGravity(Gravity.BOTTOM); // 设置dialog显示的位置
		window.setWindowAnimations(R.style.MenuDialogAnimation); // 添加动画
		return dialog;
	}

	@SuppressLint("NewApi")
	public static String getFilePath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= VERSION_CODES_KITKAT;
		if (!isKitKat) {
			return getFilePath(uri);
		}
		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if ("com.google.android.apps.photos.content".equals(uri.getAuthority()))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////

	private static String getFilePath(Uri uri) {
		try {
			if (uri.getScheme().equals("file")) {
				return uri.getPath();
			} else {
				Cursor cursor = SysUtil.getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				String imgPath = cursor.getString(1);
				cursor.close();
				return imgPath;
			}
		} catch (Exception e) {
			VLog.e("test", e);
			return null;
		}
	}

	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile) {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] b = new byte[1024 * 5];// 缓冲数组
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			outBuff.flush();// 刷新此缓冲的输出流
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inBuff != null) {
				try {
					inBuff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outBuff != null) {
				try {
					outBuff.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void startRecord() {
		try {
			mRecordTempFile = File.createTempFile("REMOTE", ".amr");
		} catch (IOException e) {
			VLog.e("test", e);
		}
		mRecorder = new MediaRecorder();// 实例化
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);// 输出文件格式
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);// 音频文件编码
		mRecorder.setOutputFile(mRecordTempFile.getAbsolutePath());// 输出文件路径
		try {// 开始录音
			mRecorder.prepare();
			mRecorder.start();// 此处可能弹出-权限提示框，阻塞下面代码的执行
		} catch (Throwable t) {
			VLog.e("test", t);
			try {
				mRecorder.stop();
				mRecorder.release();
			} catch (IllegalStateException e) {
				VLog.e("test", e);
			}
			mRecorder = null;
		}
	}

	public static File getRecordFile() {
		if(mRecorder == null){
			return null;
		}
		try {
			mRecorder.stop();
			mRecorder.release();
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		}
		mRecorder = null;
		return mRecordTempFile;
	}

	// ///////////////////////////////////////////////////////////////////////////////

	/**
	 * 根据apk路径安装apk包。
	 */
	public static void installApk(File apkFile) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		VLib.app().startActivity(intent);
	}

	/**
	 * 返回对方账号(uid)
	 */
	public static String getSideName() {
		boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
		String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		if (isBabyClient) {
			return account;
		} else {
			return Const.BABY_ACCOUNT_PREFIX + account;
		}
	}

	// public static String getChildName() {
	// XMPPConnection conn = XmppManager.get().conn();
	// if (conn != null) {
	// String curUser = conn.getUser();
	// if (curUser != null) {
	// String curName = StringUtils.parseName(curUser);
	// return Const.CHILD_ACCOUNT_PREFIX + curName;
	// }
	// }
	// String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
	// return Const.CHILD_ACCOUNT_PREFIX + account;
	// }
	//
	// public static String getParentName() {
	// String curName =
	// StringUtils.parseName(XmppManager.get().conn().getUser());
	// if (curName == null) {// isAuthenticated:false,null
	// FLog.d("getParentName==null", "isAuthenticated:" +
	// (XmppManager.get().isAuthenticated() ? true : false)
	// + "," + XmppManager.get().conn().getUser());
	// curName = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
	// }
	// return curName.substring(1);
	// }

	public static boolean isAppAuthroized() {
		return MD5.getMD5("vidmt." + VLib.app().getPackageName()).equals(Config.APP_ID);
	}

	public static Bitmap getBitmapFromFile(File file) {
		Bitmap bm = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file.getAbsolutePath());
			bm = BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			VLog.e("test", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					VLog.e("test", e);
				}
			}
		}
		return bm;
	}

	/**
	 * 不存在,返回file对象；存在,返回null
	 */
	public static File isAvatarNotCached(String avatarUri) {
		if (avatarUri == null) {
			return new File("");
		}
		File avatarFile = new File(VLib.getSdcardDir(), avatarUri);
		if (!avatarFile.exists()) {
			return avatarFile;
		}
		return null;
	}

	private static Bitmap view2Bitmap(View view) {
		view.destroyDrawingCache();
		view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}

	public static void setChildMarkerAvatar(Marker marker, Bitmap avatarBm) {
		View view = SysUtil.inflate(R.layout.avatar_child_map);
		ImageView avatarView = (ImageView) view.findViewById(R.id.head);
		if (!AccManager.get().isSideOnline()) {
			avatarBm = AvatarUtil.toTranslucent(avatarBm);
		}
		avatarBm = AvatarUtil.toRoundCorner(avatarBm);
		avatarView.setImageBitmap(avatarBm);
		BitmapDescriptor bmDesc = BitmapDescriptorFactory.fromBitmap(VidUtil.view2Bitmap(view));
		marker.setIcon(bmDesc);
	}

	public static void asyncCacheAndDisplayChildMapPinAvatar(final Marker marker, String avatarUri) {
		final View view = SysUtil.inflate(R.layout.avatar_child_map);
		final int mapPinSize = PixUtil.dp2px(40);
		final ImageView avatarView = (ImageView) view.findViewById(R.id.head);
		final File avatarFile = isAvatarNotCached(avatarUri);
		if (avatarFile != null) {
			HttpManager.get().downloadFile(Config.WEB_RES_SERVER + avatarUri, avatarFile, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					BitmapDescriptor bmDesc = null;
					if (avatarFile.length() == 0) {
						bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin);
						if (!AccManager.get().isSideOnline()) {
							bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin_offline);
						}
						avatarFile.delete();
					} else {
						Bitmap avatar = getBitmapFromFile(avatarFile);
						avatar = AvatarUtil.zoomImage(avatar, mapPinSize, mapPinSize);
						if (!AccManager.get().isSideOnline()) {
							avatar = AvatarUtil.toTranslucent(avatar);
						}
						avatar = AvatarUtil.toRoundCorner(avatar);
						avatarView.setImageBitmap(avatar);
						bmDesc = BitmapDescriptorFactory.fromBitmap(view2Bitmap(view));
					}
					marker.setIcon(bmDesc);
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					BitmapDescriptor bmDesc = null;
					bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin);
					if (!AccManager.get().isSideOnline()) {
						bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin_offline);
					}
					marker.setIcon(bmDesc);
					VLog.e("test", error + "," + msg);
				}
			});
		} else {
			File imgFile = new File(VLib.getSdcardDir(), avatarUri);
			BitmapDescriptor bmDesc = null;
			if (imgFile.length() == 0) {
				bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin);
				if (!AccManager.get().isSideOnline()) {
					bmDesc = BitmapDescriptorFactory.fromResource(R.drawable.child_def_pin_offline);
				}
				imgFile.delete();
			} else {
				Bitmap avatar = getBitmapFromFile(imgFile);
				avatar = AvatarUtil.zoomImage(avatar, mapPinSize, mapPinSize);
				if (!AccManager.get().isSideOnline()) {
					avatar = AvatarUtil.toTranslucent(avatar);
				}
				avatar = AvatarUtil.toRoundCorner(avatar);
				avatarView.setImageBitmap(avatar);
				bmDesc = BitmapDescriptorFactory.fromBitmap(view2Bitmap(view));
			}
			marker.setIcon(bmDesc);
		}
	}

	public static void asyncCacheAndDisplayAvatar(final ImageView avatarView, String avatarUri, final boolean forParent) {
		final int avatarSize = PixUtil.dp2px(40);
		final boolean isSideOnline = AccManager.get().isSideOnline();
		final boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
		if (avatarUri == null) {
			setDefAvatar(avatarView, isBabyClient, forParent, isSideOnline);
			return;
		}
		final File avatarFile = isAvatarNotCached(avatarUri);
		if (avatarFile != null) {
			HttpManager.get().downloadFile(Config.WEB_RES_SERVER + avatarUri, avatarFile, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					if (avatarFile.length() == 0) {// 若更换头像时，等待服务器返回url，可能len==0(1/15);
						setDefAvatar(avatarView, isBabyClient, forParent, isSideOnline);
						avatarFile.delete();
					} else {
						Bitmap avatar = getBitmapFromFile(avatarFile);
						if (avatar == null) {
							VLog.e("test", "getBitmapFromFile: NULL");
							return;
						}
						avatar = AvatarUtil.zoomImage(avatar, avatarSize, avatarSize);
						if (!isSideOnline) {// 对方离线
							if (isBabyClient && forParent || !isBabyClient && !forParent) {
								avatar = AvatarUtil.toTranslucent(avatar);
							}
						}
						avatar = AvatarUtil.toRoundCorner(avatar);
						avatarView.setImageBitmap(avatar);
					}
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					setDefAvatar(avatarView, isBabyClient, forParent, isSideOnline);
					VLog.e("test", error + "," + msg);
				}
			});
		} else {
			File imgFile = new File(VLib.getSdcardDir(), avatarUri);
			if (imgFile.length() == 0) {
				setDefAvatar(avatarView, isBabyClient, forParent, isSideOnline);
				imgFile.delete();
			} else {
				Bitmap avatar = getBitmapFromFile(imgFile);
				if (avatar == null) {
					VLog.e("test", "getBitmapFromFile: NULL");
					return;
				}
				avatar = AvatarUtil.zoomImage(avatar, avatarSize, avatarSize);
				if (!isSideOnline) {// 对方离线
					if (isBabyClient && forParent || !isBabyClient && !forParent) {
						avatar = AvatarUtil.toTranslucent(avatar);
					}
				}
				avatar = AvatarUtil.toRoundCorner(avatar);
				avatarView.setImageBitmap(avatar);
			}
		}
	}

	private static void setDefAvatar(ImageView avatarIv, boolean isBabyClient, boolean forParent, boolean isSideOnline) {
		if (isBabyClient && forParent) {// 小孩端为大人设置头像
			if (isSideOnline) {// 大人在线
				avatarIv.setImageResource(R.drawable.def_parent);
			} else {
				avatarIv.setImageResource(R.drawable.def_parent_offline);
			}
		} else if (!isBabyClient && !forParent) {// 大人端为小孩设置头像
			if (isSideOnline) {// 小孩在线
				avatarIv.setImageResource(R.drawable.def_child);
			} else {
				avatarIv.setImageResource(R.drawable.def_child_offline);
			}
		}
	}

	public static void setAvatar(ImageView avatarIv, Bitmap avatarBm, boolean forParent) {
		boolean isSideOnline = AccManager.get().isSideOnline();
		boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
		if (!isSideOnline) {// 对方离线
			if (isBabyClient && forParent || !isBabyClient && !forParent) {
				avatarBm = AvatarUtil.toTranslucent(avatarBm);
			}
		}
		avatarBm = AvatarUtil.toRoundCorner(avatarBm);
		avatarIv.setImageBitmap(avatarBm);
	}

	public static void startNewTaskActivity(Class<?> clz, Bundle bundle) {
		Intent intent = new Intent(App.get(), clz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtras(bundle);
		App.get().startActivity(intent);
	}

	public static void startNewTaskActivity(Class<?> clz) {
		Intent intent = new Intent(App.get(), clz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		App.get().startActivity(intent);
	}

	public static boolean isNetworkConnected() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) App.get().getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			return mNetworkInfo.isAvailable();
		}
		return false;
	}

	public static File genTmpAvatarPath(String uid) {
		String md5 = MD5.getMD5(uid).toLowerCase();
		String firstDirName = md5.substring(0, 2);
		String secondDirName = md5.substring(2, 4);
		String avatarDir = "/avatar" + "/" + firstDirName + "/" + secondDirName + "/" + uid + "/"
				+ System.currentTimeMillis() + ".jpg";
		File file = new File(VLib.getSdcardDir(), avatarDir);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		return file;
	}

	public static void clearOldAvatar(String uid) {
		String md5 = MD5.getMD5(uid).toLowerCase();
		String firstDirName = md5.substring(0, 2);
		String secondDirName = md5.substring(2, 4);
		String avatarDir = "/avatar" + "/" + firstDirName + "/" + secondDirName + "/" + uid;
		File dir = new File(VLib.getSdcardDir(), avatarDir);
		if (dir.isDirectory()) {
			File[] fileArr = dir.listFiles();
			for (File f : fileArr) {
				f.delete();// 先删除之前的头像
			}
		}
	}

	/**
	 * 将list<T>按splitNum分隔成许多T集合
	 */
	public static <T> List<List<T>> splitList(List<T> list, int splitNum) {
		int pageNum = list.size() / splitNum + 1;
		List<List<T>> lists = new ArrayList<List<T>>(pageNum);
		int k = 0;
		while (pageNum > 0) {
			List<T> inner = new ArrayList<T>();
			for (int i = k; i < list.size(); i++) {
				inner.add(list.get(i));
				k++;
				if (inner.size() == splitNum) {
					break;
				}
			}
			lists.add(inner);
			pageNum--;
		}
		return lists;
	}

	public static Bitmap rawByteArray2RGBABitmap(byte[] data, int width, int height) {
		int frameSize = width * height;
		int[] rgba = new int[frameSize];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int y = (0xff & ((int) data[i * width + j]));
				int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
				int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
				y = y < 16 ? 16 : y;

				int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
				int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
				int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

				r = r < 0 ? 0 : (r > 255 ? 255 : r);
				g = g < 0 ? 0 : (g > 255 ? 255 : g);
				b = b < 0 ? 0 : (b > 255 ? 255 : b);

				rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
			}
		}
		Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		bmp.setPixels(rgba, 0, width, 0, 0, width, height);
		return bmp;
	}

	public static String getNotifyTxt(ChatRecord chatRecord) {
		String msgTxt = null;
		ChatType type = ChatType.valueOf(chatRecord.getType());
		if (type == ChatType.TXT) {
			msgTxt = chatRecord.getData();
		} else if (type == ChatType.IMAGE) {
			msgTxt = "【图片】";
		} else if (type == ChatType.AUDIO) {
			int totalTime = chatRecord.getDuring();
			boolean isRemoteVoice = false;
			if (totalTime == Const.REMOTE_RECORD_TIME_TAG) {
				isRemoteVoice = true;
				totalTime = Const.REMOTE_RECORD_TIME_LEN;
			}
			msgTxt = (isRemoteVoice ? "[远程]" : "") + "【声音" + totalTime + "秒】";
		} else if (type == ChatType.VIDEO) {
			msgTxt = "【小视频】";
		}
		return msgTxt;
	}

	public static void addShortCut() {
		setComponentEnabled(SplashActivity.class, true);
	}

	public static void removeShortCut() {
		setComponentEnabled(SplashActivity.class, false);
	}

	private static void setComponentEnabled(Class<?> clazz, boolean enabled) {
		ComponentName c = new ComponentName(VLib.app(), clazz.getName());
		VLib.app()
				.getPackageManager()
				.setComponentEnabledSetting(
						c,
						enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
								: PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		// 备用方案参数：
		// PackageManager.COMPONENT_ENABLED_STATE_DEFAULT //显示应用图标
		// PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER // 隐藏应用图标
	}

	public static void translucentStatus(Activity act) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			// 结合layout：
			// android:clipToPadding="true"
			// android:fitsSystemWindows="true"
		}
	}

	public static void logoutApp() {
		AccManager.get().logout();
		ServiceManager.get().stopService();
		SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
		AbsBaseActivity.exitAll();
		VidUtil.startNewTaskActivity(LoginActivity.class);
	}
	public static void loginConflict() {
		AccManager.get().logout();
		ServiceManager.get().stopService();
		SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
		VidUtil.startNewTaskActivity(LoginActivity.class);
	}
}