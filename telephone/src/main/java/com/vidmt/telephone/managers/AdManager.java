package com.vidmt.telephone.managers;

import java.util.Map;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.tasks.ServerConfInfoTask;

public class AdManager {
	private static AdManager sInstance;
	private Map<String, Boolean> mAdInfoMap;

	public static AdManager get() {
		if (sInstance == null) {
			sInstance = new AdManager();
		}
		return sInstance;
	}

	public void init(final Activity ctx) {
		boolean initSuccess = false;
		try {
			//Ads.init(ctx, Const.AD_APP_ID, Const.AD_SECRET_KEY);
			initSuccess = true;
		} catch (Exception e) {
			Log.e("test", "广告初始化失败：" + e);
		}
		if (!initSuccess) {
			return;
		}
		mAdInfoMap = ServerConfInfoTask.getAdInfo();
		if (mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_WALL)) {// may responsing...,
														// not-return-yet!
			//Ads.preLoad(Const.AD_APP_WALL, Ads.AdFormat.appwall);
		}
		if (mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_WIDGET)) {
			//Ads.preLoad(Const.AD_INTERSTITIAL, Ads.AdFormat.interstitial);
		}
		if (mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_BANNER)) {
			//Ads.preLoad(Const.AD_BANNER, Ads.AdFormat.banner);
		}
	}

	public void showAdWall(Activity ctx) {
		if (mAdInfoMap == null || !mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_WALL)) {
			return;
		}
		//Ads.showAppWall(ctx, Const.AD_APP_WALL);
	}

	public void showBannerAd(Activity ctx, int containerViewId) {
		if (mAdInfoMap == null || !mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_BANNER)) {
			return;
		}
		//View bannerView = Ads.createBannerView(ctx, Const.AD_BANNER);
		//FrameLayout bannerContainer = (FrameLayout) ctx.findViewById(containerViewId);
		//bannerContainer.addView(bannerView);
	}

	public void showInterstitialAd(Activity ctx) {
		if (mAdInfoMap == null || !mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_WIDGET)) {
			return;
		}
		try {
			//Ads.showInterstitial(ctx, Const.AD_INTERSTITIAL);
		} catch (Exception e) {
			VLog.d("test", e);
		}
	}
	
	public void showCustomAd(View adView1, View adView2) {
		if (mAdInfoMap == null || !mAdInfoMap.get(ServerConfInfoTask.SHOW_AD_CUSTOM)) {
			adView1.setVisibility(View.GONE);
			adView2.setVisibility(View.GONE);
			return;
		}
		adView1.setVisibility(View.VISIBLE);
		adView2.setVisibility(View.VISIBLE);
	}
	
	public void hideCustomAd(View adView1, View adView2) {
		adView1.setVisibility(View.GONE);
		adView2.setVisibility(View.GONE);
	}

}
