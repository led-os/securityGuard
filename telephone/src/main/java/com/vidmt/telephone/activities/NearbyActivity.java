package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.lidroid.xutils.view.annotation.event.OnItemClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.LocationManager;
import com.vidmt.telephone.ui.adapters.NearbyListAdapter;
import com.vidmt.telephone.ui.adapters.NearbyListAdapter.Holder;
import com.vidmt.telephone.vos.LocVo;

@ContentView(R.layout.activity_nearby)
public class NearbyActivity extends AbsVidActivity implements OnTabChangedListener {
	@ViewInject(R.id.list)
	private ListView mListView;
	@ViewInject(R.id.loading)
	private View mLoadingView;
	@ViewInject(R.id.empty_notify)
	private View mEmptyNotifyTv;
	private List<User> mUserList;
	private List<LocVo> mLocList;
	private View mFooterView;
	private Button mLoadMoreBtn;
	private NearbyListAdapter mAdapter;
	private static final int NEARBY_PAGE_SIZE = 20;
	private int mPageIndex;
	private String mFilterGender;
	private Integer mFilterTime, mFilterAgeStart, mFilterAgeEnd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		initFilter();
		initFooterView();
		initData();
		TabChangedListener.get().setOnTabChangedListener(this);
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.nearby);
		Button rightBtn = (Button) findViewById(R.id.right);
		rightBtn.setText(R.string.filter);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initFilter() {
		mFilterGender = SysUtil.getPref(PrefKeyConst.RPEF_FILTER_GENDER);
		mFilterTime = SysUtil.getIntPref(PrefKeyConst.RPEF_FILTER_TIME);
		if (mFilterTime == -1) {
			mFilterTime = null;
		}
		int ageCode = SysUtil.getIntPref(PrefKeyConst.RPEF_FILTER_AGE_CODE);
		ageCode2AgeFilter(ageCode);
	}

	private void initData() {
		if (LocationManager.get().getCurLocation() == null) {
			MainThreadHandler.makeToast(R.string.self_not_located);
			mLoadingView.setVisibility(View.GONE);
			return;
		}
		handleEmptyNotify(false);
		mLoadingView.setVisibility(View.VISIBLE);
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					mLocList = HttpManager.get().getNearbyLocs(mFilterGender, mFilterTime, mFilterAgeStart,
							mFilterAgeEnd, ++mPageIndex, NEARBY_PAGE_SIZE);
					if (mLocList == null || mLocList.size() == 0) {
						handleEmptyNotify(true);
						--mPageIndex;
						return;
					} else if (mLocList.size() < NEARBY_PAGE_SIZE) {
						--mPageIndex;
					} else {
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								mListView.addFooterView(mFooterView);// 加载更多
							}
						});
					}
					handleEmptyNotify(false);
					mUserList = locList2UserList(mLocList);// two list the same
															// size!
					mAdapter = new NearbyListAdapter(NearbyActivity.this, mLocList, mUserList);
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							mListView.setAdapter(mAdapter);
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
					if (mLocList == null || mLocList.size() == 0) {
						handleEmptyNotify(true);
						--mPageIndex;
					}
				} finally {
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							mLoadingView.setVisibility(View.GONE);
						}
					});
				}
			}
		});
	}

	private void initFooterView() {
		mFooterView = SysUtil.inflate(R.layout.footerview_load_more);
		mLoadMoreBtn = (Button) mFooterView.findViewById(R.id.load_more);
		mLoadMoreBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoadMoreBtn.setText(R.string.loading);
				mLoadMoreBtn.setEnabled(false);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							List<LocVo> locs = HttpManager.get().getNearbyLocs(mFilterGender, mFilterTime,
									mFilterAgeStart, mFilterAgeEnd, ++mPageIndex, NEARBY_PAGE_SIZE);
							if (locs == null || locs.size() == 0) {
								MainThreadHandler.makeToast(R.string.nearby_no_more_friends);
								--mPageIndex;
							} else {
								mLocList.addAll(locs);
								mUserList.addAll(locList2UserList(locs));
								MainThreadHandler.post(new Runnable() {
									@Override
									public void run() {
										mAdapter.notifyDataSetChanged();
									}
								});
							}
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									mLoadMoreBtn.setText(R.string.load_more);
									mLoadMoreBtn.setEnabled(true);
								}
							});
						} catch (VidException e) {
							VLog.e("test", e);
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									mLoadMoreBtn.setText(R.string.load_error);
									mLoadMoreBtn.setEnabled(true);
								}
							});
						}
					}
				});
			}
		});
	}

	private void handleEmptyNotify(final boolean show) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (show) {
					mEmptyNotifyTv.setVisibility(View.VISIBLE);
				} else {
					mEmptyNotifyTv.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_MSG_FRIEND_INDEX) {
		}
	}

	@OnClick({ R.id.back, R.id.right })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.right:
			startActivityForResult(new Intent(this, FilterNearbyActivity.class), 0);
			overridePendingTransition(R.anim.act_open_from_bottom, R.anim.act_bg_fake);
			break;
		}
	}

	@OnItemClick(R.id.list)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Holder holder = (Holder) view.getTag();
		String uid = holder.uid;
		Intent i = new Intent(this, PersonInfoActivity.class);
		i.putExtra(ExtraConst.EXTRA_UID, uid);
		startActivity(i);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == FilterNearbyActivity.RES_CODE) {
			mFilterGender = data.getStringExtra(ExtraConst.EXTRA_FILTER_GENDER);// gender
			mFilterTime = data.getIntExtra(ExtraConst.EXTRA_FILTER_TIME, -1);// time
			if (mFilterTime == -1) {
				mFilterTime = null;
			}
			int ageCode = data.getIntExtra(ExtraConst.EXTRA_FILTER_AGE_CODE, -1);// age
			ageCode2AgeFilter(ageCode);
			mListView.setAdapter(null);
			mPageIndex = 0;
			if (mListView.getFooterViewsCount() == 1) {
				mListView.removeFooterView(mFooterView);
			}
			mListView.removeFooterView(mFooterView);
			initData();
		}
	}

	private List<User> locList2UserList(List<LocVo> locList) throws VidException {
		List<String> uids = new ArrayList<String>();
		for (LocVo loc : locList) {
			uids.add(loc.uid);
		}
		List<User> users = HttpManager.get().getMultUser(uids);
		List<User> list = new ArrayList<User>();// 修正顺序
		for (String uid : uids) {
			for (User user : users) {
				if (uid.equals(user.uid)) {
					list.add(user);
					break;
				}
			}
		}
		return list;
	}

	private void ageCode2AgeFilter(int ageCode) {
		if (ageCode == -1) {
			mFilterAgeStart = null;
			mFilterAgeEnd = null;
		}
		switch (ageCode) {
		case FilterNearbyActivity.AGE_RANGE_1_CODE:// 18-22
			mFilterAgeStart = FilterNearbyActivity.AGE_RANGE_1_START;
			mFilterAgeEnd = FilterNearbyActivity.AGE_RANGE_1_END;
			break;
		case FilterNearbyActivity.AGE_RANGE_2_CODE:// 23-26
			mFilterAgeStart = FilterNearbyActivity.AGE_RANGE_1_END + 1;
			mFilterAgeEnd = FilterNearbyActivity.AGE_RANGE_3_START - 1;
			break;
		case FilterNearbyActivity.AGE_RANGE_3_CODE:// 27-35
			mFilterAgeStart = FilterNearbyActivity.AGE_RANGE_3_START;
			mFilterAgeEnd = FilterNearbyActivity.AGE_RANGE_3_END;
			break;
		case FilterNearbyActivity.AGE_RANGE_4_CODE:// >35
			mFilterAgeStart = FilterNearbyActivity.AGE_RANGE_3_END + 1;
			break;
		}
	}

}
