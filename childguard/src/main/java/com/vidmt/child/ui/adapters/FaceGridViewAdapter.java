package com.vidmt.child.ui.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.Const;
import com.vidmt.child.R;

import java.util.List;

public class FaceGridViewAdapter extends BaseAdapter {
	private Activity mContext;
	private List<Integer> faceResIds;
	private List<String> faceStrs;

	public FaceGridViewAdapter(Activity ctx, List<Integer> faceResIds, List<String> faceStrs) {
		mContext = ctx;
		this.faceResIds = faceResIds;
		this.faceStrs = faceStrs;
	}

	@Override
	public int getCount() {
		return faceResIds.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		if (position == getCount() - 1) {
			return -1;
		}
		return faceResIds.get(position);
	}

	public String getFaceChar(int position) {
		if (position == faceResIds.size()) {
			return null;
		}
		return faceStrs.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			// 设置图片n×n显示
			int w = SysUtil.getDisplayMetrics().widthPixels;// w=540,h=960(60)
															// w=1080,h=1920(120)
			imageView.setLayoutParams(new GridView.LayoutParams(w / Const.FACE_COLUMNS, w / Const.FACE_COLUMNS));
			// 设置显示比例类型
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		} else {
			imageView = (ImageView) convertView;
		}
		if (position != getCount() - 1) {
			imageView.setImageResource(faceResIds.get(position));
		} else {// 删除键
			imageView.setImageResource(R.drawable.face_del);
		}
		return imageView;
	}

}