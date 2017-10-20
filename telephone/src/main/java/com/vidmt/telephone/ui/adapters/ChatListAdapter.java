package com.vidmt.telephone.ui.adapters;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.AccountSetActivity;
import com.vidmt.telephone.activities.PersonInfoActivity;
import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.MultiMediaClickedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;

public class ChatListAdapter extends BaseAdapter {
	private Activity ctx;
	private List<ChatRecord> chatContents;

	public ChatListAdapter(Activity ctx, List<ChatRecord> chatContents) {
		this.ctx = ctx;
		this.chatContents = chatContents;
	}

	@Override
	public int getCount() {
		return chatContents.size();
	}

	@Override
	public Object getItem(int position) {
		return chatContents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChatRecord chatContent = chatContents.get(position);
		final boolean isSelf = chatContent.isSelf();
		final Holder holder;
		if (convertView == null || ((Holder) convertView.getTag()).isSelfFlag != isSelf) {
			holder = new Holder();
			if (isSelf) {// by ctx to inflate is a must
				convertView = LayoutInflater.from(ctx).inflate(R.layout.chat_me_item, null);
				holder.isSelfFlag = true;
			} else {
				convertView = LayoutInflater.from(ctx).inflate(R.layout.chat_he_item, null);
				holder.isSelfFlag = false;
			}
			holder.contentBgView = (LinearLayout) convertView.findViewById(R.id.content);
			holder.avatarView = (ImageView) convertView.findViewById(R.id.avatar);
			holder.timeTv = (TextView) convertView.findViewById(R.id.time);
			holder.txtContentView = (TextView) convertView.findViewById(R.id.txt_content);
			holder.imgContentView = (ImageView) convertView.findViewById(R.id.img_content);
			holder.videoContainerView = (LinearLayout) convertView.findViewById(R.id.video_container);
			holder.videoDeletedView = (ImageView) convertView.findViewById(R.id.video_deleted);
			holder.voiceContent = (TextView) convertView.findViewById(R.id.voice_content);
			holder.voiceIcon = (ImageView) convertView.findViewById(R.id.voice_icon);
			holder.space = convertView.findViewById(R.id.space);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (isSelf) {
			User curUser = AccManager.get().getCurUser();
			VidUtil.asyncCacheAndDisplayAvatar(holder.avatarView, curUser.avatarUri, true);
		} else {
			try {
				//huawei add;
				String kefu_uid =  SysUtil.getPref(PrefKeyConst.PREF_KEFU_ACCOUNT, Const.DEF_KEFU_UID);
				if(chatContent.getFuid().equals(kefu_uid)){
					holder.avatarView.setImageResource(R.drawable.default_kefu_avatar);
				}else {
					User user = HttpManager.get().getUserInfo(chatContent.getFuid());
					VidUtil.asyncCacheAndDisplayAvatar(holder.avatarView, user.avatarUri, true);
				}
			} catch (VidException e) {
				VLog.e("test", e);
			}
		}
		DateUtil.setChatTime(holder.timeTv, chatContents, position);
		final ChatType type = ChatType.valueOf(ChatType.class, chatContent.getType());
		if (type == ChatType.TXT) {// 普通文本
			holder.txtContentView.setVisibility(View.VISIBLE);
			holder.imgContentView.setVisibility(View.GONE);
			holder.videoContainerView.setVisibility(View.GONE);
			holder.videoDeletedView.setVisibility(View.GONE);
			holder.voiceContent.setVisibility(View.GONE);
			holder.voiceIcon.setVisibility(View.GONE);
			holder.space.setVisibility(View.GONE);
			holder.txtContentView.setText(VidUtil.replaceToFace(chatContent.getData()));
		} else if (type == ChatType.IMAGE) {// 图片
			holder.imgContentView.setVisibility(View.VISIBLE);
			holder.videoContainerView.setVisibility(View.GONE);
			holder.videoDeletedView.setVisibility(View.GONE);
			holder.txtContentView.setVisibility(View.GONE);
			holder.voiceContent.setVisibility(View.GONE);
			holder.voiceIcon.setVisibility(View.GONE);
			holder.space.setVisibility(View.GONE);
			File picFile = new File(VLib.getSdcardDir(), chatContent.getData());
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			Bitmap pic = BitmapFactory.decodeFile(picFile.getAbsolutePath(),options);
			if (pic == null) {
				holder.imgContentView.setImageBitmap(SysUtil.getBitmap(R.drawable.img_deleted));
				holder.imgContentView.setTag(new Object());// 非空tag
			} else {
				holder.imgContentView.setImageBitmap(pic);
			}
		} else if (type == ChatType.AUDIO) {// 音频
			holder.voiceContent.setVisibility(View.VISIBLE);
			holder.voiceIcon.setVisibility(View.VISIBLE);
			holder.space.setVisibility(View.VISIBLE);
			if (isSelf) {
				holder.voiceIcon.setImageResource(R.drawable.me_voice_play3);
			} else {
				holder.voiceIcon.setImageResource(R.drawable.he_voice_play3);
			}
			holder.imgContentView.setVisibility(View.GONE);
			holder.videoContainerView.setVisibility(View.GONE);
			holder.videoDeletedView.setVisibility(View.GONE);
			holder.txtContentView.setVisibility(View.GONE);
			int totalTime = chatContent.getDuring();
			boolean isRemoteVoice = false;
			if (totalTime == Const.REMOTE_RECORD_TIME_TAG) {
				isRemoteVoice = true;
				totalTime = Const.REMOTE_RECORD_TIME_LEN;
			}
			if (totalTime <= 60) {
				holder.voiceContent.setText(totalTime + "''");
				if (isRemoteVoice) {
					holder.voiceContent.setText(" [远程] " + totalTime + "''");
				}
			} else {
				holder.voiceContent.setText(totalTime + "'");
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(totalTime * 8, 0);
			holder.space.setLayoutParams(params);
			File voiceFile = new File(VLib.getSdcardDir(), chatContent.getData());
			if (!voiceFile.exists()) {
				holder.voiceContent.setText(R.string.voice_deleted);
				holder.voiceContent.setTextSize(10);
			}
		} else if (type == ChatType.VIDEO) {// 视频
			holder.videoContainerView.setVisibility(View.VISIBLE);
			holder.imgContentView.setVisibility(View.GONE);
			holder.txtContentView.setVisibility(View.GONE);
			holder.voiceContent.setVisibility(View.GONE);
			holder.voiceIcon.setVisibility(View.GONE);
			holder.space.setVisibility(View.GONE);
			if (holder.videoContainerView.getChildCount() > 0) {
				holder.videoContainerView.removeAllViews();
			}
			File videoFile = new File(VLib.getSdcardDir(), chatContent.getData());
			if (!videoFile.exists()) {
				holder.videoDeletedView.setVisibility(View.VISIBLE);
				holder.videoContainerView.addView(holder.videoDeletedView);
				holder.videoContainerView.setTag(new Object());// 非空tag
			} else {
				// VideoPlayerView playerView = new VideoPlayerView(ctx);
				// holder.videoContainerView.addView(playerView);
				// playerView.setVideoSrcFile(videoFile);
				// playerView.setNoVolume(true);
				// playerView.play();
			}
		}
		holder.contentBgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (type == ChatType.TXT) {
					return;
				} else if (type == ChatType.IMAGE) {
					if (holder.imgContentView.getTag() != null) {// 图片已删
						return;
					}
					MultiMediaClickedListener.get().triggerOnMultiMediaClickedListener(ChatType.IMAGE,
							chatContent.getData());
				} else if (type == ChatType.AUDIO) {
					MultiMediaClickedListener.get().triggerOnMultiMediaClickedListener(ChatType.AUDIO,
							chatContent.getData());
					if (isSelf) {
						holder.voiceIcon.setImageResource(R.anim.me_voice_playing);
					} else {
						holder.voiceIcon.setImageResource(R.anim.he_voice_playing);
					}
					AnimationDrawable animationDrawable = (AnimationDrawable) holder.voiceIcon.getDrawable();
					animationDrawable.stop();
					animationDrawable.start();
				} else if (type == ChatType.VIDEO) {
					if (holder.videoContainerView.getTag() != null) {// 视频已删
						return;
					}
					MultiMediaClickedListener.get().triggerOnMultiMediaClickedListener(ChatType.VIDEO,
							chatContent.getData());
				}
			}
		});
		holder.avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i;
				if (isSelf) {
					i = new Intent(ctx, AccountSetActivity.class);
				} else {
					i = new Intent(ctx, PersonInfoActivity.class);
					i.putExtra(ExtraConst.EXTRA_UID, chatContent.getFuid());
				}
				ctx.startActivity(i);
			}
		});
		return convertView;
	}

	class Holder {
		LinearLayout contentBgView;
		ImageView avatarView;
		TextView txtContentView;
		ImageView imgContentView;
		LinearLayout videoContainerView;
		ImageView videoDeletedView;
		TextView voiceContent;
		ImageView voiceIcon;
		TextView timeTv;
		View space;
		boolean isSelfFlag;
	}

}
