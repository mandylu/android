package com.quanleimu.util;

import com.quanleimu.activity.CateMain;
import com.quanleimu.adapter.AllCatesAdapter;
import com.quanleimu.jsonutil.JsonUtil;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ErrorHandler extends Handler{
	
	public enum ERROR_CODE{
		ERROR_CODE_NETWORK_INVALID,
		ERROR_CODE_SERVICE_UNAVAILABLE,
		ERROR_CODE_OK	
	};
	
	protected static ErrorHandler m_instance = null;
	

	public static ErrorHandler instance(){
		if(m_instance == null){
			m_instance = new ErrorHandler();
		}
		
		return m_instance;
	}
	
	private ErrorHandler(){
		
	}
	
	@Override
	public void handleMessage(Message msg) {
		
		switch (msg.what) {
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		}
		super.handleMessage(msg);
	}

}
