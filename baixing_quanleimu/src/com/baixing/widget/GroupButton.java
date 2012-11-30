package com.baixing.widget;

import java.util.List;

import com.quanleimu.activity.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GroupButton extends LinearLayout{
	public GroupButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public GroupButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    static public class GroupButtonProperty{
		public int id;
		public int imageId;
		public String text;
	}
    
    public void setButtonProperties(List<GroupButtonProperty> buttonProperties){
    	LayoutInflater inflater = LayoutInflater.from(this.getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;        
        this.setLayoutParams(params);

        LinearLayout.LayoutParams cell_params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1.0f);
        params.gravity = Gravity.CENTER_VERTICAL;  
        
        LinearLayout.LayoutParams seperator_params = new LinearLayout.LayoutParams(2, 15);
        seperator_params.gravity = Gravity.CENTER_VERTICAL;  

        
    	RelativeLayout.LayoutParams tv_params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);

        if(buttonProperties != null && buttonProperties.size() > 0){
        	for(int i = 0; i < buttonProperties.size(); ++ i){
        		View groupButtonCell = inflater.inflate(R.layout.groupbuttoncell, null);
        		groupButtonCell.setLayoutParams(cell_params);
	        	View subView1 = groupButtonCell.findViewById(R.id.rl_gb);
	        	subView1.setId(buttonProperties.get(0).id);
	        	TextView textView1 = (TextView)groupButtonCell.findViewById(R.id.tv_gb);
	        	textView1.setLayoutParams(tv_params);
	        	textView1.setText(buttonProperties.get(0).text);
	        	Resources res = getResources();
	        	Drawable drawable = res.getDrawable(buttonProperties.get(0).imageId);
	        	drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
	        	textView1.setCompoundDrawables(drawable, null, null, null);
	        	this.addView(groupButtonCell);
	        	if(i < buttonProperties.size() - 1){
	        		View tv = new View(this.getContext());
	        		tv.setLayoutParams(seperator_params);
	        		tv.setBackgroundColor(Color.BLACK);
	        		this.addView(tv);
	        	}
        	}
        }
    }
	
	public static GroupButton createGroupButton(Context context, List<GroupButtonProperty> buttonProperties) {
        LayoutInflater inflater = LayoutInflater.from(context);
        GroupButton gbView = (GroupButton)inflater.inflate(R.layout.groupbutton, null);
        gbView.setButtonProperties(buttonProperties);
        return gbView;
	}
	
	public void show(){
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.alpha_appear);
		this.startAnimation(animation);
	}
	
	public void dismiss(){
		Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.alpha);
		this.startAnimation(animation);
	}
}