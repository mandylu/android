package com.quanleimu.interfaces;

import android.app.Dialog;
import android.view.View;

public interface OnDialogListeren {
	public void OnClick(Dialog arg0, View arg1, int arg2, boolean arg3, Object arg4);
	public void setTextViewContent(Dialog dialog,View view);
}
