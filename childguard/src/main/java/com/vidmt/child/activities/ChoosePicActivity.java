package com.vidmt.child.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.FileStorage;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.ChatController;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.listeners.ChatPhotoSendListener;
import com.vidmt.child.utils.AvatarUtil;
import com.vidmt.child.utils.HttpUtil;
import com.vidmt.child.utils.VidUtil;

import java.io.File;
import java.io.IOException;

public class ChoosePicActivity extends AbsVidActivity implements OnClickListener {
	public static final int ACT_AVATAR_RES_CODE = 1;
	public static final int ACT_CHILD_AVATAR_RES_CODE = 2;
	private static final int PHOTO_ZOOM_DIMENSITION = 64;
	private static final int PHOTO_ZOOM_RESULT_CODE = 3;
	private Dialog mPicDialog;
	private File mTakePicFile;
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
				if (mIsDlgCanceledNotByCancelBtn) {
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
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					takePicIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mTakePicFile);
					takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
				}else {
					takePicIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTakePicFile));
				}
				startActivityForResult(takePicIntent, 0);
			} catch (Throwable e) {
				VLog.e("test", e);
			}
			break;
		case R.id.choose_from:
			mIsDlgCanceledNotByCancelBtn = true;
			Intent chooseIntent = new Intent();
			chooseIntent.setType("image/*");
			chooseIntent.setAction(Intent.ACTION_GET_CONTENT);
			chooseIntent.addCategory(Intent.CATEGORY_OPENABLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果大于等于7.0使用FileProvider
				String photoName = "tmp" + String.valueOf(System.currentTimeMillis()) + ".jpg";
				mTakePicFile = new File(VLib.getSdcardDir(), FileStorage.buildCachePath(photoName));
				Uri uriForFile = FileProvider.getUriForFile(ChoosePicActivity.this, getPackageName() + ".provider", mTakePicFile);
				chooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
				chooseIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				chooseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			}
			startActivityForResult(chooseIntent, 0);
			break;
		case R.id.cancel:
			finish();
			break;
		}
		mPicDialog.cancel();
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
				Uri photoUri;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
					photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mTakePicFile);
				}else {
					photoUri=Uri.fromFile(mTakePicFile);
				}
				startPhotoZoom(photoUri);
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
				Uri photoUri;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
					photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(filePath));
				}else {
					photoUri=Uri.fromFile(new File(filePath));
				}
				startPhotoZoom(photoUri);
			}
		}
		if (requestCode == PHOTO_ZOOM_RESULT_CODE) {
			if (mTakePicFile != null && mTakePicFile.exists()) {
				mTakePicFile.delete();
			}
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				try {
					boolean isChangeParentAvatar = getIntent().getBooleanExtra(
							ExtraConst.EXTRA_IS_CHANGE_PARENT_AVATAR, true);
					if (isChangeParentAvatar) {
						HttpUtil.setAvatar(photo, true);
						setResult(ACT_AVATAR_RES_CODE, data);
					} else {
						HttpUtil.setAvatar(photo, false);
						setResult(ACT_CHILD_AVATAR_RES_CODE, data);
					}
				} catch (VidException e) {
					VLog.e("test", e);
				} catch (IOException e) {
					VLog.e("test", e);
				}
			}
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri) {
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

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			//适配7.0之后Uri问题导致图片裁剪失败
			Uri outPutUri = Uri.fromFile(mTakePicFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
			intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁匡重叠
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		}
		startActivityForResult(intent, PHOTO_ZOOM_RESULT_CODE);
	}
	
}
