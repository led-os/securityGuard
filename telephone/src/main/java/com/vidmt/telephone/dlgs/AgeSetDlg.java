package com.vidmt.telephone.dlgs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.App;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;

public class AgeSetDlg extends BaseDialog {
	private ListView mListView;
	private static final int MAX_AGE = 120;
	private List<Integer> mAgeList;
	private int mDefAge;
	private DialogClickListener mListener;

	public AgeSetDlg(Activity context, int defAge, DialogClickListener listener) {
		super(context, R.layout.dlg_age_set);
		mDefAge = defAge;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mListView = (ListView) findViewById(R.id.list);
		mAgeList = new ArrayList<Integer>();
		for (int i = 0; i < MAX_AGE; i++) {
			mAgeList.add(i);
		}
		mListView.setAdapter(mAdapter);
		int W = SysUtil.getDisplayMetrics().widthPixels / 2;
		int H = SysUtil.getDisplayMetrics().heightPixels / 2;
		LayoutParams params = new LinearLayout.LayoutParams(W, H);
		mListView.setLayoutParams(params);
		mListView.setSelection(mDefAge - 2);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putString(ExtraConst.EXTRA_TXT_CONTENT, mAgeList.get(position) + "");
				mListener.onOK(bundle);
			}
		});
	}

	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.age_set_list_item, null);
				holder.tv = (TextView) convertView.findViewById(R.id.tv);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			if (mAgeList.get(position) == mDefAge) {
				convertView.setBackgroundColor(App.get().getResources().getColor(R.color.light_blue));
			} else {
				convertView.setBackgroundResource(R.drawable.selector_white_blue);
			}
			holder.tv.setText(mAgeList.get(position) + "");
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return mAgeList.get(position);
		}

		@Override
		public int getCount() {
			return mAgeList.size();
		}
	};

	private class Holder {
		TextView tv;
	}

}
