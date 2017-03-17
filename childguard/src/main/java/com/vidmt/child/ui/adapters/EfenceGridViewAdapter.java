package com.vidmt.child.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.EfenceActivity;
import com.vidmt.child.activities.MapActivity;
import com.vidmt.child.dlgs.BeVipDlg;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.ui.views.EfencePopWindow;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.FenceVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

import java.util.ArrayList;
import java.util.List;

public class EfenceGridViewAdapter extends BaseAdapter {
	private EfenceActivity mActivity;
	private List<FenceVo> mFenceList;
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag();
			if (tag != null) {
				final FenceVo fence = (FenceVo) getItem((Integer) tag);
				MainThreadHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						MapManager.get(null).animateTo(new LatLng(fence.lat, fence.lon));
					}
				}, 500);
			} else {// +
				Integer fenceNum = UserUtil.getLvl().fenceNum;
				if (UserUtil.getLvl().code == LvlType.NONE || UserUtil.getLvl().code == null) {
					new BeVipDlg(mActivity, R.string.recharge, R.string.not_gold_vip).show();
					return;
				}
				if (getCount() - 1 >= fenceNum) {
					MainThreadHandler.makeToast(R.string.fence_num_limited);
					return;
				}
				Intent i = new Intent(mActivity, MapActivity.class);
				i.putExtra(ExtraConst.EXTRA_MAP_ADD_EFENCE, true);
				ArrayList<String> fenceNames = new ArrayList<String>();
				for (FenceVo fence : mFenceList) {
					fenceNames.add(fence.name);
				}
				i.putStringArrayListExtra(ExtraConst.EXTRA_CURRENT_FENCE_NAMES, fenceNames);
				mActivity.startActivity(i);
			}
			mActivity.finish();
		}
	};
	private OnLongClickListener longClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			EfencePopWindow popWindow = new EfencePopWindow(mActivity, v);
			popWindow.show();
			return true;
		}
	};

	public EfenceGridViewAdapter(EfenceActivity act, List<FenceVo> fenceList) {
		mActivity = act;
		mFenceList = fenceList;
	}

	@Override
	public int getCount() {
		return mFenceList.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position == getCount() - 1) {
			return null;
		}
		return mFenceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.efence_gv_item, null);
			holder.bgLayout = (RelativeLayout) convertView.findViewById(R.id.efence_item);
			holder.nameTv = (TextView) convertView.findViewById(R.id.fence_name);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (position == getCount() - 1) {
			holder.bgLayout.setBackgroundResource(R.drawable.selector_list_pin_add);
			holder.bgLayout.setTag(null);
			holder.nameTv.setText("");
		} else {
			switch(position){
			case 0:
				holder.bgLayout.setBackgroundResource(R.drawable.selector_efence_item_bg_1);
				break;
			case 1:
				holder.bgLayout.setBackgroundResource(R.drawable.selector_efence_item_bg_2);
				break;
			case 2:
				holder.bgLayout.setBackgroundResource(R.drawable.selector_efence_item_bg_3);
				break;
			}
			FenceVo fence = (FenceVo) getItem(position);
			holder.nameTv.setText(fence.name);
			holder.bgLayout.setTag(position);
			holder.bgLayout.setOnLongClickListener(longClickListener);
		}
		holder.bgLayout.setOnClickListener(clickListener);
		return convertView;
	}
	
	class Holder {
		RelativeLayout bgLayout;
		TextView nameTv;
	}

}