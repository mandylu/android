//liuchong@baixing.com
package com.baixing.broadcast;

public class CommonIntentAction 
{
	public static final String ACTION_BROADCAST_NEW_MSG = "com.baixing.action.newmsg";
	public static final String EXTRA_MSG_MESSAGE	= "extra.message.msgbody";
	
	public static final String ACTION_BROADCAST_POST_FINISH = "com.baixing.action.fisish.post";
	public static final String EXTRA_MSG_FINISHED_POST = "extra.post.info";
	
	public static final String ACTION_BROADCAST_XMPP_CONNECTED = "com.baixing.action.xmpp.connected";
	
	/**
	 * Status bar notification actions.
	 */
	public static final String ACTION_NOTIFICATION_MESSAGE = "com.baixing.action.notify.msg";//IM messages.
	public static final String ACTION_NOTIFICATION_HOT = "com.baixing.action.notify.hot";//Hot spot.
	public static final String ACTION_NOTIFICATION_BXINFO = "com.baixing.action.notify.bxinfo";
	public static final String ACTION_NOTIFICATION_UPGRADE = "com.baixing.action.notify.upgrade";
	
	/**
	 * Common extra key.
	 */
	public static final String EXTRA_COMMON_IS_THIRD_PARTY = "extra.common.isThirdParty";
	public static final String EXTRA_COMMON_RESULT_CODE = "extra.common.resultCode";
	public static final String EXTRA_COMMON_DATA = "extra.common.data";
	public static final String EXTRA_COMMON_INTENT = "extra.common.intent";
	public static final String EXTRA_COMMON_REQUST_CODE = "extra.image.reqcode";
	public static final String EXTRA_COMMON_EXTRALS = "extra.common.extral";
	
	/**
	 * Third party actions to request an image.
	 */
	public static final String ACTION_IMAGE_CAPTURE = "com.baixing.action.img.cap";
	public static final String ACTION_IMAGE_SELECT = "com.baixing.action.img.select";
	public static final String EXTRA_IMAGE_SAEV_PATH = "extra.image.savepath";
	public static final String EXTRA_IMAGE_LIST = "extra.image.list";
	
	public static interface PhotoReqCode
	{
		public static final int PHOTOZOOM = 2;
		public static final int PHOTOHRAPH = 1;
	}
	

}
