package com.vidmt.child.ui.views;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.activities.SportsActivity;
import com.vidmt.child.ui.adapters.SportsDateListAdapter;
import com.vidmt.child.utils.VidUtil;

import java.util.List;

public class SportsPopWindow extends PopupWindow {
	private SportsActivity mActivity;
	private int mX, mY;
	private SportsDateListAdapter mAdapter;
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			mAdapter.setCheckedPos(position);
			mActivity.setDate(mAdapter.getItem(position).toString());

			MainThreadHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					SportsPopWindow.this.dismiss();
				}
			}, 60);
		}
	};
	
	public SportsPopWindow(SportsActivity act, List<String> dateList) {
		mActivity = act;
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setOutsideTouchable(true);
		View view = act.getLayoutInflater().inflate(R.layout.sports_pop_window, null);
		ListView dateListView = (ListView) view.findViewById(R.id.date_list);
		mAdapter = new SportsDateListAdapter(mActivity, dateList, 0);

		View itemView = SysUtil.inflate(R.layout.sports_date_list_item);
		int itemHeight = VidUtil.getViewMeasure(itemView)[1];
		int height = (dateListView.getDividerHeight() + itemHeight) * mAdapter.getCount();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(PixUtil.dp2px(200), height);
		lp.setMargins(10, 0, 10, 0);
		dateListView.setLayoutParams(lp);

		dateListView.setAdapter(mAdapter);
		dateListView.setOnItemClickListener(mOnItemClickListener);

		setWidth(VidUtil.getViewMeasure(view)[0]);
		setHeight(VidUtil.getViewMeasure(view)[1]);
		setContentView(view);
		int[] popWh = VidUtil.getViewMeasure(view);
		int screenWidth = SysUtil.getDisplayMetrics().widthPixels;
		mX = screenWidth/2 - popWh[0] / 2;
		mY = PixUtil.dp2px(70);
		setAnimationStyle(R.style.Animations_PopUpMenu_Center);
	}
	
	public void show(){
		ViewGroup root = (ViewGroup) mActivity.findViewById(R.id.root);
		showAtLocation(root, Gravity.NO_GRAVITY, mX, mY);
		update();
	}
	
}
