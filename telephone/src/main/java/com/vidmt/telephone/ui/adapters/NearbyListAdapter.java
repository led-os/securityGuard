package com.vidmt.telephone.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.LocVo;

public class NearbyListAdapter extends BaseAdapter {
	private Context mCtx;
	private List<LocVo> mLocs;
	private List<User> mUsers;

	public NearbyListAdapter(Activity act, List<LocVo> locs, List<User> users) {
		mCtx = act;
		mLocs = locs;
		mUsers = users;
	}

	@Override
	public int getCount() {
		return mLocs.size();
	}

	@Override
	public Object getItem(int position) {
		return mLocs.get(position);
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
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.nearby_list_item, null);
			holder.avatarLayout = (LinearLayout) convertView.findViewById(R.id.avatar_layout);
			holder.nickTv = (TextView) convertView.findViewById(R.id.nick);
			holder.genderAgeLayout = convertView.findViewById(R.id.gender_age_layout);
			holder.genderIv = (ImageView) convertView.findViewById(R.id.gender_icon);
			holder.ageTv = (TextView) convertView.findViewById(R.id.age);
			holder.distanceTv = (TextView) convertView.findViewById(R.id.distance);
			holder.timeTv = (TextView) convertView.findViewById(R.id.time);
			holder.signatureTv = (TextView) convertView.findViewById(R.id.signature);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		LocVo locVo = (LocVo) getItem(position);
		User user = mUsers.get(position);
		holder.uid = user.uid;
		ImageView avatarIv = new ImageView(mCtx);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		avatarIv.setLayoutParams(params);
		if (holder.avatarLayout.getChildCount() > 0) {// 防止异步加载导致图片错乱
			holder.avatarLayout.removeAllViews();
		}
		holder.avatarLayout.addView(avatarIv);
		
		//avatarIv.setImageResource(R.drawable.default_avatar);
		VidUtil.asyncCacheAndDisplayAvatar(avatarIv, user.avatarUri, true);
		holder.nickTv.setText(user.getNick());
		if (user.gender == 'M') {
			holder.genderAgeLayout.setBackgroundResource(R.drawable.shap_male_rect_bg);
			holder.genderIv.setImageResource(R.drawable.male_icon);
		} else {
			holder.genderAgeLayout.setBackgroundResource(R.drawable.shap_female_rect_bg);
			holder.genderIv.setImageResource(R.drawable.female_icon);
		}
		holder.ageTv.setText(user.age + "");
		holder.signatureTv.setText(user.signature == null ? "" : user.signature);
		holder.distanceTv.setText(GeoUtil.formatNearbyDistance(locVo.distance));
		holder.timeTv.setText(DateUtil.formatNearbyTime(locVo.time));
		holder.lat = locVo.lat;
		holder.lon = locVo.lon;
		return convertView;
	}

	public class Holder {
		public String uid;
		LinearLayout avatarLayout;
		TextView nickTv;
		View genderAgeLayout;
		ImageView genderIv;
		TextView ageTv;
		TextView distanceTv;
		TextView timeTv;
		TextView signatureTv;
		double lat;
		double lon;
	}

}
