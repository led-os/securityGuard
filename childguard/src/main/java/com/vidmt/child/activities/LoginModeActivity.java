package com.vidmt.child.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_login_mode)
public class LoginModeActivity extends AbsVidActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		requestPermissions();

	}

	@OnClick({ R.id.login_baby, R.id.login_parent })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_baby:
			SysUtil.savePref(PrefKeyConst.PREF_IS_BABY_CLIENT, true);
			break;
		case R.id.login_parent:
			SysUtil.savePref(PrefKeyConst.PREF_IS_BABY_CLIENT, false);
			break;
		}
		startActivity(new Intent(this, LoginActivity.class));
	}
	private void requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			List<String> permissionList = new ArrayList<>();
			if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
				permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
			}
			if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
				permissionList.add(android.Manifest.permission.RECORD_AUDIO);
			}
			if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
				permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
				permissionList.add(android.Manifest.permission.CAMERA);
			}
			if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
				permissionList.add(android.Manifest.permission.READ_SMS);
			}
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
				if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.GET_ACCOUNTS)!= PackageManager.PERMISSION_GRANTED){
					permissionList.add(android.Manifest.permission.GET_ACCOUNTS);
				}
				if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
					permissionList.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
				}

				if(ContextCompat.checkSelfPermission(LoginModeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
					permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
				}
			}

			if (!permissionList.isEmpty()) {
				String[] strings = permissionList.toArray(new String[permissionList.size()]);
				ActivityCompat.requestPermissions(LoginModeActivity.this,strings,1);
			}
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case 1:
				if(grantResults.length>0){
					for (int result:grantResults){
						if(result!=PackageManager.PERMISSION_GRANTED){
							Toast.makeText(this,"授权未被同意",Toast.LENGTH_SHORT).show();
						}
					}
				}
				break;
		}
	}
}
