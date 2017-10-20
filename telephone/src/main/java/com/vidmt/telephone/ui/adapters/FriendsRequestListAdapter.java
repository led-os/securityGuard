package com.vidmt.telephone.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.AddFriendActivity;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.VidUtil;

public class FriendsRequestListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<User> mUsers;

	public FriendsRequestListAdapter(Activity ctx, List<User> users) {
		mActivity = ctx;
		mUsers = users;
	}

	@Override
	public int getCount() {
		return mUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mUsers.get(position);
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
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.friends_request_item, null);
			holder.avatarIv = (ImageView) convertView.findViewById(R.id.avatar);
			holder.nickTv = (TextView) convertView.findViewById(R.id.nick);
			holder.genderTv = (TextView) convertView.findViewById(R.id.gender);
			holder.ageTv = (TextView) convertView.findViewById(R.id.age);
			holder.agreeBtn = (Button) convertView.findViewById(R.id.agree);
			holder.rejectBtn = (Button) convertView.findViewById(R.id.reject);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		User user = mUsers.get(position);
		VidUtil.asyncCacheAndDisplayAvatar(holder.avatarIv, user.avatarUri, true);
		holder.nickTv.setText(user.getNick());
		holder.genderTv.setText(user.gender == 'M' ? R.string.male : R.string.female);
		holder.ageTv.setText(App.get().getString(R.string._age, user.age + ""));
		holder.agreeBtn.setOnClickListener(new ClickListenerWrapper(user.uid, true));
		holder.rejectBtn.setOnClickListener(new ClickListenerWrapper(user.uid, false));
		convertView.setOnClickListener(new ClickListenerWrapper(user.uid, false));
		return convertView;
	}

	private class ClickListenerWrapper implements View.OnClickListener {
		private String uid;
		private boolean isAgreeBtn;

		public ClickListenerWrapper(String uid, boolean isAgreeBtn) {
			this.uid = uid;
			this.isAgreeBtn = isAgreeBtn;
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {// click item
				Intent i = new Intent(mActivity, AddFriendActivity.class);
				i.putExtra(ExtraConst.EXTRA_UID, uid);
				mActivity.startActivity(i);
			} else {// click button
				boolean opSuccess = false;
				if (isAgreeBtn) {// agree
					opSuccess = AccManager.get().addFriend(uid);
				} else {// refuse
					opSuccess = AccManager.get().deleteFriend(uid);
				}
				if (opSuccess) {
					reloadAfterOperated(uid);
				}
				SysUtil.cancelNotification(Const.NOTIF_ID_CHAT_RCV, uid);
			}
		}
	};

	private void reloadAfterOperated(String uid) {
		User willRemoveUser = null;
		for (User user : mUsers) {
			if (user.uid.equals(uid)) {
				willRemoveUser = user;
				break;
			}
		}
		if (willRemoveUser != null) {
			mUsers.remove(willRemoveUser);
			notifyDataSetChanged();
		}
	}

	class Holder {
		ImageView avatarIv;
		TextView nickTv;
		TextView genderTv;
		TextView ageTv;
		Button agreeBtn;
		Button rejectBtn;
	}

}
