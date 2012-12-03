package com.baixing.activity.test;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.athrun.android.framework.viewelement.ViewElement;

import android.app.Instrumentation;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BXImageViewElement extends ViewElement {
	public View imgView;

	/**
	 * Constructor of {@code TmtsTextView}.
	 * 
	 * @param inst
	 *            {@code Instrumentation}.
	 * @param textView
	 *            {@code TextView}.
	 */
	protected BXImageViewElement(Instrumentation inst, View imgView) {
		super(inst, imgView);
		this.imgView = imgView;
	}
	
	private int[] getDrawableId() throws Exception {
		String packageName = inst.getTargetContext().getPackageName();
		String className = packageName + ".R$drawable";
		Class<?> innerClass = null;
		innerClass = Class.forName(className);
		Field[] fields = null;
		fields = innerClass.getFields();
		int[] intIds = new int[fields.length];
		for (int i = 0; i < fields.length; i++) {
			if (null != fields[i]) {
				intIds[i] = fields[i].getInt(null);
				//Log.i(LOG_TAG, "getDrawableId:" + imgView.getResources().getResourceName(intIds[i]));
			} else {
				intIds[i] = -1;
			}
		}
		return intIds;
	}

	public int getResourceId(boolean fromDrawable) throws Exception {
		int[] nums = getDrawableId();
		int resourceId = 0;
		Drawable d = imgView instanceof ImageView && fromDrawable ? ((ImageView)imgView).getDrawable() : imgView.getBackground();
		if (imgView instanceof ImageView && d == null) {
			Log.i(LOG_TAG, "getDrawableId:1" + (imgView instanceof ImageView ? "image" : 
				 imgView == null ? "" : imgView.getClass().getName()));
			d = imgView.getBackground();
		}
		ConstantState state = d.getCurrent().getConstantState();
		Log.i(LOG_TAG, "getDrawableId:2" + (imgView instanceof ImageView ? "image" : 
			 imgView == null ? "" : imgView.getClass().getName()));
		for (int i : nums) {
			try {
				Drawable drawableCp=inst.getTargetContext().getResources().getDrawable(i);
				if(drawableCp==null){
					//Log.i("ABC", i+" null");
					continue;
				}
				ConstantState stateCompare = drawableCp.getConstantState();
				if(stateCompare==null){
					//Log.i("ABC", i+" null");
					continue;
				}
				boolean flag = state.equals(stateCompare);
				if (flag) {
					resourceId = i;
					break;
				}
			} catch (Exception e) {
			}

		}
		return resourceId;
	}

	public String getImageUrl(boolean fromDrawable) throws Exception {
		//Log.i("ABC", getResourceId()+"");
		int resourceId = getResourceId(fromDrawable);
		if (resourceId <= 0) return "";
		return imgView.getResources().getResourceName(resourceId);
	}
	
	public boolean checkImageByName(String imgName) throws Exception {
		return checkImageByName(imgName, true);
	}
	
	public boolean checkImageByName(String imgName, boolean fromDrawable) throws Exception {
		String imgUrl = getImageUrl(fromDrawable);
		Log.i(LOG_TAG, "getDrawableId:3" + imgUrl); 
		if (imgUrl != null) {
			imgName = "/" + imgName;
			int i = imgUrl.indexOf(imgName);
			if (i > 0 && i + imgName.length() == imgUrl.length()) return true;
		}
		return false;
	}
	
	public String getTag() {
		return (String)imgView.getTag();
	}
}
