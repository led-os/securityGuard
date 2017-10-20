package com.vidmt.telephone.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
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
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.FileStorage;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.AddFriendActivity;
import com.vidmt.telephone.activities.ChattingActivity;
import com.vidmt.telephone.activities.LoginActivity;
import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.Enums.AddFriendType;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;


public class VidUtil {
	private static final int VERSION_CODES_KITKAT = 19;// Android 4.4.2

	public static boolean isPhoneNO(String mobile) {
		Pattern p = Pattern.compile("^1\\d{10}$");
		// Pattern p =
		// Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobile);
		return m.matches();
	}
	
	public static boolean isNickFormat(String nick) {
		if(nick.contains("<") || nick.contains(">")){
			return false;
		}else if(nick.contains("&")){
			return false;
		}else if(nick.contains(";")){
			return false;
		}
		return true;
	}
	
	public static boolean isNickTooLong(String nick) {
		if(nick.length() > 10){
			return false;
		}
		return true;
	}
	
	public static boolean isSignatureFormat(String nick) {
		if(nick.contains("<") || nick.contains(">")){
			return false;
		}else if(nick.contains("&")){
			return false;
		}else if(nick.contains(";")){
			return false;
		}
		return true;
	}
	
	public static boolean isSignatureTooLong(String nick) {
		if(nick.length() > 50){
			return false;
		}
		return true;
	}
	
	public static boolean isAddressFormat(String nick) {
		if(nick.contains("<") || nick.contains(">")){
			return false;
		}else if(nick.contains("&")){
			return false;
		}else if(nick.contains(";")){
			return false;
		}
		return true;
	}
	
	public static boolean isAddressTooLong(String nick) {
		if(nick.length() > 50){
			return false;
		}
		return true;
	}

	public static boolean isPwdFormat(String pwd) {
		//Pattern p = Pattern.compile("[a-z0-9A-Z_]+");
		Pattern p = Pattern.compile("[a-z0-9A-Z_]{6,12}");
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
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		App.get().startActivity(intent);
	}

	// ///////////////////////////////////////////////////////////////////////////////
	private static Map<SoundPool, Integer> mSoundPoolMap = new HashMap<SoundPool, Integer>();

	public static void playSound(final int resId, final boolean repeat) {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
				int soundID = soundPool.load(VLib.app(), resId, 1);
				mSoundPoolMap.put(soundPool, soundID);
				AudioManager mgr = (AudioManager) VLib.app().getSystemService(Context.AUDIO_SERVICE);
				float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
				float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
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

	// ///////////////////////////////////////////////////////////////////////

	public static SpannableString replaceToFace(String content) {
		if (content == null) {
			return new SpannableString("对方未开启录音权限，远程录音失败！");
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

	/**
	 *  应用程序是否位于堆栈的顶层
	 */
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
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
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
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

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

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
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

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	private static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
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

	/**
	 * 反向排列list
	 */
	public static <T> List<T> reverseList(List<T> list) {
		if (list == null) {
			return new ArrayList<T>();
		}
		Collections.reverse(list);
		return list;
	}

	// ////////////////////////////////////////////////////////////////////////////////
	private static File mRecordTempFile;
	private static MediaRecorder mRecorder;

	public static void startRecord() {
		try {
			mRecordTempFile = File.createTempFile("REMOTE", ".amr");
		} catch (IOException e) {
			VLog.e("test", e);
		}
		mRecorder = new MediaRecorder();// 实例化
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);// 输出文件格式
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 音频文件编码
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
		try {
			if(mRecorder != null) {
				mRecorder.stop();
				mRecorder.release();
			}
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

	public static boolean isAppAuthroized() {
		return MD5.getMD5("vidmt." + App.get().getPackageName()).equals(Config.APP_ID);
	}

	public static Bitmap getBitmapFromFile(File file) {
		Bitmap bm = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file.getAbsolutePath());
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			bm = BitmapFactory.decodeStream(fis,null,options);
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
		// huawei changed at here 11.18.
		Bitmap avatar = getBitmapFromFile(avatarFile);
		if(avatar == null){
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

	public static void startNewTaskActivity(Class<?> clz) {
		Intent intent = new Intent(App.get(), clz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		App.get().startActivity(intent);
	}

	public static void startNewTaskActivity(Class<?> clz, Bundle bundle) {
		Intent intent = new Intent(App.get(), clz);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtras(bundle);
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

	public static boolean joinQQGroup(Activity act, String qqGroupKey, String qqGroupNO) {
		Intent intent = new Intent();
		intent.setData(Uri
				.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
						+ qqGroupKey));
		// 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
		// //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			act.startActivity(intent);
			return true;
		} catch (Exception e) {
			// 未安装手Q或安装的版本不支持
			MainThreadHandler.makeLongToast(App.get().getString(R.string.please_add_qq_group, qqGroupNO));
			return false;
		}
	}

	public static void asyncCacheAndDisplayAvatar(final ImageView avatarView, String avatarUri, final boolean online) {
		// avatarView.setImageResource(R.drawable.default_avatar_online);//
		// 预加载图片
		final int avatarSize, defParentAvatarResId;
		avatarSize = PixUtil.dp2px(40);
		if (online) {
			defParentAvatarResId = R.drawable.default_avatar_online;
		} else {
			defParentAvatarResId = R.drawable.default_avatar;
		}
		if (avatarUri == null) {
			avatarView.setImageResource(defParentAvatarResId);
			return;
		}
		avatarUri = avatarUri.replace("static", "");// 兼容旧版本
		if (avatarUri.contains("-")) {// 兼容旧版本
			avatarUri = avatarUri.substring(0, avatarUri.indexOf("-"));
		}
		final File avatarFile = isAvatarNotCached(avatarUri);
		if (avatarFile != null) {
			VidUtil.fLog("avatarUri is: "+ Config.WEB_RES_SERVER + avatarUri);
			HttpUtil.downloadFile(Config.WEB_RES_SERVER + avatarUri, avatarFile, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					if (avatarFile.length() == 0) {// 若更换头像时，等待服务器返回url，可能len==0(1/15);
						avatarView.setImageResource(defParentAvatarResId);
						avatarFile.delete();
					} else {
						Bitmap avatar = getBitmapFromFile(avatarFile);
						if (avatar == null) {
							VLog.e("test", "getBitmapFromFile: NULL");
							return;
						}
						avatar = AvatarUtil.zoomImage(avatar, avatarSize, avatarSize);
						if (!online) {
							avatar = AvatarUtil.toTranslucent(avatar);
						}
						avatar = AvatarUtil.toRoundCorner(avatar);
						avatarView.setImageBitmap(avatar);
					}
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					avatarView.setImageResource(defParentAvatarResId);
					VLog.d("test", error + "," + msg);
				}
			});
		} else {
			File imgFile = new File(VLib.getSdcardDir(), avatarUri);
			if (imgFile.length() == 0) {
				avatarView.setImageResource(defParentAvatarResId);
				imgFile.delete();
			} else {
				Bitmap avatar = getBitmapFromFile(imgFile);
				avatar = AvatarUtil.zoomImage(avatar, avatarSize, avatarSize);
				if (!online) {
					avatar = AvatarUtil.toTranslucent(avatar);
				}
				avatar = AvatarUtil.toRoundCorner(avatar);
				avatarView.setImageBitmap(avatar);
			}
		}
	}

	public static Bitmap getAvatarFromUri(String avatarUri) {
		if (avatarUri == null) {
			return null;
		}
		File avatarFile = new File(VLib.getSdcardDir(), avatarUri);
		if (!avatarFile.exists()) {
			return null;
		}
		Bitmap bm = getBitmapFromFile(avatarFile);
		return AvatarUtil.toRoundCorner(bm);
	}

	private static void endEtCursor(EditText et) {
		Editable editable = et.getText();
		Selection.setSelection(editable, editable.length());
	}

	public static void setTextEndCursor(EditText et, String s) {
		if (s == null) {
			return;
		}
		et.setText(s);
		endEtCursor(et);
	}

	public static void setTextEndCursor(EditText et, int resId) {
		et.setText(resId);
		endEtCursor(et);
	}

	public static List<String> getAllFriendUids(boolean onlyOnline) {
		List<String> rosterUids = XmppManager.get().getRosterUids(null);
		final List<String> uids = new ArrayList<String>();
		for (String uid : rosterUids) {
			if (onlyOnline) {// 仅返回'在线'
				boolean online = XmppManager.get().isUserOnline(uid);
				if (!online) {
					continue;
				}
			}
			// 返回所有的'在线'、'离线'
			Relationship relation = XmppManager.get().getRelationship(uid);
			if (relation == Relationship.FRIEND) {
				uids.add(uid);
			} else {
				boolean isSelfVip = AccManager.get().getCurUser().isVip();
				if (isSelfVip) {// 自己是会员
					try {
						User user = HttpManager.get().getUserInfo(uid);
						if (user == null) {
							continue;
						}
						if (ServerConfInfoTask.canDirectAddFriend() && !user.isVip()
								&& relation == Relationship.WAIT_BE_AGREE) {// 对方非会员
							uids.add(uid);
						}
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			}
		}
		return uids;
	}

	/**
	 * run on background thread
	 */
	public static void removeLocSecretUids(List<String> allUids) throws VidException {
		List<String> willRemoveUids = new ArrayList<String>();
		for (String uid : allUids) {
			User user = HttpManager.get().getUserInfo(uid);
			if (user != null && user.isLocSecret()) {// 位置保密
				willRemoveUids.add(user.uid);
			}
		}
		allUids.removeAll(willRemoveUids);
	}

	public static <T> List<T> getListCopy(List<T> list) {
		List<T> cList = new ArrayList<T>();
		for (T t : list) {
			cList.add(t);
		}
		return cList;
	}

	public static void notifyChatMsg(final String uid, final String content) {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					User user = HttpManager.get().getUserInfo(uid);
					String nickname = user.getNick();
					Bitmap avatar = VidUtil.getAvatarFromUri(user.avatarUri);
					if (avatar == null) {
						avatar = SysUtil.getBitmap(R.drawable.default_avatar_online);
					}
					//huawei add;
					String kefu_uid =  SysUtil.getPref(PrefKeyConst.PREF_KEFU_ACCOUNT, Const.DEF_KEFU_UID);
					if(uid.equals(kefu_uid)){
						avatar = SysUtil.getBitmap(R.drawable.default_kefu_avatar);
					}
					notifyMsg(nickname, content, avatar, ChattingActivity.class, uid, new String[]{
							ExtraConst.EXTRA_UID, uid});
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	public static void notifyFriendRequest(final String uid) {
		final String title = App.get().getString(R.string.add_friend_request);
		final Bitmap defAvatar = SysUtil.getBitmap(R.drawable.default_avatar_online);
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					User user = HttpManager.get().getUserInfo(uid);
					final String content = "【" + user.getNick() + "】"
							+ App.get().getString(R.string.request_to_add_friend);
					String avatarUri = user.avatarUri;
					final File avatarFile = isAvatarNotCached(avatarUri);
					if (avatarFile != null) {
						VidUtil.fLog("avatarUri is: "+ Config.WEB_RES_SERVER + avatarUri);
						HttpUtil.downloadFile(Config.WEB_RES_SERVER + avatarUri, avatarFile,
								new RequestCallBack<File>() {
									@Override
									public void onSuccess(ResponseInfo<File> responseInfo) {
										if (avatarFile.length() == 0) {
											notifyMsg(title, content, defAvatar, AddFriendActivity.class, uid,
													new String[] { ExtraConst.EXTRA_UID, uid });
											avatarFile.delete();
										} else {
											Bitmap avatar = getBitmapFromFile(avatarFile);
											if (avatar == null) {
												notifyMsg(title, content, defAvatar, AddFriendActivity.class, uid,
														new String[] { ExtraConst.EXTRA_UID, uid });
												return;
											}
											avatar = AvatarUtil.toRoundCorner(avatar);
											notifyMsg(title, content, avatar, AddFriendActivity.class, uid,
													new String[] { ExtraConst.EXTRA_UID, uid });
										}
									}

									@Override
									public void onFailure(HttpException error, String msg) {
										notifyMsg(title, content, defAvatar, AddFriendActivity.class, uid,
												new String[] { ExtraConst.EXTRA_UID, uid });
										VLog.e("test", error + "," + msg);
									}
								});
					} else {
						File imgFile = new File(VLib.getSdcardDir(), avatarUri);
						if (imgFile.length() == 0) {
							notifyMsg(title, content, defAvatar, AddFriendActivity.class, uid, new String[] {
									ExtraConst.EXTRA_UID, uid });
							imgFile.delete();
						} else {
							Bitmap avatar = getBitmapFromFile(imgFile);
							avatar = AvatarUtil.toRoundCorner(avatar);
							notifyMsg(title, content, avatar, AddFriendActivity.class, uid, new String[] {
									ExtraConst.EXTRA_UID, uid });
						}
					}
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	public static void notifyMsg(final String title, final String msg, final Bitmap bm, final Class<?> clz,
			final String notifyID, final String[] extraArr) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = bm;
				if (bitmap == null) {
					bitmap = SysUtil.getBitmap(R.drawable.ic_launcher);
				}
				Notification n = new Notification();
				n.icon = R.drawable.ic_launcher;
				n.tickerText = title + " : " + msg;
				n.when = System.currentTimeMillis();
				n.defaults = Notification.DEFAULT_SOUND;

				Intent intent = new Intent(App.get(), clz);
				if (extraArr != null) {
					for (int i = 0; i < extraArr.length; i += 2) {
						intent.putExtra(extraArr[i], extraArr[i + 1]);
					}
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				PendingIntent pi = PendingIntent.getActivity(VLib.app(), (int) (Math.random() * 1000), intent,
						PendingIntent.FLAG_ONE_SHOT);
				RemoteViews contentView = new RemoteViews(VLib.app().getPackageName(), R.layout.notification_msg);
				contentView.setImageViewBitmap(R.id.avatar, bitmap);
				contentView.setTextViewText(R.id.nickname, title);
				contentView.setTextViewText(R.id.message, msg);
				n.contentView = contentView;
				n.contentIntent = pi;
				SysUtil.showNotification(Const.NOTIF_ID_CHAT_RCV, notifyID, n);
			}
		});
	}

	public static void colorPartText(TextView tv, String content, int start, int end, int color) {
		SpannableStringBuilder builder = new SpannableStringBuilder(content);
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
		builder.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(builder);
	}

	public static String getNotifyTxt(ChatRecord chatRecord) {
		String msgTxt = null;
		ChatType type = ChatType.valueOf(chatRecord.getType());// chatRecord.getType==null
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

	public static <T> List<T> set2List(Set<T> set) {
		List<T> list = new ArrayList<T>();
		for (T t : set) {
			list.add(t);
		}
		return list;
	}

	public static AddFriendType getAddFriendStatus(boolean isSelfVip, boolean isSideVip) {
		if (isSelfVip && !isSideVip) {// 1.会员+非会员（直接）
			return AddFriendType.DIRECT;
		} else if (isSelfVip && isSideVip) {// 2.会员+会员（待同意）
			return AddFriendType.VIP_WAIT;
		} else if (!isSelfVip && !isSideVip) {// 3.非会员+非会员（待同意）
			return AddFriendType.WAIT;
		} else if (!isSelfVip && isSideVip) {// 3.非会员+会员（不允许）
			return AddFriendType.NOT_ALLOWED;
		}
		return null;
	}

	/*
	 * 消息列表相关
	 */
	public static List<String> getMsgUidList() {
		Set<String> set = SysUtil.getStringSetPref(PrefKeyConst.PREF_SHOWN_ON_MSG_LIST_UIDS);
		return set2List(set);
	}

	public static void addToMsgList(String uid) {
		Set<String> msgUids = SysUtil.getStringSetPref(PrefKeyConst.PREF_SHOWN_ON_MSG_LIST_UIDS);
		if (!msgUids.contains(uid)) {
			msgUids.add(uid);
			SysUtil.saveStringSet(PrefKeyConst.PREF_SHOWN_ON_MSG_LIST_UIDS, msgUids);
		}
	}

	public static void removeFromMsgList(String uid) {
		Set<String> msgUids = SysUtil.getStringSetPref(PrefKeyConst.PREF_SHOWN_ON_MSG_LIST_UIDS);
		msgUids.remove(uid);
		SysUtil.saveStringSet(PrefKeyConst.PREF_SHOWN_ON_MSG_LIST_UIDS, msgUids);
	}

	public static boolean checkPermission(String permission) {
		PackageManager pm = App.get().getPackageManager();
		boolean permitted = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, App.get()
				.getPackageName()));
		return permitted;
	}

	public static void openSmsPermissionDlgIfNeed(Activity act) {
		String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
		Cursor cusor = act.managedQuery(Uri.parse("content://sms/inbox"), projection, null, null, "date desc");
		if (cusor != null) {
			if (cusor.moveToNext()) {
			}
			if(android.os.Build.VERSION.SDK_INT < 14){
				cusor.close();
			}
		}
	}

	/**
	 * @return true:有权限，false:权限未打开
	 */
	public static boolean openRecordPermissionDlgIfNeed() {
		MediaRecorder mRecorder = new MediaRecorder();
		try {
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			return true;
		} catch (Throwable e) {
			VidUtil.fLog("openRecordPermission  Throwable e" + e.getMessage());
			try {
				mRecorder.stop();
				mRecorder.release();
			} catch (Throwable t) {
			}
		} finally {
			try {
				mRecorder.stop();
				mRecorder.release();
			} catch (Throwable t) {
				VidUtil.fLog("openRecordPermission  stop or release Throwable t" + t.getMessage());
			}
			mRecorder = null;
		}
		return false;
	}


	//huawei test
	public static boolean isHasAudioRecordPermission() {
// 音频获取源
		int audioSource = MediaRecorder.AudioSource.MIC;
		// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
		int sampleRateInHz = 44100;
		// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
		int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
		// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		// 缓冲区字节大小
		int bufferSizeInBytes = 0;
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		AudioRecord audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
				channelConfig, audioFormat, bufferSizeInBytes);
		//开始录制音频
		try{
			// 防止某些手机崩溃，例如联想
			audioRecord.startRecording();
		}catch (IllegalStateException e){
			e.printStackTrace();
		}
		/**
		 * 根据开始录音判断是否有录音权限
		 */
		if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
			return false;
		}
		audioRecord.stop();
		audioRecord.release();
		audioRecord = null;
		return true;

	}


	public static String getArrayString(List<String> phones){
		String arrayString = "";
		int i;
		for(i = 0; i < phones.size(); i++){
			if(i == 0){
				arrayString = arrayString + phones.get(i);
			}else if(i < phones.size()) {
				arrayString = arrayString + "," + phones.get(i);
			}
		}
		return arrayString;
	}

	public static void logoutApp(){
		AccManager.get().logout();
		ServiceManager.get().stopService();
		VidUtil.startNewTaskActivity(LoginActivity.class);
		SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
		AbsBaseActivity.exitAll();
	}

	public static void fLog(String tag, String msg){
		if(Config.DEBUG) {
			FLog.d(tag, msg);
		}
	}
	public static void fLog(String msg){
		if(Config.DEBUG) {
			FLog.d(msg);
		}
	}

	public static void fLog(Throwable e) {
		if(Config.DEBUG) {
			FLog.d(e);
		}
	}

	public static void fLog(String tag, Throwable e) {
		if(Config.DEBUG) {
			FLog.d(tag, e);
		}
	}

}