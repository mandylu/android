package com.baixing.util;

public class PerformEvent{
	static public enum Event{
		E_MainActivity_Begin_Create("mainActivity_begin_create"),
		E_MainActivity_End_Create("mainActivity_end_create"),
		E_DoSplash_Begin("doSplashWork_begin"),
		E_Call_LoadConfigTask("call_LoadConfigTask"),
		E_SyncMobileConfig_Begin("syncMobileConfig_begin"),
		E_LoadConfig_Less_24("laodConfig<24h"),
		E_Leave_MobileConfigThread("leave_UpdateMobileConfigThread"),
		E_Begin_Init_Data("begin_init_data"),
		E_End_Init_Data("end_init_data"),
		E_Begin_ReadCity("begin_readcity"),
		E_End_ReadCity("end_readcity"),
		E_Begin_ReadPersonalInfo("begin_readPersonalInfo"),
		E_End_ReadPersonalInfo("end_readPersonalInfo"),
		E_Begin_ReadCategory("begin_readCategory"),
		E_End_ReadCategory("end_readCategory"),
		E_Init_Image_Mgr("init_imageMgr"),
		E_Init_Image_Mgr_Done("end_init_imageMgr"),
		E_Handle_Jobdone("begin_handle_job_done"),
		E_Handle_Jobdone_End("end_handle_job_done"),
		E_Begin_UpdateCityAndCat("begin_updateCityAndCat"),
		E_UpdateCity_Done("updateCity_done"),
		E_UpdateCat_Done("updateCat_done"),
		E_UpdateCityAndCat_FAIL("updateCityAndCat_fail"),
		E_Start_HomeFragment("start_home_fragment"),
		E_HomeFragment_Showup("home_fragment_showup"),
		E_HomeFragment_Showup_done("home_showup_done"),
		E_Category_Clicked("category_clicked"),
		E_Start_ListingFragment("start_listing"),
		E_ListingFrag_begin("begin_listingFrag_create"),
		E_ListingFrag_create_end("end_listingFrag_create"),
		E_Listing_Showup("listingFrag showup"),
		E_InitListingFragView_Begin("begin_init_listing_view"),
		E_InitListingFragView_End("end_init_listing_view"),
		E_FireRefresh_OnShowup("fireRefresh_OnShowup"),
		E_Listing_StartFetching("listing_start_fetch"),
		E_Listing_Got_First("listing_got_first"),
		E_Listing_Got_First_Leave("listing_got_first_leave"),
		E_Listing_Start_ParseJson("listing_start_parseJson"),
		E_Listing_End_ParseJson("listing_end_parseJson"),
		E_Start_PostAction("postAction_start"),
		E_PostAction_Direct_Start("postAct_direct_start"),
		E_PostAction_GetLocation_Start("postAct_getLocation_start"),
		E_POST_SUCCEEDED("post_succeeded"),
		E_Post_Send_Success_Broadcast("post_send_success_broadcast"),
		E_GeoCoding_Timeout("post_geocoding_timeout"),
		E_GeoCoding_Fetched("post_geocoding_fetched"),
		E_Post_Request_Sent("post_request_sent"),
		E_GetPostSuccessBroadcast("get_post_success_broadcast"),
		E_PersonalActivity_onCreate("personalActivity_onCreate"),
		E_JumpAfterPost_PushProfile("jump_after_post_pushProfile"),
		E_JumpAfterPost_PushMyAd("jump_after_post_pushMyAd"),
		E_Profile_OnCreate("profile_oncreate"),
		E_Profile_ShowUp("profile_showup"),
		E_MyAd_OnCreate("myAd_oncreate"),
		E_MyAd_FireRefresh("myAd_fireRefresh"),
		E_MyAdShowup("myAd_showup"),
		E_MyPost_Got("myAd_post_got"),
		E_MyPost_Got_Handled("myAd_post_got_handled"),
		E_MyAdStartFetching("myad_start_fetching"),
		E_Start_PostActivity("start_postActivity"),
		E_PostActivity_OnCreate_Begin("post_activity_oncreate_begin"),
		E_PostActivity_OnCreate_Leave("post_activity_oncreate_leave"),
		E_PGFrag_OnCreate_Start("pgFragment_oncreate_start"),
		E_Send_Camera_Bootup("send_camera_boot_up"),
		E_CameraActivity_OnCreate_Leave("camera_activity_oncreate_leave"),
		E_CameraActivity_OnCreate_Start("camera_activity_oncreate_start"),
		E_CameraActivity_onResume("camera_activity_onresume"),
		E_Start_Init_Camera("start_camera_init"),
		E_End_Init_Camera("end_camera_init");
		String event;
		Event(String event){
			this.event = event;
		}
		
		@Override
		public String toString(){
			return event;
		}
	}
}