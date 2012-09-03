package com.quanleimu.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class BaseView extends LinearLayout{
	
	public interface ViewInfoListener{
//		public void onTitleChanged(String newTitle);
//		public void onLeftBtnTextChanged(String newText);
//		public void onRightBtnTextChanged(String newText);
		public void onTitleChanged(TitleDef title);
		public void onTabChanged(TabDef tab);
		public void onBack();
		public void onBack(int message, Object obj);
		public void onNewView(BaseView view);
		public void onExit(BaseView view);//means remove current view from screen content
		public void onSwitchToTab(ETAB_TYPE tabType);
		public void onPopView(String viewClassName);
		public void onSetResult(int requestCode, int resultCode, Bundle data);
	};

	public enum EBUTT_STYLE{
		EBUTT_STYLE_BACK,
		EBUTT_STYLE_NORMAL,
		EBUTT_STYLE_CUSTOM
		//EBUTT_STYLE_FORWARD
	};
	
	public class TitleDef{		
		public boolean m_visible = true;
		public String m_leftActionHint = null;
		public EBUTT_STYLE m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
		public int leftCustomResourceId = -1;
		
		public String m_title = null;
		public View m_titleControls = null;
		
		public String m_rightActionHint = null;
		public EBUTT_STYLE m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
		public int rightCustomResourceId = -1;
	};
	
	public enum ETAB_TYPE{
		ETAB_TYPE_PREV,
		ETAB_TYPE_MAINPAGE,
		ETAB_TYPE_CATEGORY,
		ETAB_TYPE_PUBLISH,
		ETAB_TYPE_MINE,
		ETAB_TYPE_SETTING
	};
	
	public class TabDef{
		
		public boolean m_visible = true;
		public ETAB_TYPE m_tabSelected = ETAB_TYPE.ETAB_TYPE_PREV;
	};
	
	protected ProgressDialog pd;
	protected boolean isActive = true;
	
	protected ViewInfoListener m_viewInfoListener = null;	
	public void setInfoChangeListener(ViewInfoListener listener){m_viewInfoListener = listener;};
	
	public BaseView(Context context){super(context); this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	public BaseView(Context context, Bundle bundle){super(context);this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));}
	
	public Bundle extracBundle(){return new Bundle();}//return a bundle that could be used to re-build the very BaseView
	
	public void onDestroy(){isActive = false;}//called before destruction
	public void onPause(){isActive = false;}//called before put into stack
	public void onResume(){isActive = true;}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {}
	
	public void onPreviousViewBack(int message, Object obj){}
	
	public boolean onBack(){return false;}//called when back button/key pressed
	public boolean onLeftActionPressed(){return false;}//called when left button on title bar pressed, return true if handled already, false otherwise
	public boolean onRightActionPressed(){return false;}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){return new TitleDef();}
	public TabDef getTabDef(){return new TabDef();}
	
	public boolean handleContextMenuSelect(MenuItem  menuItem) { return false;};
	
};