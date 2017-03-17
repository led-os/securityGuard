package com.vidmt.child.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.R;

import java.util.List;

public class CountryListAdapter extends BaseAdapter {
	private List<Character> countrySortList;
	private List<String> countryNameList;
	private List<String> countryAreaNOList;

	public CountryListAdapter(List<Character> countrySortList, List<String> countryNameList,
			List<String> countryAreaNOList) {
		this.countrySortList = countrySortList;
		this.countryNameList = countryNameList;
		this.countryAreaNOList = countryAreaNOList;
	}

	@Override
	public int getCount() {
		return countryNameList.size();
	}

	@Override
	public Object getItem(int position) {
		return countryNameList.get(position);
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
			convertView = SysUtil.inflate(R.layout.country_list_item);
			holder.sortTv = (TextView) convertView.findViewById(R.id.sort);
			holder.countryTv = (TextView) convertView.findViewById(R.id.country_name);
			holder.areaNOTv = (TextView) convertView.findViewById(R.id.area_no);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.sortTv.setText(countrySortList.get(position) + "");
		holder.countryTv.setText(countryNameList.get(position));
		holder.areaNOTv.setText("(" + countryAreaNOList.get(position) + ")");
		return convertView;
	}

	public class Holder {
		public TextView sortTv;
		public TextView countryTv;
		public TextView areaNOTv;
	}

}
