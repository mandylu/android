package com.quanleimu.activity;


import com.quanleimu.util.Util;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MarkLable extends BaseActivity{

	private EditText etMark;
	private Button btnFinish;
	private Button ivBack;
	private String personMark = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.marklable);
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		if(intent == null)
		{
			intent = new Intent();
		}
		bundle = intent.getExtras();
		if(bundle == null)
		{
			bundle = new Bundle();
		}
		
		etMark = (EditText)findViewById(R.id.etMark);
		btnFinish = (Button)findViewById(R.id.btnFinish);
		ivBack = (Button)findViewById(R.id.ivBack);
		
		etMark.findFocus();
		//键盘弹出
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		personMark = myApp.getPersonMark();
		if(personMark != null && !personMark.equals(""))
		{
			etMark.setText(personMark);
		}
		
		btnFinish.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		//点击完成
		if(v.getId() == btnFinish.getId())
		{
			if(etMark.getText().toString() == null || etMark.getText().toString().trim().equals(""))
			{
				Toast.makeText(MarkLable.this, "内容不能为空", Toast.LENGTH_SHORT).show();
			}
			else
			{
				personMark = etMark.getText().toString();
				myApp.setPersonMark(personMark);
				new Thread(new SavePersonMark()).start();
				MarkLable.this.finish();
			}
		}
		//返回
		if(v.getId() == ivBack.getId())
		{
			MarkLable.this.finish();
		}
	}
	
	class SavePersonMark implements Runnable
	{

		@Override
		public void run() {
			Util.saveDataToLocate(MarkLable.this, "personMark", personMark);
		}
		
	}
	
}
