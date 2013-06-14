//liuchong@baixing.com
package com.baixing.data;

import android.content.Context;

import com.baixing.entity.UserBean;
import com.baixing.util.Util;

/**
 * 
 * @author liuchong
 *
 */
public class AccountManager {
	private static UserBean currentUser;
	AccountManager() {
		
	}
	
	public void logout() {
		currentUser = null;		
	}
	
	/**
    *
    * @return 返回当前 UserBean user，未登录情况下返回 null
    */
    public UserBean getCurrentUser() {
		if (currentUser != null) {
			return currentUser;
		}
		currentUser = reloadUser();
		return currentUser;
    }
   
	public boolean isUserLogin() {
		UserBean user = getCurrentUser();
		return user != null && user.getPhone() != null
				&& user.getPhone().length() > 0;
	}

	public String getMyId(Context context) {
		if (currentUser != null) {
			return currentUser.getId();
		}

		currentUser = getCurrentUser();

		return currentUser == null ? null : currentUser.getId();
	}
	
	public void updatePassword(Context cxt, String password) {
		if (currentUser != null) {
			currentUser.setPassword(password, true);
			Util.saveDataToLocate(cxt, "user", currentUser);
		}
	}

	public String refreshAndGetMyId(Context context) {
		currentUser = null;
		currentUser = getCurrentUser();

		return currentUser == null ? null : currentUser.getId();
	}

	private UserBean reloadUser() {
		UserBean user = (UserBean) Util.loadDataFromLocate(GlobalDataManager
				.getInstance().getApplicationContext(), "user", UserBean.class);
		return user;
	}
}
