package com.vidmt.telephone.fragments;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.FileStorage;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.ContactsActivity;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.activities.NearbyActivity;
import com.vidmt.telephone.activities.SearchActivity;
import com.vidmt.telephone.listeners.PaySuccessListener;
import com.vidmt.telephone.listeners.PaySuccessListener.OnPaySuccessListener;
import com.vidmt.telephone.listeners.ServerConfDownloadFinishedListener;
import com.vidmt.telephone.listeners.ServerConfDownloadFinishedListener.OnServerConfDownloadFinishedListener;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.AdManager;
import com.vidmt.telephone.services.DownloadApkService;
import com.vidmt.telephone.utils.Enums.PayType;
import com.vidmt.telephone.utils.VidUtil;

/**
 * lihuichao
 * 主界面，发现页
 */
public class FindFragment extends Fragment implements OnTabChangedListener {
	@ViewInject(R.id.search)
	private View mSearchView;
	@ViewInject(R.id.cg_ad)
	private RelativeLayout mCgAdView;
	@ViewInject(R.id.game)
	private LinearLayout mGameView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_find, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TabChangedListener.get().setOnTabChangedListener(this);
		PaySuccessListener.get().addOnPaySuccessListener(mOnPaySuccessListener);
		// not show the ad now;
		AdManager.get().hideCustomAd(mCgAdView, mGameView);
//		ServerConfDownloadFinishedListener.get().setOnServerConfDownloadFinishedListener(
//				new OnServerConfDownloadFinishedListener() {
//					@Override
//					public void onDownloadFinished() {
//						if (!AccManager.get().getCurUser().isVip()) {// 非会员，显示广告
//							AdManager.get().showCustomAd(mCgAdView, mGameView);
//						} else {
//							AdManager.get().hideCustomAd(mCgAdView, mGameView);
//						}
//					}
//				});
	}

	@Override
	public void onResume() {
		super.onResume();
		mSearchView.setVisibility(View.VISIBLE);
	}

	@OnClick({ R.id.search, R.id.phone_contacts, R.id.nearby, R.id.game, R.id.cg_install })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.search:
			startActivity(new Intent(getActivity(), SearchActivity.class));
			mSearchView.setVisibility(View.GONE);
			break;
		case R.id.phone_contacts:
			startActivity(new Intent(getActivity(), ContactsActivity.class));
			break;
		case R.id.nearby:
			startActivity(new Intent(getActivity(), NearbyActivity.class));
			break;
		case R.id.game:
			AdManager.get().showAdWall(getActivity());
			break;
		case R.id.cg_install:
			String cgApkUrl = Config.URL_CG_APK;
			String apkName = cgApkUrl.substring(cgApkUrl.lastIndexOf("/") + 1);
			File f = new File(VLib.getSdcardDir(), FileStorage.buildApkPath(apkName));
			if (f.exists()) {
				VidUtil.installApk(f);
			} else {
				MainThreadHandler.makeToast(R.string.downloading);
				Intent i = new Intent(getActivity(), DownloadApkService.class);
				i.putExtra(ExtraConst.EXTRA_APK_URL, Config.URL_CG_APK);
				getActivity().startService(i);
			}
			break;
		}
	}

	private OnPaySuccessListener mOnPaySuccessListener = new OnPaySuccessListener() {
		@Override
		public void onSuccess(PayType type) {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					AdManager.get().hideCustomAd(mCgAdView, mGameView);
				}
			});
		}
	};

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_FIND_INDEX) {
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		TabChangedListener.get().removeOnTabChangedListener(this);
		PaySuccessListener.get().removeOnPaySuccessListener(mOnPaySuccessListener);
	}

}
