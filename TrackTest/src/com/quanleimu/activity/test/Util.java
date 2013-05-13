package com.quanleimu.activity.test;

import android.view.View;

import com.jayway.android.robotium.solo.Solo;

class Util{
	public static void sleep(int millSec){
		try {
			Thread.sleep(millSec);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loginWithSpecifiedAccount(Solo solo){
//		View tabPersonal = solo.getText("个人中心");
//		solo.waitForView(tabPersonal);
//		solo.clickOnView(tabPersonal);
		
		solo.getText("设置");
		
		boolean loginned = solo.searchText("登录百姓网");
		if(loginned){
			solo.clickOnView(solo.getText("设置"));
			solo.clickOnView(solo.getText("退出登录"));
			solo.clickOnButton(0);
		}

		solo.clickOnView(solo.getText("登录百姓网"));
		solo.enterText(0, "13917542245");
		solo.enterText(1, "qqq");
		solo.clickOnButton("登录百姓网");
		solo.getText("退出登录");
		solo.goBack();
	}
}