package com.baixing.message;

/**
 *@author liuchong
 */
public interface IBxNotificationNames
{
	/**
	 * Post when : anonymous user is created.
	 */
	public static final String NOTIFICATION_USER_CREATE = "note.user.created";
	
	/**
	 * Post when : network status change. 
	 */
    public static final String NOTIFICATION_NETWORK_CHANGE = "note.networkchange";
    
    /**
     * Post when : user login using Baixing account.
     */
    public static final String NOTIFICATION_LOGIN = "note.login";
    
    /**
     * Post when : user logout using baixing account.
     */
    public static final String NOTIFICATION_LOGOUT = "note.logout";
    
    /**
     * Post when : user add / remove an favorite.
     */
    public static final String NOTIFICATION_FAV_ADDED = "note.addFav";
    public static final String NOTIFICATION_FAV_REMOVE = "note.remove";
    
    
    /**
     * 
     * Post when : each time application is restart.
     * 
     */
    public static final String NOTIFICATION_CONFIGURATION_UPDATE = "note.config.change";
}
