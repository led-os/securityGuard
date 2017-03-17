package com.vidmt.child.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.FileStorage;
import com.vidmt.child.R;
import com.vidmt.child.download.DownloadInfo;
import com.vidmt.child.download.DownloadManager;
import com.vidmt.child.download.DownloadService;
import com.vidmt.child.utils.VidUtil;

import java.io.File;

@ContentView(R.layout.activity_update)
public class UpdateActivity extends AbsVidActivity {
	private static final String TAG = "UpdateActivity";
	@ViewInject(R.id.seekbar)
	private SeekBar mSeekBar;
	@ViewInject(R.id.action)
	private Button mActionBtn;
	@ViewInject(R.id.status)
	private TextView mStatusTv;
	private DownloadInfo downloadInfo;
	private DownloadManager downloadManager;
	private boolean isNewDownload;
	private String mApkUrl;
	private File mApkFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();

		mApkUrl = getIntent().getStringExtra(ExtraConst.EXTRA_UPDATE_URL);
		String apkName = mApkUrl.substring(mApkUrl.lastIndexOf("/") + 1);
		mApkFile = new File(VLib.getSdcardDir(), FileStorage.buildApkPath(apkName));
		downloadManager = DownloadService.getDownloadManager();
		downloadInfo = getDownloadInfo(mApkUrl);
		download();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.version_update);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@OnClick({ R.id.back, R.id.action })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.action:
			download();
			break;
		}
	}

	private void download() {
		if (downloadInfo == null) {
			String TARGET_DIR = mApkFile.getAbsolutePath();
			try {
				downloadManager.addNewDownload(mApkUrl, "", TARGET_DIR, true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE(Transfer-Encoding:chunked)时将重新下载。
						true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
						new DownloadRequestCallBack());
				downloadInfo = getDownloadInfo(mApkUrl);
				isNewDownload = true;
			} catch (DbException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		if (downloadInfo != null) {
			HttpHandler.State state = downloadInfo.getState();
			switch (state) {
			case SUCCESS:
				VidUtil.installApk(mApkFile);
				break;
			case WAITING:
			case STARTED:
			case LOADING:
				try {
					if (!isNewDownload) {
						downloadManager.stopDownload(downloadInfo);
					}
					isNewDownload = false;
				} catch (DbException e) {
					Log.e(TAG, e.getMessage());
				}
				break;
			case CANCELLED:
			case FAILURE:
				try {
					RequestCallBack<File> reqCb = null;
					if (downloadInfo.getHandler() != null) {
						reqCb = downloadInfo.getHandler().getRequestCallBack();
						if (reqCb == null) {
							reqCb = new DownloadRequestCallBack();
						}
					}
					downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
				} catch (DbException e) {
					Log.e(TAG, e.getMessage());
				}
				break;
			}
		}
		refresh();
	}

	private DownloadInfo getDownloadInfo(String url) {
		for (int i = 0; i < downloadManager.getDownloadInfoListCount(); i++) {
			DownloadInfo df = downloadManager.getDownloadInfo(i);
			boolean isCached = TextUtils.equals(url, df.getDownloadUrl());
			if (isCached) {
				return df;
			}
		}
		return null;
	}

	public void refresh() {
		if (downloadInfo == null) {
			return;
		}
		if (downloadInfo.getFileLength() > 0) {
			int percent = (int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength());
			mSeekBar.setProgress(percent);
			mActionBtn.setText(percent + "%");
			mStatusTv.setText(getString(R.string.updating, percent + "%"));
		} else {
			mSeekBar.setProgress(0);
		}
		HttpHandler.State state = downloadInfo.getState();
		switch (state) {
			case CANCELLED:
				mActionBtn.setText("继续");
				mStatusTv.setText("已暂停");
				break;
			case SUCCESS:
				mActionBtn.setText("安装");
				mStatusTv.setText("已完成");
				mActionBtn.setBackgroundResource(R.drawable.shape_light_green_rect_bg);
				VidUtil.installApk(mApkFile);
				break;
			case FAILURE:
				mStatusTv.setText("出错,请重试");
				mActionBtn.setText("重试");
				break;
		}
	}

	private class DownloadRequestCallBack extends RequestCallBack<File> {
		@Override
		public void onStart() {
			Log.i(TAG, "onStart");
			refresh();
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			Log.i(TAG, "onLoading:" + (int) (current * 100 / total) + "%");
			refresh();
		}

		@Override
		public void onSuccess(ResponseInfo<File> responseInfo) {
			Log.i(TAG, "onSuccess");
			refresh();
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			Log.i(TAG, "onFailure"+msg);
			refresh();
		}

		@Override
		public void onCancelled() {
			Log.i(TAG, "onCancelled");
			refresh();
		}
	}

}
