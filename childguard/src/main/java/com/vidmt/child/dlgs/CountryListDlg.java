package com.vidmt.child.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;
import com.vidmt.child.R;
import com.vidmt.child.ui.adapters.CountryListAdapter;
import com.vidmt.child.ui.adapters.CountryListAdapter.Holder;

public class CountryListDlg extends BaseDialog {
	@ViewInject(R.id.list)
	private ListView mListView;
	private CountryListAdapter adapter;
	private DialogClickListener dialogClickListener;

	public CountryListDlg(Activity context, CountryListAdapter adapter, DialogClickListener dialogClickListener) {
		super(context, R.layout.dlg_country_list);
		this.adapter = adapter;
		this.dialogClickListener = dialogClickListener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mListView.setAdapter(adapter);
	}
	
	@OnItemClick(R.id.list)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		dismiss();
		Holder holder = (Holder) view.getTag();
		String country = holder.countryTv.getText().toString();
		String countryNO = holder.areaNOTv.getText().toString();
		countryNO = countryNO.replace("(", "").replace(")", "");
		Bundle bundle = new Bundle();
//		bundle.putString(ExtraConst.EXTRA_COUNTRY_NAME, country);
//		bundle.putString(ExtraConst.EXTRA_COUNTRY_AREA_NO, countryNO);
		dialogClickListener.onOK(bundle);
	}
	
}
