package com.vidmt.child.ui.views;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.child.R;
import com.vidmt.child.activities.EfenceActivity;
import com.vidmt.child.activities.main.MainController;
import com.vidmt.child.utils.VidUtil;

public class EfencePopWindow extends PopupWindow implements OnClickListener {
	private EfenceActivity mActivity;
	private String mEfenceName;
	private int mX, mY;

	public EfencePopWindow(EfenceActivity act, View clickedView) {
		mActivity = act;
		RelativeLayout bgLayout = (RelativeLayout) clickedView;
		TextView fenceNameTv = (TextView) bgLayout.getChildAt(0);
		mEfenceName = fenceNameTv.getText().toString();
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setOutsideTouchable(true);
		View view = act.getLayoutInflater().inflate(R.layout.efence_pop_window, null);
		view.findViewById(R.id.del_efence).setOnClickListener(this);
		setWidth(VidUtil.getViewMeasure(view)[0]);
		setHeight(VidUtil.getViewMeasure(view)[1]);
		setContentView(view);
		int[] xy = VidUtil.getAbsolutePosition(clickedView);
		int[] wh = VidUtil.getViewMeasure(clickedView);
		int[] popWh = VidUtil.getViewMeasure(view);
		int yOffset = PixUtil.dp2px(8);
		mX = xy[0] + wh[0] - popWh[0] / 2;
		mY = xy[1] - popWh[1] + yOffset;
		setAnimationStyle(R.style.Animations_PopUpMenu_Right);
	}

	public void show() {
		ViewGroup root = (ViewGroup) mActivity.findViewById(R.id.root);
		showAtLocation(root, Gravity.NO_GRAVITY, mX, mY);
		update();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.del_efence:
			MainController.get().removeEfenceByName(mEfenceName);
			dismiss();
			mActivity.reloadFenceList();
			break;
		}
	}

}
