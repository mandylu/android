package com.baixing.view.fragment;

import com.baixing.activity.BaseActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FirstRunFragment extends DialogFragment {

	public static FirstRunFragment create(String target, int layoutId)
	{
		FirstRunFragment f =  new FirstRunFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("layout", layoutId);
		bundle.putString("target", target);
		f.setArguments(bundle);
		
		return f;
	}
	
	
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		((BaseActivity) this.getActivity()).onHideFirstRun(getArguments().getString("target"));
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int style = DialogFragment.STYLE_NORMAL, theme = android.R.style.Theme_Translucent_NoTitleBar;
        setStyle(style, theme);
	}



	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View v = inflater.inflate(getArguments().getInt("layout"), null);
		v.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				FirstRunFragment.this.dismissAllowingStateLoss();
			}
		});
		
		return v;
	}
	
	public boolean hasGlobalTab()
	{
		return false;
	}
}
