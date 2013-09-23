package com.baixing.sharing.referral;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baixing.util.Util;
import com.quanleimu.activity.R;

public class RegisterOrLoginDlg extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceBundle){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.dialog_register_login, null);
        Button RL=(Button)v.findViewById(R.id.btn_register_login);
        builder.setView(v).setTitle("请登录");
        final AlertDialog Dlg = builder.create();
        
        RL.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
					SmsManager smsManager = SmsManager.getDefault();
					String smsText = Util.getDeviceUdid(getActivity());
					smsManager.sendTextMessage("106901336000", null, smsText, null, null);	
			}
		});
        return Dlg;
	}
}
