package com.vidmt.telephone.activities;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.FileStorage;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.main.ChatController;
import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.listeners.ChatPhotoSendListener;
import com.vidmt.telephone.utils.AvatarUtil;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 选择照片选项，类似dialog，有相机和图库，取三个选择
 */
public class ChoosePicActivity extends AbsVidActivity implements OnClickListener {
	private Dialog mPicDialog;
	private File mTakePicFile;
	public static final int ACT_RES_CODE = 1;
	private static final int PHOTO_ZOOM_RESULT_CODE = 2;
	private static final int PHOTO_ZOOM_DIMENSITION = 96;
	private boolean mIsWholePhoto;
	private boolean mIsDlgCanceledNotByCancelBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout picLayout = (LinearLayout) SysUtil.inflate(R.layout.dialog_choose_pic);
		picLayout.findViewById(R.id.take_pic).setOnClickListener(this);// 拍照
		picLayout.findViewById(R.id.choose_from).setOnClickListener(this);// 从相册选择
		picLayout.findViewById(R.id.cancel).setOnClickListener(this);// 取消

		mPicDialog = VidUtil.getBottomDialog(this, picLayout);
		mPicDialog.show();
		mPicDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mIsDlgCanceledNotByCancelBtn) {// 防止BACK键时的问题
					return;
				}
				finish();
			}
		});
		mIsWholePhoto = getIntent().getBooleanExtra(ExtraConst.EXTRA_IS_WHOLE_PHOTO, false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.take_pic:
			mIsDlgCanceledNotByCancelBtn = true;
			try {
				Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				String photoName = "tmp" + String.valueOf(System.currentTimeMillis()) + ".jpg";
				mTakePicFile = new File(VLib.getSdcardDir(), FileStorage.buildCachePath(photoName));
				takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTakePicFile));
				startActivityForResult(takePicIntent, 0);
			} catch (Throwable e) {
				VLog.e("test", e);
			}
			break;
		case R.id.choose_from:
			mIsDlgCanceledNotByCancelBtn = true;
			try{
				Intent chooseIntent = new Intent();
				chooseIntent.setType("image/*");
				chooseIntent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(chooseIntent, 0);
			}catch(ActivityNotFoundException e){
				MainThreadHandler.makeToast("图库选择异常，请通过拍照上传。");
				VidUtil.fLog("test", e.getMessage());
			}
			break;
		case R.id.cancel:
			finish();
			break;
		}
		mPicDialog.cancel();
		mPicDialog.dismiss();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED) {
			if (mTakePicFile != null && mTakePicFile.exists()) {
				mTakePicFile.delete();
			}
			finish();
			return;
		}
		if (data == null && mTakePicFile != null) {// 拍照获取
			if (mIsWholePhoto) {
				File tmpFile = AvatarUtil.getCompressedImgFile(mTakePicFile);
				ChatRecord chatRecord = ChatController.get().sendImage(tmpFile);
				ChatPhotoSendListener.get().triggerOnChatPhotoSendListener(chatRecord);
				tmpFile.delete();
				finish();
			} else {
				startPhotoZoom(Uri.fromFile(mTakePicFile));
			}
		} else if (data != null && data.getData() != null) {// 从相册获取
			String filePath = VidUtil.getFilePath(this, data.getData());
			if (mIsWholePhoto) {
				File tmpFile = AvatarUtil.getCompressedImgFile(new File(filePath));
				ChatRecord chatRecord = ChatController.get().sendImage(tmpFile);
				ChatPhotoSendListener.get().triggerOnChatPhotoSendListener(chatRecord);
				tmpFile.delete();
				finish();
			} else {
				startPhotoZoom(Uri.fromFile(new File(filePath)));
			}
		}
		if (requestCode == PHOTO_ZOOM_RESULT_CODE) {
			if (mTakePicFile != null && mTakePicFile.exists()) {
				mTakePicFile.delete();
			}
			//huawei add;
			if(data == null){
				finish();
				return;
			}
			//huawei add end;
			Bundle extras = data.getExtras();
			if (extras != null) {
				setResult(ACT_RES_CODE, data);
			}
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri) {
		try{
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(uri, "image/*");
			intent.putExtra("crop", "true");
			// aspectX aspectY 是宽高的比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// outputX outputY 是裁剪图片宽高
			intent.putExtra("outputX", PHOTO_ZOOM_DIMENSITION);
			intent.putExtra("outputY", PHOTO_ZOOM_DIMENSITION);
			intent.putExtra("return-data", true);
	
			startActivityForResult(intent, PHOTO_ZOOM_RESULT_CODE);
		}catch(ActivityNotFoundException e){
			MainThreadHandler.makeToast("图片大小不合适，请通过拍照上传。");
			VidUtil.fLog("test", e.getMessage());
		}
	}
	
}
