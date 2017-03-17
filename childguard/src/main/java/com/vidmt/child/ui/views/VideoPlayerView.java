package com.vidmt.child.ui.views;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;

public class VideoPlayerView extends SurfaceView {
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mPlayer;
	private boolean isSurfaceCreated;
	private boolean noVolume;
	private File mVideoSrcFile;
	private Callback mCallback = new Callback() {
		@Override
		public void surfaceCreated(SurfaceHolder sh) {
			mSurfaceHolder = sh;
			isSurfaceCreated = true;
			play();
		}

		@Override
		public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
			mSurfaceHolder = sh;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder sh) {
			mSurfaceHolder = null;
			stop();
		}
	};

	public VideoPlayerView(Context context) {
		this(context, null);
	}

	public VideoPlayerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(mCallback);
	}

	public void setVideoSrcFile(File file) {
		mVideoSrcFile = file;
	}

	public void setNoVolume(boolean noVolume) {
		this.noVolume = noVolume;
	}

	public void play() {
		if (!isSurfaceCreated) {// 先得surface created，才能play
			return;
		}
		mPlayer = new MediaPlayer();
		if (noVolume) {
			mPlayer.setVolume(0, 0);
		}
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 避免warn
		mPlayer.setDisplay(mSurfaceHolder); // 定义一个SurfaceView播放它
		mPlayer.setLooping(true);
		try {
			mPlayer.setDataSource(mVideoSrcFile.getAbsolutePath());
			mPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.start();
	}

	private void stop() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

}