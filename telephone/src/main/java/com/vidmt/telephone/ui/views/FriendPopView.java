package com.vidmt.telephone.ui.views;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.R;
import com.vidmt.telephone.fragments.FriendFragment;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.VidUtil;

public class FriendPopView extends PopupWindow implements OnClickListener {
	private int x, y;
	private View view;
	private String uid;
	private FriendFragment frag;

	public FriendPopView(Activity ctx, View v, String uid, FriendFragment frag) {
		super(ctx);
		this.view = v;
		this.uid = uid;
		this.frag = frag;
		setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		View view = ctx.getLayoutInflater().inflate(R.layout.friend_item_popview, null);
		setWidth(VidUtil.getViewMeasure(view)[0]);
		setHeight(VidUtil.getViewMeasure(view)[1]);
		setOutsideTouchable(true);
		setContentView(view);
		int[] xy = VidUtil.getAbsolutePosition(v);
		int[] wh = VidUtil.getViewMeasure(v);
		int[] popWh = VidUtil.getViewMeasure(view);
		int yOffset = 10;
		x = SysUtil.getDisplayMetrics().widthPixels / 2 - popWh[0] / 2;
		y = xy[1] + wh[1] + yOffset;
		setAnimationStyle(R.style.Animations_PopUpMenu_Center);
		view.findViewById(R.id.delete).setOnClickListener(this);
	}

	public void show() {
		showAtLocation(this.view, Gravity.NO_GRAVITY, x, y);
		update();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delete:
			AccManager.get().deleteFriend(uid);
			break;
		}
		dismiss();
	}

}
