package com.quanleimu.activity.test;

import org.athrun.android.framework.viewelement.ViewElement;

import android.app.Instrumentation;
import android.widget.ImageView;

public class BXImageViewElement extends ViewElement {
	private ImageView imgView;

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
}
