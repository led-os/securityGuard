package com.vidmt.child.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.ChatController;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.ui.views.VideoRecorderView;
import com.vidmt.child.ui.views.VideoRecorderView.OnRecordFinishedListener;

import java.io.File;

public class VideoCaptureActivity extends Activity {
	public static final int ACT_RESULT_CODE = 3;
	private VideoRecorderView mRecorderView;
	private Button mRecordBtn;
	private TextView mTagTv;
	private float mTouchY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_capture);

		mRecorderView = (VideoRecorderView) findViewById(R.id.video_recorder);
		mRecordBtn = (Button) findViewById(R.id.record_btn);
		mTagTv = (TextView) findViewById(R.id.tag);

		int h = VideoRecorderView.VIDEO_HEIGHT * SysUtil.getDisplayMetrics().widthPixels / VideoRecorderView.VIDEO_WIDTH;
		LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, h);
		mRecorderView.setLayoutParams(params);

		mRecordBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTouchY = event.getY();
				if (mTouchY < 0) {
					mTagTv.setText(R.string.release_to_cancel);
					mTagTv.setBackgroundColor(getResources().getColor(R.color.red));
				} else {
					mTagTv.setText(R.string.moveup_to_cancel);
					mTagTv.setBackgroundColor(Color.TRANSPARENT);
				}
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mTagTv.setVisibility(View.VISIBLE);
					mRecordBtn.setTextColor(getResources().getColor(R.color.pink));
					mRecorderView.record(new OnRecordFinishedListener() {
						@Override
						public void onRecordFinish() {// 满10s自动stop
							File recordFile = mRecorderView.getRecordFile();
							ChatRecord chatRecord = ChatController.get().sendVideo(recordFile);
							finishOnData(chatRecord);
						}
					});
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mTagTv.setVisibility(View.GONE);
					mRecordBtn.setTextColor(Color.WHITE);
					mRecorderView.stop();
					if (mRecorderView.getTimeCount() < 1) {
						delRecordFile();
						mRecorderView.initCamera();
						MainThreadHandler.makeToast(R.string.video_too_short);
					} else if (mTouchY < 0) {
						delRecordFile();
						mRecorderView.initCamera();
					} else {// 手动正常stop
						File recordFile = mRecorderView.getRecordFile();
						ChatRecord chatRecord = ChatController.get().sendVideo(recordFile);
						finishOnData(chatRecord);
					}
				}
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mRecorderView.initCamera();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mRecorderView.stop();
	}

	private void delRecordFile() {
		File recordFile = mRecorderView.getRecordFile();
		if (recordFile != null && recordFile.exists()) {
			recordFile.delete();
		}
	}

	private void finishOnData(ChatRecord chatRecord) {
		Intent i = new Intent();
		i.putExtra(ExtraConst.EXTRA_CHATRECORD_OBJECT, chatRecord);
		setResult(ACT_RESULT_CODE, i);
		finish();
	}

}
