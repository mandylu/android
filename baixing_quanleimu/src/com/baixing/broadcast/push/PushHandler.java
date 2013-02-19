//liuchong@baixing.com
package com.baixing.broadcast.push;

import com.baixing.entity.UserBean;
import com.baixing.util.Util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * 
 * @author liuchong
 *
 */
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
		UserBean user = (UserBean) Util.loadDataFromLocate(cxt, "user", UserBean.class);
		if (user != null)
		{
			return user.getId() != null;
		}
		
		return false;
	}
    
	
}
