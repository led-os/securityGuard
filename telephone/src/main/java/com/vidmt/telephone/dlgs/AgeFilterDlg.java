package com.vidmt.telephone.dlgs;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;

public class AgeFilterDlg extends BaseDialog {
	private ListView mListView;
	private List<String> mList;
	private int mSelectedIndex;
	private DialogClickListener mListener;

	public AgeFilterDlg(Activity context, List<String> list, int selectedIndex, DialogClickListener listener) {
		super(context, R.layout.dlg_filter_age);
		mList = list;
		mSelectedIndex = selectedIndex;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		int W = SysUtil.getDisplayMetrics().widthPixels / 2;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(W, LayoutParams.WRAP_CONTENT);
		mListView.setLayoutParams(params);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle bundle = new Bundle();
				bundle.putInt(ExtraConst.EXTRA_POSITION, position);
				mListener.onOK(bundle);
			}
		});
	}

	private BaseAdapter mAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout ll = new LinearLayout(mActivity);
			ll.setLayoutParams(new LayoutParams(PixUtil.dp2px(200), LayoutParams.WRAP_CONTENT));
			ll.setGravity(Gravity.CENTER);
			TextView tv = new TextView(mActivity);
			tv.setHeight(PixUtil.dp2px(30));
			tv.setTextSize(20);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			String str = (String) getItem(position);
			tv.setText(str);
			if (mSelectedIndex == position) {
				ll.setBackgroundColor(Color.BLUE);
				tv.setTextColor(Color.WHITE);
			} else {
				ll.setBackgroundResource(R.drawable.selector_white_blue);
				tv.setTextColor(Color.BLACK);
			}
			ll.addView(tv);
			return ll;
		}
	};
}
