//xumengyi@baixing.com
package com.baixing.util.post;

public class PostCommonValues {
	static public final int MSG_GET_META_SUCCEED = 0xFFFF0011;
	static public final int MSG_GET_META_FAIL = 0xFFFF0111;
	static public final int MSG_POST_SUCCEED = 0xFFFF0F11;
	static public final int MSG_POST_FAIL = 0xFFFF0F12;
	static public final int MSG_POST_EXCEPTION = 0xFFFF0F13;
	static public final int MSG_POST_NEED_LOGIN = 0xFFFF0F14;
	static public final int MSG_POST_LOGIN_DONE = 0xFFFF0F15;
	static public final int MSG_VERIFY_FAIL = 0xFFFF0F16;
	static public final int MSG_POST_NEED_REGISTER = 0xFFFF0F18;
	static public final int MSG_ACCOUNT_CHECK_FAIL = 0xFFFF0F19;
	static public final int MSG_POST_EDIT_SUCCEED = 0xFFFF0F20;
	static public final int MSG_CHECK_QUOTA_AFTER_LOGIN = 0xFFFF0F21;
	static public final int ACTION_POST_NEED_LOGIN_DONE = 0x11110F00;
	static public final int ACTION_POST_NEED_REVERIIFY = 0x11110F01;
	static public final int ACTION_POST_CHECK_QUOTA_OK = 0x11110F02;
	public static final String STRING_AREA = "地区";
	public final static String STRING_DETAIL_POSITION = "具体地点";
	public static final String STRING_DESCRIPTION = "description";
	public static final int MSG_GEOCODING_FETCHED = 0x00010010;
	public static final int MSG_GPS_LOC_FETCHED = 0x00010210;
	public static final int HASH_POST_BEAN = "postBean".hashCode();
	public static final int HASH_CONTROL = "control".hashCode();
	public static final String[] fixedItemNames = {"images", PostCommonValues.STRING_DESCRIPTION, "价格", "contact", PostCommonValues.STRING_DETAIL_POSITION};
	public static final String[] fixedItemDisplayNames = {"", "描述", "价格", "联系电话", PostCommonValues.STRING_DETAIL_POSITION};
	public static final String[] hiddenItemNames = {"wanted"};//, "faburen"};
	

}