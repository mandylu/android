package com.quanleimu.activity.test;

import java.lang.reflect.Field;

import org.athrun.android.framework.viewelement.ViewElement;

import android.app.Instrumentation;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.widget.ImageView;

public class BXImageViewElement extends ViewElement {
	public ImageView imgView;

	/**
	 * Constructor of {@code TmtsTextView}.
	 * 
	 * @param inst
	 *            {@code Instrumentation}.
	 * @param textView
	 *            {@code TextView}.
	 */
	protected BXImageViewElement(Instrumentation inst, ImageView imgView) {
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
			} else {
				intIds[i] = -1;
			}
		}
		return intIds;
	}

	public int getResourceId() throws Exception {
		int[] nums = getDrawableId();
		int resourceId = 0;
		Drawable d = imgView.getDrawable();
		if (d == null) d = imgView.getBackground();
		ConstantState state = d.getCurrent().getConstantState();
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

	public String getImageUrl() throws Exception {
		//Log.i("ABC", getResourceId()+"");
		return imgView.getResources().getResourceName(getResourceId());
	}
	
	public boolean checkImageByName(String imgName) throws Exception {
		String imgUrl = getImageUrl();
		if (imgUrl != null) {
			imgName = "/" + imgName;
			int i = imgUrl.indexOf(imgName);
			if (i > 0 && i + imgName.length() == imgUrl.length()) return true;
		}
		return false;
	}
}
