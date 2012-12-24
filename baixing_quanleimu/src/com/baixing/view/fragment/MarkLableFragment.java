package com.baixing.view.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.GlobalDataManager;
import com.baixing.util.Util;
import com.quanleimu.activity.R;

public class MarkLableFragment extends BaseFragment {

	private EditText etMark;
	private String personMark = "";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View markMain = inflater.inflate(R.layout.marklable, null);
//		setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		
		etMark = (EditText)markMain.findViewById(R.id.etMark);
		etMark.findFocus();
		
		//键盘弹出
		personMark = GlobalDataManager.getApplication().getPersonMark();
		if(personMark != null && !personMark.equals(""))
		{
			etMark.setText(personMark);
		}
		
		return markMain;
		
	}
	
	public void handleRightAction(){
//		if(etMark.getText().toString() == null || etMark.getText().toString().trim().equals(""))
//		{
//			Toast.makeText(getActivity(), "内容不能为空", Toast.LENGTH_SHORT).show();
//		}
//		else
//		{
			personMark = etMark.getText().toString();
			GlobalDataManager.getApplication().setPersonMark(personMark);
			(new AsyncTask<Boolean, Boolean, Boolean>() { 
				protected Boolean doInBackground(Boolean... bs) {   
//					Util.saveDataToLocate(getActivity(), "personMark", personMark);
					Util.saveDataToFile(getActivity(), null, "personMark", personMark.getBytes());
					return true;
				}
				
				protected void onPostExecute(Boolean bool) {  
//					if(null != m_viewInfoListener){
//						m_viewInfoListener.onBack();
//					}
					finishFragment();
				}
			}).execute(true);
//		}
//		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_rightActionHint = "修改";
		title.m_title = "签名档";
		title.m_leftActionHint = "返回";
	}
	
	
	
}
