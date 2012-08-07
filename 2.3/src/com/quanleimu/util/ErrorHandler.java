package com.quanleimu.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ErrorHandler extends Handler{

	/*definition of error code*/
	public final static int ERROR_OK = 0;

	public final static int ERROR_COMMON_WARNING = -2;
	public final static int ERROR_COMMON_FAILURE = -3;
	
	public final static int ERROR_SERVICE_UNAVAILABLE = -9;
	public final static int ERROR_NETWORK_UNAVAILABLE = -10;
	
//	
//	protected static ErrorHandler m_instance = null;
//	
//
//	public static ErrorHandler instance(){
//		if(m_instance == null){
//			m_instance = new ErrorHandler();
//		}
//		
//		return m_instance;
//	}
//	
	private Context context = null;
	public ErrorHandler(Context context_){
		this.context = context_;
	}
	

	//0: OK
	//-1: 
	
	@Override
	public void handleMessage(Message msg) {
		
		if(null != msg.obj){
			if(msg.obj instanceof ProgressDialog){
				((ProgressDialog)msg.obj).dismiss();
			}
		}
		
		String strToast = null;
		if(null != msg.getData() && null != msg.getData().getString("popup_message")){
			strToast = msg.getData().getString("popup_message");
		}
		
		if(null == strToast){
			switch (msg.what) {
			case ERROR_OK:
				strToast = "操作已成功！";
				break;
			case ERROR_COMMON_WARNING:
				strToast = "请注意！";
				break;
			case ERROR_COMMON_FAILURE:
				strToast = "操作失败，请检查后重试!";
				break;
			case ERROR_SERVICE_UNAVAILABLE:
				strToast = "服务当前不可用，请稍后重试！";
				break;	
			case ERROR_NETWORK_UNAVAILABLE:
				strToast = "网络连接失败，请检查设置！";
				break;	
			}
		}		
		
		if(null != strToast && 0 != strToast.length())
			Toast.makeText(this.context, strToast, Toast.LENGTH_SHORT).show();
		
		if(null != msg.getCallback()){
			msg.getCallback().run();
		}
		
		super.handleMessage(msg);
	}

}
