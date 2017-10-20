package com.vidmt.telephone.fragments.main;

import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.MapActivity;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.TmpMapManager;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.JsonResult;
import com.vidmt.telephone.utils.JsonUtil;
import com.vidmt.telephone.vos.PointVo;
import com.vidmt.telephone.vos.TraceSegVo;
import com.vidmt.telephone.vos.TraceVo;

public class TraceView implements OnClickListener {
	private static final int DAY_LEN = 7;
	private TextView mDateTv, mStartTimeTv, mEndTimeTv, mDistanceTv;
	private Button mDateBackBtn, mDateForwardBtn, mTimeBackBtn, mTimeForwardBtn;
	private View mNoneView, mTimeView;
	private int mDayOffset;
	private int mTimeOffset;

	private MapActivity mMapActivity;
	private TmpMapManager mMapMgr;
	private String mUid;
	private LoadingDlg mLoadingDlg;
	private List<TraceSegVo> mTraceSegList;

	public TraceView(MapActivity mapActivity, String uid) {
		mMapActivity = mapActivity;
		mUid = uid;
		mMapMgr = TmpMapManager.get(mapActivity);
		View view = mapActivity.findViewById(R.id.trace_show_bar);
		initViews(view);

		mDayOffset = 0;
		mDateTv.setText(DateUtil.getDateStr(0));
		reloadTraceDate(DateUtil.getDateStr(0));
	}

	private void initViews(View view) {
		TextView titleTv = (TextView) view.findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.trace);
		mDateTv = (TextView) view.findViewById(R.id.date);
		mStartTimeTv = (TextView) view.findViewById(R.id.start_time);
		mEndTimeTv = (TextView) view.findViewById(R.id.end_time);
		mDistanceTv = (TextView) view.findViewById(R.id.distance);
		mDateBackBtn = (Button) view.findViewById(R.id.date_back);
		mDateForwardBtn = (Button) view.findViewById(R.id.date_forward);
		mTimeBackBtn = (Button) view.findViewById(R.id.time_back);
		mTimeForwardBtn = (Button) view.findViewById(R.id.time_forward);
		mNoneView = view.findViewById(R.id.none_layout);
		mTimeView = view.findViewById(R.id.time_layout);
		view.findViewById(R.id.back).setOnClickListener(this);
		view.findViewById(R.id.date_back).setOnClickListener(this);
		view.findViewById(R.id.date_forward).setOnClickListener(this);
		view.findViewById(R.id.time_back).setOnClickListener(this);
		view.findViewById(R.id.time_forward).setOnClickListener(this);
	}

	public void show() {
		mMapActivity.findViewById(R.id.trace_show_bar).setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			mMapActivity.finish();
			break;
		case R.id.date_back:
			--mDayOffset;
			if (mDayOffset == -DAY_LEN + 1) {// 六天前
				mDateBackBtn.setVisibility(View.GONE);
			}
			mDateForwardBtn.setVisibility(View.VISIBLE);
			mTimeOffset = 0;
			String dateStrB = DateUtil.getDateStr(mDayOffset);
			mDateTv.setText(dateStrB);
			reloadTraceDate(dateStrB);
			break;
		case R.id.date_forward:
			++mDayOffset;
			if (mDayOffset == 0) {// 今天
				mDateForwardBtn.setVisibility(View.GONE);
			}
			mDateBackBtn.setVisibility(View.VISIBLE);
			mTimeOffset = 0;
			String dateStrF = DateUtil.getDateStr(mDayOffset);
			mDateTv.setText(dateStrF);
			reloadTraceDate(dateStrF);
			break;
		case R.id.time_back:
			if (mTimeOffset == 0) {
				return;
			}
			--mTimeOffset;
			if (mTimeOffset == 0) {
				mTimeBackBtn.setBackgroundResource(R.drawable.trace_time_back_end);
			} else {
				mTimeBackBtn.setBackgroundResource(R.drawable.trace_time_back);
			}
			mTimeForwardBtn.setBackgroundResource(R.drawable.trace_time_forward);
			drawTrace(mTimeOffset);
			break;
		case R.id.time_forward:
			//huawei add;
			if(mTraceSegList == null){
				return;
			}
			if (mTimeOffset == mTraceSegList.size() - 1) {
				return;
			}
			++mTimeOffset;
			if (mTimeOffset == mTraceSegList.size() - 1) {
				mTimeForwardBtn.setBackgroundResource(R.drawable.trace_time_forward_end);
			} else {
				mTimeForwardBtn.setBackgroundResource(R.drawable.trace_time_forward);
			}
			mTimeBackBtn.setBackgroundResource(R.drawable.trace_time_back);
			drawTrace(mTimeOffset);
			break;
		}
	}

	// @OnItemClick(R.id.date_list)
	// private void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// mTraceDateListAdapter.refresh(position);
	// mLoadingDlg.show();
	// mMapMgr.clearOverlays();
	// reloadTraceDate(DateUtil.getDateStr(-position));
	// }

	private void drawTrace(int index) {
		mNoneView.setVisibility(View.GONE);
		mTimeView.setVisibility(View.VISIBLE);
		//huawei add;
		if(mTraceSegList == null){
			return;
		}
		TraceSegVo seg = mTraceSegList.get(index);
		mStartTimeTv.setText(seg.startTime);
		mEndTimeTv.setText(seg.endTime);
		mDistanceTv.setText(seg.distance);
		mMapMgr.clearOverlays();
		mMapMgr.addMarker(seg.startLoc, R.drawable.trace_start_point);
		mMapMgr.addMarker(seg.endLoc, R.drawable.trace_end_point);
		mMapMgr.addOverlay(seg.oo);
		mMapMgr.animateTo(GeoUtil.latlng2Location(seg.startLoc));
	}

	private void reloadTraceDate(final String dateStr) {
		mMapMgr.clearOverlays();
		mLoadingDlg = new LoadingDlg(mMapActivity, R.string.loading);
		mLoadingDlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final TraceVo trace = HttpManager.get().getTrace(mUid, dateStr);
					if (trace == null) {
						MainThreadHandler.makeToast(R.string.no_trace_data);
						return;
					}
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (trace != null) {
								try {
									JsonResult<PointVo> result = JsonUtil.getCorrectJsonResult(trace.traceJson,
											PointVo.class);
									if (result == null) {
										mNoneView.setVisibility(View.VISIBLE);
										mTimeView.setVisibility(View.GONE);
										return;
									}
									List<PointVo> points = result.getArrayData();
									mTraceSegList = GeoUtil.getRoutes(points);
									if (mTraceSegList == null) {
										mNoneView.setVisibility(View.VISIBLE);
										mTimeView.setVisibility(View.GONE);
										return;
									}
									drawTrace(0);
									mTimeBackBtn.setBackgroundResource(R.drawable.trace_time_back_end);
									if (mTraceSegList.size() > 1) {
										mTimeForwardBtn.setBackgroundResource(R.drawable.trace_time_forward);
									} else {
										mTimeForwardBtn.setBackgroundResource(R.drawable.trace_time_forward_end);
									}
								} catch (VidException e) {
									VLog.e("test", e);
									mNoneView.setVisibility(View.VISIBLE);
									mTimeView.setVisibility(View.GONE);
								}
							}
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
				} finally {
					mLoadingDlg.dismiss();
				}
			}
		});
	}
}
