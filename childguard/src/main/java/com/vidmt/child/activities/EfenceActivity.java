package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Config;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.AdManager;
import com.vidmt.child.ui.adapters.EfenceGridViewAdapter;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.FenceVo;
import com.vidmt.xmpp.exts.FenceIQ;

import java.util.ArrayList;
import java.util.List;

public class EfenceActivity extends AbsVidActivity implements OnClickListener {
	private LoadingDlg mLoadingDlg;
	private GridView mEfenceGv;
	private EfenceGridViewAdapter mEfenceAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_efence);
		initTitle();
		findViewById(R.id.back).setOnClickListener(this);

		if (!UserUtil.getLvl().noAd && !Config.DEBUG) {
			AdManager.get().showInterstitialAd(this);
		}

		mEfenceGv = (GridView) findViewById(R.id.efence_gridview);

		mLoadingDlg = new LoadingDlg(this, R.string.loading);
		mLoadingDlg.show();
		reloadFenceList();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.add_efence);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	public void reloadFenceList() {
		FenceIQ fenceIq = AccManager.get().getFence();
		List<FenceVo> fenceList = new ArrayList<FenceVo>();
		if (fenceIq != null && fenceIq.jid != null) {// 有围栏数据
			for (int i = 0; i < fenceIq.nameList.size(); i++) {
				FenceVo fence = new FenceVo();
				fence.jid = fenceIq.jid;
				fence.name = fenceIq.nameList.get(i);
				fence.lat = fenceIq.latList.get(i);
				fence.lon = fenceIq.lonList.get(i);
				fence.radius = fenceIq.radiusList.get(i);
				fenceList.add(fence);
			}
		}
		mEfenceAdapter = new EfenceGridViewAdapter(EfenceActivity.this, fenceList);
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mEfenceGv.setAdapter(mEfenceAdapter);
				mLoadingDlg.dismiss();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		}
	}

}
