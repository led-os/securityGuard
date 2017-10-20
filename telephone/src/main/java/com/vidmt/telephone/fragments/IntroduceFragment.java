package com.vidmt.telephone.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.LoginActivity;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.activities.RegisterActivity;

public class IntroduceFragment extends Fragment implements OnPageChangeListener {
	private ViewPagerAdapter viewPagerAdapter;
	private List<LinearLayout> pointList;
	private int[] pagerViewIds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pagerViewIds = new int[] { R.layout.introduce_page1, R.layout.introduce_page2,
				R.layout.introduce_page3, R.layout.introduce_enter };// 电子围栏去掉:R.layout.introduce_page4
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewPager viewPager = new ViewPager(getActivity());
		List<View> pagerInnerViews = new ArrayList<View>();
		for (int i = 0; i < pagerViewIds.length; i++) {
			View view = inflater.inflate(pagerViewIds[i], container, false);
			pagerInnerViews.add(view);
			if (i == pagerViewIds.length - 1) {
//				view.findViewById(R.id.guest_login).setOnClickListener(listener);
				view.findViewById(R.id.login).setOnClickListener(listener);
				view.findViewById(R.id.register).setOnClickListener(listener);
			}
		}
		viewPagerAdapter = new ViewPagerAdapter(pagerInnerViews);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setOnPageChangeListener(this);

		return viewPager;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initPointView(pagerViewIds.length);
	}

	private void initPointView(int pages) {
		if (pages <= 1) {
			return;
		}
		LinearLayout pointLayout = (LinearLayout) getActivity().findViewById(R.id.point_layout);
		pointList = new ArrayList<LinearLayout>(pages);
		for (int i = 0; i < pages; i++) {
			ImageView iv = new ImageView(getActivity());
			LinearLayout ly = new LinearLayout(getActivity());
			ly.setLayoutParams(new LayoutParams(PixUtil.dp2px(10), PixUtil.dp2px(10)));
			ly.setPadding(PixUtil.dp2px(2), PixUtil.dp2px(2), PixUtil.dp2px(2), PixUtil.dp2px(2));
			ly.addView(iv);
			pointList.add(ly);
			if (i == 0) {
				iv.setBackgroundResource(R.drawable.point_selected);
			} else {
				iv.setBackgroundResource(R.drawable.point_no_select);
			}
			pointLayout.addView(ly);
		}
	}

	private void changePointView(int position) {
		if (pointList != null) {
			for (int i = 0; i < pointList.size(); i++) {
				pointList.get(i).getChildAt(0).setBackgroundResource(R.drawable.point_no_select);
				if (position == i) {
					pointList.get(i).getChildAt(0).setBackgroundResource(R.drawable.point_selected);
				}
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		changePointView(arg0);
	}

	/*
	 * ViewPager适配器
	 */
	public class ViewPagerAdapter extends PagerAdapter {
		private List<View> views;

		public ViewPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			if (views != null) {
				return views.size();
			}
			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return (arg0 == arg1);
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(views.get(position), 0);
			return views.get(position);
		}
	}

	private OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
//			case R.id.guest_login:
//				intent = new Intent(getActivity(), MainActivity.class);
//				break;
			case R.id.login:
				intent = new Intent(getActivity(), LoginActivity.class);
				break;
			case R.id.register:
				intent = new Intent(getActivity(), RegisterActivity.class);
				break;
			}
			startActivity(intent);
			getActivity().finish();
		}
	};

}
