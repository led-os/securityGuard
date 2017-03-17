package com.vidmt.child.activities.main;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.R;
import com.vidmt.child.activities.MapActivity;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.TmpMapManager;
import com.vidmt.child.utils.DateUtil;
import com.vidmt.child.utils.GeoUtil;
import com.vidmt.child.utils.JsonResult;
import com.vidmt.child.utils.JsonUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.PointVo;
import com.vidmt.child.vos.TraceSegVo;
import com.vidmt.xmpp.exts.TraceIQ;

import java.util.List;

public class TraceView implements OnClickListener {
	private TextView mDateTv, mStartTimeTv, mEndTimeTv, mDistanceTv;
	private Button mDateBackBtn, mDateForwardBtn, mTimeBackBtn, mTimeForwardBtn;
	private View mNoneView, mTimeView;
	private int mDayOffset;
	private int mTimeOffset;
	private int TRACE_NUM;

	private MapActivity mMapActivity;
	private TmpMapManager mMapMgr;
	private LoadingDlg mLoadingDlg;
	private List<TraceSegVo> mTraceSegList;

	public TraceView(MapActivity mapActivity) {
		mMapActivity = mapActivity;
		mMapMgr = TmpMapManager.get(mapActivity);
		View view = mapActivity.findViewById(R.id.trace_show_bar);
		initViews(view);

		mDayOffset = 0;
		mDateTv.setText(DateUtil.getDateStr(0));
		reloadTraceDate(DateUtil.getDateStr(0));
	}

	private void initViews(View view) {
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
		view.findViewById(R.id.date_back).setOnClickListener(this);
		view.findViewById(R.id.date_forward).setOnClickListener(this);
		view.findViewById(R.id.time_back).setOnClickListener(this);
		view.findViewById(R.id.time_forward).setOnClickListener(this);

		TRACE_NUM = UserUtil.getLvl().traceNum;
		if (TRACE_NUM == 1) {
			mDateBackBtn.setVisibility(View.GONE);
		} else {
			mDateBackBtn.setVisibility(View.VISIBLE);
		}
	}

	public void show() {
		mMapActivity.findViewById(R.id.trace_show_bar).setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.date_back:
			--mDayOffset;
			if (mDayOffset == -TRACE_NUM + 1) {// (TRACE_NUM-1)天前
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

	private void drawTrace(int index) {
		mNoneView.setVisibility(View.GONE);
		mTimeView.setVisibility(View.VISIBLE);
		TraceSegVo seg = mTraceSegList.get(index);
		mStartTimeTv.setText(seg.startTime);
		mEndTimeTv.setText(seg.endTime);
		mDistanceTv.setText(seg.distance);
		mMapMgr.clearOverlays();
		mMapMgr.addMarker(seg.startLoc, R.drawable.trace_start_point);
		mMapMgr.addMarker(seg.endLoc, R.drawable.trace_end_point);
		mMapMgr.addOverlay(seg.oo);
		mMapMgr.animateTo(seg.startLoc);
	}

	private void reloadTraceDate(final String dateStr) {
		mMapMgr.clearOverlays();
		mLoadingDlg = new LoadingDlg(mMapActivity, R.string.loading);
		mLoadingDlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					TraceIQ traceIq = AccManager.get().getTraceData(dateStr);
					if (traceIq != null && traceIq.jid != null) {// 有足迹数据
						JsonResult<PointVo> result = JsonUtil.getCorrectJsonResult(traceIq.traceJson, PointVo.class);
						final List<PointVo> points = result.getArrayData();
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								if (points == null || points.size() == 0) {
									mNoneView.setVisibility(View.VISIBLE);
									mTimeView.setVisibility(View.GONE);
									return;
								}
								mTraceSegList = GeoUtil.getRoutes(points);
								if (mTraceSegList == null || mTraceSegList.size() == 0) {
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
							}
						});
					} else {
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								mNoneView.setVisibility(View.VISIBLE);
								mTimeView.setVisibility(View.GONE);
							}
						});
						// MainThreadHandler.makeToast(R.string.no_trace_data);
					}
				} catch (VidException e) {
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							mNoneView.setVisibility(View.VISIBLE);
							mTimeView.setVisibility(View.GONE);
						}
					});
					VLog.e("test", e);
				} finally {
					mLoadingDlg.dismiss();
				}
			}
		});
	}

}
