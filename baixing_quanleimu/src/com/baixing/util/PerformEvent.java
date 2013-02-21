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
		E_Category_Clicked("category_clicked");
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