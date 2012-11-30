package com.baixing.broadcast;

public class CommonIntentAction 
{
	public static final String ACTION_BROADCAST_NEW_MSG = "com.baixing.action.newmsg";
	public static final String EXTRA_MSG_MESSAGE	= "extra.message.msgbody";
	
	
	public static final String ACTION_BROADCAST_XMPP_CONNECTED = "com.baixing.action.xmpp.connected";
	
	/**
	 * Status bar notification actions.
	 */
	public static final String ACTION_NOTIFICATION_MESSAGE = "com.baixing.action.notify.msg";//IM messages.
	public static final String ACTION_NOTIFICATION_HOT = "com.baixing.action.notify.hot";//Hot spot.
	public static final String ACTION_NOTIFICATION_BXINFO = "com.baixing.action.notify.bxinfo";
	public static final String ACTION_NOTIFICATION_UPGRADE = "com.baixing.action.notify.upgrade";
	

	public static interface PhotoReqCode
	{
		public static final int PHOTOZOOM = 2;
		public static final int PHOTOHRAPH = 1;
	}
	

}
