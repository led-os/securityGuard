package com.vidmt.telephone.activities;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Bundle;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.FileStorage;
import com.vidmt.telephone.R;
import com.vidmt.telephone.download.DownloadInfo;
import com.vidmt.telephone.download.DownloadManager;
import com.vidmt.telephone.download.DownloadService;
import com.vidmt.telephone.utils.VidUtil;

/**
 *
 */
@ContentView(R.layout.download_list)
public class UpdateActivity extends AbsVidActivity {

	@ViewInject(R.id.download_list)
	private ListView mDownloadList;

	private DownloadManager mDownloadMgr;
	private DownloadListAdapter mDownloadListAdapter;

	private String mApkUrl;
	private File mApkFile;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();

		mApkUrl = getIntent().getStringExtra(ExtraConst.EXTRA_UPDATE_URL);
		mApkFile = new File(VLib.getSdcardDir(), FileStorage.buildApkPath(getString(R.string.app_name_en)+ ".apk"));


		mDownloadMgr = DownloadService.getDownloadManager(getApplicationContext());

		download();

		mDownloadListAdapter = new DownloadListAdapter(getApplicationContext());
		mDownloadList.setAdapter(mDownloadListAdapter);
	}
	
	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.software_update);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void download() {
		if (mDownloadMgr.getDownloadInfoListCount() == 0) {
			try {
				String apkName = mApkUrl.substring(mApkUrl.lastIndexOf("/") + 1);
				mDownloadMgr.addNewDownload(mApkUrl, apkName, mApkFile.getAbsolutePath(), true, // 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
						true, // 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
						null);
			} catch (DbException e) {
				VLog.e("test", e);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mDownloadListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		try {
			if (mDownloadListAdapter != null && mDownloadMgr != null) {
				mDownloadMgr.backupDownloadInfoList();
			}
		} catch (DbException e) {
			LogUtils.e(e.getMessage(), e);
		}
		super.onDestroy();
	}

	private class DownloadListAdapter extends BaseAdapter {

		private final Context mContext;
		private final LayoutInflater mInflater;

		private DownloadListAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			if (mDownloadMgr == null)
				return 0;
			return mDownloadMgr.getDownloadInfoListCount();
		}

		@Override
		public Object getItem(int i) {
			return mDownloadMgr.getDownloadInfo(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			DownloadItemViewHolder holder = null;
			DownloadInfo downloadInfo = mDownloadMgr.getDownloadInfo(i);
			if (view == null) {
				view = mInflater.inflate(R.layout.download_item, null);
				holder = new DownloadItemViewHolder(downloadInfo);
				ViewUtils.inject(holder, view);
				view.setTag(holder);
				holder.refresh();
			} else {
				holder = (DownloadItemViewHolder) view.getTag();
				holder.update(downloadInfo);
			}

			HttpHandler<File> handler = downloadInfo.getHandler();
			if (handler != null) {
				RequestCallBack<File> callBack = handler.getRequestCallBack();
				if (callBack instanceof DownloadManager.ManagerCallBack) {
					DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
					if (managerCallBack.getBaseCallBack() == null) {
						managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
					}
				}
				callBack.setUserTag(new WeakReference<DownloadItemViewHolder>(holder));
			}

			return view;
		}
	}

	public class DownloadItemViewHolder {
		@ViewInject(R.id.download_percent)
		TextView label_percentTv;
		@ViewInject(R.id.download_state)
		TextView stateTv;
		@ViewInject(R.id.download_seekbar)
		SeekBar seekBar;
		@ViewInject(R.id.download_stop_btn)
		Button stopBtn;
		@ViewInject(R.id.download_remove_btn)
		Button removeBtn;

		private DownloadInfo downloadInfo;

		public DownloadItemViewHolder(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
		}

		@OnClick(R.id.download_stop_btn)
		public void stop(View view) {
			if (stopBtn.getText().toString().equals(getString(R.string.install_app))) {
				VidUtil.installApk(mApkFile);
				return;
			}
			HttpHandler.State state = downloadInfo.getState();
			switch (state) {
			case WAITING:
			case STARTED:
			case LOADING:
				try {
					mDownloadMgr.stopDownload(downloadInfo);
				} catch (DbException e) {
					LogUtils.e(e.getMessage(), e);
				}
				break;
			case CANCELLED:
			case FAILURE:
				try {
					mDownloadMgr.resumeDownload(downloadInfo, new DownloadRequestCallBack());
				} catch (DbException e) {
					LogUtils.e(e.getMessage(), e);
				}
				mDownloadListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}

		@OnClick(R.id.download_remove_btn)
		public void remove(View view) {
			try {
				mDownloadMgr.removeDownload(downloadInfo);
				mDownloadListAdapter.notifyDataSetChanged();
				if (seekBar.getProgress() == 100) {
					if (mApkFile.exists()) {
						mApkFile.delete();
					}
				}
				finish();
			} catch (DbException e) {
				LogUtils.e(e.getMessage(), e);
			}
		}

		public void update(DownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
			refresh();
		}

		public void refresh() {
			if (downloadInfo.getFileLength() > 0) {
				int percent = (int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength());
				seekBar.setProgress(percent);
				label_percentTv.setText(percent + "%");
			} else {
				seekBar.setProgress(0);
			}

			stopBtn.setText(R.string.stop);
			HttpHandler.State state = downloadInfo.getState();
			switch (state) {
			case WAITING:
				stateTv.setText(R.string.download_waiting);
				stopBtn.setText(R.string.stop);
				break;
			case STARTED:
				stateTv.setText(R.string.download_started);
				stopBtn.setText(R.string.stop);
				break;
			case LOADING:
				stateTv.setText(R.string.downloading);
				stopBtn.setText(R.string.stop);
				break;
			case CANCELLED:
				stateTv.setText(R.string.download_cancelled);
				stopBtn.setText(R.string.resume);
				break;
			case SUCCESS:
				stateTv.setText(R.string.download_success);
				stopBtn.setText(R.string.install_app);
				break;
			case FAILURE:
				stateTv.setText(R.string.download_failure);
				stopBtn.setText(R.string.retry);
				break;
			default:
				break;
			}
		}
	}

	private class DownloadRequestCallBack extends RequestCallBack<File> {

		@SuppressWarnings("unchecked")
		private void refreshListItem() {
			if (userTag == null)
				return;
			WeakReference<DownloadItemViewHolder> tag = (WeakReference<DownloadItemViewHolder>) userTag;
			DownloadItemViewHolder holder = tag.get();
			if (holder != null) {
				holder.refresh();
			}
		}

		@Override
		public void onStart() {
			refreshListItem();
		}

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			refreshListItem();
		}

		@Override
		public void onSuccess(ResponseInfo<File> responseInfo) {
			refreshListItem();
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			refreshListItem();
		}

		@Override
		public void onCancelled() {
			refreshListItem();
		}
	}
	
	@OnClick(R.id.back)
	private void onClick(View v){
		switch (v.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		if (getIntent().getBooleanExtra(ExtraConst.EXTRA_UPDATE_FORCE, false)) {
			AbsBaseActivity.exitAll();
		} else {
			super.onBackPressed();
		}
	}
	
}