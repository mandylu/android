package com.baixing.widget;

import com.quanleimu.activity.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class VerifyFailDialog extends DialogFragment{
	public static interface VerifyListener{
		void onReVerify(String mobile);
		void onSendVerifyCode(String code);
	}
	
	private VerifyListener listener;
	public VerifyFailDialog(VerifyListener listener){
		this.listener = listener;
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceBundle){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.dialog_verify_fail, null);
        final EditText editPhoneEt = (EditText) v.findViewById(R.id.dialog_phone_et);
        builder.setView(v)
        		.setTitle("验证失败")
                .setPositiveButton("重新验证", new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                    	if(listener != null){
                    		listener.onReVerify(editPhoneEt.getText().toString());
                    	}
                    }
                })
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    	if(listener != null){
                    		listener.onSendVerifyCode(editPhoneEt.getText().toString());
                    	}
                    }
                });
        final AlertDialog altDlg = builder.create();
        altDlg.setCanceledOnTouchOutside(false);
        altDlg.setOnShowListener(new OnShowListener(){

			@Override
			public void onShow(final DialogInterface dialog) {
		    	AlertDialog dlg = AlertDialog.class.cast(dialog);  
		    	if(dlg != null){
		    		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		    	}
				// TODO Auto-generated method stub
				(new CountDownTimer(60000, 1000) {  
				    public void onTick(long millisUntilFinished) {  
				    	AlertDialog dlg = AlertDialog.class.cast(dialog);  
				    	if(dlg != null){
				    		String txt = "重新验证(" + String.valueOf(millisUntilFinished / 1000) + ")";
				    		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setText(txt);
				    	}
				    }  
				    public void onFinish() {  
				    	AlertDialog dlg = AlertDialog.class.cast(dialog);  
				    	if(dlg != null){
				    		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setText("重新验证");
				    		dlg.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
				    	}  
				    }  
				 }).start(); 
			}
        	
        });
        return altDlg;
	}
	
}