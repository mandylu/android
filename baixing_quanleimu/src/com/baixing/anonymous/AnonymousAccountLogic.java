package com.baixing.anonymous;

public class AnonymousAccountLogic extends BaseAnonymousLogic{
	static private String[][] anonymousStatus = {
		{Status_UnRegistered, 			Status_Initialization, 	Action_Register,	Status_Registered},
		{Status_UnRegistered, 			Status_Registered, 		Action_SendSMS,		Status_CodeReceived},		
		{Status_UnRegistered, 			Status_CodeReceived, 	Action_Verify,		Status_Verified},
		{Status_UnRegistered, 			Status_Verified, 		Action_Custom},
		{Status_Registered_UnVerified, 	Status_Initialization, 	Action_SendSMS,		Status_CodeReceived},
		{Status_Registered_UnVerified, 	Status_Loginned,	 	Action_SendSMS,		Status_CodeReceived},
		{Status_Registered_UnVerified, 	Status_ForgetPwd, 		Action_SendSMS,		Status_CodeReceived},		
		{Status_Registered_UnVerified, 	Status_CodeReceived, 	Action_Verify,		Status_Verified},
		{Status_Registered_UnVerified, 	Status_Verified, 		Action_Custom},
		{Status_Registered_Verified, 	Status_Initialization, 	Action_Login,		Status_Loginned},
		{Status_Registered_Verified, 	Status_Loginned, 		Action_Custom},
		{Status_Registered_Verified, 	Status_ForgetPwd, 		Action_SendSMS,		Status_CodeReceived},		
		{Status_Registered_Verified, 	Status_Verified, 		Action_Custom},
		{Status_Registered_Verified, 	Status_CodeReceived, 	Action_Custom},
		
	};
	
	public AnonymousAccountLogic(String registerStatus){
		super(registerStatus);
		super.anonymousStatus = anonymousStatus;
	}
}