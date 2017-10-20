package com.vidmt.telephone.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.FileStorage;
import com.vidmt.telephone.utils.VidUtil;

public class DownloadApkService extends Service {
	private boolean mIsStopped = true;
	private MyAsyncTask mTask;
	private HttpURLConnection mConn;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private String mUrl;
	private File mApkFile;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mUrl = intent.getStringExtra(ExtraConst.EXTRA_APK_URL);
		String apkName = mUrl.substring(mUrl.lastIndexOf("/") + 1);
		mApkFile = new File(VLib.getSdcardDir(), FileStorage.buildApkPath(apkName));
		if (mApkFile.exists()) {
			VidUtil.installApk(mApkFile);
			stopSelf();
			return super.onStartCommand(intent, flags, startId);
		}
		mTask = new MyAsyncTask();
		mTask.execute(mUrl);
		return super.onStartCommand(intent, flags, startId);
	}
	
	class MyAsyncTask extends AsyncTask<String, Integer, String> {
		private int total, current, progress;

		@Override
		protected String doInBackground(String... params) {
			mIsStopped = false;
			try {
				URL mUrl = new URL(params[0]);
				mConn = (HttpURLConnection) mUrl.openConnection();
				mInputStream = mConn.getInputStream();// e
				mOutputStream = new FileOutputStream(mApkFile);
				total = mConn.getContentLength();
				byte[] buffer = new byte[1024];
				int len = -1;// 只执行一次
				while (!mIsStopped && (len = mInputStream.read(buffer)) > 0) {
					current += len;
					mOutputStream.write(buffer, 0, len);
					progress = current * 100 / total;
					publishProgress(progress);// -->onProgressUpdate(Progress...)
				}
			} catch (Exception e) {
				VLog.e("test", e);
			} finally {
				try {
					if (mInputStream != null) {
						mInputStream.close();
					}
					if (mOutputStream != null) {
						mOutputStream.close();
					}
				} catch (IOException e) {
					VLog.e("test", e);
				}
			}
			return progress + "";
		}

		@Override
		protected void onPostExecute(String result) {
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (values[0] == 100) {
				VidUtil.installApk(mApkFile);
				mIsStopped = true;
				stopSelf();
				return;
			}
		}

	}

}
