package com.baixing.activity.test.trackdata;

import com.baixing.tracking.TrackConfig.TrackMobile.PV;

public class SearchPVTestcase extends PVTestCase {

	public SearchPVTestcase() throws Exception {
		super();
	}
	
	
	public void testMainToVad() throws Exception {
		this.catMainToSecondCatErshou();
		verifyPVSequence(PV.HOME, PV.CATEGORIES);
	}

}
