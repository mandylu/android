package com.quanleimu.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class AnimatingImageView extends ImageView {
	
	private View mForefrontView = null;
	
	public AnimatingImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public AnimatingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public AnimatingImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
//	@Override
//	protected void onDraw(Canvas canvas){
//		if(null != getDrawable() 
//				&& getDrawable() instanceof AnimationDrawable 
//				&& !((AnimationDrawable)(getDrawable())).isRunning()){
//			((AnimationDrawable)(getDrawable())).start();
//		}	
//		
//		super.onDraw(canvas);
//	}
	
//	@Override
//	protected void onDetachedFromWindow (){
//		super.onDetachedFromWindow();
//		
//		if(null != getDrawable() && getDrawable() instanceof AnimationDrawable){
//			((AnimationDrawable)(getDrawable())).stop();
//		}	
//	}
//
//	
//	@Override
//	protected void onAttachedToWindow(){
//		super.onAttachedToWindow();
//		
//		if(null != getDrawable() && getDrawable() instanceof AnimationDrawable){
//			((AnimationDrawable)(getDrawable())).start();
//		}	
//	}
	
	@Override
	public void setImageBitmap(Bitmap bitmap){
		super.setImageBitmap(bitmap);
		
		if(getVisibility() != View.VISIBLE)
			setVisibility(View.VISIBLE);
		
		if(null != mForefrontView && null != bitmap){
			mForefrontView.setVisibility(View.GONE);
		}
	}
	
	
	
	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		
		if(getVisibility() != View.VISIBLE)
		{
			setVisibility(View.VISIBLE);
		}
		
		if(null != mForefrontView && resId != -1){
			mForefrontView.setVisibility(View.GONE);
		}
	}

	public void setForefrontView(View view){
		mForefrontView = view;
	}
}
