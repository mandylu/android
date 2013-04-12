package com.baixing.activity.test.trackdata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.baixing.activity.test.BaixingTestCase;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.Util;

public class PVTestCase extends BaixingTestCase {

	public PVTestCase() throws Exception {
		super();
	}
	
	protected void setUp() throws Exception {
		File flag = new File(Environment.getExternalStorageDirectory()
				+ "/baixing_debug_log_crl.dat");
		if (!flag.exists()) {
			flag.createNewFile();
		}
		
		if (!flag.exists()) {
			fail("should create baxing_debug_log_crl.dat file before test.");
		}
		
		Util.saveDataToSdCard("baixing", "tracker_addlog", new byte[0], false);
		
		super.setUp();
	}
	
	private String[] readPVLogs() {
		String allLog = new String(Util.loadDataFromSdCard("baixing", "tracker_addlog"));
		String[] logs = allLog.split("\r\n");
		List<String> pvLogs = new ArrayList<String>();
		for (String log : logs) {//"tracktype":"pageview"
			if (log.contains("\"tracktype\":\"pageview\"")) {
				pvLogs.add(log);
			}
		}
		
		return pvLogs.toArray(new String[pvLogs.size()]);
	}
	
	protected void verifyPVSequence(PV... pvs) {
		
		String[] logs = readPVLogs();
		
		if (logs.length != pvs.length) {
			fail("pv count not equals." + "expected " + pvs.length + ", real count is " + logs.length);
		}
		
		int index = -1;
		for (String ll : logs) {
			assertTrue(ll.contains(pvs[++index].getName()));
		}
	}
	
	protected void catMainToSecondCatErshou() throws Exception {
		openHomeCategoryByIndex(0);
	}
	
	protected void secondCatToListing() throws Exception {
		openSecondCategoryByIndex(0);
	}
	
	protected void listingToFilter() throws Exception {
		this.clickByText("筛选");
	}
	
	protected void listingToVad() throws Exception {
		openAdByItemIndex(0);
	}
	
	protected void vadToMap() {
		
	}
	
	
	protected void catMainToPersonal() {
		
	}
	
	//-------My ad start------------
	protected void personalToMyAdListing() {
		
	}
	
	protected void myAdListingToVad() {
		
	}
	//-------My ad end--------------
	
	//-------Favorite start-------
	protected void personalToFav() {
		
	}
	
	protected void favToVad() {
		
	}
	//------Favorite end----------

	//-------setting start------
	protected void personalToSetting() {
		
	}
	
	protected void settingToLogin() {
		
	}
	
	protected void settingToAbout() {
		
	}
	
	protected void settingToFeedback() {
		
	}
	//-------setting end---------
	
	//------login flow start---
	protected void personalToLogin() {
		
	}
	
	protected void loginToREgister() {
		
	}
	
	protected void loginToForgetPass() {
		
	}
	//-----login flow end-----

	
	protected void catMainToPost() {
		
	}
	
	protected void cameraToPost() {
		
	}
	
	protected void postToCamera() {
		
	}
	
	protected void postSucced(int catId) {
		
	}
	

}
