package com.vidmt.telephone.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.vidmt.telephone.utils.VidUtil;

public class SearchListAdapter extends BaseAdapter {
	private Context mCtx;
	private List<User> mAllUsers;
	private String mFocusContent;

	public SearchListAdapter(Activity act, List<User> allUsers, String focusContent) {
		mCtx = act;
		mAllUsers = allUsers;
		mFocusContent = focusContent;
	}

	@Override
	public int getCount() {
		return mAllUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mAllUsers.get(position);
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
			convertView = LayoutInflater.from(mCtx).inflate(R.layout.search_list_item, null);
			holder.avatarLayout = (LinearLayout) convertView.findViewById(R.id.avatar_layout);
			holder.nickTv = (TextView) convertView.findViewById(R.id.nick);
			holder.phoneTv = (TextView) convertView.findViewById(R.id.phone);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		ImageView avatarIv = new ImageView(mCtx);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		avatarIv.setLayoutParams(params);
		if (holder.avatarLayout.getChildCount() > 0) {// 防止异步加载导致图片错乱
			holder.avatarLayout.removeAllViews();
		}
		holder.avatarLayout.addView(avatarIv);
		User user = (User) getItem(position);
		holder.uid = user.uid;
		VidUtil.asyncCacheAndDisplayAvatar(avatarIv, user.avatarUri, true);
		String nick = user.getNick();
		String phone = user.account;
		if (nick.toLowerCase().contains(mFocusContent.toLowerCase())) {
			int start = nick.toLowerCase().indexOf(mFocusContent.toLowerCase());
			VidUtil.colorPartText(holder.nickTv, nick, start, start + mFocusContent.length(), Color.RED);
		} else {
			holder.nickTv.setText(nick);
		}
		if (phone.contains(mFocusContent)) {
			int start = phone.indexOf(mFocusContent);
			VidUtil.colorPartText(holder.phoneTv, phone, start, start + mFocusContent.length(), Color.RED);
		} else {
			holder.phoneTv.setText(phone);
		}
		return convertView;
	}

	public class Holder {
		public String uid;
		public LinearLayout avatarLayout;
		public TextView nickTv;
		public TextView phoneTv;
	}

}
