package com.quanleimu.view;

import com.quanleimu.activity.BaseActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

public class BaseView extends LinearLayout{
	
	public interface ViewInfoListener{
		public void onTitleChanged(String newTitle);
		public void onBack();
		public void onNewView(BaseView view);
	};
	
	public class TitleDef{
		public boolean m_visible = true;
		public String m_leftActionHint = null;
		public String m_title = null;
		public String m_rightActionHint = null;
	};
	
	public class TabDef{
		public boolean m_visible = true;
		public String m_tabSelected = "首页";
	};
	
	protected ProgressDialog pd;
	
	protected ViewInfoListener m_viewInfoListener = null;	
	public void setInfoChangeListener(ViewInfoListener listener){m_viewInfoListener = listener;};
	
	public BaseView(BaseActivity context){super(context); this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	public BaseView(BaseActivity context, Bundle bundle){super(context);this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	
	public Bundle extracBundle(){return new Bundle();}//return a bundle that could be used to re-build the very BaseView
	
	public void onDestroy(){}//called before destruction
	public void onPause(){}//called before put into stack
	
	public boolean onBack(){return false;}//called when back button/key pressed
	public boolean onLeftActionPressed(){return false;}//called when left button on title bar pressed, return true if handled already, false otherwise
	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){return new TitleDef();}
	public TabDef getTabDef(){return new TabDef();}
	
};