package com.quanleimu.util;
import android.widget.ImageView;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import java.io.FileInputStream;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Paint;

public class BXDecorateImageView extends ImageView{
	public enum ImagePos{
		ImagePos_LeftTop,
		ImagePos_LeftBottom,
		ImagePos_RightTop,
		ImagePos_RightBottom,
		ImagePos_Center
	}
	private ImagePos pos = ImagePos.ImagePos_LeftTop;
	private Bitmap bmp = null;

	public void setDecorateResource(int resId, ImagePos pos){
		bmp = null;
		bmp = BitmapFactory.decodeResource(getResources(), resId);
		this.pos = pos;
	}

	public void setDecorateBitmap(String url, ImagePos pos){
		this.pos = pos;
	    try {
	         FileInputStream fis = new FileInputStream(url);
	         bmp = BitmapFactory.decodeStream(fis);
	    } catch (FileNotFoundException e) {
	         e.printStackTrace();
	         bmp = null;
	    }
	}

	public void setDecorateBitmap(Bitmap bmp, ImagePos pos){
		this.pos = pos;
		this.bmp = bmp;
	}
	
	public BXDecorateImageView(Context context)
	{
		super(context);
	}
	public BXDecorateImageView(Context context, AttributeSet set)
	{
		super(context, set);
	}
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if(this.bmp == null) return;
		
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int x = 0, y = 0;
		switch(this.pos){
		case ImagePos_LeftBottom:
			y = this.getHeight() - height;
			break;
		case ImagePos_RightTop:
			x = this.getWidth() - width;
			break;
		case ImagePos_RightBottom:
			x = this.getWidth() - width;
			y = this.getHeight() - height;
			break;
		case ImagePos_Center:
			x = (this.getWidth() - width) / 2;
			y = (this.getHeight() - height) / 2;
			break;
		default:
			break;
		}	
		canvas.drawBitmap(this.bmp, x, y, new Paint());
	}
}