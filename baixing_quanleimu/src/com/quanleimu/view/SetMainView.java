package com.quanleimu.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.activity.R.id;
import com.quanleimu.activity.R.layout;
import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;


public class SetMainView extends BaseView implements View.OnClickListener{

	// 定义控件
	public Dialog changePhoneDialog;
	private UserBean user;
	
	
	protected void Init(){
		LayoutInflater inflator = LayoutInflater.from(getContext());
		View setmain = inflator.inflate(R.layout.setmain, null);
		this.addView(setmain);
		
		((RelativeLayout) findViewById(R.id.rlTelNum)).setOnClickListener(this);
		((RelativeLayout) findViewById(R.id.rlClearCache)).setOnClickListener(this);
		( (RelativeLayout) findViewById(R.id.rlAbout)).setOnClickListener(this);
		((RelativeLayout) findViewById(R.id.rlMark)).setOnClickListener(this);
		((RelativeLayout) findViewById(R.id.rlTextImage)).setOnClickListener(this);
		((RelativeLayout) findViewById(R.id.rlBack)).setOnClickListener(this);
		
		final TextView textImg = (TextView)findViewById(R.id.textView3);
		if(QuanleimuApplication.isTextMode()){
			textImg.setText("文字");
		}
		else{
			textImg.setText("图片");
		}
		((RelativeLayout)findViewById(R.id.rlTextImage)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(textImg.getText().equals("图片")){
					textImg.setText("文字");
					QuanleimuApplication.setTextMode(true);
				}
				else{
					textImg.setText("图片");
					QuanleimuApplication.setTextMode(false);
				}				
			}
		});
		
		((TextView)setmain.findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
		
		user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
		if (user != null) {
			((TextView)setmain.findViewById(R.id.tvPhoneNum)).setText(user.getPhone());
		}
	}
	
	public SetMainView(Context context){
		super(context);		
		
		Init();
	}
	
	public SetMainView(Context context, Bundle bundle){
		super(context, bundle);
		
		Init();
	}
	
	public void onResume(){
		((TextView)findViewById(R.id.personMark)).setText(QuanleimuApplication.getApplication().getPersonMark());
		
		user = (UserBean) Util.loadDataFromLocate(getContext(), "user");
		if (user != null) {
			((TextView)findViewById(R.id.tvPhoneNum)).setText(user.getPhone());
		}		
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "设置";
		//title.m_leftActionHint = "首页";
		
		return title;
		}
	
	@Override	
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;		
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_SETTING;
		
		return tab;
	}

	@Override
	public void onClick(View v) {
		// 手机号码
		if (v.getId() == ((RelativeLayout) findViewById(R.id.rlTelNum)).getId()) {
			if (((TextView)findViewById(R.id.tvPhoneNum)).getText().equals("")) {
				// 跳转登录界面
				if(null != m_viewInfoListener){
					m_viewInfoListener.onNewView(new LoginView(getContext(), "设置"));
				}
			} else {
				// 修改对话框

				LayoutInflater inflater = LayoutInflater.from(getContext());
				View linearlayout = inflater.inflate(
						R.layout.changephonedialog, null);
				TextView tvTelNum = (TextView) linearlayout
						.findViewById(R.id.tvTelNum);
				tvTelNum.setText("您已经绑定" + ((TextView)findViewById(R.id.tvPhoneNum)).getText().toString()
						+ ",确定要修改吗？");
				Button btnChange = (Button) linearlayout
						.findViewById(R.id.btnChange);
				Button btnCancel = (Button) linearlayout
						.findViewById(R.id.btnCancel);

				btnChange.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 点击换号码，重新跳转到登录
						changePhoneDialog.dismiss();
						
						if(null != m_viewInfoListener){
							m_viewInfoListener.onNewView(new LoginView(getContext(), "设置"));
						}
					}
				});

				btnCancel.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						changePhoneDialog.dismiss();
					}
				});
				
				changePhoneDialog = new AlertDialog.Builder(getContext()).setView(linearlayout).create();
				changePhoneDialog.show();
			}
		}

		// 签名档
		else if (v.getId() == ((RelativeLayout) findViewById(R.id.rlMark)).getId()) {
			if(null != m_viewInfoListener){
				m_viewInfoListener.onNewView(new MarkLableView(getContext()));
			}
		}

		// 清空缓存
		else if (v.getId() == ((RelativeLayout) findViewById(R.id.rlClearCache)).getId()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("提示:")
					.setMessage("是否清空缓存？")
					.setNegativeButton("否", null)
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String[] files = getContext().fileList();
									for(int i=0;i<files.length;i++){
										String file_path = files[i];
										getContext().deleteFile(file_path);
									}
									//清空手机号码
									((TextView)findViewById(R.id.tvPhoneNum)).setText("");
									//清空签名档
									((TextView)findViewById(R.id.personMark)).setText("");
								}
							});
			builder.create().show();
		}
		
		//aboutus
		else if(v.getId() == ((RelativeLayout) findViewById(R.id.rlAbout)).getId()){
			if(null != m_viewInfoListener){
				m_viewInfoListener.onNewView(new AboutUs(getContext()));
			}
		}
		
		// 反馈
		else if (v.getId() ==((RelativeLayout) findViewById(R.id.rlBack)).getId()) {
			m_viewInfoListener.onNewView(new OpinionBackView(getContext(), null));
		}
	}

}
