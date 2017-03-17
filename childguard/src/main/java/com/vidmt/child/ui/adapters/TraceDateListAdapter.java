package com.vidmt.child.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vidmt.child.R;
import com.vidmt.child.utils.UserUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TraceDateListAdapter extends BaseAdapter {
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat DF = new SimpleDateFormat("MM-dd");
	private long mCurDateInMills = new Date().getTime();
	private Activity mActivity;
	private int mSelectedItem;

	public TraceDateListAdapter(Activity act, int selectedItem) {
		mActivity = act;
		mSelectedItem = selectedItem;
	}

	@Override
	public int getCount() {
		return UserUtil.getLvl().traceNum;
	}

	@Override
	public Object getItem(int position) {
		return DF.format(new Date(mCurDateInMills - position * 24 * 60 * 60 * 1000));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView dateTv;
		if (convertView == null) {
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.trace_date_list_item, null);
			dateTv = (TextView) convertView.findViewById(R.id.date);
			convertView.setTag(dateTv);
		} else {
			dateTv = (TextView) convertView.getTag();
		}
		dateTv.setText(getItem(position).toString());
		if (position == mSelectedItem) {
			dateTv.setBackgroundResource(R.color.pink);
		} else {
			dateTv.setBackgroundResource(R.color.dark_grey);
		}
		return convertView;
	}

	public void refresh(int selectedItem) {
		mSelectedItem = selectedItem;
		notifyDataSetChanged();
	}

}
