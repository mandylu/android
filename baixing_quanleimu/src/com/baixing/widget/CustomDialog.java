package com.baixing.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.activity.R;

public class CustomDialog extends Dialog {
	
	private Context context = null;
	private Button button = null;
	private ListView listview_custom = null;
	
	
	public CustomDialog(Context context) {
//		super(context);
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
		 this.getWindow().setBackgroundDrawableResource(R.drawable.custom_shape);
		 setContentView(R.layout.custom_dialog);

		 listview_custom = (ListView)findViewById(R.id.listview_custom);
		 button = (Button) findViewById(R.id.button_cancel);
		 button.setOnClickListener(new android.view.View.OnClickListener(){
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
