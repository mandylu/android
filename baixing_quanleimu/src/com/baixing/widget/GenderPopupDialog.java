package com.baixing.widget;

import android.widget.Spinner;
import java.util.ArrayList;  

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;  
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;  
import android.widget.CheckBox;
import android.widget.ListView;  
import android.widget.Spinner;  
import android.widget.AdapterView.OnItemClickListener; 

public class GenderPopupDialog extends Dialog implements View.OnClickListener{  
  
    public static Dialog dialog = null;  
    private ArrayList<String> list;  
//    public static String text;  
    private Context context;
    private boolean isBoy = true;
  
    public GenderPopupDialog(Context context, boolean isBoy) {
    	super(context);
        this.context = context;  
        this.isBoy = isBoy;
    }  
    
    protected void onCreate(Bundle savedInstanceState){  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.gender_popup);  
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.findViewById(R.id.rl_boy).setOnClickListener(this);
        this.findViewById(R.id.rl_girl).setOnClickListener(this);
        ((CheckBox)(this.findViewById(R.id.ivGenderBoy))).setSelected(isBoy);
        ((CheckBox)(this.findViewById(R.id.ivGenderGirl))).setSelected(!isBoy);
    }   
  
    @Override  
    public void onClick(View v){
//        setSelection(position);  
//        setText(list.get(position));
    	if(v.getId() == R.id.rl_boy){
    		isBoy = true;
    	}else if(v.getId() == R.id.rl_girl){
    		isBoy = false;
    	}
    	this.dismiss();
    }
    
    public boolean isBoy(){
    	return isBoy;
    }
}  
