// lumengdi@baixing.net

package com.baixing.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.R;

public class HomeToast {

	public static void show(Context context, LayoutInflater inflater, String tip, boolean isLongTime) {  
		  
        Toast toast = new Toast(context);  
        if (isLongTime) {  
            toast.setDuration(Toast.LENGTH_LONG);  
        } else {  
            toast.setDuration(Toast.LENGTH_SHORT);  
        }  
        toast.setGravity(Gravity.TOP, 0, /*70*/(int)context.getResources().getDimension(R.dimen.title_height));
        
        View layout = inflater.inflate(R.layout.home_toast,null);
        TextView text = (TextView)layout.findViewById(R.id.toast_tip);
        text.setText(tip);
        toast.setView(layout);
        toast.show();    
    }
	
}
