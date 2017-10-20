package com.vidmt.telephone.activities;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.FriendsRequestListAdapter;

/**
 * 申请加我为好友的人的列表页
 */
@ContentView(R.layout.activity_friends_request)
public class FriendsRequestActivity extends AbsVidActivity {
	@ViewInject(R.id.list)
	private ListView mListView;
	@ViewInject(R.id.empty_notify)
	private TextView mEmptyNotifyTv;
	@ViewInject(R.id.loading)
	private View mLoadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();

		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = HttpManager.get().getRequestUsers(false);
					if (users == null || users.size() == 0) {
						handleEmptyNotify(true);
					} else {
						final FriendsRequestListAdapter adapter = new FriendsRequestListAdapter(
								FriendsRequestActivity.this, users);
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								mListView.setAdapter(adapter);
							}
						});
					}
					hideLoadingView();
				} catch (VidException e) {
					VLog.e("test", e);
					hideLoadingView();
				}
			}
		});
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.new_friends);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@OnClick({ R.id.back })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		}
	}

	private void hideLoadingView() {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mLoadingView.setVisibility(View.GONE);
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

}
