package com.vidmt.telephone.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.utils.AvatarUtil;

/**
 * 聊天界面点击图片，查看图片页
 */
public class ImgViewerActivity extends AbsVidActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.img_viewer);

		String imgFilePath = getIntent().getStringExtra(ExtraConst.EXTRA_IMG_FILE_PATH);
		Bitmap bm = AvatarUtil.getBitmapFromFile(imgFilePath);

		int bmWidth = bm.getWidth();
		int bmHeight = bm.getHeight();

		int screenWidth = SysUtil.getDisplayMetrics().widthPixels;
		int screenHeight = SysUtil.getDisplayMetrics().heightPixels;

		float zoomScale = ((float) screenWidth) / bmWidth;
		if (zoomScale <= 1 && ((float) bmHeight) / zoomScale > screenHeight || zoomScale > 1
				&& ((float) bmHeight) * zoomScale > screenHeight) {// 高比例太大
			zoomScale = ((float) screenHeight) / bmHeight;
		}
		bm = AvatarUtil.zoomImage(bm, zoomScale);

		ImageView imageView = (ImageView) findViewById(R.id.img);
		imageView.setImageBitmap(bm);
	}

}
