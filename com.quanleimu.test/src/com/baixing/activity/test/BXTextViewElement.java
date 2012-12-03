package com.baixing.activity.test;

import org.athrun.android.framework.viewelement.TextViewElement;

import android.app.Instrumentation;
import android.widget.TextView;

public class BXTextViewElement extends TextViewElement {
	private TextView textView;

	/**
	 * Constructor of {@code TmtsTextView}.
	 * 
	 * @param inst
	 *            {@code Instrumentation}.
	 * @param textView
	 *            {@code TextView}.
	 */
	protected BXTextViewElement(Instrumentation inst, TextView textView) {
		super(inst, textView);
		this.textView = textView;
	}
	
	public int getInputType() {
		return textView.getInputType();
	}
}
