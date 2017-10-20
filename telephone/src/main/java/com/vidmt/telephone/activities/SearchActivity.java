package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.App;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.InviteUserDlg;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.AdManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.SearchListAdapter;
import com.vidmt.telephone.ui.adapters.SearchListAdapter.Holder;
import com.vidmt.telephone.utils.Enums.AccType;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 搜索页
 */
public class SearchActivity extends AbsVidActivity implements OnClickListener, OnItemClickListener {
	private EditText mSearchEt;
	private ImageView mClearIv;
	private ListView mListView;
	private LinearLayout mFindPeopleLayout;
	private TextView mPhoneTv;
	private SearchListAdapter mAdapter;
	private List<User> mAllUsers;
	private List<User> mFilteredUsers = new ArrayList<User>();
	private InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		imm = (InputMethodManager) App.get().getSystemService(Context.INPUT_METHOD_SERVICE);// 软键盘管理类
		mSearchEt = (EditText) findViewById(R.id.search_txt);
		mClearIv = (ImageView) findViewById(R.id.clear_words);
		mListView = (ListView) findViewById(R.id.list);
		mFindPeopleLayout = (LinearLayout) findViewById(R.id.find_people);
		mPhoneTv = (TextView) findViewById(R.id.phone);
		findViewById(R.id.clear_words).setOnClickListener(this);
		findViewById(R.id.find_people).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);

		findViewById(R.id.root).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				finishSelf();
				return true;
			}
		});
		mListView.setOnItemClickListener(this);

		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<String> uids = VidUtil.getAllFriendUids(false);
					mAllUsers = HttpManager.get().getMultUser(uids);
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
		mSearchEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.length() > 0) {
					mClearIv.setVisibility(View.VISIBLE);
					mSearchEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
					mSearchEt.setInputType(EditorInfo.TYPE_CLASS_TEXT);
				} else {
					mClearIv.setVisibility(View.GONE);
					mSearchEt.setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED);
					mSearchEt.setInputType(EditorInfo.TYPE_CLASS_TEXT);
				}
				String content = mSearchEt.getText().toString();
				mFilteredUsers = new ArrayList<User>();
				if (mAllUsers != null && !TextUtils.isEmpty(content)) {
					for (User user : mAllUsers) {
						if(user.account == null){
							continue;
						}
						if (user.getNick().toLowerCase().contains(content.toLowerCase())
								|| user.accType.equals(AccType.PHONE.name()) && user.account.contains(content)) {
							mFilteredUsers.add(user);
						}
					}
					if (mFilteredUsers.size() > 0) {
						mFindPeopleLayout.setVisibility(View.GONE);
					} else {
						mPhoneTv.setText(content);
						mFindPeopleLayout.setVisibility(View.VISIBLE);
					}
				} else {
					if (TextUtils.isEmpty(content)) {
						mFindPeopleLayout.setVisibility(View.GONE);
					} else {
						mFindPeopleLayout.setVisibility(View.VISIBLE);
						mPhoneTv.setText(content);
					}
				}
				mAdapter = new SearchListAdapter(SearchActivity.this, mFilteredUsers, content);
				mListView.setAdapter(mAdapter);
			}
		});

		mSearchEt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search();
					imm.hideSoftInputFromInputMethod(mSearchEt.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clear_words:
			mSearchEt.setText("");
			break;
		case R.id.find_people:
			search();
			break;
		case R.id.cancel:
			finishSelf();
			break;
		}
	}

	private void search() {
		final LoadingDlg loadingDlg = new LoadingDlg(this, R.string.searching);
		loadingDlg.show();
		final String phone = mSearchEt.getText().toString();
		if (!VidUtil.isPhoneNO(phone)) {
			loadingDlg.dismiss();
			MainThreadHandler.makeToast(R.string.phone_format_wrong);
			return;
		}
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					User user = HttpManager.get().getUserByAccount(phone);
//					if (!AccManager.get().getCurUser().isVip()) {
//						MainThreadHandler.post(new Runnable() {
//							@Override
//							public void run() {
//								AdManager.get().showInterstitialAd(SearchActivity.this);
//							}
//						});
//					}
					if (user != null && user.uid != null) {// 搜索到了
						Intent i = new Intent(SearchActivity.this, PersonInfoActivity.class);
						i.putExtra(ExtraConst.EXTRA_UID, user.uid);
						SearchActivity.this.startActivity(i);
						loadingDlg.dismiss();
						finish();
					} else {
						final String phoneAddr = HttpManager.get().getPhoneAddr(phone);
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								new InviteUserDlg(SearchActivity.this, phoneAddr).show();
							}
						});
					}
				} catch (VidException e) {
					VLog.e("test", e);
				}
				loadingDlg.dismiss();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Holder holder = (Holder) view.getTag();
		String uid = holder.uid;
		Intent i = new Intent(this, PersonInfoActivity.class);
		i.putExtra(ExtraConst.EXTRA_UID, uid);
		startActivity(i);
		finish();
	}

	private void finishSelf() {
		finish();
		overridePendingTransition(0, R.anim.act_alpha_out);
	}

	@Override
	public void onBackPressed() {
		finishSelf();
	}

}
