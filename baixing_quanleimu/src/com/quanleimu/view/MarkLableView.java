package com.quanleimu.view;


import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.util.Util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MarkLableView extends BaseView{

	private EditText etMark;
	private String personMark = "";
	
	
	protected ViewInfoListener m_viewInfoListener = null;	
	public void setInfoChangeListener(ViewInfoListener listener){m_viewInfoListener = listener;};
	
	protected void Init(){
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		LayoutInflater inflator = LayoutInflater.from(getContext());
		View markMain = inflator.inflate(R.layout.marklable, null);
		
		
		etMark = (EditText)markMain.findViewById(R.id.etMark);
		etMark.findFocus();
		
		//键盘弹出
		personMark = QuanleimuApplication.getApplication().getPersonMark();
		if(personMark != null && !personMark.equals(""))
		{
			etMark.setText(personMark);
		}
		
		this.addView(markMain);
	}
	
	public MarkLableView(Context context){
		super(context); 
		
		Init();
	}
	public MarkLableView(Context context, Bundle bundle){
		super(context);
		
		Init();
	}
	
	public boolean onRightActionPressed(){
		/*if(etMark.getText().toString() == null || etMark.getText().toString().trim().equals(""))
		{
			Toast.makeText(getContext(), "内容不能为空", Toast.LENGTH_SHORT).show();
		}
		else
		{*/
			personMark = etMark.getText().toString();
			QuanleimuApplication.getApplication().setPersonMark(personMark);
			(new AsyncTask<Boolean, Boolean, Boolean>() { 
				protected Boolean doInBackground(Boolean... bs) {   
					Util.saveDataToLocate(MarkLableView.this.getContext(), "personMark", personMark);
					return true;
				}
				
				protected void onPostExecute(Boolean bool) {  
					if(null != m_viewInfoListener){
						m_viewInfoListener.onBack();
					}
				}
			}).execute(true);
		//}
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_rightActionHint = "修改";
		title.m_title = "签名档";
		title.m_leftActionHint = "返回";
		return title;
	}
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}
}
