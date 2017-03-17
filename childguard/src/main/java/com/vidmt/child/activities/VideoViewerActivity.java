package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.ui.views.VideoPlayerView;
import com.vidmt.child.ui.views.VideoRecorderView;

import java.io.File;

public class VideoViewerActivity extends AbsVidActivity implements OnTouchListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_viewer);
		findViewById(R.id.root).setOnTouchListener(this);
		
		VideoPlayerView videoPlayerView = (VideoPlayerView) findViewById(R.id.video_player);
		int h = VideoRecorderView.VIDEO_HEIGHT * SysUtil.getDisplayMetrics().widthPixels / VideoRecorderView.VIDEO_WIDTH;
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, h);
		videoPlayerView.setLayoutParams(params);
		File srcFile = new File(getIntent().getStringExtra(ExtraConst.EXTRA_VIDEO_FILE_PATH));
		videoPlayerView.setVideoSrcFile(srcFile);
		videoPlayerView.play();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		finish();
		return true;
	}
	
}