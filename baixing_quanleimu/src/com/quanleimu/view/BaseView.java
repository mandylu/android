package com.quanleimu.view;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

public class BaseView extends LinearLayout{
	
	public interface ViewInfoListener{
		public void onTitleChanged(String newTitle);
	}
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
	
	public void setInfoChangeListener(ViewInfoListener listener){};
	
	public BaseView(Context context){super(context); this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	public BaseView(Context context, Bundle bundle){super(context);this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	
	public Bundle extracBundle(){return new Bundle();}//return a bundle that could be used to re-build the very BaseView
	
	public void onDestroy(){}//called before destruction
	public void onPause(){}//called before put into stack
	
	public boolean onBack(){return false;}//called when back button/key pressed
	public boolean onLeftActionPressed(){return false;}//called when left button on title bar pressed, return true if handled already, false otherwise
	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){return new TitleDef();}
	public TabDef getTabDef(){return new TabDef();}
	
};