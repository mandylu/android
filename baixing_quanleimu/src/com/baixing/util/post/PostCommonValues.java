package com.baixing.util.post;

public class PostCommonValues {
	static public final int MSG_GET_META_SUCCEED = 0xFFFF0011;
	static public final int MSG_GET_META_FAIL = 0xFFFF0111;
	static public final int MSG_POST_SUCCEED = 0xFFFF0F11;
	static public final int MSG_POST_FAIL = 0xFFFF0F12;
	public static final String STRING_AREA = "地区";
	public final static String STRING_DETAIL_POSITION = "具体地点";
	public static final String STRING_DESCRIPTION = "description";
	public static final int MSG_GEOCODING_FETCHED = 0x00010010;
	public static final int MSG_GPS_LOC_FETCHED = 0x00010210;
	public static final int HASH_POST_BEAN = "postBean".hashCode();
	public static final int HASH_CONTROL = "control".hashCode();
	public static final String []mainCategories = {"物品交易", "车辆买卖", "房屋租售", "全职招聘", "兼职招聘", "求职简历", "交友活动", "宠物", "生活服务", "教育培训"};
	public static final String[] fixedItemNames = {"images", PostCommonValues.STRING_DESCRIPTION, "价格", "contact", PostCommonValues.STRING_DETAIL_POSITION};
	public static final String[] fixedItemDisplayNames = {"", "描述", "价格", "联系电话", PostCommonValues.STRING_DETAIL_POSITION};
	public static final String[] hiddenItemNames = {"wanted", "faburen"};
	

}