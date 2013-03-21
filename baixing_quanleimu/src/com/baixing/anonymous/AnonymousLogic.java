//xumengyi@baixing.com
package com.baixing.anonymous;

import android.util.Pair;

public class AnonymousLogic{
	static public final String Status_UnRegistered = "UR";
	static public final String Status_Registered_UnVerified = "RU";
	static public final String Status_Registered_Verified = "RV";
	static public final String Status_Number_Available = "NA";
	static public final String Status_Number_UnAvailable = "NU";
	static public final String Status_Initialization = "IN";
	static public final String Status_Registered = "RD";
	static public final String Status_Verified = "VD";
	static public final String Status_Loginned = "LD";
	static public final String Action_AutoVerifiy = "A_AV";
	static public final String Action_Verifiy = "A_V";
	static public final String Action_Register = "A_R";
	static public final String Action_Post = "A_P";
	static public final String Action_Login = "A_L";
	static private String[][] anonymousStatus = {
		{Status_UnRegistered, 			Status_Number_Available, 	Status_Initialization, 	Action_AutoVerifiy, Status_Verified},
		{Status_UnRegistered, 			Status_Number_Available, 	Status_Verified, 		Action_Post},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Initialization, 	Action_Register,	Status_Registered},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Registered, 		Action_Verifiy,		Status_Verified},
		{Status_UnRegistered, 			Status_Number_UnAvailable, 	Status_Verified, 		Action_Post},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_Initialization, 	Action_AutoVerifiy,	Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_Available, 	Status_Verified, 		Action_Post},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_Initialization, 	Action_Verifiy,		Status_Verified},
		{Status_Registered_UnVerified, 	Status_Number_UnAvailable, 	Status_Verified, 		Action_Post},
		{Status_Registered_Verified, 	Status_Number_Available, 	Status_Initialization, 	Action_Post},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_Initialization, 	Action_Login,		Status_Loginned},
		{Status_Registered_Verified, 	Status_Number_UnAvailable, 	Status_Loginned, 		Action_Post}
	};
	
	public Pair<String, String> getActionAndNextStatus(){
		for(int i = 0; i < anonymousStatus.length; ++ i){
			if(anonymousStatus[i][0] == registerStatus && anonymousStatus[i][1] == numberStatus && anonymousStatus[i][2] == currentStatus){
				String nextStatus = "";
				if(anonymousStatus[i].length == 5){
					nextStatus = anonymousStatus[i][4];
				}
				return new Pair<String, String>(anonymousStatus[i][3], nextStatus);
			}
		}
		return null;
	}
	
	private String registerStatus;
	private String numberStatus;
	private String currentStatus;
	
	public AnonymousLogic(String registerStatus, String numberStatus){
		this.registerStatus = registerStatus;
		this.numberStatus = numberStatus;
		this.currentStatus = Status_Initialization;
	}
	
	public void setCurrentStatus(String curStatus){
		currentStatus = curStatus;
	}
	
	public String getCurrentStatus(){
		return currentStatus;
	}
}