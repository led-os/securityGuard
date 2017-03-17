package com.vidmt.child.ui.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vidmt.child.R;

import java.util.List;

public class SportsDateListAdapter extends BaseAdapter {
	private Activity mActivity;
	private List<String> mDateList;
	private int mCheckedPos;
	
	public SportsDateListAdapter(Activity act, List<String> dateList, int checkedPos) {
		mActivity = act;
		mDateList = dateList;
		mCheckedPos = checkedPos;
	}

	@Override
	public int getCount() {
		return mDateList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDateList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setCheckedPos(int position) {
		mCheckedPos = position;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView dateTv;
		if (convertView == null) {
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.sports_date_list_item, null);
			dateTv = (TextView) convertView.findViewById(R.id.date);
			convertView.setTag(dateTv);
		} else {
			dateTv = (TextView) convertView.getTag();
		}
		dateTv.setText(mDateList.get(position));
		if (position == mCheckedPos) {
			convertView.setBackgroundResource(R.color.pink);
		} else {
			convertView.setBackgroundResource(R.color.white);
		}
		return convertView;
	}
	
}
