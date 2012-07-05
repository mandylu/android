package com.quanleimu.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AnimatingImageView extends ImageView {
	
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
	
	
	@Override
	protected void onDraw(Canvas canvas){
		if(null != getDrawable() 
				&& getDrawable() instanceof AnimationDrawable 
				&& !((AnimationDrawable)(getDrawable())).isRunning()){
			((AnimationDrawable)(getDrawable())).start();
		}	
		
		super.onDraw(canvas);
	}
	
	@Override
	protected void onDetachedFromWindow (){
		if(null != getDrawable() && getDrawable() instanceof AnimationDrawable){
			((AnimationDrawable)(getDrawable())).stop();
		}	
	}

	
	@Override
	protected void onAttachedToWindow(){
		if(null != getDrawable() && getDrawable() instanceof AnimationDrawable){
			((AnimationDrawable)(getDrawable())).start();
		}	
	}
}
