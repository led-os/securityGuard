package com.vidmt.telephone.activities;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.ContactsListAdapter;
import com.vidmt.telephone.ui.adapters.CustomArrayAdapter;
import com.vidmt.telephone.utils.AvatarUtil;
import com.vidmt.telephone.utils.Enums.AccType;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.ContactVo;
import com.vidmt.telephone.vos.ContactVo.RecommendType;

/**
 * 手机联系人页
 */
public class ContactsActivity extends AbsVidActivity implements OnClickListener {
	/** 获取数据库中表字段 **/
	private static final String[] PHONES_PROJECTION = new String[] { Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,
			Phone.CONTACT_ID };
	private static final int PHONES_DISPLAY_NAME_INDEX = 0;  // 联系人显示名称
	private static final int PHONES_NUMBER_INDEX = 1;        // 电话号码
	private static final int PHONES_PHOTO_ID_INDEX = 2;      // 头像ID
	private static final int PHONES_CONTACT_ID_INDEX = 3;    // 联系人的ID

	private ContactsListAdapter mAdapter;
	private ListView mListView;
	private AutoCompleteTextView mSearchAutoTv;
	private List<ContactVo> mAllPhoneContacts;
	private View mLoadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		initTitle();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mSearchAutoTv = (AutoCompleteTextView) findViewById(R.id.search);
		mListView = (ListView) findViewById(R.id.contact_list);
		mLoadingView = findViewById(R.id.loading);
		initData();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.phone_contacts);
		findViewById(R.id.back).setOnClickListener(this);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				List<String> fuids = VidUtil.getAllFriendUids(false);
				try {
					//根据id获得用户信息
					List<User> fusers = HttpManager.get().getMultUser(fuids);
					mAllPhoneContacts = getPhoneContacts(fusers);
					mAllPhoneContacts = getOrderedPhoneContacts();
					final CustomArrayAdapter<String> adapter = new CustomArrayAdapter<String>(ContactsActivity.this,
							android.R.layout.simple_dropdown_item_1line, getPhoneNames());
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							mSearchAutoTv.setAdapter(adapter);
							mAdapter = new ContactsListAdapter(ContactsActivity.this, mAllPhoneContacts);
							mListView.setAdapter(mAdapter);

							mLoadingView.setVisibility(View.GONE);
							mSearchAutoTv.setVisibility(View.VISIBLE);
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
		mSearchAutoTv.setThreshold(1);
		mSearchAutoTv.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(final Editable s) {
				mSearchAutoTv.dismissDropDown();
				MainThreadHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						CustomArrayAdapter<String> adapter = (CustomArrayAdapter<String>) mSearchAutoTv.getAdapter();
						List<String> phoneNames = new ArrayList<String>();
						if(adapter == null){
							return;
						}
						for (int i = 0; i < adapter.getCount(); i++) {
							phoneNames.add(adapter.getItem(i) + "");
						}
						List<ContactVo> filteredContacts = null;
						if (s.length() == 0) {
							filteredContacts = mAllPhoneContacts;
						} else {
							filteredContacts = getFilteredContacts(mAllPhoneContacts, phoneNames);
						}
						mAdapter = new ContactsListAdapter(ContactsActivity.this, filteredContacts);
						mListView.setAdapter(mAdapter);
					}
				}, 1000);
			}
		});
	}

	private List<ContactVo> getOrderedPhoneContacts() {
		List<ContactVo> contacts = VidUtil.getListCopy(mAllPhoneContacts);
		for (int i = 0; i < mAllPhoneContacts.size(); i++) {
			ContactVo contact = contacts.get(i);
			if (contact.recommendType != RecommendType.INVITE) {
				ContactVo ct = mAllPhoneContacts.get(i);
				mAllPhoneContacts.remove(ct);
				mAllPhoneContacts.add(0, ct);
			}
		}
		return mAllPhoneContacts;
	}

	/** 得到手机通讯录联系人信息,如果是朋友则不显示，不是则加到searchNumbers中 **/
	private List<ContactVo> getPhoneContacts(List<User> fusers) {
		final List<ContactVo> contacts = new ArrayList<ContactVo>();
		final List<String> searchNumbers = new ArrayList<String>();
		ContentResolver resolver = getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);// 得到手机号码
				if (!TextUtils.isEmpty(phoneNumber)) {
					phoneNumber = phoneNumber.replace(" ", "");
				} else {
					continue;
				}
				if (phoneNumber.charAt(0) == '0' || phoneNumber.length() < 11) {
					continue;
				}
				phoneNumber = phoneNumber.substring(phoneNumber.length() - 11, phoneNumber.length());
				if (phoneNumber.charAt(0) != '1') {
					continue;
				}
				//huawei add;
				if(AccManager.get().getCurUser() == null){
					continue;
				}
				if (phoneNumber.equals(AccManager.get().getCurUser().account)) {// 移除自己手机号
					continue;
				}
				String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);// 得到联系人名称
				Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);// 得到联系人ID
				Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);// 得到联系人头像ID
				Bitmap contactPhoto = null;// 得到联系人头像Bitamp
				if (photoid > 0) {// photoid 大于0 表示联系人有头像,如果没有给此人设置头像则给他一个默认的
					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactid);
					InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 2;
					contactPhoto = BitmapFactory.decodeStream(input,null,options);// null-pointer
					if (contactPhoto != null) {
						contactPhoto = AvatarUtil.toRoundCorner(contactPhoto);
					}
				}
				final ContactVo contact = new ContactVo();
				contact.name = contactName;
				contact.phone = phoneNumber;
				contact.avatar = contactPhoto;
				//huawei add;
				contacts.add(contact);
				User user = getUserByPhone(fusers, phoneNumber);
				boolean isFriend = user != null;
				if (isFriend) {
					contact.uid = user.uid;
					contact.recommendType = RecommendType.FRIEND;
					if (contactPhoto == null) {
						contact.avatarUri = user.avatarUri;
						if (contact.avatarUri != null && mAdapter != null) {
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									mAllPhoneContacts = getOrderedPhoneContacts();
									mAdapter.notifyDataSetChanged();
								}
							});
						}
					}
				} else {

					searchNumbers.add(phoneNumber);
				}
			}

			//huawei change for high efficiency
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						//联系人中不是朋友，查询是否在用软件
						List<User> users = HttpManager.get().getUserMatchPhones(searchNumbers);
						for (User user : users) {
							if(user.account == null){
								continue;
							}
							//huawei change for ConcurrentModificationException;
							//List<ContactVo> tmpContacts = VidUtil.getListCopy(contacts);
							List<ContactVo> tmpContacts = new ArrayList<ContactVo>(contacts);
							//for(ContactVo contact : contacts){
							for(ContactVo contact : tmpContacts){
								if(contact.phone.equals(user.account)){
									contact.recommendType = RecommendType.ADD;
									contact.uid = user.uid;
									if (contact.avatar == null) {
										contact.avatarUri = user.avatarUri;
									}
									if (contact.avatarUri != null && mAdapter != null) {
										MainThreadHandler.post(new Runnable() {
											@Override
											public void run() {
												mAllPhoneContacts = getOrderedPhoneContacts();
												mAdapter.notifyDataSetChanged();
											}
										});
									}
								}
							}
						}

					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});

			try {
				phoneCursor.close();
			} catch (IllegalStateException e) {
				VLog.e("test", "cursor close err:" + e);
			} finally {
				phoneCursor = null;
			}
		}
		return contacts;
	}

	//获得此人的朋友，看自己是否在其中
	private User getUserByPhone(List<User> users, String phone) {
		if (users == null) {
			return null;
		}
		for (User user : users) {
			if(user.account == null){
				continue;
			}
			if (user.accType.equals(AccType.PHONE.name()) && user.account.equals(phone)) {
				return user;
			}
		}
		return null;
	}

	private List<String> getPhoneNames() {
		List<String> phoneNames = new ArrayList<String>();
		for (ContactVo contact : mAllPhoneContacts) {
			phoneNames.add(contact.name);
		}
		return phoneNames;
	}

	private List<ContactVo> getFilteredContacts(List<ContactVo> contacts, List<String> names) {
		List<ContactVo> contactList = new ArrayList<ContactVo>();
		for (String name : names) {
			for (ContactVo contact : contacts) {
				if (contact.name.equals(name)) {
					contactList.add(contact);
					break;
				}
			}
		}
		return contactList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		}
	}

}
