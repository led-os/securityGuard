package com.vidmt.telephone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.umeng.fb.FeedbackAgent;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.AccountSetActivity;
import com.vidmt.telephone.activities.ChattingActivity;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.dlgs.VipQQGroupDlg;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.VidUtil;

/**
 * lihuichao
 * 主界面，设置
 */
public class SettingFragment extends Fragment implements OnTabChangedListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_setting, container, false);
		View vipCenter = view.findViewById(R.id.vip_center);
		boolean b = ServerConfInfoTask.hideVip();
		if(b){
			vipCenter.setVisibility(View.GONE);
		}
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		TabChangedListener.get().setOnTabChangedListener(this);
	}

	@OnClick({ R.id.vip_center, R.id.account_set, R.id.share, R.id.feedback, R.id.join_vip_group})
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.vip_center:
			startActivity(new Intent(getActivity(), VipCenterActivity.class));
			break;
		case R.id.account_set:
			startActivity(new Intent(getActivity(), AccountSetActivity.class));
			break;
		case R.id.share:
			String s1 = getString(R.string.share_app_msg);
			String s2 = getString(R.string.app_download_url) + Config.URL_LATEST_APK;
			VidUtil.share(s1 + s2, null);
			break;
		case R.id.feedback:
			//huawei change 100006应该换为客服的UID；
//			FeedbackAgent agent = new FeedbackAgent(getActivity());
//			agent.startFeedbackActivity();
			String kefu_uid =  SysUtil.getPref(PrefKeyConst.PREF_KEFU_ACCOUNT, Const.DEF_KEFU_UID);
			Intent i = new Intent(getActivity(), ChattingActivity.class);
			i.putExtra(ExtraConst.EXTRA_UID, kefu_uid);
			startActivity(i);
			break;
		case R.id.join_vip_group:
			if (AccManager.get().getCurUser().isVip()) {
				VidUtil.joinQQGroup(getActivity(), Const.QQ_VIP_GROUP_KEY, Const.QQ_VIP_GROUP_NO);
			} else {
				new VipQQGroupDlg(getActivity()).show();
			}
			break;
//		case R.id.join_qq_group:
//			VidUtil.joinQQGroup(getActivity(), Const.QQ_GROUP_KEY, Const.QQ_GROUP_NO);
//			break;
		}
	}

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_SETTING_INDEX) {
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		TabChangedListener.get().removeOnTabChangedListener(this);
	}

}
