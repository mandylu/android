package com.quanleimu.view;

import com.quanleimu.activity.BaseActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

public class BaseView extends LinearLayout{
	
	public interface ViewInfoListener{
		public void onTitleChanged(String newTitle);
//		public void onLeftBtnTextChanged(String newText);
		public void onRightBtnTextChanged(String newText);
		public void onBack();
		public void onNewView(BaseView view);
	};

	public enum EBUTT_STYLE{
		EBUTT_STYLE_BACK,
		EBUTT_STYLE_NORMAL,
		//EBUTT_STYLE_FORWARD
	};
	
	public class TitleDef{		
		public boolean m_visible = true;
		public String m_leftActionHint = null;
		public EBUTT_STYLE m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
		public String m_title = null;
		public String m_rightActionHint = null;
		public EBUTT_STYLE m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
	};
	
	public enum ETAB_TYPE{
		ETAB_TYPE_MAINPAGE,
		ETAB_TYPE_CATEGORY,
		ETAB_TYPE_PUBLISH,
		ETAB_TYPE_MINE,
		ETAB_TYPE_SETTING
	};
	
	public class TabDef{
		
		public boolean m_visible = true;
		public ETAB_TYPE m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
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