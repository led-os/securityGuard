package com.vidmt.telephone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.activities.SearchActivity;
import com.vidmt.telephone.listeners.MsgFriendFragChangedListener;
import com.vidmt.telephone.listeners.MsgFriendFragChangedListener.OnMsgFriendFragChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;


/**
 * lihuichao
 * 主界面，消息
 */
public class MsgFriendFragment extends Fragment implements OnTabChangedListener, OnMsgFriendFragChangedListener {
	@ViewInject(R.id.search)
	private View mSearchView;
	private Fragment mMsgFragment, mFriendFragment;
	private int mCurrentIndex;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_msg_friend, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMsgFragment = new MsgFragment();
		mFriendFragment = new FriendFragment();
		switch(mCurrentIndex){
		case 0:
			handleFragment(mMsgFragment, mFriendFragment);
			break;
		case 1:
			handleFragment(mFriendFragment, mMsgFragment);
			break;
		}
		TabChangedListener.get().setOnTabChangedListener(this);
		MsgFriendFragChangedListener.get().setOnMsgFriendFragChangedListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mSearchView.setVisibility(View.VISIBLE);
	}

	@OnClick({ R.id.search })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			startActivity(new Intent(getActivity(), SearchActivity.class));
			mSearchView.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onFragChange(int index) {
		if (index == 0) {// 消息
			handleFragment(mMsgFragment, mFriendFragment);
		} else if (index == 1) {// 好友
			handleFragment(mFriendFragment, mMsgFragment);
		}
		mCurrentIndex = index;
	}

	private void handleFragment(Fragment showFrag, Fragment hideFrag) {
		if (hideFrag.isAdded()) {
			hideFrag.onPause();
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (showFrag.isAdded()) {
			showFrag.onResume();
		} else {
			ft.add(R.id.frags, showFrag);
			ft.commit();
		}
		ft = getFragmentManager().beginTransaction();
		ft.show(showFrag);
		ft.commit();
		if (hideFrag.isAdded()) {
			ft = getFragmentManager().beginTransaction();
			ft.hide(hideFrag);
			ft.commit();
		}
	}

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_MSG_FRIEND_INDEX) {
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		TabChangedListener.get().removeOnTabChangedListener(this);
	}

}
