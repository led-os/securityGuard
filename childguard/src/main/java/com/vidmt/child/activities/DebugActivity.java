package com.vidmt.child.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.VersionInfo;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.App;
import com.vidmt.child.Config;
import com.vidmt.child.R;

@ContentView(R.layout.activity_debug)
public class DebugActivity extends AbsVidActivity {
	@ViewInject(R.id.debug)
	private TextView mDebugTv;
	@ViewInject(R.id.build_time)
	private TextView mBuildTimeTv;
	@ViewInject(R.id.pkg)
	private TextView mPkgTv;
	@ViewInject(R.id.version)
	private TextView mVerTv;
	@ViewInject(R.id.bdmap_version)
	private TextView mMapVerTv;
	@ViewInject(R.id.bdloc_version)
	private TextView mLocVerTv;
	@ViewInject(R.id.alipay_version)
	private TextView mPayVerTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		mDebugTv.setText("debug:" + Config.DEBUG);
		mBuildTimeTv.setText("build time:" + Config.BUILD_TIME);
		mPkgTv.setText("package:" + App.get().getPackageName());
		mVerTv.setText("version:" + SysUtil.getPkgInfo().versionName);
		mMapVerTv.setText("map version:" + VersionInfo.VERSION_INFO);
		mLocVerTv.setText("loc version:" + new LocationClient(this).getVersion());
		mPayVerTv.setText("pay version:" + new PayTask(this).getVersion());
	}

}
