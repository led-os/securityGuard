package com.vidmt.child.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vidmt.child.App;
import com.vidmt.child.Const;
import com.vidmt.child.R;

import java.util.List;

public class VipInfoGridViewAdapter extends BaseAdapter {
	private Context mCtx;
	private List<Integer> mShownFuncIndexList;
	private List<String> mExtraDataList;

	public VipInfoGridViewAdapter(Activity act, List<Integer> shownFuncIndexList, List<String> extraDataList) {
		mCtx = act;
		mShownFuncIndexList = shownFuncIndexList;
		mExtraDataList = extraDataList;
	}

	@Override
	public int getCount() {
		return mShownFuncIndexList.size();
	}

	@Override
	public Object getItem(int position) {
		return mShownFuncIndexList.get(position);
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
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.vip_info_gv_item, null);
			holder.iconIv = (ImageView) convertView.findViewById(R.id.icon);
			holder.titleTv = (TextView) convertView.findViewById(R.id.title);
			holder.detailTv = (TextView) convertView.findViewById(R.id.detail);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		int index = mShownFuncIndexList.get(position);
		holder.iconIv.setImageResource(Const.VIP_INFO_ICON_ID_ARR[index]);
		holder.titleTv.setText(Const.VIP_INFO_TITLE_ID_ARR[index]);
		String extraData = mExtraDataList.get(position);
		if (extraData != null) {
			holder.detailTv.setText(App.get().getString(Const.VIP_INFO_MSG_ID_ARR[index], extraData));
		} else {
			holder.detailTv.setText(Const.VIP_INFO_MSG_ID_ARR[index]);
		}
		return convertView;
	}

	private class Holder {
		ImageView iconIv;
		TextView titleTv;
		TextView detailTv;
	}

}
