package com.baixing.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.activity.R;

public class CustomDialog extends Dialog {
	
	private Context context = null;
	private TextView textview = null;
	private ListView listview_custom = null;
	
	public CustomDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	 public ListView getListView() {
		return listview_custom;
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
	}


	protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.custom_dialog);

//		Button buttonCancel = (Button) findViewById(R.id.button_cancel);
//		buttonCancel.setOnClickListener(new Button.OnClickListener(){
//
//				public void onClick(View v) {
//					dismiss();
//					
//				}
//	        });
		 listview_custom = (ListView)findViewById(R.id.listview_custom);
		 textview = (TextView) findViewById(R.id.textview_cancel);
		 textview.setOnClickListener(new android.view.View.OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		 });
	 }
	 
	 //called when this dialog is dismissed
	 protected void onStop() {
		 Log.d("TAG","+++++++++++++++++++++++++++");
	 }
	 

}
