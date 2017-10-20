package com.vidmt.telephone.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vidmt.telephone.Const;
import com.vidmt.telephone.R;

public class VipInfoGridViewAdapter extends BaseAdapter {
	private Context mCtx;

	public VipInfoGridViewAdapter(Activity act) {
		mCtx = act;
	}

	@Override
	public int getCount() {
		return Const.VIP_INFO_TITLE_ID_ARR.length;
	}

	@Override
	public Object getItem(int position) {
		return Const.VIP_INFO_TITLE_ID_ARR[position];
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
		holder.iconIv.setImageResource(Const.VIP_INFO_ICON_ID_ARR[position]);
		holder.titleTv.setText(Const.VIP_INFO_TITLE_ID_ARR[position]);
		holder.detailTv.setText(Const.VIP_INFO_MSG_ID_ARR[position]);
		return convertView;
	}

	private class Holder {
		ImageView iconIv;
		TextView titleTv;
		TextView detailTv;
	}

}
