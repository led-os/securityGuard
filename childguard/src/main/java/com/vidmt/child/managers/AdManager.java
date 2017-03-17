package com.vidmt.child.managers;

import android.app.Activity;
import android.view.View;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.Const;
import com.vidmt.child.tasks.AdInfoTask;
import com.wandoujia.ads.sdk.Ads;

import java.util.Map;

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
			Ads.init(ctx, Const.AD_APP_ID, Const.AD_SECRET_KEY);
			initSuccess = true;
		} catch (Exception e) {
			VLog.d("test", e);
		}
		if (!initSuccess) {
			return;
		}
		mAdInfoMap = AdInfoTask.getAdInfo();
		if (mAdInfoMap.get(AdInfoTask.SHOW_AD_WALL)) {// may responsing..., not
														// return yet!
			Ads.preLoad(Const.AD_APP_WALL, Ads.AdFormat.appwall);
		}
		if (mAdInfoMap.get(AdInfoTask.SHOW_AD_WIDGET)) {
			Ads.preLoad(Const.AD_INTERSTITIAL, Ads.AdFormat.interstitial);
		}
		if (mAdInfoMap.get(AdInfoTask.SHOW_AD_BANNER)) {
			Ads.preLoad(Const.AD_BANNER, Ads.AdFormat.banner);
		}
	}

	public void showAdWall(Activity ctx) {
		Ads.showAppWall(ctx, Const.AD_APP_WALL);
	}

	public void showBannerAd(Activity ctx) {
		if (mAdInfoMap == null || !mAdInfoMap.get(AdInfoTask.SHOW_AD_BANNER)) {
			return;
		}
		View bannerView = Ads.createBannerView(ctx, Const.AD_BANNER);
		// FrameLayout bannerContainer = (FrameLayout)
		// ctx.findViewById(R.id.banner_ad_container);
		// bannerContainer.addView(bannerView);
	}

	public void showInterstitialAd(Activity ctx) {
		try {
			Ads.showInterstitial(ctx, Const.AD_INTERSTITIAL);
		} catch (Exception e) {
			VLog.d("test", e);
		}
	}

}
