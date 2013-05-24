//xuweiyan@baixing.com
package com.baixing.view.vad;

import java.util.Date;

import android.content.Context;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.TrackConfig.TrackMobile.Value;
import com.baixing.tracking.LogData;
import com.baixing.tracking.Tracker;

/**
 * 
 */
public class VadLogger {
	public static final void trackPageView(Ad detail, Context context) {
		if ((detail != null && GlobalDataManager.getInstance().isMyAd(
				detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)))
				|| !detail.isValidMessage()) {
			Tracker.getInstance()
					.pv(PV.MYVIEWAD)
					.append(Key.SECONDCATENAME,
							detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID,
							detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
					.append(Key.ADSENDERID,
							GlobalDataManager.getInstance().getAccountManager()
									.getMyId(context))
					.append(Key.ADSTATUS, detail.getValueByKey("status")).end();
		} else {
			PV pv = PV.VIEWAD;
			Tracker.getInstance()
					.pv(pv)
					.append(Key.SECONDCATENAME,
							detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID,
							detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
					.end();
		}
	}
	
	public static final void event(BxEvent event, Key key, Value value) {
		LogData log = Tracker.getInstance().event(event);
		if (key != null && value != null ) {
			log.append(key, value);
		}
		log.end();
	}
	
	public static final void trackMofifyEvent(Ad detail, BxEvent event) {
		String tmpCateName = detail.data.get("categoryEnglishName");
        String secondCategoryName = tmpCateName != null ? tmpCateName : "empty categoryEnglishName";
        String tmpInsertedTime = detail.data.get("insertedTime");
        long postedSeconds = -1;
        if (tmpInsertedTime != null) {
            long nowTime = new Date().getTime() / 1000;
            postedSeconds = nowTime - Long.valueOf(tmpInsertedTime);
        }

        Tracker.getInstance().event(event)
                .append(Key.SECONDCATENAME, secondCategoryName)
                .append(Key.POSTEDSECONDS, postedSeconds)
                .end();
	}
	
	public static final void trackLikeUnlike(Ad detail) {
		
		if (detail == null) {
			return;
		}
		
		Tracker.getInstance()
		.event(GlobalDataManager.getInstance().isFav(detail)?BxEvent.VIEWAD_UNFAV : BxEvent.VIEWAD_FAV)
		.append(Key.SECONDCATENAME, detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
		.append(Key.ADID, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID))
		.end();
	}
	
	public static final void trackViewMap(Ad detail) {
		Tracker.getInstance().pv(PV.VIEWADMAP).append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME)).append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)).end();
	}

	public static final void trackContactEvent(BxEvent event, Ad detail) {
		Tracker.getInstance().event(event)
				.append(Key.SECONDCATENAME, detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
				.append(Key.ADID, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID))
				.end();
	}

	public static final void trackShowMapEvent(Ad detail) {
		Tracker.getInstance().event(BxEvent.VIEWAD_SHOWMAP)
				.append(Key.SECONDCATENAME, detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
				.append(Key.ADID, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID))
				.end();
	}
}
