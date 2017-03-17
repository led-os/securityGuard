package com.vidmt.child.download;

import android.util.Log;

import com.lidroid.xutils.exception.DbException;
import com.vidmt.child.App;

public class DownloadService {
	private static final String TAG = "DownloadService";
    private static DownloadManager sInstance;

    private DownloadService() {
    }

    public static DownloadManager getDownloadManager() {
        if (sInstance == null) {
            sInstance = new DownloadManager(App.get().getApplicationContext());
        }
        return sInstance;
    }

    public static void onDestroy() {
        if (sInstance != null) {
            try {
                sInstance.stopAllDownload();
                sInstance.backupDownloadInfoList();
            } catch (DbException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

}
