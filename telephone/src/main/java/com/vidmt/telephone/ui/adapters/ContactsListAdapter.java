package com.vidmt.telephone.ui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.PersonInfoActivity;
import com.vidmt.telephone.dlgs.AddFriendDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.Enums.AddFriendType;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.ContactVo;
import com.vidmt.telephone.vos.ContactVo.RecommendType;

public class ContactsListAdapter extends BaseAdapter {
	private Activity mContext;
	private List<ContactVo> mContacts;

	public ContactsListAdapter(Activity context, List<ContactVo> contacts) {
		mContext = context;
		mContacts = contacts;
	}

	@Override
	public int getCount() {
		return mContacts.size();
	}

	@Override
	public Object getItem(int position) {
		return mContacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.contacts_list_item, null);
			holder = new Holder();
			holder.nameTv = (TextView) convertView.findViewById(R.id.name);
			holder.phoneTv = (TextView) convertView.findViewById(R.id.number);
			holder.avatarIv = (ImageView) convertView.findViewById(R.id.head);
			holder.btn = (TextView) convertView.findViewById(R.id.button);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final ContactVo contact = mContacts.get(position);
		holder.nameTv.setText(contact.name);
		holder.phoneTv.setText(contact.phone);
		if (contact.avatar != null) {
			holder.avatarIv.setImageBitmap(contact.avatar);
		} else {
			if (contact.recommendType != RecommendType.INVITE) {
				VidUtil.asyncCacheAndDisplayAvatar(holder.avatarIv, contact.avatarUri, true);
			} else {
				holder.avatarIv.setImageResource(R.drawable.default_avatar_online);
			}
		}
		if (contact.recommendType == RecommendType.FRIEND) {
			holder.btn.setText(R.string.already_added);
			holder.btn.setTextColor(Color.GRAY);
			holder.btn.setEnabled(false);
		} else if (contact.recommendType == RecommendType.INVITE) {
			holder.btn.setText(R.string.invite);
			holder.btn.setTextColor(App.get().getResources().getColor(R.color.selector_txt_blue_white));
			holder.btn.setEnabled(true);
		} else if (contact.recommendType == RecommendType.ADD) {
			holder.btn.setText(R.string.add);
			holder.btn.setTextColor(App.get().getResources().getColor(R.color.selector_txt_blue_white));
			holder.btn.setEnabled(true);
		}
		holder.btn.setOnClickListener(new ClickListenerWrapper(contact, holder.btn));
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contact.recommendType == RecommendType.FRIEND) {
					Intent i = new Intent(mContext, PersonInfoActivity.class);
					i.putExtra(ExtraConst.EXTRA_UID, contact.uid);
					mContext.startActivity(i);
				}
			}
		});
		return convertView;
	}

	private class ClickListenerWrapper implements OnClickListener {
		private ContactVo contact;
		private TextView btn;

		public ClickListenerWrapper(ContactVo contact, TextView btn) {
			this.contact = contact;
			this.btn = btn;
		}

		@Override
		public void onClick(View v) {
			if (contact.recommendType == RecommendType.ADD) {
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						boolean isSelfVip = AccManager.get().getCurUser().isVip();
						try {
							final User user = HttpManager.get().getUserInfo(contact.uid);
							final AddFriendType type = VidUtil.getAddFriendStatus(isSelfVip,
									user != null && user.isVip());
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									if (type == AddFriendType.DIRECT) {
										boolean success = AccManager.get().addFriend(contact.uid);
										if (success) {
											new AddFriendDlg(mContext, type).show();
											//huawei change;
//											btn.setText(R.string.already_added);
//											btn.setBackgroundDrawable(null);
//											btn.setTextColor(Color.GRAY);
//											btn.setEnabled(false);
//											contact.recommendType = RecommendType.FRIEND;
										}
									} else if (type == AddFriendType.WAIT || type == AddFriendType.VIP_WAIT) {
										boolean success = AccManager.get().addFriend(contact.uid);
										if (success) {
											new AddFriendDlg(mContext, type).show();
										}
									} else if (type == AddFriendType.NOT_ALLOWED) {
										new AddFriendDlg(mContext, type).show();
									} else if (user.isAvoidDisturb()) {// 对方防骚扰
										MainThreadHandler.makeToast(R.string.side_open_avoid_disturb);
									}
								}
							});
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			} else if (contact.recommendType == RecommendType.INVITE) {
				String s = App.get().getString(R.string.share_app_msg) + Config.URL_LATEST_APK;
				VidUtil.share(s, null);
			}
		}
	};

	class Holder {
		TextView nameTv;
		TextView phoneTv;
		ImageView avatarIv;
		TextView btn;
	}

}
