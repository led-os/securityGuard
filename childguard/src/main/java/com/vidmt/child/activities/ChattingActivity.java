package com.vidmt.child.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.App;
import com.vidmt.child.Const;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.ChatController;
import com.vidmt.child.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.child.dlgs.CancelVoiceDlg;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.listeners.AvatarChangedListener;
import com.vidmt.child.listeners.ChatPhotoSendListener;
import com.vidmt.child.listeners.ChatPhotoSendListener.OnChatPhotoSendListener;
import com.vidmt.child.listeners.ShowMsgNotifListener;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.ui.adapters.ChatListAdapter;
import com.vidmt.child.ui.views.DropDownToRefreshListView;
import com.vidmt.child.utils.DBUtil;
import com.vidmt.child.utils.VXmppUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.exts.MultimediaExt;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChattingActivity extends AbsVidActivity implements OnClickListener {
	public static final int HISTORY_RECORD_INTERVAL = 20;
	private static final int MIN_RECORD_TIME = 1000;
	private Button mRecordBtn, mPlusBtn, mSendBtn;
	private EditText mContentEt;
	private ViewPager mFacePager;
	private View mNormalModeView, mFaceView, mMoreWidgetsView;
	private ChatListAdapter mChatListAdapter;
	private List<ChatRecord> mShownRecordList = new ArrayList<ChatRecord>();
	private DropDownToRefreshListView mChatListView;
	private PopupWindow mRecordWindow;// 录音弹窗
	private ImageView mRecordAMIv;
	private File mRecordFile;
	private ChatController mController;
	private int mInitialHistSize;
	private boolean mIsBabyClient;
	private OnMsgReceivedListener mMsgReceivedListener = new OnMsgReceivedListener() {
		@Override
		public void onMsgReceived(Chat chat, Message msg) {
			if (msg.getBody() == null && msg.getExtension(MultimediaExt.ELEMENT, MultimediaExt.NAMESPACE) == null) {
				return;
			}
			ChatRecord chatRecord = VXmppUtil.parseChatMessage(msg);
			refreshListView(chatRecord);
		}
	};
	private OnTouchListener mOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
				case R.id.chat_list:
					hideView(mFaceView);
					hideView(mMoreWidgetsView);
					mController.handleSoftInputWindow(mContentEt, false);
					if (TextUtils.isEmpty(mContentEt.getText().toString())) {
						hideView(mSendBtn);
						showView(mPlusBtn);
					}
					break;
				case R.id.record_btn:
					float y = event.getY();
					if (y < 0 && mRecordFile != null) {
						int recordBtnHeight = VidUtil.getViewMeasure(mRecordBtn)[1];
						int touchY = SysUtil.getDisplayMetrics().heightPixels - recordBtnHeight - (int) Math.abs(y);
						int[] winPositions = VidUtil.getWindowPosition(mRecordWindow.getContentView());
						float x = event.getX();
						if (x > winPositions[0] && x < winPositions[2] && touchY > winPositions[1]
								&& touchY < winPositions[3]) {
							mRecordWindow.getContentView().findViewById(R.id.voice_notify).setVisibility(View.GONE);
							mRecordWindow.getContentView().findViewById(R.id.voice_del).setVisibility(View.VISIBLE);
						} else {
							mRecordWindow.getContentView().findViewById(R.id.voice_notify).setVisibility(View.VISIBLE);
							mRecordWindow.getContentView().findViewById(R.id.voice_del).setVisibility(View.GONE);
						}
					}
					switch (event.getAction()) {
						case MotionEvent.ACTION_UP:
							try {
								Thread.sleep(100);// 延迟1/10秒,防止尾音未录入
							} catch (InterruptedException e) {
								mController.stopRecord();
								VLog.e("test", e);
							}
							mRecordBtn.setText(R.string.press_and_say);
							mRecordBtn.setGravity(Gravity.CENTER);
							mRecordBtn.setBackgroundResource(R.drawable.shape_chat_record_bg);
							mController.stopGettingAM();
							if (mRecordFile == null) {
								mController.stopRecord();
								break;
							}
							mRecordWindow.dismiss();
							final long recordTimeLen = mController.stopRecord();
							if (recordTimeLen < MIN_RECORD_TIME) {
								MainThreadHandler.makeToast(App.get().getString(R.string.record_time_too_short,
										MIN_RECORD_TIME / 1000 + ""));
								deleteRecordFile();
							} else if (mRecordWindow.getContentView().findViewById(R.id.voice_del).getVisibility() == View.VISIBLE) {
								CancelVoiceDlg dlg = new CancelVoiceDlg(ChattingActivity.this);
								dlg.setOnClickListener(new DialogClickListener() {
									@Override
									public void onOK() {
										ChatRecord chatRecord = mController.sendAudio(mRecordFile, recordTimeLen);
										refreshListView(chatRecord);
									}

									@Override
									public void onCancel() {
										deleteRecordFile();
									}
								});
								dlg.show();
								mRecordWindow.getContentView().findViewById(R.id.voice_del).setVisibility(View.GONE);
								mRecordWindow.getContentView().findViewById(R.id.voice_notify).setVisibility(View.VISIBLE);
							} else {
								if (mRecordFile.length() == 0) {
									MainThreadHandler.makeToast(R.string.please_check_record_permission_wether_opened);
								} else {
									ChatRecord chatRecord = mController.sendAudio(mRecordFile, recordTimeLen);
									refreshListView(chatRecord);
								}
							}
							break;
						case MotionEvent.ACTION_DOWN:
							mRecordBtn.setText(R.string.release_to_end);
							mRecordBtn.setGravity(Gravity.CENTER);
							mRecordBtn.setBackgroundResource(R.drawable.shape_chat_record_pressed_bg);
							mRecordFile = mController.initMediaRecorder();
							if (mRecordFile == null) {
								MainThreadHandler.makeToast(R.string.please_check_record_permission_wether_opened);
								break;
							}
							mRecordWindow.showAtLocation(mRecordBtn, Gravity.CENTER, 0, 0);
							mRecordWindow.getContentView().findViewById(R.id.recorder_window_root).setVisibility(View.VISIBLE);
							mController.startRecord();
							mController.startGettignAM(mRecordAMIv);
							break;
					}
					break;
			}
			return false;
		}
	};
	private OnChatPhotoSendListener mOnChatPhotoSendListener = new OnChatPhotoSendListener() {
		@Override
		public void onChatPhotoSend(ChatRecord chatRecord) {
			refreshListView(chatRecord);
		}
	};
	private TextWatcher mTextWatcher = new TextWatcher() {
		CharSequence temp;

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			temp = s;
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (temp.length() > 0) {// 输入有文字
				hideView(mPlusBtn);
				showView(mSendBtn);
			} else {
				hideView(mSendBtn);
				showView(mPlusBtn);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!XmppManager.get().isAuthenticated()) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			return;
		}
		setContentView(R.layout.activity_chat);
		boolean isRestartFromCrash = getIntent().getBooleanExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, false);
		if (isRestartFromCrash) {// 崩溃后重启
			moveTaskToBack(true);
		}
		mIsBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);

		initTitle();
		initComponents();
		mController = ChatController.get();
		mController.init(this);
		initChatHist();

		mContentEt.addTextChangedListener(mTextWatcher);
		mChatListView.setOnTouchListener(mOnTouchListener);
		mController.initFaceView(mFacePager, mContentEt);

		Chat chat = XmppManager.get().createChat(VidUtil.getSideName());
		mController.setChat(chat);
		XmppManager.get().addXmppListener(mMsgReceivedListener);
		ShowMsgNotifListener.get().setCurChatUser(VidUtil.getSideName());
		mController.setOnMultiMediaClickedListener();
		mController.setOnRefreshListViewListener(mChatListView, mInitialHistSize, mShownRecordList, mChatListAdapter);
		ChatPhotoSendListener.get().setOnChatPhotoSendListener(mOnChatPhotoSendListener);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (!XmppManager.get().isAuthenticated()) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			return;
		}
		super.onNewIntent(intent);
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		Button rightBtn = (Button) findViewById(R.id.right);
		if (mIsBabyClient) {
			titleTv.setText(R.string.chat_parent);
			findViewById(R.id.back).setVisibility(View.GONE);
			rightBtn.setBackgroundResource(R.drawable.selector_alarm);
		} else {
			titleTv.setText(R.string.chat_child);
			rightBtn.setBackgroundResource(R.drawable.selector_voice_remote);
		}
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initComponents() {
		mRecordBtn = (Button) findViewById(R.id.record_btn);
		mPlusBtn = (Button) findViewById(R.id.plus_btn);
		mSendBtn = (Button) findViewById(R.id.send_btn);
		mContentEt = (EditText) findViewById(R.id.content);
		mFacePager = (ViewPager) findViewById(R.id.face_pager);
		mNormalModeView = findViewById(R.id.normal_mode);
		mFaceView = findViewById(R.id.face_layout);
		mMoreWidgetsView = findViewById(R.id.more_widgets);
		mChatListView = (DropDownToRefreshListView) findViewById(R.id.chat_list);
		mRecordWindow = new PopupWindow(SysUtil.inflate(R.layout.voice_record_window), LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mRecordAMIv = (ImageView) mRecordWindow.getContentView().findViewById(R.id.voice_am);

		mRecordBtn.setOnTouchListener(mOnTouchListener);
		mPlusBtn.setOnClickListener(this);
		mSendBtn.setOnClickListener(this);
		mContentEt.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.right).setOnClickListener(this);
		findViewById(R.id.voice_btn).setOnClickListener(this);
		findViewById(R.id.face_btn).setOnClickListener(this);
		findViewById(R.id.send_img_btn).setOnClickListener(this);
		findViewById(R.id.send_video_btn).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.right:
			if (mIsBabyClient) {
				mController.launchAlarm();
			} else {
				mController.launchRemoteAudio();
			}
			break;
		case R.id.voice_btn:
			if (viewIsVisible(mRecordBtn)) {
				hideView(mRecordBtn);
				showView(mNormalModeView);
			} else {
				mController.handleSoftInputWindow(mContentEt, false);
				hideView(mFaceView);
				hideView(mMoreWidgetsView);
				hideView(mNormalModeView);
				showView(mRecordBtn);
			}
			break;
		case R.id.plus_btn:
			if (viewIsVisible(mMoreWidgetsView)) {
				hideView(mMoreWidgetsView);
			} else {
				mController.handleSoftInputWindow(mContentEt, false);
				mContentEt.requestFocus();
				hideView(mFaceView);
				showView(mMoreWidgetsView);
			}
			break;
		case R.id.content:
			mController.handleSoftInputWindow(mContentEt, true);
			hideView(mFaceView);
			hideView(mMoreWidgetsView);
			break;
		case R.id.face_btn:
			if (viewIsVisible(mFaceView)) {
				hideView(mFaceView);
			} else {
				mController.handleSoftInputWindow(mContentEt, false);
				mContentEt.requestFocus();
				hideView(mMoreWidgetsView);
				showView(mFaceView);
			}
			break;
		case R.id.send_btn:
			String content = mContentEt.getText().toString();
			if (TextUtils.isEmpty(content)) {
				return;
			}
			mContentEt.setText("");
			ChatRecord chatRecord = mController.sendTxtMsg(content);
			refreshListView(chatRecord);
			hideView(mSendBtn);
			showView(mPlusBtn);
			break;
		case R.id.send_img_btn:
			Intent i = new Intent(this, ChoosePicActivity.class);
			i.putExtra(ExtraConst.EXTRA_IS_WHOLE_PHOTO, true);
			startActivity(i);
			break;
		case R.id.send_video_btn:
			mController.handleSoftInputWindow(mContentEt, false);
			startActivityForResult(new Intent(this, VideoCaptureActivity.class), 0);
			break;
		}
	}

	private void initChatHist() {
		mInitialHistSize = DBUtil.getChatRecordCount();
		mShownRecordList = DBUtil.getRangeChatRecords(mInitialHistSize - HISTORY_RECORD_INTERVAL,
				HISTORY_RECORD_INTERVAL);
		mChatListAdapter = new ChatListAdapter(this, mShownRecordList);
		mChatListView.setAdapter(mChatListAdapter);
		mChatListView.setSelection(Integer.MAX_VALUE);
		MainThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mChatListView.setSelection(Integer.MAX_VALUE);
			}
		}, 1 * 1000);// [depressed]
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_CANCELED) {
			return;
		}
		switch (resultCode) {
		case VideoCaptureActivity.ACT_RESULT_CODE:
			ChatRecord chatRecord = data.getParcelableExtra(ExtraConst.EXTRA_CHATRECORD_OBJECT);
			refreshListView(chatRecord);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		if (viewIsVisible(mFaceView)) {
			hideView(mFaceView);
		} else if (viewIsVisible(mMoreWidgetsView)) {
			hideView(mMoreWidgetsView);
		} else {
			if (mIsBabyClient) {
				moveTaskToBack(true);
			} else {
				if (!MapManager.isInstantiated()) {
					startActivity(new Intent(ChattingActivity.this, MainActivity.class));
				}
				finish();
			}
		}
	}

	@Override
	public void onPause() {
		DBUtil.setAllChatRead();
		super.onPause();
	}

	@Override
	protected void onResume() {
		SysUtil.cancelNotification(Const.NOTIF_ID_CHAT_RCV, VidUtil.getSideName());
		MainThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mController.handleSoftInputWindow(mContentEt, false);
			}
		}, 1000);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		XmppManager.get().removeXmppListener(mMsgReceivedListener);
		ShowMsgNotifListener.get().setCurChatUser(null);
		mChatListAdapter.removeOnAvatarChangedListener();
	}

	private void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	private void hideView(View v) {
		v.setVisibility(View.GONE);
	}

	private boolean viewIsVisible(View v) {
		return !(v == null || v.getVisibility() == View.GONE);
	}

	private void deleteRecordFile() {
		if (mRecordFile != null && mRecordFile.exists()) {
			mRecordFile.delete();
		}
	}

	private void refreshListView(ChatRecord chatRecord) {
		if (chatRecord == null) {
			MainThreadHandler.makeToast(R.string.connection_crack_off);
			return;
		}
		mShownRecordList.add(chatRecord);
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mChatListAdapter.notifyDataSetChanged();
				mChatListView.setSelection(Integer.MAX_VALUE);
			}
		});
	}

}
