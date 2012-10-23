package com.quanleimu.activity.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.athrun.android.framework.utils.RClassUtils;
import org.athrun.android.framework.viewelement.ViewElement;
import org.athrun.android.framework.viewelement.ViewGroupElement;
import org.athrun.android.framework.viewelement.ViewUtils;

import android.app.Instrumentation;
import android.view.View;
import android.view.ViewGroup;
import android.test.TouchUtils;

public class BXViewGroupElement extends ViewGroupElement {
	private ViewGroup viewGroup;

	protected BXViewGroupElement(Instrumentation inst, ViewGroup viewGroup) {
		super(inst, viewGroup);
		this.viewGroup = viewGroup;
	}
	
	public void doTouch(float toX) {
		logger.info("doToch().");
		viewOperation.drag(0, toX, getViewCenter().getY(), getViewCenter().getY(), 10);
	}
	
	public void scrollTop(float fromY, float toY) {
		viewOperation.drag(getViewCenter().getX(), getViewCenter().getX(), fromY, toY, 10);
	}
}
