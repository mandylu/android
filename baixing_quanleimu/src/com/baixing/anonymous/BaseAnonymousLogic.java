//xumengyi@baixing.com
package com.baixing.anonymous;

import android.util.Pair;

public class BaseAnonymousLogic{
	protected String[][] anonymousStatus = null;
	static public final String Status_UnRegistered = "UR";
	static public final String Status_Registered_UnVerified = "RU";
	static public final String Status_Registered_Verified = "RV";
	static public final String Status_Initialization = "IN";
	static public final String Status_Registered = "RD";
	static public final String Status_Verified = "VD";
	static public final String Status_Loginned = "LD";
	static public final String Status_ForgetPwd = "FPWD";
	static public final String Status_CodeReceived = "CRD";
	static public final String Action_Verify = "A_V";
	static public final String Action_Register = "A_R";
	static public final String Action_Custom = "A_C";
	static public final String Action_Login = "A_L";
	static public final String Action_SendSMS = "A_SMS";

	public Pair<String, String> getActionAndNextStatus(){
		for(int i = 0; i < anonymousStatus.length; ++ i){
			if(anonymousStatus[i][0] == registerStatus && anonymousStatus[i][1] == currentStatus){
				String nextStatus = "";
				if(anonymousStatus[i].length == 4){
					nextStatus = anonymousStatus[i][3];
				}
				return new Pair<String, String>(anonymousStatus[i][2], nextStatus);
			}
		}
		return null;
	}
	
	private String registerStatus;
	private String currentStatus;
	
	public BaseAnonymousLogic(String registerStatus){
		this.registerStatus = registerStatus;
		this.currentStatus = Status_Initialization;
	}
	
	public void setCurrentStatus(String curStatus){
		currentStatus = curStatus;
	}
	
	public String getCurrentStatus(){
		return currentStatus;
	}
}