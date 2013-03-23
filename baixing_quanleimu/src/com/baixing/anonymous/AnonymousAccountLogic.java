package com.baixing.anonymous;

public class AnonymousAccountLogic extends BaseAnonymousLogic{
	static private String[][] anonymousStatus = {
		{Status_UnRegistered, 			Status_Number_Available, 	Status_Initialization, 	Action_AutoVerifiy, Status_Verified},
		{Status_UnRegistered, 			Status_Number_Available, 	Status_Verified, 		Action_Custom},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Initialization, 	Action_Register,	Status_Registered},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Registered, 		Action_Verify,		Status_Verified},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Verified, 		Action_Custom},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_Initialization, 	Action_AutoVerifiy,	Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_Loginned,	 	Action_AutoVerifiy,	Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_Verified, 		Action_Custom},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_Initialization, 	Action_Verify,		Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_Loginned,	 	Action_Verify,		Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_Verified, 		Action_Custom},
		{Status_Registered_Verified, 	Status_Number_Available, 	Status_Initialization, 	Action_Custom},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_Initialization, 	Action_Login,		Status_Loginned},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_Loginned, 		Action_Custom},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_Verified, 		Action_Custom},
		{Status_Registered_Verified, 	Status_Number_Available, 	Status_Verified, 		Action_Custom},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_ForgetPwd, 		Action_Verify,		Status_Verified},
		{Status_Registered_Verified, 	Status_Number_Available, 	Status_ForgetPwd, 		Action_Verify,		Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_ForgetPwd, 		Action_AutoVerifiy,	Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_ForgetPwd, 		Action_Verify,		Status_Verified},
		
	};
	
	public AnonymousAccountLogic(String registerStatus, String numberStatus){
		super(registerStatus, numberStatus);
		super.anonymousStatus = anonymousStatus;
	}
}