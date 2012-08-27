package com.quanleimu.view;

import com.quanleimu.entity.UserBean;
import com.quanleimu.util.Util;

import android.content.Context;

/**
 * 
 * @author liuchong
 *
 * Use to control what to do after authentication.
 */
public class AuthController {

	private Runnable succedAction;
	private Runnable failAction;
	
	public AuthController()
	{
		
	}
	
	/**
	 * wait authentication to finish.
	 * @param succedAction
	 * @param failAction
	 */
	public void startWaitingAuth(Runnable succedAction, Runnable failAction)
	{
		this.succedAction = succedAction;
		this.failAction= failAction;
	}
	
	public void cancelAuth()
	{
		this.succedAction = null;
		this.failAction = null;
	}

	/**
	 * Check if authentication succed.
	 * @param context
	 */
	public void checkAfterAuth(Context context)
	{
		try
		{
			UserBean user = (UserBean) Util.loadDataFromLocate(context, "user");
			if (user == null || user.getId() == null)
			{
				if (failAction != null)
				{
					failAction.run();
				}
			}
			else if (succedAction != null)
			{
				succedAction.run();
			}
		}
		finally
		{
			cancelAuth();
		}
	}
	
}
