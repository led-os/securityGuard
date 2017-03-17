package com.vidmt.child.activities.main;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.App;
import com.vidmt.child.Const;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.FileStorage;
import com.vidmt.child.R;
import com.vidmt.child.activities.ChattingActivity;
import com.vidmt.child.activities.ImgViewerActivity;
import com.vidmt.child.activities.SendAlarmActivity;
import com.vidmt.child.activities.VideoViewerActivity;
import com.vidmt.child.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.child.dlgs.RemoteDlg;
import com.vidmt.child.dlgs.RemoteTimeDlg;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.listeners.MultiMediaClickedListener;
import com.vidmt.child.listeners.MultiMediaClickedListener.OnMultiMediaClickedListener;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.DBManager;
import com.vidmt.child.ui.adapters.ChatListAdapter;
import com.vidmt.child.ui.adapters.FaceGridViewAdapter;
import com.vidmt.child.ui.adapters.FacePagerAdapter;
import com.vidmt.child.ui.views.DropDownToRefreshListView;
import com.vidmt.child.ui.views.DropDownToRefreshListView.OnRefreshListener;
import com.vidmt.child.utils.Enums.CmdType;
import com.vidmt.child.utils.Enums.VipFuncType;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VXmppUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.inner.XmppManager;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatController {
	private static ChatController sInstance;
	private ChattingActivity mActivity;
	private InputMethodManager imm = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);// 软键盘管理类
	private MediaRecorder mRecorder;
	private long mRecordStartTime;
	private RecordRunnable mRecordRunnable;
	private Chat mChat;
	private MediaPlayer mMediaPlayer;
	private OnMultiMediaClickedListener mOnMultiMediaClickedListener = new OnMultiMediaClickedListener() {
		@Override
		public void onAudioClick(String mediaUri) {
			try {
				File mediaFile = new File(VLib.getSdcardDir(), mediaUri);
				playAudio(mediaFile.getAbsolutePath());
			} catch (Exception e) {
				VLog.e("test", e);
			}
		}

		@Override
		public void onImageClick(String mediaUri) {
			Intent i = new Intent(mActivity, ImgViewerActivity.class);
			File imgFile = new File(VLib.getSdcardDir(), mediaUri);
			i.putExtra(ExtraConst.EXTRA_IMG_FILE_PATH, imgFile.getAbsolutePath());
			mActivity.startActivity(i);
		}

		@Override
		public void onVideoClick(String mediaUri) {
			Intent i = new Intent(mActivity, VideoViewerActivity.class);
			File videoFile = new File(VLib.getSdcardDir(), mediaUri);
			i.putExtra(ExtraConst.EXTRA_VIDEO_FILE_PATH, videoFile.getAbsolutePath());
			mActivity.startActivity(i);
		}
	};
	private int mDropDownRefreshNum = 1;// 注：适时初始化一下

	private ChatController() {
	}

	public static ChatController get() {
		if (sInstance == null) {
			sInstance = new ChatController();
		}
		return sInstance;
	}

	public void init(ChattingActivity activity) {
		mActivity = activity;
	}

	public void handleSoftInputWindow(EditText et, boolean show) {
		if (show) {
			imm.showSoftInputFromInputMethod(et.getWindowToken(), 0);
		} else {
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}

	public void launchAlarm() {
		if (!XmppManager.get().isAuthenticated()) {
			AndrUtil.makeToast(R.string.not_login);
			return;
		}
		if (!AccManager.get().isSideOnline()) {
			AndrUtil.makeToast(R.string.parent_not_login);
			return;
		}
		if (!UserUtil.isAuthorized(mActivity, VipFuncType.ALARM)) {
			return;
		}
		mActivity.startActivity(new Intent(mActivity, SendAlarmActivity.class));
	}

	public void launchRemoteAudio() {
		if (!UserUtil.isAuthorized(mActivity, VipFuncType.REMOTE_AUDIO)) {
			return;
		}
		if (!XmppManager.get().isAuthenticated()) {
			AndrUtil.makeToast(R.string.not_login);
			return;
		}
		if (!AccManager.get().isSideOnline()) {
			AndrUtil.makeToast(R.string.baby_not_login);
			return;
		}
		RemoteDlg remoteDlg = new RemoteDlg(mActivity);
		remoteDlg.setOnClickListener(new DialogClickListener() {
			@Override
			public void onOK() {
				super.onOK();
				AccManager.get().sendCommand(CmdType.REMOTE_AUDIO);
				RemoteTimeDlg dlg = new RemoteTimeDlg(mActivity);
				dlg.setCancelable(false);
				dlg.show();
			}
		});
		remoteDlg.show();
	}

	public void initFaceView(ViewPager facePager, final EditText contentEt) {
		int screenHeight = SysUtil.getDisplayMetrics().heightPixels;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				screenHeight * 7 / 24);
		facePager.setLayoutParams(layoutParams);
		TypedArray faceIdTypes = App.get().getResources().obtainTypedArray(R.array.face_res_ids);
		List<Integer> faceResIds = new ArrayList<Integer>();
		for (int i = 0; i < faceIdTypes.length(); i++) {
			faceResIds.add(faceIdTypes.getResourceId(i, -1));
		}
		String[] faceStrArr = App.get().getResources().getStringArray(R.array.face_strs);
		List<String> faceStrs = Arrays.asList(faceStrArr);
		int faceNumPerPage = Const.FACE_COLUMNS * Const.FACE_ROWS - 1;
		List<List<Integer>> splittedFaceResIds = VidUtil.splitList(faceResIds, faceNumPerPage);
		List<List<String>> splittedFaceStrs = VidUtil.splitList(faceStrs, faceNumPerPage);
		List<View> faceViews = new ArrayList<View>();
		for (int i = 0; i < splittedFaceResIds.size(); i++) {
			List<Integer> fResIds = splittedFaceResIds.get(i);
			List<String> fStrs = splittedFaceStrs.get(i);
			final FaceGridViewAdapter adapter = new FaceGridViewAdapter(mActivity, fResIds, fStrs);
			GridView gv = new GridView(mActivity);
			gv.setPadding(5, 5, 5, 5);
			gv.setNumColumns(Const.FACE_COLUMNS);
			gv.setAdapter(adapter);
			faceViews.add(gv);
			gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// 插入的表情
					String faceChar = adapter.getFaceChar(position);
					if (faceChar == null) {// 删除
						contentEt.onKeyDown(KeyEvent.KEYCODE_DEL, new KeyEvent(KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_DEL));
						return;
					}
					SpannableString ss = new SpannableString(faceChar);
					Drawable d = App.get().getResources().getDrawable((int) adapter.getItemId(position));
					int screenWidth = SysUtil.getDisplayMetrics().widthPixels;
					d.setBounds(0, 0, screenWidth / Const.FACE_COLUMNS / 3, screenWidth / Const.FACE_COLUMNS / 3);// 设置表情图片的显示大小
					ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
					ss.setSpan(span, 0, faceChar.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					contentEt.getText().insert(contentEt.getSelectionStart(), ss);// 在光标所在处插入表情
				}
			});
		}
		facePager.setAdapter(new FacePagerAdapter(faceViews));
		LinearLayout facePointLayout = (LinearLayout) mActivity.findViewById(R.id.face_points);
		final List<LinearLayout> mFacePointList = new ArrayList<LinearLayout>(faceViews.size());
		for (int i = 0; i < faceViews.size(); i++) {
			ImageView iv = new ImageView(mActivity);
			LinearLayout ly = new LinearLayout(mActivity);
			ly.setLayoutParams(new LayoutParams(20, 20));
			ly.setPadding(5, 5, 5, 5);
			ly.addView(iv);
			mFacePointList.add(ly);
			if (i == 0) {
				iv.setBackgroundResource(R.drawable.shape_point_white);
			} else {
				iv.setBackgroundResource(R.drawable.shape_point_lightgrey);
			}
			facePointLayout.addView(ly);
		}

		facePager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (mFacePointList != null) {
					for (int i = 0; i < mFacePointList.size(); i++) {
						mFacePointList.get(i).getChildAt(0).setBackgroundResource(R.drawable.shape_point_lightgrey);
						if (position == i) {
							mFacePointList.get(i).getChildAt(0).setBackgroundResource(R.drawable.shape_point_white);
						}
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	public File initMediaRecorder() {
		// Once recording is stopped, you will have to configure it again as if
		// it has just been constructed
		mRecorder = new MediaRecorder();
		try {// 此处可能弹出-权限提示框，通过异常判断权限状态
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		} catch (RuntimeException e) {
			VLog.d("test", e);
			stopRecord();
			return null;
		}
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		File file = null;
		try {
			file = setRecorderOutput();
		} catch (IOException e) {
			VLog.e("test", e);
		}
		return file;
	}

	private File setRecorderOutput() throws IOException {
		File recordFile = new File(VLib.getSdcardDir(), FileStorage.buildChatAudioPath(System.currentTimeMillis()
				+ ".amr"));
		recordFile.createNewFile();
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD 没有安装.  它的状态是" + state + ".");
		}
		if (!recordFile.exists() && !recordFile.mkdirs()) {
			throw new IOException("文件不能被创建.");
		}
		mRecorder.setOutputFile(recordFile.getAbsolutePath());
		return recordFile;
	}

	public void startRecord() {
		mRecordStartTime = System.currentTimeMillis();
		try {
			mRecorder.prepare();
			mRecorder.start();
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		} catch (IOException e) {
			VLog.e("test", e);
		} catch (RuntimeException e) {
			VLog.e("test", e);
		}
	}

	public long stopRecord() {
		try {
			mRecorder.stop();
		} catch (RuntimeException e) {
			VLog.e("test", e);
		} finally {
			try {// finalize() timed out after 10 seconds
				mRecorder.reset();// set state to idle
				mRecorder.release();
			} catch (Exception e) {
				VLog.e("test", e);
			}
			mRecorder = null;
		}
		return System.currentTimeMillis() - mRecordStartTime;
	}

	private int getAM() {
		if (mRecorder == null) {
			return 0;
		}
		return 10 * mRecorder.getMaxAmplitude() / 32768;// 分贝
	}

	public void startGettignAM(View recordAMIv) {
		mRecordRunnable = new RecordRunnable(recordAMIv);
		DefaultThreadHandler.post(mRecordRunnable);
	}

	public void stopGettingAM() {
		DefaultThreadHandler.remove(mRecordRunnable);
	}

	public void setChat(Chat chat) {
		mChat = chat;
	}

	public ChatRecord sendTxtMsg(String txt) {
		try {
			Message msg = XmppManager.get().sendMessage(mChat, txt);
			ChatRecord chatRecord = VXmppUtil.saveChatRecord(mChat.getParticipant(), msg, true);
			return chatRecord;
		} catch (NotConnectedException e) {
			VLog.e("test", e);
		}
		return null;
	}

	public ChatRecord sendAudio(File recordFile, long recordTime) {
		int during = (int) recordTime / 1000;
		try {
			Message message = XmppManager.get().sendMessage(mChat, ChatType.AUDIO, recordFile, during);
			ChatRecord chatRecord = VXmppUtil.saveChatRecord(mChat.getParticipant(), message, true);
			return chatRecord;
		} catch (NotConnectedException e) {
			VLog.e("test", e);
		}
		return null;
	}

	public ChatRecord sendImage(File imgFile) {
		try {
			Message message = XmppManager.get().sendMessage(mChat, ChatType.IMAGE, imgFile, 0);
			ChatRecord chatRecord = VXmppUtil.saveChatRecord(mChat.getParticipant(), message, true);
			return chatRecord;
		} catch (NotConnectedException e) {
			VLog.e("test", e);
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		}
		return null;
	}

	public ChatRecord sendVideo(File videoFile) {
		try {
			Message message = XmppManager.get().sendMessage(mChat, ChatType.VIDEO, videoFile, 0);
			ChatRecord chatRecord = VXmppUtil.saveChatRecord(mChat.getParticipant(), message, true);
			return chatRecord;
		} catch (NotConnectedException e) {
			VLog.e("test", e);
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		}
		return null;
	}

	public void setOnMultiMediaClickedListener() {
		MultiMediaClickedListener.get().setOnMultiMediaClickedListener(mOnMultiMediaClickedListener);
	}

	private void playAudio(String path) {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mMediaPlayer.reset();
				}
			});
		}
		mMediaPlayer.reset();
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			VLog.e("test", e);
		} catch (SecurityException e) {
			VLog.e("test", e);
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		} catch (IOException e) {
			VLog.e("test", e);
		}
	}

	public void setOnRefreshListViewListener(final DropDownToRefreshListView listView, final int initialHistSize,
			final List<ChatRecord> shownRecordList, final ChatListAdapter chatListAdapter) {
		mDropDownRefreshNum = 1;
		listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				int INTERVAL = ChattingActivity.HISTORY_RECORD_INTERVAL;
				int histIntervalItems = (int) Math.ceil(Double.valueOf(initialHistSize + "") / INTERVAL);
				if (histIntervalItems > mDropDownRefreshNum) {
					int refreshLen = INTERVAL;
					int leftHistSize = initialHistSize - mDropDownRefreshNum * INTERVAL;
					if (leftHistSize / INTERVAL < 1) {
						refreshLen = leftHistSize;
					}
					int startIndex = leftHistSize - mDropDownRefreshNum * INTERVAL;
					List<ChatRecord> records = DBManager.get().getRangeEntity(ChatRecord.class, startIndex, refreshLen);
					shownRecordList.addAll(0, records);
					chatListAdapter.notifyDataSetChanged();
					mDropDownRefreshNum++;
				} else {
					MainThreadHandler.makeToast(R.string.no_more_hist_record);
				}
				MainThreadHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						listView.onRefreshComplete();
					}
				}, 100);
				listView.setSelection(0);
			}
		});
	}

	private class RecordRunnable implements Runnable {
		private View recordAMIv;

		public RecordRunnable(View amView) {
			this.recordAMIv = amView;
		}

		@Override
		public void run() {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					int am = getAM();
					switch (am) {
						case 0:
							recordAMIv.setBackgroundResource(R.drawable.voice1);
							break;
						case 1:
							recordAMIv.setBackgroundResource(R.drawable.voice2);
							break;
						case 2:
							recordAMIv.setBackgroundResource(R.drawable.voice3);
							break;
						case 3:
							recordAMIv.setBackgroundResource(R.drawable.voice4);
							break;
						case 4:
							recordAMIv.setBackgroundResource(R.drawable.voice5);
							break;
						case 5:
							recordAMIv.setBackgroundResource(R.drawable.voice6);
							break;
						case 6:
							recordAMIv.setBackgroundResource(R.drawable.voice7);
							break;
						case 7:
							recordAMIv.setBackgroundResource(R.drawable.voice8);
							break;
						case 8:
							recordAMIv.setBackgroundResource(R.drawable.voice9);
							break;
						case 9:
							recordAMIv.setBackgroundResource(R.drawable.voice10);
							break;
					}
					DefaultThreadHandler.post(RecordRunnable.this, 350);
				}
			});
		}
	}

}
