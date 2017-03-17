package com.vidmt.acmn.abs;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GestureListener extends SimpleOnGestureListener implements OnTouchListener {
	private GestureDetector mGestureDetector;
	private static final int MODE_SINGLE_FINGER = 1;
	private static final int MODE_DOUBALE_FINEGER = 2;
	private int mode_finger = MODE_SINGLE_FINGER;

	public GestureListener(Context context) {
		mGestureDetector = new GestureDetector(context, this);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent evt) {

		int action = evt.getActionMasked();
		String actionS = debugAction(action);
		if (actionS != null && actionS.length() > 0) {
			Log.i("test", debugAction(action) + " pntCount=" + evt.getPointerCount());
		}

		switch (action) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (evt.getPointerCount() > 1) {
				mode_finger = MODE_DOUBALE_FINEGER;
				onDoubleFinger(evt);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (mode_finger == MODE_DOUBALE_FINEGER) {
				onDoubleFinger(evt);
				mode_finger = MODE_SINGLE_FINGER;
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (mode_finger == MODE_DOUBALE_FINEGER) {
				onDoubleFinger(evt);
				return false;
			}
		case MotionEvent.ACTION_CANCEL:
			if (mode_finger == MODE_DOUBALE_FINEGER) {
				onDoubleFinger(evt);
				mode_finger = MODE_SINGLE_FINGER;
			}
			break;
		}

		return mGestureDetector.onTouchEvent(evt);
	}

	protected void onDoubleFinger(MotionEvent evt) {
	};

	private static final String debugAction(int action) {
		StringBuilder sb = new StringBuilder();
		if (action == MotionEvent.ACTION_DOWN) {
			sb.append("down,");
		}
		if (action == MotionEvent.ACTION_UP) {
			sb.append("up,");
		}
		if (action == MotionEvent.ACTION_MOVE) {
			sb.append("move,");
		}
		if (action == MotionEvent.ACTION_POINTER_DOWN) {
			sb.append("pntdown,");
		}
		if (action == MotionEvent.ACTION_POINTER_UP) {
			sb.append("pntup,");
		}
		int cnt = sb.length();
		if (cnt > 0) {
			sb.replace(cnt - 1, cnt, "\n");
		}
		return sb.toString();
	}
}
