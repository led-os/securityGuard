package com.vidmt.child.ui.views;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.FileStorage;
import com.vidmt.child.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class VideoRecorderView extends LinearLayout implements OnErrorListener {
	public static final int VIDEO_WIDTH = 320;// 视频分辨率宽度
	public static final int VIDEO_HEIGHT = 240;// 视频分辨率高度
	private static final int VIDEO_ENCODING_BIT_RATE = 1 * 800 * 480;// 设置帧频率，然后就清晰了
	private static final int RECORD_TIME_LEN = 10;// 一次拍摄最长时间(秒)

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private ProgressBar mProgressBar;
	private MediaRecorder mMediaRecorder;
	private Timer mTimer;// 计时器
	private Camera mCamera;
	private OnRecordFinishedListener mOnRecordFinishedListener;// 录制完成回调接口
	private int mTimeCount;// 时间计数
	private File mRecordFile;// 文件
	private Callback mCallback = new Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			initCamera();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			VLog.i("lk", "format:" + format + ",w*h:" + width + "*" + height);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			freeCameraResource();
		}
	};

	public VideoRecorderView(Context context) {
		this(context, null);
	}

	public VideoRecorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoRecorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.view_video_recorder, this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(RECORD_TIME_LEN);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(mCallback);
	}

	public void initCamera() {
		if (mCamera != null) {
			freeCameraResource();
		}
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			VLog.e("test", e);
			freeCameraResource();
		}
		if (mCamera == null) {
			return;
		}
		setCameraParams();
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			VLog.e("test", e);
		}
		mCamera.startPreview();
		// 以下方法导致onPreviewFrame不被回调，因为录制时不允许同时取帧.录制时才会用到
		mCamera.unlock();
	}

	private void setCameraParams() {
		if (mCamera != null) {
			mCamera.setDisplayOrientation(90);
			Parameters params = mCamera.getParameters();
			params.set("orientation", "portrait");
			params.setPreviewSize(VIDEO_WIDTH, VIDEO_HEIGHT);
			List<String> focusModes = params.getSupportedFocusModes();
			if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);// 持续自动聚焦
			}
			mCamera.setParameters(params);
		}
	}

	private void freeCameraResource() {
		if (mCamera != null) {
			// Stops capturing and drawing preview frames to the surface
			mCamera.stopPreview();
			mCamera.lock();
			mCamera.release();
			mCamera = null;
		}
	}

	private void resetCamera() throws IOException {
		freeCameraResource();
		initCamera();
	}

	private void createRecordPath() {
		String fileName = System.currentTimeMillis() + ".mp4";
		mRecordFile = new File(VLib.getSdcardDir(), FileStorage.buildChatVideoPath(fileName));
		try {
			mRecordFile.createNewFile();
		} catch (IOException e) {
			VLog.e("test", e);
		}
	}

	private void initRecord() throws IOException {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.reset();
		if (mCamera != null) {
			mMediaRecorder.setCamera(mCamera);
		}
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setVideoSource(VideoSource.CAMERA);// 视频源
		mMediaRecorder.setAudioSource(AudioSource.MIC);// 音频源
		mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// 视频输出格式
		mMediaRecorder.setVideoEncoder(VideoEncoder.MPEG_4_SP);// 视频录制格式
		mMediaRecorder.setAudioEncoder(AudioEncoder.AMR_NB);// 音频格式
		mMediaRecorder.setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT);// 设置分辨率
		// mMediaRecorder.setVideoFrameRate(14);// 每秒录制帧数,这个用默认值即可
		mMediaRecorder.setVideoEncodingBitRate(VIDEO_ENCODING_BIT_RATE);
		mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
		// mediaRecorder.setMaxDuration(RECORD_TTIME_LEN * 1000);
		createRecordPath();
		mMediaRecorder.setOutputFile(mRecordFile.getAbsolutePath());
		mMediaRecorder.prepare();
		try {
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		} catch (RuntimeException e) {
			VLog.e("test", e);
		} catch (Exception e) {
			VLog.e("test", e);
		}
	}

	public void record(final OnRecordFinishedListener onRecordFinishListener) {
		mOnRecordFinishedListener = onRecordFinishListener;
		try {
			initRecord();
			mTimeCount = 0;// 时间计数器重新赋值
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mTimeCount++;
					mProgressBar.setProgress(mTimeCount);// 设置进度条
					if (mTimeCount == RECORD_TIME_LEN) {// 达到指定时间，停止拍摄
						stop();
						if (mOnRecordFinishedListener != null) {
							mOnRecordFinishedListener.onRecordFinish();
						}
					}
				}
			}, 0, 1000);
		} catch (IOException e) {
			VLog.e("test", e);
		}
	}

	public void stop() {
		stopRecord();
		releaseRecord();
		try {
			resetCamera();
		} catch (IOException e) {
			VLog.e("test", e);
		}
	}

	private void stopRecord() {
		mProgressBar.setProgress(0);
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);// 设置后不会崩
			mMediaRecorder.setPreviewDisplay(null);
			try {
				mMediaRecorder.stop();
			} catch (IllegalStateException e) {
				VLog.e("test", e);
			} catch (RuntimeException e) {
				VLog.e("test", e);
			} catch (Exception e) {
				VLog.e("test", e);
			}
		}
	}

	private void releaseRecord() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			try {
				mMediaRecorder.reset();
				mMediaRecorder.release();
			} catch (IllegalStateException e) {
				VLog.e("test", e);
			} catch (Exception e) {
				VLog.e("test", e);
			}
		}
		mMediaRecorder = null;
	}

	public int getTimeCount() {
		return mTimeCount;
	}

	public File getRecordFile() {
		return mRecordFile;
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		try {
			if (mr != null) {
				mr.reset();
			}
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		} catch (Exception e) {
			VLog.e("test", e);
		}
	}
	
	/**
	 * 录制完成回调接口
	 */
	public interface OnRecordFinishedListener {
		void onRecordFinish();
	}

}