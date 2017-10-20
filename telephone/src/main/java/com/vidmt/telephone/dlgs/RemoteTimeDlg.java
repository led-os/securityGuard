package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.R;

public class RemoteTimeDlg extends BaseDialog {
	private int mCount = Const.REMOTE_RECORD_TIME_LEN;

	public RemoteTimeDlg(Activity context) {
		super(context, R.layout.dlg_remote_time);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final TextView tickerTv = (TextView) mView.findViewById(R.id.time_ticker);
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mCount == -1) {
					mCount = Const.REMOTE_RECORD_TIME_LEN;
					RemoteTimeDlg.this.dismiss();
					return;
				}
				tickerTv.setText(mCount-- + "");
				MainThreadHandler.postDelayed(this, 1 * 1000);
			}
		});
	}

}
