package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.fragments.FindFragment;
import com.vidmt.telephone.fragments.HomeFragment;
import com.vidmt.telephone.fragments.MsgFriendFragment;
import com.vidmt.telephone.fragments.SettingFragment;
import com.vidmt.telephone.listeners.ChatStatusListener;
import com.vidmt.telephone.listeners.ChatStatusListener.OnChatStatusListener;
import com.vidmt.telephone.listeners.MsgFriendFragChangedListener;
import com.vidmt.telephone.listeners.PEPEventListener;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.AdManager;
import com.vidmt.telephone.tasks.UpdateTask;
import com.vidmt.telephone.ui.views.CustomViewPager;
import com.vidmt.telephone.utils.DBUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.utils.VipInfoUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;

/**
 * 主页界面
 */
public class MainActivity extends AbsFragmentActivity implements OnClickListener {
	private View mHomeTitleView, mMsgFriendTitleView, mFindTitleView, mSettingTitleView;
	private RadioGroup mMsgFriendRg;
	private CustomViewPager mViewPager;
	private LinearLayout mHomeBottomLayout, mFindBottomLayout, mSettingBottomLayout;
	private RelativeLayout mMsgFriendBottomLayout;
	private ImageView mHomeIcon, mMsgFriendIcon, mFindIcon, mSettingIcon;
	private TextView mHomeTv, mMsgFriendTv, mFindTv, mSettingTv;
	private TextView mUnReadNumTv;

	private FragmentPagerAdapter mAdapter;
	private List<Fragment> mFragments = new ArrayList<Fragment>();
	public static final int TAB_HOME_INDEX = 0;
	public static final int TAB_MSG_FRIEND_INDEX = 1;
	public static final int TAB_FIND_INDEX = 2;
	public static final int TAB_SETTING_INDEX = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				VipInfoUtil.init();
				UpdateTask.launchUpdateTask(MainActivity.this, false);
			}
		});

		boolean isRestartFromCrash = getIntent().getBooleanExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, false);
		if (isRestartFromCrash) {// 崩溃后后台重启
			moveTaskToBack(false);
		}

		XmppManager.get().addXmppListener(mOnMsgReceivedListener);
		//huawei change;
		if(!XmppManager.get().containsXmppListener(PEPEventListener.get())) {
			VidUtil.fLog("MainActivity addXmppListener(PEPEventListener.get())");
			XmppManager.get().addXmppListener(PEPEventListener.get());
		}else {
			VidUtil.fLog("MainActivity containsXmppListener(PEPEventListener.get())");
		}


		initViews();
		showTotalUnReadNum(true, 0);
		ChatStatusListener.get().addOnChatStatusListener(mOnChatStatusListener);
	}

	private void initViews() {
		mHomeTitleView = findViewById(R.id.title_bar_page_home);
		mMsgFriendTitleView = findViewById(R.id.title_bar_page_msg_friend);
		mFindTitleView = findViewById(R.id.title_bar_page_find);
		mSettingTitleView = findViewById(R.id.title_bar_page_setting);

		mMsgFriendRg = (RadioGroup) findViewById(R.id.rg);

		mViewPager = (CustomViewPager) findViewById(R.id.id_viewpager);
		mHomeBottomLayout = (LinearLayout) findViewById(R.id.home_page);
		mMsgFriendBottomLayout = (RelativeLayout) findViewById(R.id.msg_friend);
		mFindBottomLayout = (LinearLayout) findViewById(R.id.find);
		mSettingBottomLayout = (LinearLayout) findViewById(R.id.setting);

		mHomeIcon = (ImageView) findViewById(R.id.home_page_iv);
		mMsgFriendIcon = (ImageView) findViewById(R.id.msg_iv);
		mFindIcon = (ImageView) findViewById(R.id.find_iv);
		mUnReadNumTv = (TextView) findViewById(R.id.unread_num);
		mSettingIcon = (ImageView) findViewById(R.id.setting_iv);
		mHomeTv = (TextView) findViewById(R.id.home_page_tv);
		mMsgFriendTv = (TextView) findViewById(R.id.msg_friend_tv);
		mFindTv = (TextView) findViewById(R.id.find_tv);
		mSettingTv = (TextView) findViewById(R.id.setting_tv);
		mHomeBottomLayout.setOnClickListener(this);
		mMsgFriendBottomLayout.setOnClickListener(this);
		mFindBottomLayout.setOnClickListener(this);
		mSettingBottomLayout.setOnClickListener(this);

		mHomeTitleView.findViewById(R.id.search).setOnClickListener(this);

		Fragment tab01 = new HomeFragment();
		Fragment tab02 = new MsgFriendFragment();
		Fragment tab03 = new FindFragment();
		Fragment tab04 = new SettingFragment();
		mFragments.add(tab01);
		mFragments.add(tab02);
		mFragments.add(tab03);
		mFragments.add(tab04);

		mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragments.size();
			}

			@Override
			public Fragment getItem(int position) {
				return mFragments.get(position);
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				super.destroyItem(container, position, object);
				switch (position) {
				case 0:
					VLog.i("homeFrag", "destroyItem:主页");
					break;
				case 1:
					VLog.i("homeFrag", "destroyItem:消息");
					break;
				case 2:
					VLog.i("homeFrag", "destroyItem:发现");
					break;
				case 3:
					VLog.i("homeFrag", "destroyItem:设置");
					break;
				}
			}
		};
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(mFragments.size());// viewPager缓存数量
		mViewPager.setScrollable(false);// 地图页不可左右滑动
		initReconnectView(mHomeTitleView.findViewById(R.id.reconnect_layout));// 初始化重连view
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				resetTabBtn();
				resetTitleView();
				int checkedColorId = getResources().getColor(R.color.dark_blue);
				switch (position) {
				case TAB_HOME_INDEX:
					mHomeTitleView.setVisibility(View.VISIBLE);
					mHomeIcon.setBackgroundResource(R.drawable.home_checked);
					mHomeTv.setTextColor(checkedColorId);
					mViewPager.setScrollable(false);
					initReconnectView(mHomeTitleView.findViewById(R.id.reconnect_layout));
					break;
				case TAB_MSG_FRIEND_INDEX:
					mMsgFriendTitleView.setVisibility(View.VISIBLE);
					mMsgFriendIcon.setBackgroundResource(R.drawable.msg_friend_checked);
					mMsgFriendTv.setTextColor(checkedColorId);
					mViewPager.setScrollable(true);
					initReconnectView(mMsgFriendTitleView.findViewById(R.id.reconnect_layout));
					break;
				case TAB_FIND_INDEX:
					mFindTitleView.setVisibility(View.VISIBLE);
					mFindIcon.setBackgroundResource(R.drawable.find_checked);
					mFindTv.setTextColor(checkedColorId);
					mViewPager.setScrollable(true);
					initReconnectView(mFindTitleView.findViewById(R.id.reconnect_layout));
					break;
				case TAB_SETTING_INDEX:
					mSettingTitleView.setVisibility(View.VISIBLE);
					mSettingIcon.setBackgroundResource(R.drawable.setting_checked);
					mSettingTv.setTextColor(checkedColorId);
					mViewPager.setScrollable(true);
					initReconnectView(mSettingTitleView.findViewById(R.id.reconnect_layout));
					break;
				}
				TabChangedListener.get().triggerOnTabChangedListener(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mViewPager.setCurrentItem(0);// 默认第一个tab
		// mViewPager.setOffscreenPageLimit(0);
		mMsgFriendRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.myfrind_msg) {
					MsgFriendFragChangedListener.get().triggerOnMsgFriendFragChangedListener(0);
				} else if (checkedId == R.id.myfrind_friend) {
					MsgFriendFragChangedListener.get().triggerOnMsgFriendFragChangedListener(1);
				}
			}
		});
	}

	private void resetTitleView() {
		mHomeTitleView.setVisibility(View.GONE);
		mMsgFriendTitleView.setVisibility(View.GONE);
		mFindTitleView.setVisibility(View.GONE);
		mSettingTitleView.setVisibility(View.GONE);
	}

	private void resetTabBtn() {
		mHomeIcon.setBackgroundResource(R.drawable.home);
		mMsgFriendIcon.setBackgroundResource(R.drawable.msg_friend);
		mFindIcon.setBackgroundResource(R.drawable.find);
		mSettingIcon.setBackgroundResource(R.drawable.setting);

		int resetColorId = getResources().getColor(R.color.darker_grey);
		mHomeTv.setTextColor(resetColorId);
		mMsgFriendTv.setTextColor(resetColorId);
		mFindTv.setTextColor(resetColorId);
		mSettingTv.setTextColor(resetColorId);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_page:
			mViewPager.setCurrentItem(TAB_HOME_INDEX);
			break;
		case R.id.msg_friend:
			mViewPager.setCurrentItem(TAB_MSG_FRIEND_INDEX);
			break;
		case R.id.find:
			mViewPager.setCurrentItem(TAB_FIND_INDEX);
			break;
		case R.id.setting:
			mViewPager.setCurrentItem(TAB_SETTING_INDEX);
			break;
		case R.id.search:
			startActivity(new Intent(this, SearchActivity.class));
			overridePendingTransition(R.anim.act_alpha_in, R.anim.act_bg_fake);
			break;
		}
	}

	private OnMsgReceivedListener mOnMsgReceivedListener = new OnMsgReceivedListener() {
		@Override
		public void onMsgReceived(Chat chat, Message msg) {
			if (ChatStatusListener.get().getCurChatUser() != null) {// 聊天中
				return;
			}
			showTotalUnReadNum(true, 1);
		}
	};

	private OnChatStatusListener mOnChatStatusListener = new OnChatStatusListener() {
		@Override
		public void onChat(String withWhom, boolean isStartNotEnd) {
			if (!isStartNotEnd) {
				DBUtil.setAllChatRead(withWhom);
				DefaultThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						int total = DBUtil.getAllUnReadNum();
						showUnread(total);
					}
				}, 1 * 1000);//[depressed]
			}
		}
	};

	private void showTotalUnReadNum(boolean add, int num) {
		final int total;
		String unReadNumTxt = mUnReadNumTv.getText().toString();
		if (TextUtils.isEmpty(unReadNumTxt)) {
			total = DBUtil.getAllUnReadNum();
		} else {
			int origin = Integer.parseInt(unReadNumTxt);
			if (add) {// 增加
				total = origin + num;
			} else {// 删除
				total = origin - num;
			}
		}
		showUnread(total);
	}

	private void showUnread(final int total) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (total == 0) {
					mUnReadNumTv.setVisibility(View.GONE);
					mUnReadNumTv.setText("");
				} else {
					mUnReadNumTv.setVisibility(View.VISIBLE);
					if (total > 99) {
						mUnReadNumTv.setText("99+");
					} else {
						mUnReadNumTv.setText(total + "");
					}
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		XmppManager.get().removeXmppListener(mOnMsgReceivedListener);
		ChatStatusListener.get().removeOnChatStatusListener(mOnChatStatusListener);
		super.onDestroy();
	};

	/**
	 * 连续按两次返回键就退出
	 */
	private long firstTime;

	@Override
	public void onBackPressed() {
		if ((System.currentTimeMillis() - firstTime) > 2000) {
			AndrUtil.makeToast("再按一次退出");
			firstTime = System.currentTimeMillis();
		} else {
			AccManager.get().logout();
			AbsBaseActivity.exitAll();
			AbsBaseActivity.killProcess();
		}
	}

}