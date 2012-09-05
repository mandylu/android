package com.quanleimu.broadcast.push;

import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

public abstract class PushHandler {

	protected Context cxt;
	
	PushHandler(Context context) {
		this.cxt = context;
	}
	
	public abstract boolean acceptMessage(String type);
	
	public abstract void processMessage(String message);
	
    
    protected boolean isUIActive(String expActivity)
    {
        ActivityManager manager = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        
        ComponentName compName = manager.getRunningTasks(1).get(0).topActivity;
        if (compName.getClassName().startsWith(expActivity))
        {
            return true;
        }
        
        return false;
    }
    
	protected boolean isAuthenticated()
	{
		UserBean user = (UserBean) Util.loadDataFromLocate(cxt, "user");
		if (user != null)
		{
			return user.getId() != null;
		}
		
		return false;
	}
    
	
}
