package com.baixing.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.quanleimu.activity.R;

public class EditTitleDialogFragment extends DialogFragment {
	
	public static interface ICallback {
		public void onTitleChangeFinished(String newTitle);
	}

	private ICallback callback;
	
	private String title;
	
	public void setCallback(ICallback callback){
		this.callback = callback;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_edit_username, null);
        final EditText etTitle = (EditText) v.findViewById(R.id.dialog_edit_username_et);
        etTitle.setHint("标题");
        etTitle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});  
        
        Bundle bundle = this.getArguments();
        if(bundle != null){
        	title = bundle.getString("title");
        }
        etTitle.setText(title);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("修改标题")
        	.setView(v)
        	.setPositiveButton("确定", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(callback != null){
						callback.onTitleChangeFinished(etTitle.getText().toString());
					}
				}
        		
        	})
        	.setNegativeButton("取消", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
        	});
        
        final AlertDialog dialog = builder.create();
        
        dialog.setOnShowListener(new OnShowListener(){

			@Override
			public void onShow(DialogInterface dlg) {
				// TODO Auto-generated method stub
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
			}
        	
        });
        
        etTitle.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(!TextUtils.isEmpty(s) && !s.toString().equals(title)){
					dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        return dialog;
    }

}
